package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by oppl on 15/12/2016.
 */
public class ReplaceStateCommand extends ProcessChangeCommand {

    private State state;
    private State newState;
    private Subject subject;

    public ReplaceStateCommand(Subject s, State state, State newState) {
        super();
        this.subject = s;
        this.state = state;
        this.newState = newState;
    }

    @Override
    public boolean perform(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        Set<State> predecessorStates = subject.getPredecessorStates(state);
        Map<State, Condition> nextStates = state.getNextStates();
        subject.addState(newState);
        if (state instanceof SendState) subject.addExpectedMessage(((SendState) state).getSentMessage());
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        for (State nextState : nextStates.keySet()) {
            newState.addNextState(nextState, nextStates.get(nextState));
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(newState);

        } else {
            for (State predecessor : predecessorStates) {
                Condition c = predecessor.getNextStates().get(state);
                predecessor.removeNextState(state);
                predecessor.addNextState(newState, c);
            }
        }
        if (state.isEndState()) newState.setEndState(true);
        subject.removeState(state);
        newActiveState = newState;

        return true;
    }

    @Override
    public boolean undo(Process p) {
        subject = p.getSubjectByUUID(subject.getUUID());
        Set<State> predecessorStates = subject.getPredecessorStates(state);
        subject.addState(state);
        if (state instanceof SendState) subject.removeExpectedMessage(((SendState) state).getSentMessage());
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages())
                subject.removeProvidedMessage(m);
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(state);

        } else {
            for (State pre : predecessorStates) {
                Condition c = pre.getNextStates().get(newState);
                pre.removeNextState(newState);
                pre.addNextState(state, c);
            }
        }
        subject.removeState(newState);
        newActiveState = state;

        return true;
    }

    @Override
    public String toString() {
        return "Replaced \""+state+"\" with \""+newState+"\" in \""+subject+"\"";
    }
}
