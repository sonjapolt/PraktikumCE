package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.LogHelper;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import sun.rmi.runtime.Log;

import java.util.*;

/**
 * Created by oppl on 22/11/2016.
 */
public class Subject extends ProcessElement {

    public static String ANONYMOUS = "Anonymous";

    private String name;

    private UUID firstState;

    @XStreamOmitField
    private Process parentProcess;

    private Set<State> states;
    private Set<Transition> transitions;
    private Set<UUID> expectedMessages;
    private Set<UUID> providedMessages;

    public Subject(String name) {
        super();
        this.name = name;
        this.parentProcess = null;
        this.firstState = null;
        this.states = new HashSet<>();
        this.transitions = new HashSet<>();
        this.expectedMessages = new HashSet<>();
        this.providedMessages = new HashSet<>();
    }

    public Subject(Subject s, Process newProcess) {
        super(s);
        this.name = s.toString();
        this.parentProcess = newProcess;
        this.states = new HashSet<>();
        this.transitions = new HashSet<>();

        for (State state: s.getStates()) {
            State newState = null;
            if (state instanceof ActionState) newState = new ActionState((ActionState) state, this);
            if (state instanceof SendState) newState = new SendState((SendState) state, this);
            if (state instanceof RecvState) newState = new RecvState((RecvState) state, this);
            this.addState(newState);
        }
        for (Transition transition: s.getTransitions()) {
            this.addTransition(new Transition(transition));
        }
        this.firstState = s.firstState;
        this.expectedMessages = new HashSet<>();
        this.providedMessages = new HashSet<>();
        for (Message m:s.getExpectedMessages()) {
            expectedMessages.add(m.getUUID());
        }
        for (Message m:s.getProvidedMessages()) {
            providedMessages.add(m.getUUID());
        }

    }

    public Map<State,Condition> getNextStatesForState(State s) {
        Map<State,Condition> nextStates = new HashMap<State, Condition>();
        for (Transition t: transitions) {
            if (t.getSource().equals(s.getUUID())) nextStates.put(getStateByUUID(t.getDest()),t.getCondition());
        }
        return nextStates;
    }

    public Process getParentProcess() {
        return parentProcess;
    }

    public void addState(State state) {
        states.add(state);
        state.setParentSubject(this);
    }

    public void removeState(State state) {
        removeAllOutgoingTransitionsFrom(state);
        removeAllIncomingTransitionsTo(state);
        states.remove(state);
    }

    public State setFirstState(State firstState) {
        if (firstState == null) {
            this.firstState = null;
            return null;
        }
        if (!states.contains(firstState)) {
            states.add(firstState);
            firstState.setParentSubject(this);
        }
        this.firstState = firstState.getUUID();
        return firstState;
    }

    public State getFirstState() {
        return getStateByUUID(firstState);
    }

    @Override
    public String toString() {
        return name;
    }

    public Collection<State> getStates() { return this.states;}

    public State getStateByUUID(UUID stateID) {
        for (State state: states)
            if (state.getUUID().equals(stateID)) {
                return state;
            }
        return null;
    }

    public Set<State> getSendStates(Message m) {
        Set<State> sendState = new HashSet<>();
        for (State s: getStates()) {
            if (s instanceof SendState && ((SendState) s).getSentMessage().equals(m)) sendState.add(s);
        }
        return sendState;
    }

    public Set<State> getRecvStates(Message m) {
        Set<State> recvState = new HashSet<>();
        for (State s: getStates()) {
            if (s instanceof RecvState && ((RecvState) s).getRecvdMessages().contains(m)) recvState.add(s);
        }
        return recvState;
    }

    public Set<Message> getSentMessages() {
        HashSet<Message> sentMessages = new HashSet<>();
        for (State s: states) {
            if (s instanceof SendState) {
                Message sentMessage = ((SendState) s).getSentMessage();
                sentMessages.add(sentMessage);
            }
        }
        return sentMessages;
    }

    public Set<Message> getRecvdMessages() {
        HashSet<Message> recvdMessages = new HashSet<>();
        for (State s: states) {
            if (s instanceof RecvState) {
                Set<Message> recvdMessagesFromState = ((RecvState) s).getRecvdMessages();
                recvdMessages.addAll(recvdMessagesFromState);
            }
        }
        return recvdMessages;
    }

    public Set<Message> getExpectedMessages() {
        Set<Message> messages = new HashSet<>();
        if (parentProcess == null) return messages;
        for (UUID mID: expectedMessages) {
            messages.add(parentProcess.getMessageByUUID(mID));
        }
        return messages;
    }

    public void addExpectedMessage(Message expectedMessage) {
        this.expectedMessages.add(expectedMessage.getUUID());
    }

    public void removeExpectedMessage(Message expectedMessage) {
        this.expectedMessages.remove(expectedMessage.getUUID());
    }

    public Set<Message> getProvidedMessages() {
        Set<Message> messages = new HashSet<>();
        if (parentProcess == null) return messages;
        for (UUID mID: providedMessages) {
            messages.add(parentProcess.getMessageByUUID(mID));
        }
        return messages;
    }

    public void addProvidedMessage(Message providedMessage) {
        this.providedMessages.add(providedMessage.getUUID());
    }

    public void removeProvidedMessage(Message providedMessage) { this.providedMessages.remove(providedMessage.getUUID()); }

    public Set<State> getPredecessorStates(State target) {
        Set<State> predecessorStates = new HashSet<State>();
        for (Transition t: transitions) {
            if (t.getDest().equals(target.getUUID())) predecessorStates.add(getStateByUUID(t.getSource()));
        }
        return predecessorStates;

/*        Set<State> predecessorStates = new HashSet<>();
        for (State s: states) {
            for (State nextState: s.getNextStates().keySet()) {
                if (nextState.equals(target)) predecessorStates.add(s);
            }
        }
        return predecessorStates;*/
    }

    public void addTransition(Transition t) {
        transitions.add(t);
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }

    public void removeAllIncomingTransitionsTo(State s) {
        Set<Transition> toBeRemoved = new HashSet<>();
        for (Transition t: transitions) {
            if (t.getDest().equals(s.getUUID())) toBeRemoved.add(t);
        }
        transitions.removeAll(toBeRemoved);
    }

    public void removeAllOutgoingTransitionsFrom(State s) {
        Set<Transition> toBeRemoved = new HashSet<>();
        for (Transition t: transitions) {
            if (t.getSource().equals(s.getUUID())) toBeRemoved.add(t);
        }
        transitions.removeAll(toBeRemoved);
    }

    public void removeAllTransitionsBetween(State source, State dest) {
        Set<Transition> toBeRemoved = new HashSet<>();
        for (Transition t: transitions) {
            if (t.getSource().equals(source.getUUID()) && t.getDest().equals(dest.getUUID())) toBeRemoved.add(t);
        }
        transitions.removeAll(toBeRemoved);

    }

    public void removeTransition(Transition t) {
        transitions.remove(t);
    }

    public void setParentProcess(Process parentProcess) {
        this.parentProcess = parentProcess;
    }

    public void reconstructParentRelations(Process p) {
        this.parentProcess = p;
        for (State s: states) {
            s.reconstructParentRelations(this);
        }
        for (Transition t: transitions) {
            t.reconstructParentRelations(this);
        }
    }

}
