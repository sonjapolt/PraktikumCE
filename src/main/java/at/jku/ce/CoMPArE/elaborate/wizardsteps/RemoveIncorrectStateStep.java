package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveStateCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class RemoveIncorrectStateStep extends ElaborationStep {

    State state;

    final OptionGroup answerOptions;
    final String optionRemove;
    final String optionReplace;

    public RemoveIncorrectStateStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("\"" + state + "\" is incorrect.");

        final Label questionPrompt = new Label("\"" + state + "\" is incorrect.");

        answerOptions = new OptionGroup("How can this be corrected?");
        optionRemove = new String("Simply remove \"" + state + "\".");
        optionReplace = new String("Replace \"" + state + "\" with something else.");

        answerOptions.addItem(optionRemove);
        answerOptions.addItem(optionReplace);

        answerOptions.addValueChangeListener(e -> {
            setCanAdvance(true);
            String selection = (String) answerOptions.getValue();
            if (selection != null) {
                removeNextSteps();
                ElaborationStep step = null;
                if (selection.equals(optionReplace)) step = new ReplaceIncorrectStateStep(owner, subject, instance);
                addNextStep(step);
            }

        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);

        String selection = (String) answerOptions.getValue();
        if (selection.equals(optionRemove)) {
            processChanges.add(new RemoveStateCommand(subject, state));
        }
        return processChanges;
    }
}
