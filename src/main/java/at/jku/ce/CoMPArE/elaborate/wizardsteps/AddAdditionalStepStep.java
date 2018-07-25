package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.ElaborationUI;
import at.jku.ce.CoMPArE.elaborate.StateClickListener;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveProvidedMessageCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;
import java.util.UUID;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddAdditionalStepStep extends ElaborationStep implements StateClickListener {

    final String optionNoInput;
    final OptionGroup availableProvidedMessages;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;
    ResultsProvidedToOthersStep newMessageStep;

    public AddAdditionalStepStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        newMessageStep = null;
        caption = new String("I want to add an additional newMessageStep for  " + subject + ".");
        questionPrompt = new Label("I want to add an additional newMessageStep for  " + subject + ".");
        inputField = new TextField("What do you want to do?");
        newMessage = new CheckBox("This activity leads to results I can provide to others.");

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
            if (newMessageStep != null) {
                removeParticularFollowingStep(newMessageStep);
                newMessageStep = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
                addNextStep(newMessageStep);
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            removeNextSteps();
            if (value == Boolean.TRUE) newMessageStep = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
            else newMessageStep = null;
            addNextStep(newMessageStep);
        });

        availableProvidedMessages = new OptionGroup("There is input available, on which you might want to react:");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        optionNoInput = new String("I don't want to react on any input.");
        availableProvidedMessages.addItem(optionNoInput);
        availableProvidedMessages.setValue(optionNoInput);


        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        if (!subject.getProvidedMessages().isEmpty()) fLayout.addComponent(availableProvidedMessages);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {

        RecvState newRecvState = null;
        if (availableProvidedMessages.getValue() instanceof Message) {
            Message m = (Message) availableProvidedMessages.getValue();
            newRecvState = new RecvState("Wait for " + m);
            newRecvState.addRecvdMessage(m);
            processChanges.add(new AddStateCommand(subject,instance.getHistoryForSubject(subject).getFirst().getState(),newRecvState,false));
            processChanges.add(new RemoveProvidedMessageCommand(subject, m));
        }
        State newActionState = new ActionState(inputField.getValue());
        if (inputField.getData() instanceof UUID) newActionState = subject.getStateByUUID((UUID) inputField.getData());
        if (newRecvState != null) processChanges.add(new AddStateCommand(subject, newRecvState, newActionState,false));
        else processChanges.add(new AddStateCommand(subject, instance.getHistoryForSubject(subject).getFirst().getState(), newActionState,false));
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
