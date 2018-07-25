package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.LogHelper;

import java.util.UUID;

/**
 * Created by oppl on 23/11/2016.
 */
public class MessageCondition extends Condition {

    private UUID receivedMessageID;

    public MessageCondition(UUID receivedMessage) {
        super(receivedMessage.toString());
        this.receivedMessageID = receivedMessage;
    }

    public MessageCondition(MessageCondition messageCondition, Transition inTransition) {
        super(messageCondition);
        receivedMessageID = messageCondition.getMessage();
    }

    public boolean checkCondition(Message messageToBeChecked) {
        if (messageToBeChecked.getUUID().equals(receivedMessageID)) return true;
        return false;
    }

/*    public Message getMessage() {
        if (parentState == null) return null;
        if (parentState.getParentSubject() == null) return null;
        if (parentState.getParentSubject().getParentProcess() == null) return null;
        return parentState.getParentSubject().getParentProcess().getMessageByUUID(receivedMessageID);
    }*/

    public UUID getMessage() {
        return receivedMessageID;
    }

}
