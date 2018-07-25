package at.jku.ce.CoMPArE.process;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.*;

/**
 * Created by oppl on 22/11/2016.
 */
public class State extends ProcessElement {

    private String name;
    private boolean isEndState;
//    private Map<UUID, Condition> nextStates;

    @XStreamOmitField
    Subject parentSubject;

    public State(String name) {
        super();
        this.parentSubject = null;
        this.isEndState = false;
        this.name = name;
//        this.nextStates = new HashMap<>();
    }

    public State(State s, Subject newSubject) {
        super(s);
        this.parentSubject = newSubject;
        this.name = s.getName();
        this.isEndState = s.isEndState();
//        this.nextStates = new HashMap<>();
    }

    public State(String name, boolean isEndState) {
        this(name);
        this.isEndState = isEndState;
    }

    public Map<State,Condition> getNextStates() {
//
        if (parentSubject == null) return new HashMap<State,Condition>();
        return parentSubject.getNextStatesForState(this);
//        for (UUID nextStateID: nextStates.keySet())
//            nextStateMap.put(parentSubject.getStateByUUID(nextStateID),nextStates.get(nextStateID));
//        return nextStateMap;
    }

    public State addNextState(State nextState) {
        if (parentSubject == null) return null;
        parentSubject.addState(nextState);
        parentSubject.addTransition(new Transition(this,nextState,null));
//        this.nextStates.put(nextState.getUUID(), null);
        return parentSubject.getStateByUUID(nextState.getUUID());
    }

    public State addNextState(State nextState, Condition condition) {
        if (parentSubject == null) return null;
        parentSubject.addState(nextState);
        Condition newCondition = condition;
        if (newCondition != null && newCondition.getCondition().equals("")) newCondition = null;
        parentSubject.addTransition(new Transition(this,nextState,condition));
//        this.nextStates.put(nextState.getUUID(), newCondition);
//        if (newCondition != null) newCondition.setParentState(this);
        return parentSubject.getStateByUUID(nextState.getUUID());
    }

    public void removeNextState(State stateToBeRemoved) {
        parentSubject.removeAllTransitionsBetween(this,stateToBeRemoved);
//        nextStates.remove(stateToBeRemoved.getUUID());
    }

    public void removeAllNextStates() {
        parentSubject.removeAllOutgoingTransitionsFrom(this);
//        nextStates.clear();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

/*    public Set<State> getAllFollowingStates() { return this.getAllFollowingStates(this, new HashSet<State>());}

    private Set<State> getAllFollowingStates(State state, Set<State> states) {
        if (state == null) return states;
        if (states.contains(state)) return null;
        states.add(state);
        for (State next: state.getNextStates().keySet()) {
            Set<State> nextStates = this.getAllFollowingStates(next, states);
            if (nextStates!=null) states.addAll(nextStates);
        }
        return states;
    }*/

    public Subject getParentSubject() {
        return parentSubject;
    }

    public void setParentSubject(Subject parentSubject) {
        this.parentSubject = parentSubject;
    }

    public void reconstructParentRelations(Subject subject) {
        this.parentSubject = subject;
/*        for (Condition c: nextStates.values()) {
            if (c!=null) c.reconstructParentRelations(this);
        }*/
    }

    public boolean isEndState() {
        return isEndState;
    }

    public void setEndState(boolean endState) {
        isEndState = endState;
    }
}
