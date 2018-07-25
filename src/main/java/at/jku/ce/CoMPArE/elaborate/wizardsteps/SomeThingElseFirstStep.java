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
import java.util.UUID;

/**
 * Created by oppl on 17/12/2016.
 */
public class SomeThingElseFirstStep extends ElaborationStep {

    State state;
    ResultsProvidedToOthersStep step;

    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;
    final OptionGroup availableProvidedMessages;
    final String optionNo;

    public SomeThingElseFirstStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I need to do something else before I do \"" + state + "\".");

        questionPrompt = new Label("I need to do something else before I do \"" + state + "\".");
        inputField = new TextField("What do you need to do?");
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

        availableProvidedMessages = new OptionGroup("Do you want to react on any of the following available inputs in this newMessageStep?");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        optionNo = new String("No");
        availableProvidedMessages.addItem(optionNo);
        availableProvidedMessages.setValue(optionNo);

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        if (!subject.getProvidedMessages().isEmpty()) fLayout.addComponent(availableProvidedMessages);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);
        Object selectedItem = availableProvidedMessages.getValue();

        RecvState newRecvState = null;
        if (availableProvidedMessages.getValue() instanceof Message) {
            Message m = (Message) availableProvidedMessages.getValue();
            newRecvState = new RecvState("Wait for " + m);
            newRecvState.addRecvdMessage(m);
            processChanges.add(new AddStateCommand(subject,state,newRecvState,true));
            processChanges.add(new RemoveProvidedMessageCommand(subject, m));
        }
        State newActionState = new ActionState(inputField.getValue());
        if (inputField.getData() instanceof UUID) newActionState = subject.getStateByUUID((UUID) inputField.getData());
        if (newRecvState != null) processChanges.add(new AddStateCommand(subject, newRecvState, newActionState,false));
        else processChanges.add(new AddStateCommand(subject, state, newActionState,true));

        return processChanges;
    }
}
