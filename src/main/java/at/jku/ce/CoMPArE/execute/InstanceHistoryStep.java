package at.jku.ce.CoMPArE.execute;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by oppl on 18/01/2017.
 */
public class InstanceHistoryStep {
    private UUID id;
    private Subject affectedSubject;
    private State state;
    private Map<Subject,State> availableStates;
    private Map<Subject, Set<Message>> inputBuffer;
    private Map<Subject, Set<Message>> receivedMessages;
    private Map<Subject, Message> latestProcessedMessages;
    private Map<Subject, Boolean> subjectReachedEndState;


    public InstanceHistoryStep(Subject affectedSubject, State state, Map<Subject,State> availableStates, Map<Subject, Set<Message>> receivedMessages, Map<Subject, Set<Message>> inputBuffer, Map<Subject, Message> latestProcessedMessages, Map<Subject,Boolean> subjectReachedEndState) {
        this.state = state;
        this.affectedSubject = affectedSubject;
        this.id = UUID.randomUUID();
        this.availableStates = availableStates;
        this.receivedMessages = receivedMessages;
        this.inputBuffer = inputBuffer;
        this.latestProcessedMessages = latestProcessedMessages;
        this.subjectReachedEndState = subjectReachedEndState;
    }

    public UUID getId() {
        return id;
    }

    public Map<Subject, State> getAvailableStates() {
        return availableStates;
    }

    public Map<Subject, Set<Message>> getInputBuffer() {
        return inputBuffer;
    }

    public Map<Subject, Set<Message>> getReceivedMessages() {
        return receivedMessages;
    }

    public Map<Subject, Message> getLatestProcessedMessages() {
        return latestProcessedMessages;
    }

    public State getState() {
        return state;
    }

    public Subject getAffectedSubject() {
        return affectedSubject;
    }

    public Map<Subject, Boolean> getSubjectReachedEndState() {
        return subjectReachedEndState;
    }
}
