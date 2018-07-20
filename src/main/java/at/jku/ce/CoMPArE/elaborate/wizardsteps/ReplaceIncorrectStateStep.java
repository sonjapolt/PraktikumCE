package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.elaborate.ElaborationUI;
import at.jku.ce.CoMPArE.elaborate.StateClickListener;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ReplaceStateCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;
import java.util.UUID;

/**
 * Created by oppl on 17/12/2016.
 */
public class ReplaceIncorrectStateStep extends ElaborationStep implements StateClickListener {

    State state;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;

    ResultsProvidedToOthersStep step;

    public ReplaceIncorrectStateStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I want to replace \"" + state + "\" with something else.");

        questionPrompt = new Label("I want to replace \"" + state + "\" with something else.");
        inputField = new TextField("What is the new step?");
        newMessage = new CheckBox("This step leads to results I can provide to others.");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        final Button selectFromExisting = new Button("Let me choose from existing steps");
        selectFromExisting.addClickListener(e -> {
            CoMPArEUI parent = ((CoMPArEUI) owner.getUI());
            parent.notifyAboutClickedState(this);
            parent.expandVisualizationSlider(s);
            ElaborationUI elaborationUI = (ElaborationUI) parent.getWindows().iterator().next();
            elaborationUI.setVisible(false);
        });

        inputField.addValueChangeListener(e -> {
            if (!(inputField.getData() instanceof UUID)) {
                newMessage.setVisible(true);
                newMessage.setDescription("");
            }
            if (inputField.getData() instanceof UUID && !subject.getStateByUUID((UUID) inputField.getData()).toString().equals(inputField.getValue()))
                inputField.setData(null);
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
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);

        if (state != null) {
            State newState = new ActionState(inputField.getValue());
            if (inputField.getData() instanceof UUID) newState = subject.getStateByUUID((UUID) inputField.getData());
            processChanges.add(new ReplaceStateCommand(subject, state, newState));
        }
        return processChanges;
    }

    @Override
    public void clickedState(State state) {
        CoMPArEUI parent = ((CoMPArEUI) owner.getUI());
        ElaborationUI elaborationUI = (ElaborationUI) parent.getWindows().iterator().next();
        elaborationUI.setVisible(true);
        if (state != null) {
            inputField.setValue(state.getName());
            inputField.setData(state.getUUID());
            newMessage.setVisible(false);
            newMessage.setDescription("You cannot alter the selected existing newMessageStep here.");
            newMessage.setDescription("Existing steps can only be inserted as alternatives to the current newMessageStep.");
        }
    }

}
