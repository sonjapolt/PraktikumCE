package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collection;

/**
 * Created by oppl on 20/12/2016.
 */
public class OnboardingAgent extends ScaffoldingAgent {

    static int INTRO_FUNDAMENTAL = 0;
    static int INTRO_ENACTMENT = 1;
    static int INTRO_VISUALIZATION = 2;
    static int INTRO_ELABORATION = 3;
    static int INTRO_SCAFFOLDING = 4;

    static private int MIDDLE = 0;
    static private int TOP = 1;
    static private int BOTTOM = 2;
    static private int RIGHT = 1;
    static private int LEFT = 2;


    private CoMPArEUI mainUI;
    private boolean onboardingActive;
    private int stage;
    private Button onboarding;
    private int stepCounter;

    private boolean receiveShown;
    private boolean sendShown;
    private boolean decisionShown;
    private boolean restartShown;

    private int vizStep;
    private int elabStep;
    private String lastVizCaption;

    public OnboardingAgent(Process p, ScaffoldingManager manager, Panel scaffoldingPanel) {
        super(p, manager);
        mainUI = (CoMPArEUI) scaffoldingPanel.getUI();
        this.freq = ScaffoldingAgent.FREQ_EACHSTEP;
        onboardingActive = false;
        stepCounter = 0;
        receiveShown = false;
        sendShown = false;
        decisionShown = false;
        restartShown = false;
        vizStep = 0;
        elabStep = 0;
        lastVizCaption = new String("");
        stage = OnboardingAgent.INTRO_FUNDAMENTAL;
    }

    @Override
    public void init() {
        super.init();
        onboarding = new Button("What's this all about?");
        onboarding.addClickListener( e -> {
            onboardingActive = true;
            updateScaffolds(null,null);
        });
        mainUI.getToolBar().addComponent(onboarding,0);
    }

