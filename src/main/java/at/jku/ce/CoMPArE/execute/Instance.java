package at.jku.ce.CoMPArE.execute;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import sun.rmi.runtime.Log;

import java.util.*;

/**
 * Created by oppl on 22/11/2016.
 */
public class Instance {

    private Process p;
    private Map<Subject,State> availableStates;
    private Map<Subject, Set<Message>> inputBuffer;
    private Map<Subject, Set<Message>> receivedMessages;
    private Map<Subject, Message> latestProcessedMessages;
    private Map<Subject, Boolean> subjectReachedEndState;
    private InstanceHistory history;
    private boolean processHasBeenChanged;

    public Instance(Process p) {
        this.p = p;
        p.setTimestampToNow();
        // LogHelper.logDebug("Constructing new instance of "+p+" "+p.getTimestamp());
        availableStates = new HashMap<>();
        inputBuffer = new HashMap<>();
        receivedMessages = new HashMap<>();
        latestProcessedMessages = new HashMap<>();
        subjectReachedEndState = new HashMap<>();
        processHasBeenChanged = false;
        history = new InstanceHistory();
        for (Subject s: p.getSubjects()) {
            availableStates.put(s, s.getFirstState());
            inputBuffer.put(s,new HashSet<Message>());
            receivedMessages.put(s,new HashSet<Message>());
            latestProcessedMessages.put(s,null);
            subjectReachedEndState.put(s,Boolean.FALSE);
        }
    }

    public Map<Subject, State> getAvailableStates() {
        return availableStates;
    }

    public State getAvailableStateForSubject(Subject s) {
        // LogHelper.logDebug(""+availableStates.get(s));
        return availableStates.get(s);
    }

    public Set<State> getNextStatesOfSubject(Subject s) {
        State currentState = availableStates.get(s);
        if (currentState == null) return null;
        return currentState.getNextStates().keySet();
    }

    public Condition getConditionForStateInSubject(Subject subject, State state) {
        State currentState = availableStates.get(subject);
        if (currentState == null) return null;
        return currentState.getNextStates().get(state);
    }

    public Set<Message> getAvailableMessagesForSubject(Subject subject) {
 //       // LogHelper.logDebug("getAvailableMessagesForSubject "+subject+": "+inputBuffer.get(subject).size()+" messages available");
        Set <Message> availableMessages = inputBuffer.get(subject);
        if (availableMessages == null) availableMessages = new HashSet<>();
        return availableMessages;
    }

    public Message getLatestProcessedMessageForSubject(Subject subject) {
 //       // LogHelper.logDebug("getLatestProcessedMessageForSubject "+subject+": "+latestProcessedMessages.get(subject));
        return latestProcessedMessages.get(subject);

    }

    public boolean subjectCanProgress(Subject subject) {
        State state = availableStates.get(subject);
        if (state == null) return false;
        if (state instanceof RecvState) {
            if (inputBufferContainsAcceptableMessage(subject) == null) return false;
        }
        return true;
    }

    public boolean subjectFinished(Subject s) {
        if (subjectReachedEndState.get(s) == Boolean.TRUE) return true;
        return false;
    }

    public boolean processFinished() {
        boolean finished = true;
        for (Subject s: p.getSubjects()) {
            if (subjectReachedEndState.get(s) == Boolean.FALSE) finished = false;
        }
        return finished;
    }

    public boolean processIsBlocked() {
        boolean isBlocked = true;
        for (State s : getAvailableStates().values()) {
            if (s instanceof ActionState || s instanceof SendState) isBlocked = false;
        }
        for (Subject s : getProcess().getSubjects()) {
            for (Message m: s.getExpectedMessages()) {
                for (State state : getAvailableStates().values()) {
                    if (state instanceof RecvState && ((RecvState) state).getRecvdMessages().contains(m)) isBlocked = false;
                }
            }
        }
        return isBlocked;
    }

