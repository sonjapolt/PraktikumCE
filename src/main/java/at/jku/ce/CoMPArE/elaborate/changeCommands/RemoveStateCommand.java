package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class RemoveStateCommand extends ProcessChangeCommand {

    State state;
    Subject subject;

    State replacementState;

    public RemoveStateCommand(Subject subject, State state) {
        super();
        this.state = state;
        this.subject = subject;
        replacementState = null;
    }

    @Override
    public boolean perform(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        Set<State> predecessorStates = subject.getPredecessorStates(state);
        Map<State, Condition> nextStates = state.getNextStates();
        if (state instanceof SendState) {
            subject.addExpectedMessage(((SendState) state).getSentMessage());
            subject.getParentProcess().addMessage(((SendState) state).getSentMessage());
        }
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages()) {
                subject.addProvidedMessage(m);
                subject.getParentProcess().addMessage(m);
            }
        }
        if (predecessorStates.isEmpty()) {
            if (nextStates.size() == 1) {
                subject.setFirstState(nextStates.keySet().iterator().next());
                subject.removeState(state);
            } else {
                if (nextStates.size() > 1)
                    state.setName("Make decision");
            }
        } else {

            for (State pre : predecessorStates) {
                for (State s : nextStates.keySet())
                    pre.addNextState(s, nextStates.get(s));
                pre.removeNextState(state);
                subject.removeState(state);
            }
        }

        if (state.isEndState()) {
            if (nextStates.isEmpty()) {
                for (State pre : predecessorStates) {
                    pre.setEndState(true);
                }
            }
            else {
                for (State post: nextStates.keySet()) {
                    post.setEndState(true);
                }
            }
        }

        if (nextStates.size()==1) newActiveState = nextStates.keySet().iterator().next();
        else newActiveState = predecessorStates.iterator().next();

        replacementState = newActiveState;

        return true;
    }

    @Override
    public boolean undo(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        Set<State> predecessorStates = subject.getPredecessorStates(replacementState);
        if (state instanceof SendState) {
            subject.removeExpectedMessage(((SendState) state).getSentMessage());
//            subject.getParentProcess().removeMessage(((SendState) state).getSentMessage());
        }
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages()) {
                subject.removeProvidedMessage(m);
//                subject.getParentProcess().removeMessage(m);
            }
        }
        if (predecessorStates.isEmpty()) {
            subject.setFirstState(state);
        } else {

            for (State pre : predecessorStates) {
                Condition c = pre.getNextStates().get(replacementState);
                pre.removeNextState(replacementState);
                pre.addNextState(state,c);
            }
        }

        if (state.isEndState()) {
            if (state.getNextStates().isEmpty()) {
                for (State pre : predecessorStates) {
                    pre.setEndState(false);
                }
            }
            else {
                for (State post: state.getNextStates().keySet()) {
                    post.setEndState(false);
                }
            }
        }

        subject.addState(state);
        newActiveState = state;

        return true;
    }

    @Override
    public String toString() {
        return "Removed \""+state+"\" from \""+subject+"\"";
    }

}
