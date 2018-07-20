package at.jku.ce.CoMPArE;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import at.jku.ce.CoMPArE.elaborate.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.execute.InstanceHistoryStep;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.simulate.Simulator;
import at.jku.ce.CoMPArE.storage.FileStorageHandler;
import at.jku.ce.CoMPArE.storage.GroupIDEntryWindow;
import at.jku.ce.CoMPArE.visualize.VisualizeModel;
import at.jku.ce.CoMPArE.visualize.VisualizeModelEvolution;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.*;
import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.SliderPanelStyles;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderPanelListener;
import org.vaadin.sliderpanel.client.SliderTabPosition;
import sun.rmi.runtime.Log;

import java.io.File;
import java.util.*;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */

@Theme("demo")
@Push(transport= Transport.WEBSOCKET_XHR)
public class CoMPArEUI extends UI implements SliderPanelListener {

    private Map<Subject,Panel> subjectPanels;
    private Panel scaffoldingPanel;
    private HorizontalLayout mainLayoutFrame;
    private HorizontalLayout toolBar;
    private VerticalLayout mainInteractionArea;
    private GridLayout subjectLayout;
    private TabSheet visualizationTabs;
    private SliderPanel visualizationSlider;
    private SliderPanel historySlider;

    private Process currentProcess;
    private Instance currentInstance;

    private GoogleAnalyticsTracker tracker;
    private ScaffoldingManager scaffoldingManager;
    private Simulator simulator;
    private StateClickListener stateClickListener;
    private boolean initialStartup;
    private boolean selectionMode;
    private boolean onboardingActive;
    private boolean elaborationAvailable;
    private boolean elaborationActive;
    private boolean doNotNotifyScaffoldingManager;

    private ProcessChangeHistory processChangeHistory;

    private Subject lastActiveSubject;

    private Button differentProcess;
    private Button simulate;
    private Button restart;
    private Button elaborationHistory;

    private FileStorageHandler fileStorageHandler;

    private long id;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        id = -1;
        initialStartup = true;
        selectionMode = false;
        onboardingActive = false;
        elaborationAvailable = true;
        elaborationActive = false;
        doNotNotifyScaffoldingManager = false;
        stateClickListener = null;
        currentProcess = DemoProcess.getTask1Process();
        processChangeHistory = new ProcessChangeHistory();
        tracker = new GoogleAnalyticsTracker("UA-37510687-4","auto");
        tracker.extend(this);
        currentInstance = new Instance(currentProcess);
        currentInstance.setProcessHasBeenChanged(true);

        scaffoldingPanel = new Panel("What to consider:");
        scaffoldingPanel.setWidth("950px");
        scaffoldingPanel.setHeight("200px");
        scaffoldingPanel.setContent(new GridLayout(3,1));

        differentProcess = new Button("Select different Process");
        differentProcess.addClickListener( e -> {
            selectDifferentProcess();
        });

        restart = new Button("Restart Process");

        toolBar = new HorizontalLayout();

        createBasicLayout();
        simulator = new Simulator(currentInstance,subjectPanels,this);
        scaffoldingManager = new ScaffoldingManager(currentProcess,scaffoldingPanel);

        updateUI();

        Page.getCurrent().addBrowserWindowResizeListener(e -> {
            recalculateSubjectLayout(e.getWidth());
        });