    @Override
    public void updateScaffolds(Instance currentInstance, State finishedState) {
        boolean alreadyActive = false;
        if (!onboardingActive) return;
        if (stage == OnboardingAgent.INTRO_FUNDAMENTAL) {
            mainUI.getVisualizationSlider().setVisible(false);
            mainUI.getScaffoldingPanel().setVisible(false);
            mainUI.setOnboardingActive(true);
            mainUI.getSimulate().setVisible(false);
            mainUI.getDifferentProcess().setVisible(false);
            onboarding.setVisible(false);
            ConfirmDialog d = ConfirmDialog
                    .show(mainUI,
                            "Welcome",
                            "To get you started, we have deactivated most of the functionality for now.<p/>"+
                                    "We will explore the app now and gradually introduce the features that allow you to " +
                                    "explore your work processes.<p/>" +
                                    "<i>Please start by clicking the \"<b>Perform step</b>\" button.</i>",
                            "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        stage = OnboardingAgent.INTRO_ELABORATION;
                                        stepCounter = 1;
                                    } else {
                                        deactivate();
                                    }
                                }
                            });
            setDefaultDialogProperties(d, "16em",OnboardingAgent.MIDDLE,OnboardingAgent.MIDDLE);
            alreadyActive = true;
        }
        if (stage == OnboardingAgent.INTRO_ENACTMENT && currentInstance != null) {
            Collection<State> c = currentInstance.getAvailableStates().values();

            boolean isDecisionState = false;
            boolean isSendState = false;
            boolean isReceiveState = false;

            State targetedDecState = null;
            State targetedRecvState = null;
            State targetedSendState = null;
            for (State s: c) {
                if (s == null) continue;
                if (s.getNextStates().size() > 1 && !decisionShown && !isDecisionState) {
                    boolean onlyContainsMessageConditions = true;
                    for (Condition condition: s.getNextStates().values()) {
                        if (!(condition instanceof MessageCondition)) onlyContainsMessageConditions = false;
                    }
                    isDecisionState = !onlyContainsMessageConditions;
                    if (isDecisionState) targetedDecState = s;
                }
                if (s instanceof SendState && !sendShown && !isSendState) {
                    isSendState = true;
                    targetedSendState = s;
                }
                if (s instanceof RecvState && !receiveShown && !isReceiveState) {
                    isReceiveState = true;
                    targetedRecvState = s;
                }
            }
            if (!alreadyActive && stepCounter == 1) {
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Enacting the process",
                                "The \"<b>Perform step</b>\" button let's you enact work steps defined in the behavior descriptions " +
                                        "of the involved actors.<p/>"+
                                        "The app guides you through the work process as defined in its description. " +
                                        "Progressing through the steps let's you follow the process and check, whether " +
                                        "it is described correctly.<p/>" +
                                        "<i>Please continue by clicking the \"<b>Perform step</b>\" button. Whenever " +
                                        "something new comes along, we will get back to you.</i>",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d,"20em", OnboardingAgent.MIDDLE,OnboardingAgent.MIDDLE);
            }
            else {
                if (isDecisionState && !decisionShown && !alreadyActive) {
                    ConfirmDialog d = ConfirmDialog
                            .show(mainUI,
                                    "Decision to be made in the behavior of "+currentInstance.getProcess().getSubjectWithState(targetedDecState),
                                    "The step \"<b>"+targetedDecState+"</b>\" requires a decision to be able to continue.<p/>" +
                                            "In some cases, there might be different variants of a work process, which " +
                                            "depend upon which decisions you make when performing a particular work step. " +
                                            "Whenever such a step comes along, the app asks you to make this decision. " +
                                            "Do not bother too much which decision you make. You will be able to explore " +
                                            "the other options later on.<p/>" +
                                            "<i>Please continue by selecting an option and then click the \"<b>Perform step</b>\" button. Whenever " +
                                            "something new comes along, we will get back to you.</i>",
                                    "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                updateScaffolds(currentInstance, finishedState);
                                            } else {
                                                deactivate();
                                            }
                                        }
                                    });
                    setDefaultDialogProperties(d,"20em", OnboardingAgent.MIDDLE, OnboardingAgent.BOTTOM);
                    decisionShown = true;
                    alreadyActive = true;
                }
                if (isReceiveState && !receiveShown && !alreadyActive) {
                    String pointerToSendStates = "We have already encountered such a step earlier.";
                    if (!sendShown) pointerToSendStates = "As soon as we encounter such a step, we will have a closer look.";
                    ConfirmDialog d = ConfirmDialog
                            .show(mainUI,
                                    "Actor "+currentInstance.getProcess().getSubjectWithState(targetedRecvState)+" waits for input",
                                    "The step \"<b>"+targetedRecvState+"</b>\" indicates that the actor is waiting for input from somebody else.<p/>" +
                                            "As you can see, you cannot progress here until this required input is received. Inputs are usually " +
                                            "provided by other actors via \"Send\"-steps. " + pointerToSendStates + "<p/>"+
                                            "<i>Please continue by clicking the \"<b>Perform step</b>\" button. Whenever " +
                                            "something new comes along, we will get back to you.</i>",
                                    "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                updateScaffolds(currentInstance, finishedState);
                                            } else {
                                                deactivate();
                                            }
                                        }
                                    });
                    setDefaultDialogProperties(d,"20em",OnboardingAgent.MIDDLE, OnboardingAgent.BOTTOM);
                    receiveShown = true;
                    alreadyActive = true;
                }
                if (isSendState && !sendShown && !alreadyActive) {
                    ConfirmDialog d = ConfirmDialog
                            .show(mainUI,
                                    "Actor "+currentInstance.getProcess().getSubjectWithState(targetedSendState)+" is able to provide input to others",
                                    "The step \"<b>"+targetedSendState+"</b>\" indicates that the actor is able to provide input to somebody else.<p/>" +
                                            "If you click the \"<b>Perform step</b>\" button, the respective input is sent to the recipient. You will " +
                                            "notice that the actor receiving the input displays a small message indicating which input it has received.<p/>"+
                                            "<i>Please continue by clicking the \"<b>Perform step</b>\" button. Whenever " +
                                            "something new comes along, we will get back to you.</i>",
                                    "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                updateScaffolds(currentInstance, finishedState);
                                            } else {
                                                deactivate();
                                            }
                                        }
                                    });
                    setDefaultDialogProperties(d,"20em",OnboardingAgent.MIDDLE, OnboardingAgent.BOTTOM);
                    sendShown = true;
                    alreadyActive = true;
                }
            }
            if (mainUI.getRestart().isVisible() && !restartShown) {
                String restartPrompt = "There are still variants of the work process you have not yet explored, because of " +
                        "the decisions you have made during the first run. You can restart the enactment of the " +
                        "work process by clicking the \"<b>Restart process</b>\" button and explore alternatives" +
                        "by making different decisions.</p>";
                if (!decisionShown) restartPrompt = "You can further explore the features of the app by restarting the enactment " +
                        "of the work process once again.<p/>";
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "You have finished playing through your first work process.",
                                "With the last step you have performed, you have finished the first run through the work process. " +
                                        "The are no more steps available for any of the actors in description of the work process<p/>" +
                                        restartPrompt +
                                        "<i>Please continue by clicking the \"<b>Restart process</b>\" button.</i>",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {
                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d,"22em",OnboardingAgent.MIDDLE,OnboardingAgent.TOP);
                restartShown = true;
                alreadyActive = true;
            }
            if (receiveShown && sendShown && decisionShown && restartShown && !alreadyActive) {
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Congratulations ",
                                "Congratulations, you have fully explored the features that are needed to enact a work process.<p/>" +
                                        "With these basic features, you are able to play through the descriptions of work" +
                                        "processes stored in the system and check, whether they are correct.<p/>"+
                                        "<i>Please continue by restarting enactment with the \"<b>Restart Process</b>\" button. We " +
                                        "now will gradually activate and introduce further features.</i>",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            stage = OnboardingAgent.INTRO_VISUALIZATION;
                                            stepCounter = 0;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d,"20em",OnboardingAgent.MIDDLE, OnboardingAgent.MIDDLE);
            }
        }
        if (stage == OnboardingAgent.INTRO_VISUALIZATION) {
            if (!mainUI.getWindows().isEmpty()) return;
            if (vizStep == 0 && stepCounter == 1) {
                mainUI.getVisualizationSlider().setVisible(true);
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Get known to the Behaviour Panel ",
                                "On the left border of the screen, you now find a new tab, which allows you to take a closer " +
                                        "look at the behavior descriptions of the actors.<p/>" +
                                        "If you click the panel, it will expand and show you diagrams of the stored actor behaviour.<p/>" +
                                        "<i>Please continue by clicking on the tab reading \"<b>Show behaviour</b>\". We " +
                                        "now will explore this panel.</i>",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            vizStep=1;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.MIDDLE, OnboardingAgent.MIDDLE);
                alreadyActive = true;
            }
            if (vizStep == 1 && mainUI.getVisualizationSlider().isExpanded() && !alreadyActive) {
                lastVizCaption = mainUI.getVisualizationTabs().getSelectedTab().getCaption();
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Explore the Behaviour Panel ",
                                "You now can see a diagram that graphically visualizes the behaviour of the actor that performed " +
                                        "the last step.<p/>" +
                                        "The behaviour is visualized as a sequence of steps that are connected with each other. " +
                                        "The first step can be found at the very top. Already visited steps are shown in grey.<p/>" +
                                        "<i>Have a look at the diagram and see if you can identify the next steps. Then switch to " +
                                        "a different actor by <b>clicking on any of the tabs at the upper border of the panel</b>.</i>",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            vizStep=2;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.RIGHT, OnboardingAgent.MIDDLE);
                alreadyActive = true;
            }
            if (vizStep == 2 && mainUI.getVisualizationSlider().isExpanded() && !alreadyActive) {
                String selectedTab = mainUI.getVisualizationTabs().getSelectedTab().getCaption();
                if (selectedTab.equals(lastVizCaption)) return;
                if (!selectedTab.equals("Interaction")) {
                    ConfirmDialog d = ConfirmDialog
                            .show(mainUI,
                                    "Explore the Behaviour Panel ",
                                    "You have now switched to the behaviour of actor "+ selectedTab +".</p>" +
                                            "It shows the behaviour of this actor like before. You can switch between the " +
                                            "tabs to get an overview of the overall behaviour stored in the process description.<p/>" +
                                            "<i>Now please select the tab reading \"<b>Interaction</b>\".</i>",
                                    "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                vizStep=3;
                                                updateScaffolds(currentInstance, finishedState);
                                            } else {
                                                deactivate();
                                            }
                                        }
                                    });
                    setDefaultDialogProperties(d, "20em", OnboardingAgent.RIGHT, OnboardingAgent.MIDDLE);
                    alreadyActive = true;
                }
                else {
                    vizStep=3;
                }
            }
            if (vizStep == 3 && mainUI.getVisualizationSlider().isExpanded() && !alreadyActive) {
                String selectedTab = mainUI.getVisualizationTabs().getSelectedTab().getCaption();
                if (selectedTab.equals(lastVizCaption)) return;
                if (selectedTab.equals("Interaction")) {
                    ConfirmDialog d = ConfirmDialog
                            .show(mainUI,
                                    "Explore the Behaviour Panel ",
                                    "You have now switched to the visualization of the actors' interaction.</p>" +
                                            "It shows the messages the actors exchange when providing input to each other.<p/>" +
                                            "<i>Have a look at the diagram and see if you can find messages you remember " +
                                            "for enacting the process. Then close the panel with the diagrams again " +
                                            "clicking again on the tab reading \"<b>Show behaviour</b>\".</i>",
                                    "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                vizStep = 4;
                                                updateScaffolds(currentInstance, finishedState);
                                            } else {
                                                deactivate();
                                            }
                                        }
                                    });
                    setDefaultDialogProperties(d, "20em", OnboardingAgent.RIGHT, OnboardingAgent.MIDDLE);
                    alreadyActive = true;
                }
            }
            if (vizStep == 4 && !mainUI.getVisualizationSlider().isExpanded() && !alreadyActive) {
                ConfirmDialog d = ConfirmDialog
                            .show(mainUI,
                                    "Congratualations",
                                    "You have now finished the exploration of the visualization panel</p>" +
                                            "You can use it at any time to have a look at upcoming steps or the " +
                                            "overall behaviour of the actors<p/>" +
                                            "<i>Please continue enacting the work process again using the \"<b>Perform step</b>\" " +
                                            "buttons. We will be back soon with the next feature to explore.</i>",
                                    "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                stage = INTRO_ELABORATION;
                                                stepCounter = 0;
                                                updateScaffolds(currentInstance, finishedState);
                                            } else {
                                                deactivate();
                                            }
                                        }
                                    });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.MIDDLE, OnboardingAgent.MIDDLE);
                alreadyActive = true;
            }
        }
        if (stage == OnboardingAgent.INTRO_ELABORATION) {
            if (elabStep == 0 && stepCounter == 1) {
                mainUI.setElaborationAvailable(true);
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Get known to the Elaboration Functionality ",
                                "In each of the actors' panels you can now find a new button labeled " +
                                        "\"<b>I have a problem here</b>\". " +
                                        "If you click this button, you can modify the stored actor behaviour.<p/>" +
                                        "<i>Please continue by clicking on any active button reading \"<b>I have a problem here</b>\". We " +
                                        "will now will explore this functionality.</i>",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            elabStep=1;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.MIDDLE, OnboardingAgent.MIDDLE);
                alreadyActive = true;
            }
            if (elabStep == 1 && mainUI.isElaborationActive()) {
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "This is the elaboration panel",
                                "The elaboration panel guides you through the process of making changes to an actor's " +
                                        "behaviour. You can explore the options in the panel by selecting them from the " +
                                        "list displayed above. Each option adds further steps to the elaboration process. " +
                                        "Changes to the process are only made if you click the \"<b>Finish</b>\"-button. " +
                                        "\"<b>Cancel</b>\" returns to the main screen without any changes.<p/>" +
                                        "<i>Please continue by adding a new activity to be performed before the presently " +
                                        "active one by always selecting the top-most option and finally entering a name " +
                                        "for the new activity. Confirm the change by clicking the \"<b>Finish</b>\"-button.",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            elabStep=2;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.MIDDLE, OnboardingAgent.BOTTOM);
                alreadyActive = true;
            }
            if (elabStep == 2 && !mainUI.getProcessChangeHistory().getHistory().isEmpty()) {
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Explore the elaboration panel further",
                                "You have now changed the behaviour of the actor you selected before. As you can see, " +
                                        "the actor's next activity is now set to the one you just added. You can now " +
                                        "proceed to enact the process as you did before. Your changes are permanent and " +
                                        "will also be available in future enactments of the process. In this way, you " +
                                        "can alter the process step by step whenever you encounter anything that does" +
                                        "not fit the process in the real world.<p/>" +
                                        "<i>Please continue to explore the elaboration options by making further " +
                                        "modifications to the actors' behaviours. You can be creative here, we will " +
                                        "reset the process once you have finished exploring. As soon as you have made a " +
                                        "few modifications (say, 2-3), we will be back with further input.",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            elabStep=3;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.MIDDLE, OnboardingAgent.BOTTOM);
                alreadyActive = true;
            }
            if (elabStep == 3 && mainUI.getProcessChangeHistory().getHistory().size()>3) {
                ConfirmDialog d = ConfirmDialog
                        .show(mainUI,
                                "Adding additional behaviour to an actor which has already finished.",
                                "You have now changed the behaviour of the actor you selected before. As you can see, " +
                                        "the actor's next activity is now set to the one you just added. You can now " +
                                        "proceed to enact the process as you did before. Your changes are permanent and " +
                                        "will also be available in future enactments of the process. In this way, you " +
                                        "can alter the process step by step whenever you encounter anything that does" +
                                        "not fit the process in the real world.<p/>" +
                                        "<i>Please continue to explore the elaboration options by making further " +
                                        "modifications to the actors' behaviours. You can be creative here, we will " +
                                        "reset the process once you have finished exploring. As soon as you have made a " +
                                        "few modifications (say, 2-3), we will be back with further input.",
                                "OK", "Do not bug my anymore", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            elabStep=3;
                                            updateScaffolds(currentInstance, finishedState);
                                        } else {
                                            deactivate();
                                        }
                                    }
                                });
                setDefaultDialogProperties(d, "20em", OnboardingAgent.MIDDLE, OnboardingAgent.BOTTOM);
                alreadyActive = true;
            }
        }
        if (stage == OnboardingAgent.INTRO_SCAFFOLDING) {

        }
        if (finishedState !=null) stepCounter ++;
    }

    private void deactivate() {
        onboardingActive = false;
        mainUI.getVisualizationSlider().setVisible(true);
        mainUI.getScaffoldingPanel().setVisible(true);
        mainUI.setOnboardingActive(false);
        mainUI.getSimulate().setVisible(true);
    }

    private void setDefaultDialogProperties(ConfirmDialog d, String height, int x, int y) {
        d.setContentMode(ConfirmDialog.ContentMode.HTML);
        d.getCancelButton().setStyleName(Reindeer.BUTTON_LINK);
        d.setHeight(height);
        d.setModal(false);

        int calcX = Math.round(UI.getCurrent().getPage().getBrowserWindowWidth()-d.getWidth()*15)/2;
        int calcY = Math.round(UI.getCurrent().getPage().getBrowserWindowHeight()-d.getHeight()*15)/2;

        if (x==OnboardingAgent.LEFT) {
            calcX = 50;
        }
        if (x==OnboardingAgent.RIGHT) {
            calcX = Math.round(UI.getCurrent().getPage().getBrowserWindowWidth()-d.getWidth()*15-50);
        }
        if (y==OnboardingAgent.TOP) {
            calcY = 50;
        }
        if (y==OnboardingAgent.BOTTOM) {
            calcY = Math.round(UI.getCurrent().getPage().getBrowserWindowHeight()-d.getHeight()*15-50);
        }
        d.setPosition(calcX, calcY);
    }
}
