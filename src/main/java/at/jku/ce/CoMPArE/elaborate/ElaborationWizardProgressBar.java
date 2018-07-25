package at.jku.ce.CoMPArE.elaborate;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardProgressBar;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.*;

import java.util.List;

/**
 * Created by oppl on 18/01/2017.
 */

@StyleSheet("wizard-progress-bar.css")
public class ElaborationWizardProgressBar extends CustomComponent implements
        WizardProgressListener {

    private final Wizard wizard;
    private final ProgressBar progressBar = new ProgressBar();
    private final VerticalLayout stepCaptions = new VerticalLayout();
    private int activeStepIndex;

    public ElaborationWizardProgressBar(Wizard wizard) {
        setStyleName("wizard-progress-bar");
        this.wizard = wizard;

        stepCaptions.setWidth("100%");
        progressBar.setWidth("100%");
        progressBar.setHeight("13px");

        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.addComponent(stepCaptions);
//        layout.addComponent(progressBar);
        setCompositionRoot(layout);
        setWidth("100%");
    }

    private void updateProgressBar() {
        int stepCount = wizard.getSteps().size();
        float padding = (1.0f / stepCount) / 2;
        float progressValue = padding + activeStepIndex / (float) stepCount;
        progressBar.setValue(progressValue);
    }

    private void updateStepCaptions() {
        stepCaptions.removeAllComponents();
        int index = 1;
        for (WizardStep step : wizard.getSteps()) {
            Label label = createCaptionLabel(index, step);
            stepCaptions.addComponent(label);
            index++;
        }
    }

    private Label createCaptionLabel(int index, WizardStep step) {
        Label label = new Label(index + ". " + step.getCaption());
        label.addStyleName("step-caption");

        // Add styles for themeing.
        if (wizard.isCompleted(step)) {
            label.addStyleName("completed");
        }
        if (wizard.isActive(step)) {
            label.addStyleName("current");
        }/*
        if (wizard.isFirstStep(step)) {
            label.addStyleName("first");
        }
        if (wizard.isLastStep(step)) {
            label.addStyleName("last");
        }*/

        return label;
    }

    private void updateProgressAndCaptions() {
        updateProgressBar();
        updateStepCaptions();
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent event) {
        List<WizardStep> allSteps = wizard.getSteps();
        activeStepIndex = allSteps.indexOf(event.getActivatedStep());
        updateProgressAndCaptions();
    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent event) {
        updateProgressAndCaptions();
    }

    @Override
    public void wizardCompleted(WizardCompletedEvent event) {
        progressBar.setValue(1.0f);
        updateStepCaptions();
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent event) {
        // NOP, no need to react to cancellation
    }
}