        fileStorageHandler = new FileStorageHandler();
        if (!fileStorageHandler.isIDCookieAvailable()) {
            GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
            this.getUI().addWindow(groupIDEntryWindow);
        }
    }


    private void createBasicLayout() {

//        LogHelper.logDebug("Building basic layout");
        mainLayoutFrame = new HorizontalLayout();

//        SliderPanel scaffoldingSlider = createScaffoldingSlider(process, instance);
        visualizationSlider = createVisualizationSlider();
        historySlider = createHistorySlider();

        mainInteractionArea = new VerticalLayout();

        HorizontalLayout toolBar = createToolbar();
        Component subjects = createSubjectLayout(Page.getCurrent().getBrowserWindowWidth());

        HorizontalLayout hPadding = new HorizontalLayout();
        hPadding.setHeight("1px");
        hPadding.setWidth((this.getPage().getBrowserWindowWidth()-150)+"px");
        mainInteractionArea.addComponent(hPadding);
        mainInteractionArea.addComponent(subjects);
        mainInteractionArea.addComponent(toolBar);
        mainInteractionArea.addComponent(scaffoldingPanel);
        if (onboardingActive) scaffoldingPanel.setVisible(false);
        mainInteractionArea.setMargin(true);
        mainInteractionArea.setSpacing(true);


        VerticalLayout vPadding = new VerticalLayout();
        vPadding.setWidth("50px");
        vPadding.setHeight(this.getPage().getBrowserWindowHeight()+"px");


//        mainLayoutFrame.addComponent(scaffoldingSlider);
        mainLayoutFrame.addComponent(visualizationSlider);
        mainLayoutFrame.addComponent(historySlider);
        if (onboardingActive) visualizationSlider.setVisible(false);
        mainLayoutFrame.addComponent(vPadding);
        mainLayoutFrame.addComponent(mainInteractionArea);

        this.setContent(mainLayoutFrame);

    }

    private SliderPanel createScaffoldingSlider(Process process, Instance instance) {
        return new SliderPanelBuilder(scaffoldingPanel, "What to consider").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();

    }

    private SliderPanel createVisualizationSlider() {
        VerticalLayout visualizationSliderContent = new VerticalLayout();
        visualizationSliderContent.removeAllComponents();
        visualizationSliderContent.setWidth((this.getPage().getBrowserWindowWidth()-150)+"px");
        visualizationSliderContent.setHeight((this.getPage().getBrowserWindowHeight()-150)+"px");
        visualizationSliderContent.setMargin(true);
        visualizationSliderContent.setSpacing(true);

        visualizationTabs = new TabSheet();
        visualizationSliderContent.addComponent(visualizationTabs);
        for (Subject s: currentProcess.getSubjects()) {
            VerticalLayout subjectVizualization = new VerticalLayout();
            subjectVizualization.setCaption(s.toString());
            visualizationTabs.addTab(subjectVizualization,s.toString());
        }
        VerticalLayout interaction = new VerticalLayout();
        interaction.setCaption("Interaction");
        VerticalLayout overall = new VerticalLayout();
        overall.setCaption("Overall");
        VerticalLayout overallFlow = new VerticalLayout();
        overallFlow.setCaption("Flow of activities");
        visualizationTabs.addTab(interaction, "Interaction");
        visualizationTabs.addTab(overall,"Overall");
        visualizationTabs.addTab(overallFlow,"Flow of activities");
        visualizationTabs.addSelectedTabChangeListener( e -> {
            String selected = e.getTabSheet().getSelectedTab().getCaption();
//            // LogHelper.logDebug("Now processing visualizationTab "+selected);
            if (selected != null) {
                VerticalLayout vl = (VerticalLayout) e.getTabSheet().getSelectedTab();
                vl.removeAllComponents();
                VisualizeModel visualizeModel = new VisualizeModel(selected, this,
                        this.getPage().getBrowserWindowWidth()-280,
                        this.getPage().getBrowserWindowHeight()-200);
                visualizeModel.setCaption(selected);
                if (selected.equals("Interaction")) {
                    visualizeModel.showSubjectInteraction(currentProcess);
                    LogHelper.logInfo("Visualization: " + currentProcess+": showing visualization of interaction model");

                }
                if (selected.equals("Overall")) {
                    visualizeModel.showWholeProcess(currentProcess);
                    visualizeModel.greyOutCompletedStates(currentInstance.getWholeHistory(),currentInstance.getAvailableStates().values());
                    LogHelper.logInfo("Visualization: " + currentProcess+": showing visualization of overall model");
                }
                if (selected.equals("Flow of activities")) {
                    visualizeModel.showWholeProcessFlow(currentProcess);
                    visualizeModel.greyOutCompletedStates(currentInstance.getWholeHistory(),currentInstance.getAvailableStates().values());
                    LogHelper.logInfo("Visualization: " + currentProcess+": showing visualization of overall flow-oriented model");
                }
                if (!selected.equals("Interaction") && !selected.equals("Overall") && !selected.equals("Flow of activities")) {
                    Subject s = currentProcess.getSubjectWithName(selected);
                    visualizeModel.showSubject(s);
                    visualizeModel.greyOutCompletedStates(currentInstance.getHistoryForSubject(s),currentInstance.getAvailableStateForSubject(s));
                    LogHelper.logInfo("Visualization: " + currentProcess+": showing visualization of subject "+s);

                }
                vl.addComponent(visualizeModel);
                if (onboardingActive && !doNotNotifyScaffoldingManager) {
                    scaffoldingManager.updateScaffolds(currentInstance,null);
                }
            }
        });

        final SliderPanel visualizationSlider =
                new SliderPanelBuilder(visualizationSliderContent, "Show behaviour").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();

        visualizationSlider.addListener(this);
        return visualizationSlider;
    }

    @Override
    public void onToggle(boolean b) {
        if (b && !selectionMode) {

            String toBeActivated = null;
            Set<Subject> candidates = new HashSet<>();
            for (Subject s: subjectPanels.keySet()) {
                if (currentInstance.subjectCanProgress(s)) candidates.add(s);
            }
            if (candidates.size() == 0) toBeActivated = "Interaction";
            else if (candidates.size() == 1) toBeActivated = candidates.iterator().next().toString();
            else if (candidates.contains(lastActiveSubject)) toBeActivated = lastActiveSubject.toString();
            else toBeActivated = candidates.iterator().next().toString();

            Iterator<Component> i = visualizationTabs.iterator();
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(toBeActivated)) {
                    if (visualizationTabs.getSelectedTab() == tab) {
                        doNotNotifyScaffoldingManager = true;
                        visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount()-1);
                        doNotNotifyScaffoldingManager = false;
                    }
                    visualizationTabs.setSelectedTab(tab);
                }
            }
        }
        if (!b && selectionMode) {
            selectionMode = false;
            if (stateClickListener != null) {
                stateClickListener.clickedState(null);
                stateClickListener = null;
            }
        }
        if (!b && onboardingActive) {
            scaffoldingManager.updateScaffolds(currentInstance,null);
        }

    }

    private SliderPanel createHistorySlider() {
        VisualizeModelEvolution historySliderContent = new VisualizeModelEvolution(currentProcess,processChangeHistory);
        historySliderContent.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth()-150)+"px");
        historySliderContent.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-150)+"px");
        final SliderPanel historySlider =
                new SliderPanelBuilder(historySliderContent, "Show history").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.MIDDLE).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();
        historySlider.addListener(new HistoryListener(historySliderContent));
        return historySlider;

    }

    public class HistoryListener implements SliderPanelListener {

        VisualizeModelEvolution historySliderContent;

        public HistoryListener(VisualizeModelEvolution historySliderContent) {

            this.historySliderContent = historySliderContent;
        }

        @Override
        public void onToggle(boolean b) {

            if (b) {
                historySliderContent.createLayout();
                LogHelper.logInfo("HistoryVisualization: " + currentProcess +": history slider opened");
            }
            else {
                LogHelper.logInfo("HistoryVisualization: " + currentProcess +": history slider closed");
            }
        }
    }

    private HorizontalLayout createToolbar() {
        toolBar.removeAllComponents();

        simulate = new Button("Auto-progress");
        simulate.addClickListener( e -> {
            LogHelper.logInfo("Simulator: " + currentProcess+": auto-progress triggered");
            selectionMode = true;
            Iterator<Component> i = visualizationTabs.iterator();
            String targetTab = new String("Interaction");
            if (currentProcess.getSubjects().size()==1) targetTab = currentProcess.getSubjects().iterator().next().toString();
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(targetTab)) {
                    if (visualizationTabs.getSelectedTab() == tab) {
                        if (targetTab.equals("Interaction")) visualizationTabs.setSelectedTab(0);
                        else visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount()-1);
                    }
                    visualizationTabs.setSelectedTab(tab);
                }
            }
            Notification.show("Please select where to progress to.", Notification.Type.WARNING_MESSAGE);
            visualizationSlider.expand();
        });

        restart = new Button("Restart Process");
        restart.addClickListener( e -> {
            LogHelper.logInfo("Execution: " + currentProcess+": process restarted");
            // LogHelper.logDebug("starting restart");
            mainLayoutFrame.removeAllComponents();
            // LogHelper.logDebug("creating layout");
            createBasicLayout();
            // LogHelper.logDebug("updating scaffolds for finished instance");
            scaffoldingManager.updateScaffolds(currentInstance);
            // LogHelper.logDebug("creating new instance");
            currentInstance = new Instance(currentProcess);
            // LogHelper.logDebug("updating scaffolds for finished step after reset");
            scaffoldingManager.updateScaffolds(currentInstance,null);
            // LogHelper.logDebug("resetting simulator");
            simulator = new Simulator(currentInstance, subjectPanels, this);
            // LogHelper.logDebug("updating UI");
            updateUI();
        });

        elaborationHistory = new Button("Open Process Change History");
        elaborationHistory.addClickListener( e -> {
            LogHelper.logInfo("Elaboration: " + currentProcess +": process change history opened");
            HistoryUI historyUI = new HistoryUI(processChangeHistory);
            this.getUI().addWindow(historyUI);
            historyUI.addCloseListener( e1 -> {
                rollbackChangesTo(historyUI.getSelectedTransaction());
                if (historyUI.getSelectedTransaction() != null) LogHelper.logInfo("Elaboration: " + currentProcess +": rolling back changes made through elaboration. Last undone transaction: "+historyUI.getSelectedTransaction());
                else LogHelper.logInfo("Elaboration: " + currentProcess +": process change history closed again without any changes");
            });
        });
        if (processChangeHistory.getHistory().isEmpty()) {
            elaborationHistory.setVisible(false);
            historySlider.setVisible(false);
        }

        if (!currentProcess.getSubjects().isEmpty()) toolBar.addComponent(simulate);
        if (onboardingActive) simulate.setVisible(false);
        toolBar.addComponent(restart);
        toolBar.addComponent(differentProcess);
        toolBar.addComponent(elaborationHistory);
        toolBar.setSpacing(true);
        return toolBar;

    }

    private void rollbackChangesTo(ProcessChangeTransaction rollbackTo) {
        if (rollbackTo != null) {
            for (ProcessChangeTransaction transaction: processChangeHistory.getHistory()) {
                transaction.undo(currentProcess);
                if (transaction == rollbackTo) break;
            }
            InstanceHistoryStep instanceState = rollbackTo.getAffectedInstanceHistoryState();
            currentInstance.reconstructInstanceState(instanceState);
            processChangeHistory.removeUntil(rollbackTo);
            createBasicLayout();
            updateUI();
        }
    }

    public void notifyAboutClickedState(StateClickListener listener) {
        stateClickListener = listener;
    }

    public void informAboutSelectedNode(String vizName, String name) {
        if (!selectionMode) return;

        if (vizName.equals("Interaction")) {
            Iterator<Component> i = visualizationTabs.iterator();
            Subject selectedSubject = currentProcess.getSubjectByUUID(UUID.fromString(name));
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(selectedSubject.toString())) {
                    visualizationTabs.setSelectedTab(tab);
                    return;
                }
            }

        }

        State selectedState = currentProcess.getStateByUUID(UUID.fromString(name));
        if (selectedState == null) return;

        selectionMode = false;
        visualizationSlider.collapse();

        if (stateClickListener == null) {
            simulate(selectedState);
        }
        else {
            stateClickListener.clickedState(selectedState);
            stateClickListener = null;
        }
    }

    public void expandVisualizationSlider(Subject withSubject) {
        selectionMode = true;
        Iterator<Component> i = visualizationTabs.iterator();
        while (i.hasNext()) {
            Component tab = i.next();
            if (tab.getCaption().equals(withSubject.toString())) {
                doNotNotifyScaffoldingManager = true;
                if (visualizationTabs.getSelectedTab() == tab) {
                    visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount()-1);
                }
                visualizationTabs.setSelectedTab(tab);
                doNotNotifyScaffoldingManager = false;
            }
        }
        Notification.show("Please select the existing step you want to use.", Notification.Type.WARNING_MESSAGE);
        visualizationSlider.expand();
    }

    private Component recalculateSubjectLayout(int availableWidth) {
        int numberOfSubjects = subjectPanels.keySet().size();
        int numberOfColumns = availableWidth / 350;
        int numberOfRows = numberOfSubjects / numberOfColumns + 1;

        GridLayout oldLayout = subjectLayout;

        subjectLayout = new GridLayout(numberOfColumns,numberOfRows);
        subjectLayout.setSpacing(true);
        for (Panel p: subjectPanels.values()) {
            subjectLayout.addComponent(p);
        }

        mainInteractionArea.replaceComponent(oldLayout,subjectLayout);

        return subjectLayout;
    }
    private Component createSubjectLayout(int availableWidth) {
        int numberOfSubjects = currentProcess.getSubjects().size();
        int numberOfColumns = availableWidth / 350;
        int numberOfRows = numberOfSubjects / numberOfColumns + 1;

        subjectLayout = new GridLayout(numberOfColumns,numberOfRows);

        subjectPanels = new HashMap<>();
        for (Subject s: currentProcess.getSubjects()) {
            Panel panel = new Panel(s.toString());
            panel.setWidth("300px");
            panel.setHeight("400px");
            VerticalLayout panelLayout = new VerticalLayout();
            panelLayout.setSpacing(true);
            panelLayout.setMargin(true);
            panel.setContent(panelLayout);
            subjectPanels.put(s,panel);
            subjectLayout.addComponent(panel);
        }
//        subjectLayout.setMargin(true);
        subjectLayout.setSpacing(true);

        Button addInitialSubject = new Button ("Add a first actor");
        addInitialSubject.addClickListener( e -> {
            openElaborationOverlay(null,ElaborationUI.INITALSUBJECT);
        });

        if (currentProcess.getSubjects().isEmpty()) return addInitialSubject;
        else return subjectLayout;

    }

    private void updateUI() {
        differentProcess.setVisible(false);

        for (Subject s: currentInstance.getProcess().getSubjects()) {
            fillSubjectPanel(s);
        }
        if (initialStartup) {
            differentProcess.setVisible(true);
            initialStartup = false;
        }

        if (!currentInstance.processFinished() && !currentInstance.processIsBlocked()) {
            restart.setVisible(false);
            if (!onboardingActive) scaffoldingPanel.setVisible(true);
        }

        if (!currentInstance.processFinished() && currentInstance.processIsBlocked()) {
            // LogHelper.logDebug("Process blocked, offering to restart ...");
//             scaffoldingPanel.setVisible(false);
            restart.setVisible(true);
            if (currentInstance.isProcessHasBeenChanged()) {
                if (fileStorageHandler == null) fileStorageHandler = new FileStorageHandler();
                if (!fileStorageHandler.isIDCookieAvailable()) {
                    GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
                    this.getUI().addWindow(groupIDEntryWindow);
                    groupIDEntryWindow.addCloseListener(e -> {
                        fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                        fileStorageHandler.saveToServer();
                    });
                } else {
                    fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                    fileStorageHandler.saveToServer();
                }
            }
            Button download = new Button("Download");
            download.addClickListener( e-> {
                LogHelper.logInfo("Download: " + currentProcess +": Download Button clicked");
                fileStorageHandler.openDownloadWindow(this.getUI());
            });
            toolBar.addComponent(download);
        }

        if (currentInstance.processFinished()) {
            simulate.setVisible(false);
            // LogHelper.logDebug("Process finished, offering to restart ...");
            mainLayoutFrame.removeComponent(scaffoldingPanel);
            scaffoldingPanel.setVisible(false);
            if (currentInstance.getProcess().getSubjects().size() > 0) {
                restart.setVisible(true);
                if (currentInstance.isProcessHasBeenChanged()) {
                    if (fileStorageHandler == null) fileStorageHandler = new FileStorageHandler();
                    if (!fileStorageHandler.isIDCookieAvailable()) {
                        GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
                        this.getUI().addWindow(groupIDEntryWindow);
                        groupIDEntryWindow.addCloseListener(e -> {
                            fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                            fileStorageHandler.saveToServer();
                        });
                    } else {
                        fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                        fileStorageHandler.saveToServer();
                    }
                }
                Button download = new Button("Download");
                download.addClickListener( e-> {
                    fileStorageHandler.openDownloadWindow(this.getUI());
                    LogHelper.logInfo("Download: " + currentProcess +": Download Button clicked");
                });
                toolBar.addComponent(download);
            }
            differentProcess.setVisible(true);
        }
    }

    private void fillSubjectPanel(Subject s) {

        VerticalLayout panelContent = (VerticalLayout) subjectPanels.get(s).getContent();
        final OptionGroup conditions = new OptionGroup("Select one of the following options:");
        panelContent.removeAllComponents();

        Label availableMessageList = new Label("");
        Label processMessageLabel = new Label("");
        Label expectedMessageLabel = new Label("<small>The following messages are expected from"+s+", but are not currently provided:</small>", ContentMode.HTML);
        final ComboBox expectedMessageSelector = new ComboBox("please select:");
        Button expectedMessageSend = new Button("Send");

        Set<Message> availableMessages = currentInstance.getAvailableMessagesForSubject(s);
        if (availableMessages != null && availableMessages.size() > 0) {
            StringBuffer list = new StringBuffer("<small>The following messages are available:<ul>");
            for (Message m: availableMessages) {
                list.append("<li>"+m.toString()+"</li>");
            }
            list.append("</ul></small>");
            availableMessageList = new Label(list.toString(), ContentMode.HTML);
        }
        if (currentInstance.getLatestProcessedMessageForSubject(s) != null) {
            processMessageLabel = new Label("<small>Recently received message:<ul><li>"+currentInstance.getLatestProcessedMessageForSubject(s)+"</li></ul></small>", ContentMode.HTML);
        }

        if (s.getExpectedMessages().size()>0) {
            for (Message m: s.getExpectedMessages()) {
                expectedMessageSelector.addItem(m);
            }
            expectedMessageSelector.setValue(s.getExpectedMessages().iterator().next());
            expectedMessageSend.addClickListener( e -> {
                Message m = (Message) expectedMessageSelector.getValue();
                Subject recipient = currentInstance.getProcess().getRecipientOfMessage(m);
                currentInstance.putMessageInInputbuffer(recipient,m);
                updateUI();
            });
        }

        StringBuffer providedMessages = new StringBuffer();
        if (s.getProvidedMessages().size()>0) {
            providedMessages.append("<small>The following messages are provided to "+s+" but are not currently used:<ul>");
            for (Message m: s.getProvidedMessages()) {
                providedMessages.append("<li>"+m+"</li>");
            }
            providedMessages.append("</ul></small>");
        }
        Label providedMessagesLabel = new Label(providedMessages.toString(),ContentMode.HTML);

        State currentState = currentInstance.getAvailableStateForSubject(s);
        if (currentState != null) {
            Label label1 = new Label(currentState.toString(),ContentMode.HTML);
            panelContent.addComponent(label1);

            Set<State> nextPossibleSteps = currentInstance.getNextStatesOfSubject(s);
            if (nextPossibleSteps != null && nextPossibleSteps.size()>0) {
                if (nextPossibleSteps.size() == 1) {
                    State nextState = nextPossibleSteps.iterator().next();
                    if (currentInstance.getConditionForStateInSubject(s, nextState) != null) {
                        Label label2 = new Label("You can only progress under the following condition: <br>"+currentInstance.getConditionForStateInSubject(s,nextState),ContentMode.HTML);
                        panelContent.addComponent(label2);
                    }
                }
                else {
                    if (currentInstance.subjectCanProgress(s)) {
                        boolean toBeShown = false;
                        for (State nextState : nextPossibleSteps) {
                            Condition condition = currentInstance.getConditionForStateInSubject(s, nextState);
                            conditions.addItem(condition);
                            if (!(condition instanceof MessageCondition)) toBeShown = true;
                        }
                        conditions.addValueChangeListener(event -> {
//                            // LogHelper.logDebug("UI: condition for subject " + s + " changed to " + event.getProperty().getValue());
                        });
                        if (toBeShown) panelContent.addComponent(conditions);
                    }
                }
            }

        }
        else {
            Label label1 = new Label("nothing to do");
            if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(label1);
        }

        Button perform = new Button("Perform step");
        perform.addClickListener( e -> {
//            // LogHelper.logDebug("UI: clicking on perfom button for subject "+s);
            lastActiveSubject = s;
            Condition c = null;
            if (conditions.size() > 0) c = (Condition) conditions.getValue();
            currentInstance.advanceStateForSubject(s, c, false);
            updateUI();
            scaffoldingManager.updateScaffolds(currentInstance,currentState);
        });

        Button elaborate = new Button("I have a problem here");
        elaborate.addClickListener( e -> {
            perform.setEnabled(false);
            elaborate.setEnabled(false);
            openElaborationOverlay(s,ElaborationUI.ELABORATE);
            if (onboardingActive) scaffoldingManager.updateScaffolds(currentInstance,currentInstance.getAvailableStateForSubject(s));

        });

        perform.setEnabled(currentInstance.subjectCanProgress(s));

        Button addInitialStep = new Button("Add an initial step");
        addInitialStep.addClickListener( e -> {
            openElaborationOverlay(s,ElaborationUI.INITIALSTEP);

        });

        Button addAdditionStep = new Button("Add an additional step");
        addAdditionStep.addClickListener( e -> {
            openElaborationOverlay(s, ElaborationUI.ADDITIONALSTEP);
        });

        if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(perform);
        if (elaborationAvailable /*&& currentInstance.subjectCanProgress(s)*/) panelContent.addComponent(elaborate);
        if (!availableMessages.isEmpty()) panelContent.addComponent(availableMessageList);
        if (!processMessageLabel.getValue().equals("")) panelContent.addComponent(processMessageLabel);
        if (s.getExpectedMessages().size()>0) {
            panelContent.addComponents(expectedMessageLabel, expectedMessageSelector, expectedMessageSend);
        }
        if (s.getProvidedMessages().size()>0) {
            panelContent.addComponent(providedMessagesLabel);
        }
        if (elaborationAvailable && s.getFirstState() == null && !s.toString().equals(Subject.ANONYMOUS)) panelContent.addComponent(addInitialStep);
        if (elaborationAvailable && currentInstance.subjectFinished(s) && s.getFirstState() != null) panelContent.addComponent(addAdditionStep);
    }

    private void openElaborationOverlay(Subject s, int mode) {
        elaborationActive = true;
        ElaborationUI elaborationUI = new ElaborationUI(processChangeHistory);
        getUI().addWindow(elaborationUI);

        if (mode == ElaborationUI.ELABORATE) elaborationUI.elaborate(s, currentInstance);
        if (mode == ElaborationUI.INITIALSTEP) elaborationUI.initialStep(s, currentInstance);
        if (mode == ElaborationUI.ADDITIONALSTEP) elaborationUI.additionalStep(s, currentInstance);
        if (mode == ElaborationUI.INITALSUBJECT) elaborationUI.initialSubject(currentInstance);

        elaborationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                elaborationActive = false;
                currentInstance.removeLatestHistoryStepForSubject(s);
                if (!processChangeHistory.getHistory().isEmpty()) {
                    elaborationHistory.setVisible(true);
                    historySlider.setVisible(true);
                }
                createBasicLayout();
                scaffoldingManager.updateScaffolds(currentInstance,currentInstance.getAvailableStateForSubject(s));
                updateUI();
            }
        });

    }

    public boolean isElaborationActive() {
        return elaborationActive;
    }

    private void selectDifferentProcess() {
        ProcessSelectorUI processSelectorUI = new ProcessSelectorUI();
        getUI().addWindow(processSelectorUI);
        processSelectorUI.showProcessSelector();
        processSelectorUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                Process newProcess = processSelectorUI.getSelectedProcess();
                LogHelper.logInfo("Execution: " + newProcess +": New process loaded (former process: "+currentProcess+")");
                if (newProcess != null) {
                    currentProcess = newProcess;
                    initialStartup = true;
                    processChangeHistory = processSelectorUI.getProcessChangeHistory();
                    currentInstance = new Instance(currentProcess);
                    createBasicLayout();
                    simulator = new Simulator(currentInstance, subjectPanels, CoMPArEUI.this);
                    if (fileStorageHandler == null) fileStorageHandler = new FileStorageHandler();
                    fileStorageHandler.newProcessStarted();
                    updateUI();
                }
            }
        });

    }

    public ProcessChangeHistory getProcessChangeHistory() {
        return processChangeHistory;
    }

    public boolean simulate(State toState) {
        boolean simSuccessful = simulator.simulatePathToState(toState);
        if (!simSuccessful) Notification.show("Could not go to "+toState,
                "The process has already been executed too far. Finish this round and try again after restarting.",
                Notification.Type.ASSISTIVE_NOTIFICATION);
        return simSuccessful;
    }

    public Map<Subject, Panel> getSubjectPanels() {
        return subjectPanels;
    }

    public Panel getScaffoldingPanel() {
        return scaffoldingPanel;
    }

    public HorizontalLayout getToolBar() {
        return toolBar;
    }

    public GridLayout getSubjectLayout() {
        return subjectLayout;
    }

    public TabSheet getVisualizationTabs() {
        return visualizationTabs;
    }

    public SliderPanel getVisualizationSlider() {
        return visualizationSlider;
    }

    public Button getDifferentProcess() {
        return differentProcess;
    }

    public Button getSimulate() {
        return simulate;
    }

    public Button getRestart() {
        return restart;
    }

    public void setOnboardingActive(boolean onboardingActive) {
        this.onboardingActive = onboardingActive;
        this.elaborationAvailable = !onboardingActive;
        for (Subject s: subjectPanels.keySet())
            fillSubjectPanel(s);
    }

    public void setElaborationAvailable(boolean elaborationAvailable) {
        this.elaborationAvailable = elaborationAvailable;
        for (Subject s: subjectPanels.keySet())
            fillSubjectPanel(s);

    }

    public Subject getLastActiveSubject() {
        return lastActiveSubject;
    }

    @WebServlet(urlPatterns = "/*", name = "CoMPArEServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CoMPArEUI.class, productionMode = false)
    public static class CoMPArEServlet extends VaadinServlet {

        protected void servletInitialized() throws ServletException {
            super.servletInitialized();

            // Get the result folder as defined in WEB-INF/web.xml
            resultFolderName = getServletConfig().getServletContext()
                    .getInitParameter(resultFolderKey);
            File fRf = new File(resultFolderName);
            boolean isWorking = fRf.exists() && fRf.isDirectory()
                    || fRf.mkdirs();
            if (!isWorking) {
                resultFolderName = null;
            }
        }

        private static String resultFolderName = null;
        private final static String resultFolderKey = "at.jku.ce.CoMPAreE.resultfolder";

        public static String getResultFolderName() {
            return resultFolderName;
        }


    }


}
