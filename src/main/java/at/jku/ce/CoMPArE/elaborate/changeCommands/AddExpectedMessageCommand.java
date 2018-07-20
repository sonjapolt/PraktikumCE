package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddExpectedMessageCommand extends ProcessChangeCommand {

    private Subject subject;
    private Message message;

    public AddExpectedMessageCommand(Subject s, Message m) {
        super();
        subject = s;
        message = m;
    }

    @Override
    public boolean perform(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        if (subject == null || message == null) return false;
        subject.addExpectedMessage(message);
        subject.getParentProcess().addMessage(message);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        if (!subject.getExpectedMessages().contains(message)) return false;
//        subject.getParentProcess().removeMessage(message);
        subject.removeExpectedMessage(message);
        return true;
    }

    @Override
    public String toString() {
        return "Added expected input \""+message+"\" to \""+subject+"\"";
    }

}
