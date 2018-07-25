package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.AddConditionalStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForConditionsStep extends ElaborationStep {

    State state;

    State alreadyExistingState;

    final Label questionPrompt;
    TextField inputFieldNew;
    TextField inputFieldOld;

    OptionGroup availableProvidedMessagesNew;
    OptionGroup availableProvidedMessagesOld;

    CheckBox checkBoxDependentOnInput;

    final Set<State> predecessorStates;

    final Map<State, TextField> originalConditionTextFields;
    final Map<State, TextField> newConditionTextFields;

    final Map<State, OptionGroup> originalConditionOptionGroups;
    final Map<State, OptionGroup> newConditionOptionGroups;

    final Map<State, Condition> originalConditions;
    final Map<State, Condition> newConditions;

    String newState;

    public AskForConditionsStep(Wizard owner, State newState, Subject s, Instance i) {
        this(owner,newState.getName(),s,i);
        alreadyExistingState = newState;
    }

    public AskForConditionsStep(Wizard owner, String newState, Subject s, Instance i) {
        super(owner, s, i);
        alreadyExistingState = null;
        state = instance.getAvailableStateForSubject(subject);
        this.newState = newState;

        predecessorStates = subject.getPredecessorStates(state);

        originalConditionTextFields = new HashMap<>();
        newConditionTextFields = new HashMap<>();

        originalConditionOptionGroups = new HashMap<>();
        newConditionOptionGroups  = new HashMap<>();

        originalConditions = new HashMap<>();
        newConditions = new HashMap<>();


        caption = new String("\"" + newState + "\" replaces \"" + state + "\" under certain conditions.");
        questionPrompt = new Label("\"" + newState + "\" replaces \"" + state + "\" under certain conditions.");
//        inputFieldNew = new TextField("What is the condition for \"" + newState + "\"?");
//        inputFieldOld = new TextField("What is the condition for \"" + state + "\"?");

        checkBoxDependentOnInput = new CheckBox("The conditions depend on already available input");
        checkBoxDependentOnInput.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) {
                for (State predecessor: originalConditionTextFields.keySet()) {
                    originalConditionTextFields.get(predecessor).setVisible(false);
                    newConditionTextFields.get(predecessor).setVisible(false);
                    originalConditionOptionGroups.get(predecessor).setVisible(true);
                    newConditionOptionGroups.get(predecessor).setVisible(true);
                }
            }
            else {
                for (State predecessor: originalConditionTextFields.keySet()) {
                    originalConditionTextFields.get(predecessor).setVisible(true);
                    newConditionTextFields.get(predecessor).setVisible(true);
                    originalConditionOptionGroups.get(predecessor).setVisible(false);
                    newConditionOptionGroups.get(predecessor).setVisible(false);
                }
            }
        });

        if (predecessorStates.isEmpty()) {
            State dummyState = new ActionState("Make Decision");
            dummyState.addNextState(state);
            predecessorStates.add(dummyState);
        }
        for (State predecessor : predecessorStates) {
            inputFieldNew = new TextField("What is the condition for \"" + newState + "\" when coming from \"" + predecessor + "\"?");
            inputFieldOld = new TextField("What is the condition for \"" + state + "\" when coming from \"" + predecessor + "\"?");

            inputFieldNew.addValueChangeListener( e -> {
                if (inputFieldNew.getValue().equals("") || inputFieldOld.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
                if (inputFieldNew.getValue().equals(inputFieldOld.getValue())) {
                    Notification.show("Conditions must not be identical!",Notification.Type.WARNING_MESSAGE);
                    setCanAdvance(false);
                }
            });

            inputFieldOld.addValueChangeListener( e -> {
                if (inputFieldNew.getValue().equals("") || inputFieldOld.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
                if (inputFieldNew.getValue().equals(inputFieldOld.getValue())) {
                    Notification.show("Conditions must not be identical!",Notification.Type.WARNING_MESSAGE);
                    setCanAdvance(false);
                }
            });

            Condition originalCondition = predecessor.getNextStates().get(state);
            if (originalCondition != null && !originalCondition.getCondition().equals(""))
                originalConditions.put(predecessor, originalCondition);
            else originalConditions.put(predecessor, null);
            if (originalConditions.get(predecessor) != null) inputFieldOld.setValue(originalConditions.get(predecessor).getCondition());
            if (originalConditions.get(predecessor) instanceof MessageCondition) {
                inputFieldOld.setEnabled(false);
                inputFieldOld.setDescription("This condition is bound to incoming input and cannot be changed here");
            }
            originalConditionTextFields.put(predecessor, inputFieldOld);
            newConditionTextFields.put(predecessor, inputFieldNew);

            availableProvidedMessagesNew = new OptionGroup("Upon which input should we progress to \"" + newState + " when coming from " + predecessor + "\"?" );
            for (Message m : subject.getRecvdMessages()) {
                availableProvidedMessagesNew.addItem(m);
            }
            for (Message m : subject.getProvidedMessages()) {
                availableProvidedMessagesNew.addItem(m);
            }
            availableProvidedMessagesNew.addValueChangeListener( e -> {
                if (availableProvidedMessagesOld.getValue() == null || availableProvidedMessagesOld.getValue() == null) setCanAdvance(false);
                else setCanAdvance(true);
                if (availableProvidedMessagesNew.getValue() != null && availableProvidedMessagesNew.getValue().equals(availableProvidedMessagesOld.getValue())) {
                    Notification.show("Conditions must not be identical!",Notification.Type.WARNING_MESSAGE);
                    setCanAdvance(false);
                }
            });

            availableProvidedMessagesOld = new OptionGroup("Upon which input should we progress to \"" + state + " when coming from " + predecessor + "\"?" );
            for (Message m : subject.getRecvdMessages()) {
                availableProvidedMessagesOld.addItem(m);
            }
            for (Message m : subject.getProvidedMessages()) {
                availableProvidedMessagesOld.addItem(m);
            }
            availableProvidedMessagesOld.addValueChangeListener( e -> {
                if (availableProvidedMessagesOld.getValue() == null || availableProvidedMessagesOld.getValue() == null) setCanAdvance(false);
                else setCanAdvance(true);
                if (availableProvidedMessagesNew.getValue() != null && availableProvidedMessagesNew.getValue().equals(availableProvidedMessagesOld.getValue())) {
                    Notification.show("Conditions must not be identical!",Notification.Type.WARNING_MESSAGE);
                    setCanAdvance(false);
                }
            });
            originalConditionOptionGroups.put(predecessor, availableProvidedMessagesOld);
            newConditionOptionGroups.put(predecessor, availableProvidedMessagesNew);

        }

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(checkBoxDependentOnInput);
        for (State predecessor: originalConditionTextFields.keySet()) {
            fLayout.addComponent(originalConditionTextFields.get(predecessor));
            fLayout.addComponent(newConditionTextFields.get(predecessor));
            fLayout.addComponent(originalConditionOptionGroups.get(predecessor));
            fLayout.addComponent(newConditionOptionGroups.get(predecessor));
            originalConditionOptionGroups.get(predecessor).setVisible(false);
            newConditionOptionGroups.get(predecessor).setVisible(false);

        }
    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        State newInsertedState = new ActionState(newState);
        if (alreadyExistingState != null) newInsertedState = alreadyExistingState;
        state = instance.getAvailableStateForSubject(subject);

        for (State predecessor : predecessorStates) {
            if (checkBoxDependentOnInput.getValue() == Boolean.FALSE) {
                if (!(originalConditions.get(predecessor) instanceof MessageCondition))
                    originalConditions.put(predecessor, new Condition(originalConditionTextFields.get(predecessor).getValue()));
                newConditions.put(predecessor, new Condition(newConditionTextFields.get(predecessor).getValue()));
            } else {
                originalConditions.put(predecessor, new MessageCondition(((Message) originalConditionOptionGroups.get(predecessor).getValue()).getUUID()));
                newConditions.put(predecessor, new MessageCondition(((Message) newConditionOptionGroups.get(predecessor).getValue()).getUUID()));
            }
        }


        processChanges.add(new AddConditionalStateCommand(subject,state, newInsertedState, originalConditions, newConditions));

        return processChanges;
    }
}