    public State advanceStateForSubject(Subject s, Condition c, boolean incomingMessage) {
        State currentState = availableStates.get(s);
        Map<Subject,State> currentAvailableStates = new HashMap<>(availableStates);
        Map<Subject, Set<Message>> currentInputBuffer = new HashMap<>(inputBuffer);
        Map<Subject, Set<Message>> currentReceivedMessage = new HashMap<>(receivedMessages);
        Map<Subject, Message> currentLatestProcessedMessages = new HashMap<>(latestProcessedMessages);

        latestProcessedMessages.replace(s,null);

        if (!(incomingMessage && (currentState == null) && (s.getFirstState() instanceof RecvState))) {
            if (incomingMessage && !(currentState instanceof RecvState)) return currentState;
        }
        if (currentState == null) {
            //LogHelper.logDebug("Subject "+s+" already finished ... checking if eligible to restart");
            if ((subjectReachedEndState.get(s) == Boolean.TRUE) && s.getFirstState() instanceof RecvState) {
                //LogHelper.logDebug("would be eligble");
                availableStates.replace(s,s.getFirstState());
                Message m = inputBufferContainsAcceptableMessage(s);
                if (m == null) {
                    availableStates.replace(s,null);
                    return currentState;
                }
                else {
                    //LogHelper.logDebug("message found ... is restarted now");
                    inputBuffer.get(s).remove(m);
                    receivedMessages.get(s).add(m);
                    latestProcessedMessages.replace(s,m);
                    subjectReachedEndState.put(s,Boolean.FALSE);
                    currentState = s.getFirstState();
                }

            }
        }
        else {
            if (currentState instanceof RecvState) {
                Message m = inputBufferContainsAcceptableMessage(s);
                if (m == null) {
                //    LogHelper.logDebug("advanceStateForSubject "+s+": necessary message not yet received, still waiting");
                    return currentState;
                } else {
                 //   LogHelper.logDebug("advanceStateForSubject "+s+": necessary message received, state is now actionable");
                    inputBuffer.get(s).remove(m);
                    receivedMessages.get(s).add(m);
                    latestProcessedMessages.replace(s, m);
                }
            }
            if (currentState instanceof SendState) {
                Subject recipient = p.getRecipientOfMessage(((SendState) currentState).getSentMessage());
             //   LogHelper.logDebug("advanceStateForSubject "+s+": sending message to "+recipient);
                this.addMessageToInputBuffer(recipient, ((SendState) currentState).getSentMessage());
                advanceStateForSubject(recipient, null, true);
            }
        }
        Map<State,Condition> nextStates = currentState.getNextStates();
        if (nextStates.size() == 1 && nextStates.values().iterator().next() == null) {
            availableStates.replace(s,nextStates.keySet().iterator().next());
        //    LogHelper.logDebug("advanceStateForSubject "+s+": progressing to next state "+availableStates.get(s));
        }
        else {
            for (State nextState: nextStates.keySet()) {
                Condition conditionToBeChecked = nextStates.get(nextState);
//                // LogHelper.logDebug("advanceStateForSubject "+s+": checking condition "+ conditionToBeChecked + ", which is a "+conditionToBeChecked.getClass());
                if ((conditionToBeChecked == null && c == null) || conditionToBeChecked.equals(c)) {
                    availableStates.replace(s, nextState);
//                    // LogHelper.logDebug("advanceStateForSubject "+s+": progressing to next state under condition "+ conditionToBeChecked);
                    break;
                }
                if (conditionToBeChecked instanceof MessageCondition) {
                    for (Message m: receivedMessages.get(s)) {
//                        // LogHelper.logDebug("advanceStateForSubject "+s+": checking message "+m);
                        if (((MessageCondition) conditionToBeChecked).checkCondition(m)) {
                            availableStates.replace(s, nextState);
                            receivedMessages.get(s).remove(m);
//                            // LogHelper.logDebug("advanceStateForSubject "+s+": progressing to next state because of message condition "+ conditionToBeChecked);
                            break;
                        }
                    }
                }
            }
        }
        if (currentState.isEndState()) {
            // LogHelper.logDebug("advanceStateForSubject "+s+": instance finished");
            availableStates.replace(s, null);
            subjectReachedEndState.put(s,Boolean.TRUE);
        }
        history.addHistoryStep(new InstanceHistoryStep(s,currentState,currentAvailableStates,currentReceivedMessage,currentInputBuffer,currentLatestProcessedMessages, subjectReachedEndState));
        LogHelper.logInfo("Execution: " + getProcess()+" - "+s+": progressed from "+currentState+" to "+availableStates.get(s));
        return availableStates.get(s);
    }

