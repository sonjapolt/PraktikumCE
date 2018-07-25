package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;

/**
 * Created by oppl on 11/02/2017.
 */
public class RemoveMessageCommand extends ProcessChangeCommand {

    Message message;
    Process process;

    public RemoveMessageCommand(Process p, Message m) { //, Instance i) {
        super();
        message = m;
        process = p;

    }

    @Override
    public boolean perform(Process p) {
        process = p;
        process.removeMessage(message);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        process = p;
        process.addMessage(message);
        return true;
    }

    @Override
    public String toString() {
        return "Removed message \""+message+"\"";
    }


}
