package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddConditionalStateCommand extends ProcessChangeCommand {

    private State target;
    private State newState;
    private Subject subject;
    private Map<State, Condition> originalConditions;
    private Map<State, Condition> newConditions;
    private State decisionState;

    public AddConditionalStateCommand(Subject s, State target, State newState, Map<State, Condition> originalConditions, Map<State, Condition> newConditions) {
        super();
        this.target = target;
        this.newState = newState;
        this.decisionState = null;
        this.subject = s;
        this.originalConditions = originalConditions;
        this.newConditions = newConditions;
    }

    @Override
    public boolean perform(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        subject.addState(newState);
        if (target == subject.getFirstState()) {
            decisionState = new ActionState("Make decision");
            subject.addState(decisionState);
            decisionState.addNextState(target, originalConditions.values().iterator().next());
            decisionState.addNextState(newState, newConditions.values().iterator().next());
            subject.setFirstState(decisionState);
            newActiveState = decisionState;
        }
        else {
            Set<State> predecessorStates = subject.getPredecessorStates(target);

            if (!predecessorStates.isEmpty()) {
                for (State predecessorState : predecessorStates) {
                    predecessorState.removeNextState(target);
                    predecessorState.addNextState(target, originalConditions.get(predecessorState));
                    predecessorState.addNextState(newState, newConditions.get(predecessorState));
                }
            }
        }
        newState.setEndState(true);
        newActiveState = newState;
        return true;
    }

    @Override
    public boolean undo(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        subject.removeState(newState);
        Set<State> predecessorStates = subject.getPredecessorStates(target);

        if (decisionState != null) {
            subject.removeState(decisionState);
            decisionState = null;
            subject.setFirstState(target);
        }


        if (!predecessorStates.isEmpty()) {
            for (State predecessorState : predecessorStates) {
                predecessorState.removeNextState(newState);
            }
        }
        newActiveState = target;
        return true;
    }

    @Override
    public String toString() {
        return "Added \""+newState+"\" as an alternative to \""+target+"\"";
    }

}