    public void updateAvailableStateForSubject(Subject s, State state) {
        if (s.getStateByUUID(state.getUUID()) == null) return;
        State currentState = availableStates.get(s);
        Map<Subject,State> currentAvailableStates = new HashMap<>(availableStates);
        Map<Subject, Set<Message>> currentInputBuffer = new HashMap<>(inputBuffer);
        Map<Subject, Set<Message>> currentReceivedMessage = new HashMap<>(receivedMessages);
        Map<Subject, Message> currentLatestProcessedMessages = new HashMap<>(latestProcessedMessages);
        availableStates.put(s, state);
        history.addHistoryStep(new InstanceHistoryStep(s,currentState,currentAvailableStates,currentReceivedMessage,currentInputBuffer,currentLatestProcessedMessages, subjectReachedEndState));
    }

    private Message inputBufferContainsAcceptableMessage(Subject s) {
        State currentState = availableStates.get(s);
        if (currentState instanceof RecvState) {
            Set<Message> availableMessages = inputBuffer.get(s);
            Set<Message> acceptableMessages = ((RecvState) currentState).getRecvdMessages();
            for (Message m : availableMessages) {
                if (acceptableMessages.contains(m)) return m;
            }
        }
        return null;
    }

    private void addMessageToInputBuffer(Subject recipient, Message m) {
        if (!inputBuffer.keySet().contains(recipient)) inputBuffer.put(recipient,new HashSet<>());
        inputBuffer.get(recipient).add(m);
    }

    public void putMessageInInputbuffer(Subject s, Message m) {
        addMessageToInputBuffer(s,m);
        advanceStateForSubject(s, null, true);
    }

    public Process getProcess() {
        return p;
    }

    public LinkedList<InstanceHistoryStep> getWholeHistory() {
        return history.getWholeHistory();
    }

    public LinkedList<InstanceHistoryStep> getHistoryForSubject(Subject s) {
        return history.getHistoryForSubject(s);
    }

    public InstanceHistoryStep getLatestInstanceHistoryStep() {
        if (history.getWholeHistory().isEmpty()) return null;
        else return history.getWholeHistory().getFirst();
    }

    public void removeLatestHistoryStepForSubject(Subject s) {
        history.removeLatestStepForSubject(s);
    }

    public boolean isProcessHasBeenChanged() {
        return processHasBeenChanged;
    }

    public void setProcessHasBeenChanged(boolean processHasBeenChanged) {
        this.processHasBeenChanged = processHasBeenChanged;
    }

    public void reconstructInstanceState(InstanceHistoryStep state) {
        if (state == null) {
            this.inputBuffer = new HashMap<>();
            this.receivedMessages = new HashMap<>();
            this.availableStates = new HashMap<>();
            this.latestProcessedMessages = new HashMap<>();
            this.subjectReachedEndState = new HashMap<>();
            for (Subject s: p.getSubjects()) {
                availableStates.put(s, s.getFirstState());
                inputBuffer.put(s,new HashSet<Message>());
                receivedMessages.put(s,new HashSet<Message>());
                latestProcessedMessages.put(s,null);
            }
            this.history = new InstanceHistory();
        }
        else {
            this.inputBuffer = state.getInputBuffer();
            this.receivedMessages = state.getReceivedMessages();
            this.availableStates = state.getAvailableStates();
            this.latestProcessedMessages = state.getLatestProcessedMessages();
            this.subjectReachedEndState = state.getSubjectReachedEndState();
            this.history.removeAllStepsUntil(state);
        }
    }

    public boolean checkForRemovedSubjects() {
        boolean subjectRemoved = false;
        Set<Subject> registeredSubjects = availableStates.keySet();
        for (Subject s:registeredSubjects) {
            if (p.getSubjectByUUID(s.getUUID())==null) {
                availableStates.remove(s);
                inputBuffer.remove(s);
                receivedMessages.remove(s);
                latestProcessedMessages.remove(s);
                subjectReachedEndState.remove(s);
                subjectRemoved = true;
            }
        }
        return subjectRemoved;
    }
}
