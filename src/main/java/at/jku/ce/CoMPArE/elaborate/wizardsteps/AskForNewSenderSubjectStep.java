package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.RecvState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForNewSenderSubjectStep extends ElaborationStep {

    State state;

    final Label questionPrompt;
    final TextField inputField;
    final String messageName;

    public AskForNewSenderSubjectStep(Wizard owner, String input, Subject s, Instance i) {
        super(owner, s, i);

        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I can get \"" + input + "\" from somebody else.");
        questionPrompt = new Label("I can get \"" + input + "\" from somebody else.");
        inputField = new TextField("Whom do you get \"" + input + "\" from?");
        messageName = input;

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);
        Subject newSubject = new Subject(inputField.getValue());
        processChanges.add(new AddSubjectCommand(instance.getProcess(),newSubject));
        RecvState newState = new RecvState("Wait for " + messageName);
        Message newMessage = new Message(messageName);
        processChanges.add(new AddStateCommand(subject,state,newState,true));
        processChanges.add(new AddMessageToRecvStateCommand(newState,newMessage));
        processChanges.add(new AddExpectedMessageCommand(newSubject,newMessage));
        return processChanges;
    }
}
