package at.jku.ce.CoMPArE.scaffolding.scaffolds;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.scaffolding.agents.ScaffoldingAgent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Created by oppl on 25/11/2016.
 */
public class Scaffold {

    public static int TYPE_NONE = 0;
    public static int TYPE_CONCEPTUAL = 1;
    public static int TYPE_METACOGNITIVE = 2;
    public static int TYPE_STRATEGIC = 3;
    public static int TYPE_PROCEDURAL = 4;
    public static int TYPE_ALL = 5;


    public static int PRIO_HIGH = 1;
    public static int PRIO_MED = 2;
    public static int PRIO_LOW = 1;

    protected Component interactiveComponent;

    private int prio;
    private String scaffoldingPrompt;
    protected ScaffoldingAgent generator;

    private String uniqueID;

    public Scaffold(String scaffoldingPrompt, ScaffoldingAgent generator, String uniqueID) {
        this.scaffoldingPrompt = scaffoldingPrompt;
        this.generator = generator;
        this.prio = Scaffold.PRIO_MED;
        this.uniqueID = uniqueID;
        this.interactiveComponent = new Button("Dismiss");
        ((Button)interactiveComponent).addClickListener( e -> {
            generator.removeScaffold(this);
        });
    }

    public Scaffold(String scaffoldingPrompt, ScaffoldingAgent generator, int prio, String uniqueID) {
        this(scaffoldingPrompt, generator, uniqueID);
        this.prio = prio;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }

    public String getScaffoldingPrompt() {
        return scaffoldingPrompt;
    }

    public void setScaffoldingPrompt(String scaffoldingPrompt) {
        this.scaffoldingPrompt = scaffoldingPrompt;
    }

    public Component getInteractiveComponent() {
        return interactiveComponent;
    }

}
