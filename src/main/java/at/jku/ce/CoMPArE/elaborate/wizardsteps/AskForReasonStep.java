package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.RecvState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 16/12/2016.
 */
public class AskForReasonStep extends ElaborationStep {

    public AskForReasonStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        State state = instance.getAvailableStateForSubject(subject);
        caption = new String("What is the problem with \"" + state + "\"?");

        final Label questionPrompt = new Label("What is the problem with \"" + state + "\"?");

        final OptionGroup answerOptions = new OptionGroup("Please select:");
        final String option1 = new String("It can't be done at the moment.");
        final String option2 = new String("I rather need to do something else instead.");
        final String option3 = new String("It's too vague to be performed.");
        final String option4 = new String("It's incorrect.");
        final String option5 = new String("I need to react on additional input here");

        answerOptions.addItem(option1);
        answerOptions.addItem(option2);
        answerOptions.addItem(option3);
        answerOptions.addItem(option4);
        // LogHelper.logDebug("Checking if RecvState");
        if (i.getAvailableStateForSubject(s) instanceof RecvState) answerOptions.addItem(option5);
        answerOptions.addValueChangeListener(e -> {
            setCanAdvance(true);
            String selection = (String) answerOptions.getValue();
            if (selection != null) {
                removeNextSteps();
                ElaborationStep step = null;
                if (selection.equals(option1)) step = new CantBeDoneStep(owner, subject, instance);
                if (selection.equals(option2)) step = new SomeThingElseInsteadStep(owner, subject, instance);
                if (selection.equals(option3)) step = new TooVagueStep(owner, subject, instance);
                if (selection.equals(option4)) step = new RemoveIncorrectStateStep(owner, subject, instance);
                if (selection.equals(option5)) step = new AddMessageToRecvStep(owner, subject, instance);
                addNextStep(step);
            }
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        return processChanges;
    }

}
