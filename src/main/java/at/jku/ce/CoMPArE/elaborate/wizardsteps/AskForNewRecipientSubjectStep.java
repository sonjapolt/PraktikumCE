package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForNewRecipientSubjectStep extends ElaborationStep {

    State state;

    final Label questionPrompt;
    final TextField inputField;
    final String messageName;

    String newState;

    public AskForNewRecipientSubjectStep(Wizard owner, String newState, String input, Subject s, Instance i) {
        super(owner, s, i);

        this.newState = newState;
        state = instance.getAvailableStateForSubject(subject);

        caption = new String("I can provide \""+input+"\" to somebody else.");
        questionPrompt = new Label("I can provide \"" + input + "\" to somebody else.");
        inputField = new TextField("Whom can you provide \"" + input + "\" with?");
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
        SendState newState = new SendState("Send " + messageName);
        Message newMessage = new Message(messageName);
        processChanges.add(new AddStateCommand(subject,this.newState,newState,false));
        processChanges.add(new AddMessageToSendStateCommand(newState,newMessage));
        processChanges.add(new AddProvidedMessageCommand(newSubject,newMessage));

        return processChanges;
    }
}
