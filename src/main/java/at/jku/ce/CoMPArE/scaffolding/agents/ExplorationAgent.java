package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.execute.InstanceHistory;
import at.jku.ce.CoMPArE.execute.InstanceHistoryStep;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldGroup;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.ExpandingScaffold;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.SimulatingScaffold;
import at.jku.ce.CoMPArE.simulate.Simulator;

import java.util.*;

/**
 * Created by oppl on 25/11/2016.
 */
public class ExplorationAgent extends ScaffoldingAgent {

    Map<Subject,Set<State>> visitedStates;
    Instance finishedInstance;

    public ExplorationAgent(Process p, ScaffoldingManager manager) {
        super(p, manager);
        mode = ScaffoldingAgent.FREQ_EACHINSTANCE;
        visitedStates = new HashMap<>();
        finishedInstance = null;
    }

    @Override
    public void init() {
        super.init();
        visitedStates.clear();
        for (Subject s : process.getSubjects()) {
            visitedStates.put(s,new HashSet<>());
        }
    }

    @Override
    public void updateScaffolds(Instance finishedInstance) {

        clearAllScaffolds();
        this.finishedInstance = finishedInstance;
        for (Subject s : finishedInstance.getProcess().getSubjects()) {
            // LogHelper.logDebug("ExplorationAgent checking subject "+s.toString());
            if (!visitedStates.keySet().contains(s)) visitedStates.put(s,new HashSet<>());
            LinkedList<InstanceHistoryStep> steps = finishedInstance.getHistoryForSubject(s);
            for (InstanceHistoryStep step: steps) visitedStates.get(s).add(step.getState());
            ScaffoldGroup sg = generateScaffoldsForSubject(s);
            if (sg != null) scaffoldGroups.add(sg);
        }
    }

    public ScaffoldGroup generateScaffoldsForSubject(Subject s) {
        ScaffoldGroup sg = new ScaffoldGroup();
        Set<State> remainingStates = new HashSet<>();
        remainingStates.addAll(s.getStates());
        remainingStates.removeAll(visitedStates.get(s));
        // LogHelper.logDebug("ExplorationAgent: removing "+visitedStates.get(s).size()+" visited states of "+s.getStates().size()+" overall states for "+s+" ... now "+remainingStates.size()+" states remaining");
        if (remainingStates.isEmpty()) return null;
        State nextSuggestedState = findNextSuggestedState(s.getFirstState(),remainingStates, new HashSet<>());
        if (nextSuggestedState!=null) {
            if (nextSuggestedState == s.getFirstState()) {
                // LogHelper.logDebug("ExplorationAgent: checking "+ s.getFirstState() +" for "+s);
                sg.addScaffold(Scaffold.TYPE_METACOGNITIVE, new Scaffold(s + " has not yet been explored at all.", this, "ExA"+s.toString()+Scaffold.TYPE_METACOGNITIVE));
                StringBuffer stratDescr = new StringBuffer();
                if (nextSuggestedState instanceof RecvState) {
                    // LogHelper.logDebug("ExplorationAgent: "+ s.getFirstState() +" is a RecvState");
                    stratDescr.append("<p>"+s+" is waiting for information to start its behaviour.</p>" +
                            "<p>The information it waits for is ");
                    Iterator<Message> i = ((RecvState)nextSuggestedState).getRecvdMessages().iterator();
                    while (i.hasNext()) {
                        Message m = i.next();
                        // LogHelper.logDebug("ExplorationAgent: can progress on Message  "+ m);
                        stratDescr.append("\""+m+"\" (provided by "+ process.getSenderOfMessage(m)+")");
                        if (i.hasNext()) stratDescr.append(" or ");
                    }
                    stratDescr.append("</p>");
                }
                else  stratDescr.append("<p>You can perform \"" + nextSuggestedState + "\" by clicking " +
                        "the \"Perform step\"-Button of " + s + "</p>");
                sg.addScaffold(Scaffold.TYPE_STRATEGIC, new ExpandingScaffold(
                        s + " still needs to be explored. It starts with step \"" + nextSuggestedState + "\" that has not yet performed.",
                        stratDescr.toString(),
                        this,
                        "ExA"+nextSuggestedState.toString()+Scaffold.TYPE_STRATEGIC
                ));

            }
            else {
                // LogHelper.logDebug("ExplorationAgent: checking "+ nextSuggestedState +" for "+s);
                sg.addScaffold(Scaffold.TYPE_METACOGNITIVE, new Scaffold(s + " contains steps that you have not yet explored.", this, "ExA"+s.toString()+Scaffold.TYPE_METACOGNITIVE));
                sg.addScaffold(Scaffold.TYPE_STRATEGIC, new SimulatingScaffold(
                        s + " contains step \"" + nextSuggestedState + "\" that has not yet been explored.",
                        "<p>If you are unsure how to get to \"" + nextSuggestedState + "\", you can check the path by clicking " +
                                "the \"Show behaviour\"-Button of " + s + "<p>",
                        this,
                        "ExA"+nextSuggestedState.toString()+Scaffold.TYPE_STRATEGIC,
                        nextSuggestedState
                ));
                StringBuffer procDescr = new StringBuffer();
                procDescr.append("<p>You need to make sure that the following things happen (in this order) to reach \"" + nextSuggestedState + "\":<ol>");
                // LogHelper.logDebug("ExplorationAgent: checking path to "+ nextSuggestedState +" for "+s);
                procDescr.append(getConditionsToState(s,nextSuggestedState));
/*                for (Condition c : getConditionsToState(s, nextSuggestedState)) {
                    // LogHelper.logDebug("ExplorationAgent: condition " +c+ " required for "+ nextSuggestedState +" in "+s);
                    if (c instanceof MessageCondition)
                        procDescr.append("<li>Input \"" + process.getMessageByUUID(((MessageCondition) c).getMessage()).toString() + "\" needs to be provided by " + process.getSenderOfMessage(process.getMessageByUUID(((MessageCondition) c).getMessage())) + "</li>");
                    else procDescr.append("<li>Option \"" + c + "\" needs to be selected.</li>");
                }*/
                procDescr.append("</ol></p>");
                sg.addScaffold(Scaffold.TYPE_PROCEDURAL, new SimulatingScaffold(
                        "You might want to navigate to \"" + nextSuggestedState + "\" in " + s + " to further explore its behaviour.",
                        procDescr.toString(),
                        this,
                        "ExA"+nextSuggestedState.toString()+Scaffold.TYPE_PROCEDURAL,
                        nextSuggestedState
                ));
            }
        }
        else return null;
        return sg;
    }

    private State findNextSuggestedState(State state, Set<State> remainingStates, Set<State> alreadyVisited) {
        if (remainingStates.contains(state)) return state;
        alreadyVisited.add(state);
        for (State next: state.getNextStates().keySet()) {
            if (alreadyVisited.contains(next)) return null;
            State candidate = findNextSuggestedState(next,remainingStates, alreadyVisited);
            if (candidate!=null) return candidate;
        }
        return null;
    }

    private String getConditionsToState(Subject s, State target) {
        LinkedList<Condition> conditions = new LinkedList<>();
        Simulator sim = new Simulator(finishedInstance);
        return sim.getPathToState(target);
/*        State currentState = target;
        while (currentState != s.getFirstState()) {
            State predecessor = s.getPredecessorStates(currentState).iterator().next();
            Condition c = predecessor.getNextStates().get(currentState);
            if (c != null && !c.toString().equals("")) conditions.addFirst(c);
            currentState = predecessor;
        }
        return conditions;*/
    }
}
