package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldGroup;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.ExpandingScaffold;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;

import java.util.Random;

/**
 * Created by oppl on 25/11/2016.
 */
public class DummyAgent extends ScaffoldingAgent {

    public DummyAgent(Process p, ScaffoldingManager manager) {
        super(p,manager);
        freq = ScaffoldingAgent.FREQ_EACHSTEP;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void updateScaffolds(Instance currentInstance, State finishedState) {
        clearAllScaffolds();
        Random r = new Random();
        for (int i=0; i < r.nextInt(20); i++) {
            ScaffoldGroup sg = new ScaffoldGroup();
            sg.addScaffold(Scaffold.TYPE_CONCEPTUAL, new ExpandingScaffold("This is my conceptual scaffold "+ i,"This is the <b>detailed</b> description", this,"DuA"+i));
            if (i % 2 == 0) sg.addScaffold(Scaffold.TYPE_METACOGNITIVE, new ExpandingScaffold("This is my metacognitive scaffold "+ i,"This is the <b>detailed</b> description", this,"DuA"+i));
            if (i % 3 == 0) sg.addScaffold(Scaffold.TYPE_STRATEGIC, new ExpandingScaffold("This is my strategic scaffold "+ i,"This is the <b>detailed</b> description", this,"DuA"+i));
            if (i % 5 == 0) sg.addScaffold(Scaffold.TYPE_PROCEDURAL, new ExpandingScaffold("This is my procedural scaffold "+ i,"This is the <b>detailed</b> description", this,"DuA"+i));
            scaffoldGroups.add(sg);
        }
    }

}
