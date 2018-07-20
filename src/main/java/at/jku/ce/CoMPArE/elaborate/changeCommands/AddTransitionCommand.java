package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;
import at.jku.ce.CoMPArE.process.Transition;

/**
 * Created by oppl on 11/02/2017.
 */
public class AddTransitionCommand extends ProcessChangeCommand {

    Transition transition;
    Subject subject;

    public AddTransitionCommand(Subject s, Transition t) {
        super();
        transition = t;
        subject = s;

    }

    @Override
    public boolean perform(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        for (Transition t : subject.getTransitions()) {
            if (t.equals(transition)) {
                return false;
            }
        }
        subject.addTransition(transition);
        return true;
    }

    @Override
    public boolean undo(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        subject.removeTransition(transition);
        return true;
    }

    @Override
    public String toString() {
        return "Added \""+transition.toString()+"\"";
    }


}
