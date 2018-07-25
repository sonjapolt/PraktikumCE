package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 11/02/2017.
 */
public class RemoveSubjectCommand extends ProcessChangeCommand {

    Subject subject;
    Process process;

    public RemoveSubjectCommand(Process p, Subject s) { //, Instance i) {
        super();
        subject = s;
        process = p;
    }

    @Override
    public boolean perform(Process p) {
        process = p;
        process.removeSubject(subject);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        process = p;
        process.addSubject(subject);
        return true;
    }

    @Override
    public String toString() {
        return "Removed actor \""+subject+"\"";
    }

}
