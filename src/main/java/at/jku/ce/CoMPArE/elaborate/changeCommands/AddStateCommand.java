package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by oppl on 15/12/2016.
 */
public class AddStateCommand extends ProcessChangeCommand {

    private State target;
    private State newState;
    private Subject s;
    private boolean before;
    private String delayedTarget;

    public AddStateCommand(Subject s, State target, State newState, boolean before) {
        super();
        this.target = target;
        this.newState = newState;
        this.before = before;
        this.s = s;
        this.delayedTarget = null;
    }

    public AddStateCommand(Subject s, String target, State newState, boolean before) {
        super();
        this.target = null;
        this.delayedTarget = target;
        this.newState = newState;
        this.before = before;
        this.s = s;
    }

    public AddStateCommand(Subject s, State newState) {
        super();
        this.newState = newState;
        this.s = s;
        this.before = true;
        this.target = null;
    }

    @Override
    public boolean perform(Process p) {
        s = p.getSubjectByUUID(s.getUUID());
        Iterator<State> i = s.getStates().iterator();
        while (i.hasNext()) {
            State state = i.next();
            if (state.getName().equals(delayedTarget)) target = state;
        }
//        if (target == null) return false;
        if (before) newActiveState = newState;
        s.addState(newState);
        if (newState instanceof RecvState) {
            s.getParentProcess().addMessages(((RecvState) newState).getRecvdMessages());
        }
        if (newState instanceof SendState) {
            s.getParentProcess().addMessage(((SendState) newState).getSentMessage());
        }
        if (before) {
            if (target == null) {
                if (s.getFirstState() == null) s.setFirstState(newState);
                else s.addState(newState);
                return true;
            }
            if (target == s.getFirstState() || s.getFirstState() == null) {
                s.setFirstState(newState);
                if (target != null) newState.addNextState(target);
                return true;
            }

            Set<State> predecessorStates = s.getPredecessorStates(target);

            if (!predecessorStates.isEmpty()) {
                for (State predecessorState : predecessorStates) {
                    Condition c = predecessorState.getNextStates().get(target);
                    predecessorState.removeNextState(target);
                    predecessorState.addNextState(newState, c);
                }
                newState.addNextState(target);
                return true;
            } else return false;
        } else {
            for (State nextState : target.getNextStates().keySet()) {
                newState.addNextState(nextState, target.getNextStates().get(nextState));
            }
            target.removeAllNextStates();
            target.addNextState(newState);
            if (target.isEndState()) {
                target.setEndState(false);
                newState.setEndState(true);
            }
            return true;
        }
    }

    @Override
    public boolean undo(Process p) {
//        if (target == null) return false;
        s = p.getSubjectByUUID(s.getUUID());
        newActiveState = target;

        if (newState instanceof RecvState) {
            for (Message m:((RecvState) newState).getRecvdMessages()) {
//                s.getParentProcess().removeMessage(m);
            }
        }
        if (newState instanceof SendState) {
//            s.getParentProcess().removeMessage(((SendState) newState).getSentMessage());
            //todo: this bugfix might leave messages as artifacts - we should check if a message is still needed and then remove it
        }
        if (before) {
            if (target == null) {
                if (s.getFirstState().equals(newState)) s.setFirstState(null);
                s.removeState(newState);
                return true;
            }
            if (newState == s.getFirstState()) {
//                // LogHelper.logDebug("setting "+target+" to new first state instead of "+newState);
                s.setFirstState(target);
                newActiveState = target;
                s.removeState(newState);
                return true;
            }

            Set<State> predecessorStates = s.getPredecessorStates(newState);

            if (!predecessorStates.isEmpty()) {
                for (State predecessorState : predecessorStates) {
                    Condition c = predecessorState.getNextStates().get(newState);
                    predecessorState.removeNextState(newState);
                    predecessorState.addNextState(target, c);
                }
                newActiveState = target;
                s.removeState(newState);
                return true;
            } else return false;
        } else {
            target.removeNextState(newState);
            for (State nextState : newState.getNextStates().keySet()) {
                target.addNextState(nextState, newState.getNextStates().get(nextState));
            }
            newActiveState = target;
            s.removeState(newState);
            if (newState.isEndState()) target.setEndState(true);
            return true;
        }
    }

    @Override
    public String toString() {
        return "Added \""+newState+"\" "+(before?"before":"after")+" \""+target+"\"";
    }

}

