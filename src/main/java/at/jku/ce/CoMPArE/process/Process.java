package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.LogHelper;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by oppl on 22/11/2016.
 */
public class Process extends ProcessElement {

    private String name;
    private Date timestamp;

    private Set<Subject> subjects;
    private Set<Message> messages;

    public Process(String name) {
        super();
        this.name = name;
        subjects = new HashSet<>();
        messages = new HashSet<>();
        this.timestamp = new Date();
    }

    public Process(Process p) {
        super(p);
        name = p.toString();
        timestamp = p.getTimestamp();
        subjects = new HashSet<>();
        messages = new HashSet<>();

        for (Message m: p.getMessages()) {
            messages.add(new Message (m));
//            // LogHelper.logDebug("added message "+m.getUUID()+" "+m);
        }

        for (Subject s: p.getSubjects()) {
            subjects.add(new Subject(s,this));
        }
    }

    public void addSubject(Subject s) {
        subjects.add(s);
        s.setParentProcess(this);
    }
    public void removeSubject(Subject s) { if (s!=null) subjects.remove(s); }

    public void addMessage(Message m) {
        if (m!=null) {
            messages.add(m);
        }
    }

    public void addMessages(Set<Message> messages) { this.messages.addAll(messages); }

    public void removeMessage(Message m) { messages.remove(m); }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public Set<Message> getMessages() { return messages; }

    public Subject getSenderOfMessage(Message message) {
        for (Subject s: subjects) {
//            // LogHelper.logDebug("getSenderOfMessage "+ message +": examining subject "+ s);
            if (s.getSentMessages().contains(message)) {
                return s;
            }
            if (s.getExpectedMessages().contains(message)) {
                return s;
            }
        }
        return null;
    }

    public Subject getRecipientOfMessage(Message message) {
        for (Subject s: subjects) {
            if (s.getRecvdMessages().contains(message)) return s;
            if (s.getProvidedMessages().contains(message)) return s;
        }
        return null;
    }

    public State getStateByUUID(UUID stateID) {
        State state = null;
        for (Subject s: subjects) {
            state = s.getStateByUUID(stateID);
            if (state != null) return state;
        }
        return null;
    }

    public Subject getSubjectByUUID(UUID subjectID) {
        for (Subject s: subjects) {
            if (s.getUUID().equals(subjectID)) return s;
        }
        return null;
    }

    public Subject getSubjectWithName(String name) {
        for (Subject s: subjects) {
            if (s.toString().equals(name)) return s;
        }
        return null;
    }

    public Message getMessageByUUID(UUID messageID) {
//        // LogHelper.logDebug("looking for message with UUID "+messageID);
        for (Message m: messages) {
            if (m.getUUID().equals(messageID)) {
//                // LogHelper.logDebug("found it");
                return m;
            }
        }
//        // LogHelper.logDebug("message not found");
        return null;
    }

    public Subject getSubjectWithState(State state) {
        for (Subject s: subjects) {
            if (s.getStates().contains(state)) return s;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setTimestampToNow() {
        timestamp = new Date();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void reconstructParentRelations() {
        for (Subject s: subjects) {
            s.reconstructParentRelations(this);
        }
    }
}
