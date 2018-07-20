package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForSystemStep extends ElaborationStep{

    State state;
    final Label questionPrompt;
    final TextField inputField;
    final String newMessage;

    public AskForSystemStep(Wizard owner, String input, Subject s, Instance i) {
        super(owner, s, i);
        newMessage = input;
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I can retrieve \"" + input + "\" from a system I have access to.");

        questionPrompt = new Label("I can retrieve \"" + input + "\" from a system I have access to.");
        inputField = new TextField("Which system is this?");

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
        processChanges.add(new AddStateCommand(subject, state, new ActionState("Retrieve " + newMessage + " from " + inputField.getValue()),true));
        return processChanges;
    }
}
