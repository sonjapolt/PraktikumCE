package at.jku.ce.CoMPArE.process;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by oppl on 22/11/2016.
 */
public class RecvState extends State {

    private Set<UUID> recvdMessageIDs;

    public RecvState(String name) {
        super(name);
        recvdMessageIDs = new HashSet<>();
    }

    public RecvState(String name, Message recvdMessage) {
        this(name);
        recvdMessageIDs.add(recvdMessage.getUUID());
    }

    public RecvState(String name, Set<Message> recvdMessages) {
        this(name);
        for (Message m: recvdMessages) {
            this.recvdMessageIDs.add(m.getUUID());
        }
    }

    public RecvState(RecvState s, Subject container) {
        super(s,container);
        recvdMessageIDs = new HashSet<>();
        for (UUID messageID:s.recvdMessageIDs) {
            recvdMessageIDs.add(messageID);
        }
    }

    public Set<Message> getRecvdMessages() {
        Set<Message> recvdMessages = new HashSet<>();
        for (UUID recvdMessageID: recvdMessageIDs) {
            recvdMessages.add(parentSubject.getParentProcess().getMessageByUUID(recvdMessageID));
        }
        return recvdMessages;
    }

    public void addRecvdMessage(Message recvdMessage) {
        parentSubject.getParentProcess().addMessage(recvdMessage);
        this.recvdMessageIDs.add(recvdMessage.getUUID());
    }

    public void removeRecvdMessage(Message recvdMessage) {
        //parentSubject.getParentProcess().removeMessage(recvdMessage);
        this.recvdMessageIDs.remove(recvdMessage.getUUID());
    }


}
