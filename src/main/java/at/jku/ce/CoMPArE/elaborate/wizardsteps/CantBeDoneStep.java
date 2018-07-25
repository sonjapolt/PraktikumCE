package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 16/12/2016.
 */
public class CantBeDoneStep extends ElaborationStep {

    public CantBeDoneStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);

        State state = instance.getAvailableStateForSubject(subject);
        caption = new String("\"" + state + "\" can't be done at the moment.");

        final Label questionPrompt = new Label("\"" + state + "\" can't be done at the moment.");

        final OptionGroup answerOptions = new OptionGroup("Why?");
        final String optionSomethingElse = new String("I need to do something else first.");
        final String optionMoreInput = new String("I need more input to be able to do this activity.");

        answerOptions.addValueChangeListener(e -> {
            setCanAdvance(true);
            String selection = (String) answerOptions.getValue();
            if (selection != null) {
                removeNextSteps();
                ElaborationStep step = null;
                if (selection.equals(optionSomethingElse)) step = new SomeThingElseFirstStep(owner, subject, instance);
                if (selection.equals(optionMoreInput)) step = new NeedMoreInputStep(owner, subject, instance);
                addNextStep(step);
            }
        });

        answerOptions.addItem(optionSomethingElse);
        answerOptions.addItem(optionMoreInput);

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        return processChanges;
    }

}
