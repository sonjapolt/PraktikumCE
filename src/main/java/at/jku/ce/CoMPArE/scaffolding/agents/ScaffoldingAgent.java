package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldGroup;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 24/11/2016.
 */
public class ScaffoldingAgent {

    public static int FREQ_EACHSTEP = 1;
    public static int FREQ_EACHINSTANCE = 2;

    protected Process process;
    protected int mode;
    protected int freq;

    private ScaffoldingManager manager;
    protected Set<ScaffoldGroup> scaffoldGroups;
    protected Set<String> dismissedScaffoldIDs;

    public ScaffoldingAgent(Process p, ScaffoldingManager manager) {
        process = p;
        this.manager = manager;
        mode = Scaffold.TYPE_CONCEPTUAL;
        freq = ScaffoldingAgent.FREQ_EACHINSTANCE;
        dismissedScaffoldIDs = new HashSet<>();
        scaffoldGroups = new HashSet<>();
    }

    public void init() {

    }

    public final void switchMode(int newMode) {
        mode = newMode;
    }

    public final Set<Scaffold> getScaffolds(int requestedType) {

        Set<Scaffold> requestedScaffolds = new HashSet<>();
        if (requestedType == ScaffoldingManager.MODE_NONE) return requestedScaffolds;

        for (ScaffoldGroup sg : scaffoldGroups) {
            Scaffold s = sg.getMostConcreteScaffoldForType(requestedType);
            if (s != null) {
                if (!dismissedScaffoldIDs.contains(s.getUniqueID())) requestedScaffolds.add(s);
            }
        }

        return requestedScaffolds;
    }

    public void updateScaffolds(Instance currentInstance, State finishedState) {

    }

    public void updateScaffolds(Instance finishedInstance) {

    }

    public final int getFreq() {
        return freq;
    }

    public final void removeScaffold(Scaffold scaffold) {
        LogHelper.logInfo("Scaffolding: scaffold dismissed: "+scaffold.getScaffoldingPrompt());
        Set<String> idsOfRemovedScaffolds = new HashSet<>();
        for (ScaffoldGroup sg : scaffoldGroups) {
            if (sg.contains(scaffold)) idsOfRemovedScaffolds.addAll(sg.removeScaffoldAndMoreVagueFromGroup(scaffold));
        }
        dismissedScaffoldIDs.addAll(idsOfRemovedScaffolds);
        manager.immediatelyUpdateScaffoldingPanel(this);
    }

    public final void removeScaffoldGroupOfScaffold(Scaffold scaffold) {
        LogHelper.logInfo("Scaffolding: scaffold group removed because a contained interactive scaffold was used: "+scaffold.getScaffoldingPrompt());
        ScaffoldGroup toBeRemoved = null;
        for (ScaffoldGroup sg : scaffoldGroups) {
            if (sg.contains(scaffold)) {
                toBeRemoved = sg;
            }
        }
        if (toBeRemoved != null) scaffoldGroups.remove(toBeRemoved);
        manager.immediatelyUpdateScaffoldingPanel(this);

    }
    public final ScaffoldingManager getManager() {
        return manager;
    }

    protected final void clearAllScaffolds() {
        scaffoldGroups.clear();
    }
}
