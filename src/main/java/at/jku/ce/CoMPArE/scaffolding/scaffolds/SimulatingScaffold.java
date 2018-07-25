package at.jku.ce.CoMPArE.scaffolding.scaffolds;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.scaffolding.agents.ScaffoldingAgent;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Created by oppl on 30/11/2016.
 */
public class SimulatingScaffold extends ExpandingScaffold {

    Button simulate;

    public SimulatingScaffold(String scaffoldingPrompt, String description, ScaffoldingAgent generator, String uniqueID, State target) {
        this(scaffoldingPrompt, description, generator, Scaffold.PRIO_MED, uniqueID, target);
    }

    public SimulatingScaffold(String scaffoldingPrompt, String description, ScaffoldingAgent generator, int prio, String uniqueID, State target) {
        super(scaffoldingPrompt, description, generator, prio, uniqueID);
        this.interactiveComponent = new Button("Show Details");
        ((Button)interactiveComponent).addClickListener( e -> {
            LogHelper.logInfo("Scaffolding: window with detailed instructions opened: "+scaffoldingPrompt);
            generator.getManager().openScaffoldingDetails(new TriggerSimulationUI(description,target));
        });
    }

    private class TriggerSimulationUI extends Window {

        GridLayout gLayout = new GridLayout(3,3);

        public TriggerSimulationUI(String description, State target) {
            super("Description");
            this.center();
            gLayout.setWidth("100%");
            this.setWidth("400px");
            Label titleLabel = new Label("<b>"+getScaffoldingPrompt()+"</b>", ContentMode.HTML);
            Label descrLabel = new Label(description, ContentMode.HTML);
            Button close = new Button("Close");
            Button dismiss = new Button("Dismiss");
            Button simulate = new Button("Take me there");

            close.addClickListener( e -> {
                this.close();
            });

            dismiss.addClickListener( e -> {
                SimulatingScaffold.this.generator.removeScaffold(SimulatingScaffold.this);
                this.close();
            });

            simulate.addClickListener( e -> {
                this.close();
                boolean simSuccessful = ((CoMPArEUI)UI.getCurrent()).simulate(target);
                if (simSuccessful) SimulatingScaffold.this.generator.removeScaffoldGroupOfScaffold(SimulatingScaffold.this);

            });

            gLayout.addComponent(titleLabel,0,0,2,0);
            gLayout.addComponent(descrLabel,0,1,2,1);
            gLayout.addComponent(close,0,2);
            gLayout.addComponent(simulate,1,2);
            gLayout.addComponent(dismiss,2,2);

            gLayout.setRowExpandRatio(1,1);

            gLayout.setMargin(true);
            gLayout.setSpacing(true);
            setContent(gLayout);
        }
    }

}
