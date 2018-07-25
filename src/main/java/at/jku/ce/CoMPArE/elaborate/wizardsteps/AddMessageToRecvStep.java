package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.RecvState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 08/06/2017.
 */
public class AddMessageToRecvStep extends ElaborationStep {
    State state;

    final Label questionPrompt;
    final TextField inputField;
    final OptionGroup availableProvidedMessages;
    final String optionSpecifyMyself;

    final OptionGroup infoSource;
    final String optionSomebodyElse;
    final String optionSystem;
    final String optionDontKnow;

    ElaborationStep step;

    public AddMessageToRecvStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        step = null;
        state = instance.getAvailableStateForSubject(subject);

        caption = new String("I need to react on additional input in \"" + state + "\".");

        questionPrompt = new Label("I need to react on additional input in \"" + state + "\".");
        infoSource = new OptionGroup("Where could you get it from?");

        for (Subject sub : instance.getProcess().getSubjects()) {
            if (sub != subject) infoSource.addItem(sub);
        }
        optionSomebodyElse = new String("I can get this input from somebody else.");
        optionSystem = new String("I can retrieve this input from a system I have access to.");
        optionDontKnow = new String("I do not know, where I can get this input from");
        infoSource.addItem(optionSomebodyElse);
        infoSource.addItem(optionSystem);
        infoSource.addItem(optionDontKnow);

        inputField = new TextField("Which input would you need?");
        if (subject.getProvidedMessages().size() != 0) {
            inputField.setVisible(false);
            infoSource.setVisible(false);
        }

        inputField.addValueChangeListener(e -> {
            if (infoSource.getValue() != null) {
                if (inputField.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
            }
            if (step != null ) {
                removeParticularFollowingStep(step);
                Object selectedItem = infoSource.getValue();
                if (selectedItem.equals(optionSystem)) step = new AskForSystemStep(owner, inputField.getValue(), subject, instance);
                if (selectedItem.equals(optionSomebodyElse)) step = new AskForNewSenderSubjectStep(owner, inputField.getValue(), subject, instance);
                addNextStep(step);
            }
        });

        availableProvidedMessages = new OptionGroup("There is some input available, which you currently do not use:");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        optionSpecifyMyself = new String("I need different input.");
        availableProvidedMessages.addItem(optionSpecifyMyself);
        availableProvidedMessages.addValueChangeListener(e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setVisible(true);
                infoSource.setVisible(true);
                if (inputField.getValue().equals("")) setCanAdvance(false);
            } else {
                inputField.setVisible(false);
                infoSource.setVisible(false);
                setCanAdvance(true);
            }
        });

        infoSource.addValueChangeListener(e -> {
            if ((!subject.getProvidedMessages().isEmpty() && availableProvidedMessages.getValue() != optionSpecifyMyself) || !inputField.getValue().equals(""))
                setCanAdvance(true);
            Object selectedItem = e.getProperty().getValue();
            removeNextSteps();

            if (selectedItem.equals(optionSystem)) step = new AskForSystemStep(owner, inputField.getValue(), subject, instance);
            if (selectedItem.equals(optionSomebodyElse)) step = new AskForNewSenderSubjectStep(owner, inputField.getValue(), subject, instance);
            addNextStep(step);
        });

        fLayout.addComponent(questionPrompt);
        if (subject.getProvidedMessages().size() != 0) fLayout.addComponent(availableProvidedMessages);
        fLayout.addComponent(inputField);
        fLayout.addComponent(infoSource);
        if (!inputField.isEnabled()) infoSource.setVisible(false);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        state = instance.getAvailableStateForSubject(subject);

        if (inputField.isEnabled() && infoSource.getValue() != null) {
            String selection = infoSource.getValue().toString();
            if (selection.equals(optionDontKnow)) {
                Subject anonymous = new Subject(Subject.ANONYMOUS);
                processChanges.add(new AddSubjectCommand(instance.getProcess(), anonymous));
                Message newMessage = new Message(inputField.getValue());
                processChanges.add(new AddMessageToRecvStateCommand((RecvState)state,newMessage));
                processChanges.add(new AddExpectedMessageCommand(anonymous, newMessage));
                return processChanges;
            }
            if (infoSource.getValue() instanceof Subject) {
                Message newMessage = new Message(inputField.getValue());
                processChanges.add(new AddMessageToRecvStateCommand((RecvState)state,newMessage));
                processChanges.add(new AddExpectedMessageCommand((Subject) infoSource.getValue(), newMessage));
                return processChanges;
            }
        } else {
            Message m = (Message) availableProvidedMessages.getValue();
            processChanges.add(new AddMessageToRecvStateCommand((RecvState)state,m));
            processChanges.add(new RemoveProvidedMessageCommand(subject, m));
            return processChanges;
        }
        return processChanges;
    }
}
