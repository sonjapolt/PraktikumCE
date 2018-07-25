package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddSubjectCommand extends ProcessChangeCommand {

    Subject subject;
    Process process;
//    Instance instance;
    boolean intialAnonymousAdd;

    public AddSubjectCommand(Process p, Subject s) { //, Instance i) {
        super();
        subject = s;
        process = p;
//        instance = i;
        intialAnonymousAdd = false;
    }

    @Override
    public boolean perform(Process p) {
        process = p;
        for (Subject s : process.getSubjects()) {
            if (subject.toString().equals(Subject.ANONYMOUS) && s.toString().equals(Subject.ANONYMOUS)) {
                return true;
            }
            if (s.toString().equals(subject.toString())) {
                return false;
            }
        }
        if (subject.toString().equals(Subject.ANONYMOUS)) intialAnonymousAdd = true;
        process.addSubject(subject);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        process = p;
        if (subject.toString().equals(Subject.ANONYMOUS) && intialAnonymousAdd == false) return true;
        process.removeSubject(subject);
        return true;
    }

    @Override
    public String toString() {
        return "Added actor \""+subject+"\"";
    }

}
