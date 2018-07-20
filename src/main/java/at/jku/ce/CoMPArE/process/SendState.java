package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.LogHelper;
import sun.rmi.log.LogInputStream;

import java.util.UUID;

/**
 * Created by oppl on 22/11/2016.
 */
public class SendState extends State {

    private UUID sentMessageID;

    public SendState(String name) {
        super(name);
        sentMessageID = null;
    }

    public SendState(String name, Message sentMessage) {
        super(name);
        this.sentMessageID = sentMessage.getUUID();
    }

    public SendState(String name, Message sentMessage, boolean isEndState) {
        this(name, sentMessage);
        this.setEndState(isEndState);
    }

    public SendState(SendState s, Subject container) {
        super(s,container);
/*        // LogHelper.logDebug(container+" "+container.getParentProcess()+" "+s.sentMessageID+" "+container.getParentProcess().getMessageByUUID(s.sentMessageID));
        for (Message m: container.getParentProcess().getMessages()) {
            // LogHelper.logDebug(m.getUUID()+" "+m);
        }
        // LogHelper.logDebug(""+container.getParentProcess().getMessages().size());*/
        if (s.sentMessageID == null) sentMessageID = null;
        else sentMessageID = container.getParentProcess().getMessageByUUID(s.sentMessageID).getUUID();
    }

    public Message getSentMessage() {
        if (sentMessageID == null) return null;
        return parentSubject.getParentProcess().getMessageByUUID(sentMessageID);
    }

    public void setSentMessage(Message sentMessage) {
        parentSubject.getParentProcess().addMessage(sentMessage);
        if (sentMessage == null) this.sentMessageID = null;
        else this.sentMessageID = sentMessage.getUUID();
    }
}
