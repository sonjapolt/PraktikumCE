package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;
import at.jku.ce.CoMPArE.process.Transition;

/**
 * Created by oppl on 11/02/2017.
 */
public class RemoveTransitionCommand extends ProcessChangeCommand {

    Transition transition;
    Subject subject;

    public RemoveTransitionCommand(Subject s, Transition t) {
        super();
        transition = t;
        subject = s;

    }

    @Override
    public boolean perform(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        subject.removeTransition(transition);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        subject.addTransition(transition);
        return true;
    }

    @Override
    public String toString() {
        return "Removed \""+transition.toString()+"\"";
    }


}
