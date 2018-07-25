package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldGroup;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.ExpandingScaffold;

import java.util.Vector;

/**
 * Created by oppl on 25/11/2016.
 */
public class ElaborationProcessAgent extends ScaffoldingAgent {

    int instanceCounter;

    Vector<ScaffoldGroup> buffer;

    public ElaborationProcessAgent(Process p, ScaffoldingManager manager) {
        super(p, manager);
        this.freq = ScaffoldingAgent.FREQ_EACHINSTANCE;

        buffer = new Vector<>();

        ScaffoldGroup motivation = new ScaffoldGroup();

        motivation.addScaffold(Scaffold.TYPE_CONCEPTUAL, new ExpandingScaffold(
                "Work processes undergo constant change. Check them in regular intervals.",
                "<p>Your work process are subject of constant change. You should therefore check your support systems " +
                        "in regular intervalls to make sure they still fit your organizational reality.</p>" +
                        "<p>This system can help you to validate and adapt your work process models by playing through them.</p>",
                this,
                "EPAmotivation"+Scaffold.TYPE_CONCEPTUAL));


        ScaffoldGroup intro = new ScaffoldGroup();
        intro.addScaffold(Scaffold.TYPE_STRATEGIC, new ExpandingScaffold(
                "Play through your work process in different variants several times to check its appropriateness.",
                "<p>Your work process potentially has different variants, i.e. comprises different steps depending on the " +
                        "situation it carried out in.</p>" +
                        "<p>By playing through it and making different decisions each time, you can explore these variants.",
                this,
                "EPAintro"+Scaffold.TYPE_STRATEGIC));
        intro.addScaffold(Scaffold.TYPE_PROCEDURAL, new ExpandingScaffold(
                "You can play through your work process by using the \"Perform Step\"-Buttons.",
                "<p>Your work process potentially has different variants, i.e. comprises different steps depending on the " +
                        "situation it carried out in.</p>" +
                        "<p>You can playing through the work steps for each actor by using the \"Perform Step\"-Buttons. If there are decisions " +
                        "to be made, they will be displayed above the Button. By making different decisions, you can explore these variants. " +
                        "When you are finished, you can restart the work process by clicking the \"Restart Process\"-Button, which will appear " +
                        "at the bottom of the screen.</p>",
                this,
                "EPAintro"+Scaffold.TYPE_PROCEDURAL));

        ScaffoldGroup elaborate = new ScaffoldGroup();
        elaborate.addScaffold(Scaffold.TYPE_STRATEGIC, new ExpandingScaffold( // index 2
                "You might want to adapt your work process description if you encounter misfits to your actual work.",
                "<p>Sometimes while playing through the process, you will encounter steps that do not fit your actual work.</p>" +
                        "<p>If this happens, you can use the button labeled" +
                        "\"I have a problem here.\" to adapt the description accordingly.<p>",
                this,
                "EPAelaborate"+Scaffold.TYPE_STRATEGIC));

        buffer.add(motivation);
        buffer.add(intro);
        buffer.add(elaborate);
    }

    @Override
    public void init() {
        super.init();
        instanceCounter = 0;
        this.mode = Scaffold.TYPE_CONCEPTUAL;
        scaffoldGroups.add(buffer.elementAt(0));
        scaffoldGroups.add(buffer.elementAt(1));
    }

    @Override
    public void updateScaffolds(Instance finishedInstance) {
        clearAllScaffolds();
        instanceCounter ++;
        if (instanceCounter == 1) scaffoldGroups.add(buffer.elementAt(1));

    }

}
