package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.AddSubjectCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddInitialSubjectStep extends ElaborationStep {

    final Label questionPrompt;
    final TextField inputField;

    public AddInitialSubjectStep(Wizard owner, Instance i) {
        super(owner, null, i);

        caption = new String("You want to add your first actor.");
        questionPrompt = new Label("You want to add your first actor.");
        inputField = new TextField("What's its name?");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        Subject newSubject = new Subject(inputField.getValue());
        processChanges.add(new AddSubjectCommand(instance.getProcess(),newSubject));
        return processChanges;
    }
}
