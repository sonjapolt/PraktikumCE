package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.ElaborationUI;
import at.jku.ce.CoMPArE.elaborate.StateClickListener;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;
import sun.rmi.runtime.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by oppl on 17/12/2016.
 */
public class SomeThingElseInsteadStep extends ElaborationStep implements StateClickListener {

    State state;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;
    final OptionGroup relationship;
    final String optionConditionalReplace;
    final String optionAdditionalActivity;

    ResultsProvidedToOthersStep newMessageStep = null;
    ElaborationStep specifyConditionsStep = null;

    public SomeThingElseInsteadStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I need to do something else instead of \"" + state + "\".");

        questionPrompt = new Label("I need to do something else instead of \"" + state + "\".");
        inputField = new TextField("What do you need to do?");
        newMessage = new CheckBox("This step leads to results I can provide to others.");
        relationship = new OptionGroup("How does this relate to \"" + state + "\"?");
        optionConditionalReplace = new String("It replaces \"" + state + "\" under certain conditions.");
        optionAdditionalActivity = new String("It is complementary to \"" + state + "\", I still need to do \"" + state + "\", too.");

        final Button selectFromExisting = new Button("Let me choose from existing steps"); // needs to remove
        selectFromExisting.addClickListener(e -> {
            CoMPArEUI parent = ((CoMPArEUI) owner.getUI());
            parent.notifyAboutClickedState(this);
            parent.expandVisualizationSlider(s);
            ElaborationUI elaborationUI = (ElaborationUI) parent.getWindows().iterator().next();
            elaborationUI.setVisible(false);
        });

        inputField.addValueChangeListener(e -> {
            if (inputField.getData() instanceof UUID && !subject.getStateByUUID((UUID) inputField.getData()).toString().equals(inputField.getValue()))
                inputField.setData(null);

            if (!(inputField.getData() instanceof UUID)) {
                newMessage.setVisible(true);
                newMessage.setDescription("");
                relationship.setEnabled(true);
                relationship.setDescription("");
                if (specifyConditionsStep != null) {
                    removeParticularFollowingStep(specifyConditionsStep);
                    if (inputField.getData() instanceof UUID) specifyConditionsStep = new AskForConditionsStep(owner, subject.getStateByUUID((UUID) inputField.getData()), subject, instance);
                    else specifyConditionsStep = new AskForConditionsStep(owner, inputField.getValue(), subject, instance);
                    if (newMessageStep != null) {
                        removeParticularFollowingStep(newMessageStep);
                        specifyConditionsStep.addNextStep(newMessageStep);
                    }
                    addNextStep(specifyConditionsStep);
                }
                if (newMessageStep != null) {
                    removeParticularFollowingStep(newMessageStep);
                    newMessageStep = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
                    if (specifyConditionsStep != null) {
                        specifyConditionsStep.addNextStep(newMessageStep);
                    }
                    else addNextStep(newMessageStep);

                }
            }
            if ((inputField.getData() instanceof UUID) && (newMessageStep != null)) {
                removeParticularFollowingStep(newMessageStep);
                newMessageStep = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
                if (specifyConditionsStep != null) {
                    specifyConditionsStep.addNextStep(newMessageStep);
                }
                else addNextStep(newMessageStep);
            }
            if (specifyConditionsStep != null) {
                if (inputField.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
            }

        });

        relationship.addItem(optionConditionalReplace);
        relationship.addItem(optionAdditionalActivity);

        relationship.addValueChangeListener(e -> {
            Object selectedItem = e.getProperty().getValue();

            if (selectedItem == optionConditionalReplace) {
                if (inputField.getData() instanceof UUID) specifyConditionsStep = new AskForConditionsStep(owner, subject.getStateByUUID((UUID) inputField.getData()), subject, instance);
                else specifyConditionsStep = new AskForConditionsStep(owner, inputField.getValue(), subject, instance);
                if (newMessageStep != null) {
                    removeParticularFollowingStep(newMessageStep);
                    addNextStep(specifyConditionsStep);
                    specifyConditionsStep.addNextStep(newMessageStep);
                }
                else {
                    addNextStep(specifyConditionsStep);
                }
                if (!inputField.getValue().equals("")) setCanAdvance(true);
            }
            else {
                removeParticularFollowingStep(specifyConditionsStep);
                specifyConditionsStep = null;
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) {
                newMessageStep = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
                if (specifyConditionsStep != null) specifyConditionsStep.addNextStep(newMessageStep);
                else addNextStep(newMessageStep);
                if (!inputField.getValue().equals("") && specifyConditionsStep != null) setCanAdvance(true);
            }
            else {
                removeParticularFollowingStep(newMessageStep);
                newMessageStep = null;
            }

        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(relationship);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);

        String selection = relationship.getValue().toString();

        if (selection.equals(optionAdditionalActivity)) {
            if (inputField.getData() instanceof UUID) processChanges.add(new AddStateCommand(subject,state, subject.getStateByUUID((UUID) inputField.getData()),true));
            else processChanges.add(new AddStateCommand(subject,state, new ActionState(inputField.getValue()),true));
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
            newMessage.setValue(false);
            newMessage.setVisible(false);
            newMessage.setDescription("You cannot alter the selected existing newMessageStep here.");
            newMessage.setDescription("Existing steps can only be inserted as alternatives to the current newMessageStep.");
            if (newMessageStep != null) {
                removeParticularFollowingStep(newMessageStep);
                newMessageStep = null;
            }
        }
        if (specifyConditionsStep != null) {
            removeParticularFollowingStep(specifyConditionsStep);
            specifyConditionsStep = new AskForConditionsStep(owner, subject.getStateByUUID((UUID) inputField.getData()), subject, instance);
            addNextStep(specifyConditionsStep);
        }
     }

}
