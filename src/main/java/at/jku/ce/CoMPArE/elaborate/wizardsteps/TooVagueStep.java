package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ReplaceStateCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 16/12/2016.
 */
public class TooVagueStep extends ElaborationStep {

    State state;

    Label questionPrompt;
    TextField inputField;
    CheckBox newMessage;
    ResultsProvidedToOthersStep step;

    public TooVagueStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("\"" + state + "\" is too vague.");

        questionPrompt = new Label("\"" + state + "\" is too vague.");
        inputField = new TextField("What would be the first step you need to do when refining \"" + state + "\"?");
        newMessage = new CheckBox("This step leads to results I can provide to others.");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
            if (step != null) {
                removeParticularFollowingStep(step);
                step = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
                addNextStep(step);
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            removeNextSteps();
            if (value == Boolean.TRUE) step = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
            else step = null;
            addNextStep(step);
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);

        State newState = new ActionState(inputField.getValue());
        processChanges.add(new ReplaceStateCommand(subject, instance.getAvailableStateForSubject(subject),newState));
        return processChanges;
    }
}
