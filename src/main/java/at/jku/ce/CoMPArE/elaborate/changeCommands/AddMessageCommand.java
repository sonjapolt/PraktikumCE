package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 11/02/2017.
 */
public class AddMessageCommand extends ProcessChangeCommand {

    Message message;
    Process process;

    public AddMessageCommand(Process p, Message m) { //, Instance i) {
        super();
        message = m;
        process = p;

    }

    @Override
    public boolean perform(Process p) {
        process = p;
        for (Message m : process.getMessages()) {
            if (m.toString().equals(message.toString())) {
                return false;
            }
        }
        process.addMessage(message);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        process = p;
        process.removeMessage(message);
        return true;
    }

    @Override
    public String toString() {
        return "Added message \""+message+"\"";
    }


}
