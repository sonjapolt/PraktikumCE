package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.RecvState;
import at.jku.ce.CoMPArE.process.SendState;

/**
 * Created by oppl on 16/01/2017.
 */
public class AddMessageToRecvStateCommand extends ProcessChangeCommand {

    RecvState state;
    Message message;

    public AddMessageToRecvStateCommand(RecvState state, Message message) {
        this.state = state;
        this.message = message;
    }

    @Override
    public boolean perform(Process p) {
        state = (RecvState) p.getStateByUUID(state.getUUID());
        state.addRecvdMessage(message);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        state = (RecvState) p.getStateByUUID(state.getUUID());
        state.removeRecvdMessage(message);
        return true;
    }

    @Override
    public String toString() {
        return "Added input \""+message+"\" to \""+state+"\"";
    }
}
