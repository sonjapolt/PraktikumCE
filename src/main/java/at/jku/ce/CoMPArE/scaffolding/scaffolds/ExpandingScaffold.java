package at.jku.ce.CoMPArE.scaffolding.scaffolds;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.scaffolding.agents.ScaffoldingAgent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Created by oppl on 25/11/2016.
 */
public class ExpandingScaffold extends Scaffold {

    private String description;

    public ExpandingScaffold(String scaffoldingPrompt, String description, ScaffoldingAgent generator, String uniqueID) {
        this(scaffoldingPrompt, description, generator, Scaffold.PRIO_MED, uniqueID);
    }

    public ExpandingScaffold(String scaffoldingPrompt, String description, ScaffoldingAgent generator, int prio, String uniqueID) {
        super(scaffoldingPrompt, generator, prio, uniqueID);
        this.description = description;
        this.interactiveComponent = new Button("Show Details");
        ((Button)interactiveComponent).addClickListener( e -> {
            LogHelper.logInfo("Scaffolding: window with detailed instructions opened: "+scaffoldingPrompt);
            generator.getManager().openScaffoldingDetails(new DescriptionUI(description));
        });
    }

    private class DescriptionUI extends Window {

        GridLayout gLayout = new GridLayout(2,3);

        public DescriptionUI(String description) {
            super("Description");
            this.center();
            gLayout.setWidth("100%");
            this.setWidth("400px");
            Label titleLabel = new Label("<b>"+getScaffoldingPrompt()+"</b>",ContentMode.HTML);
            Label descrLabel = new Label(description, ContentMode.HTML);
            Button close = new Button("Close");
            Button dismiss = new Button("Dismiss");

            close.addClickListener( e -> {
                this.close();
            });

            dismiss.addClickListener( e -> {
                ExpandingScaffold.this.generator.removeScaffold(ExpandingScaffold.this);
                this.close();
            });

            gLayout.addComponent(titleLabel,0,0,1,0);
            gLayout.addComponent(descrLabel,0,1,1,1);
            gLayout.addComponent(close,0,2);
            gLayout.addComponent(dismiss,1,2);

            gLayout.setRowExpandRatio(1,1);

            gLayout.setMargin(true);
            gLayout.setSpacing(true);
            setContent(gLayout);
        }
    }
}
