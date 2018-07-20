package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveProvidedMessageCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddInitialStepStep extends ElaborationStep {

    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;
    final String optionNoInput;
    final OptionGroup availableProvidedMessages;

    public AddInitialStepStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);

        caption = new String("I want to set an initial step for  " + subject + ".");
        questionPrompt = new Label("I want to set an initial step for  " + subject + ".");
        inputField = new TextField("What do you want to do?");
        newMessage = new CheckBox("This activity leads to results I can provide to others.");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            removeNextSteps();
            ElaborationStep step;
            if (value == Boolean.TRUE) step = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
            else step = null;
            addNextStep(step);
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
            processChanges.add(new AddStateCommand(subject,(String) null,newRecvState,true));
            processChanges.add(new RemoveProvidedMessageCommand(subject, m));
        }
        State newActionState = new ActionState(inputField.getValue());
        if (newRecvState != null) processChanges.add(new AddStateCommand(subject, newRecvState, newActionState,false));
        else processChanges.add(new AddStateCommand(subject, (String) null, newActionState,true));
        return processChanges;
    }
}
