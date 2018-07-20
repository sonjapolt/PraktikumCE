package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.RecvState;
import at.jku.ce.CoMPArE.process.SendState;

/**
 * Created by oppl on 16/01/2017.
 */
public class AddMessageToSendStateCommand extends ProcessChangeCommand {

    SendState state;
    Message message;

    Message oldMessage;

    public AddMessageToSendStateCommand(SendState state, Message message) {
        this.state = state;
        this.message = message;
    }

    @Override
    public boolean perform(Process p) {
        state = (SendState) p.getStateByUUID(state.getUUID());
        oldMessage = state.getSentMessage();
        state.setSentMessage(message);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        state = (SendState) p.getStateByUUID(state.getUUID());
        state.setSentMessage(oldMessage);
        return true;
    }

    @Override
    public String toString() {
        return "Added input \""+message+"\" to \""+state+"\"";
    }
}
