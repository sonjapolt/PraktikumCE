package at.jku.ce.CoMPArE.visualize;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.diff.ModelDiff;
import at.jku.ce.CoMPArE.elaborate.ProcessChangeHistory;
import at.jku.ce.CoMPArE.elaborate.ProcessChangeTransaction;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.ProcessElement;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;
import org.apache.commons.logging.impl.Log4JLogger;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroup;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroupItemComponent;

import java.util.*;

/**
 * Created by oppl on 09/02/2017.
 */
public class VisualizeModelEvolution extends GridLayout {

    FlexibleOptionGroup vizSwitcher;
    TabSheet tabSheet;

    Vector<Process> history;

    public VisualizeModelEvolution(Process process, ProcessChangeHistory processChangeHistory) {
        LinkedList<Process> historyList = new LinkedList<>();
        LinkedList<ProcessChangeTransaction> transactions = processChangeHistory.getHistory();
        LinkedList<ProcessChangeTransaction> reverse = new LinkedList<>();
        historyList.addFirst(new Process(process));
        for (ProcessChangeTransaction transaction: transactions) {
            transaction.undo(process);
            historyList.addFirst(new Process(process));
            reverse.addFirst(transaction);
        }
        for (ProcessChangeTransaction transaction: reverse) {
            transaction.perform(process);
        }
        history = new Vector<>(historyList);

//        createLayout();
    }

    public VisualizeModelEvolution(Vector<Process> processes) {
        history = sortProcesses(processes);
        createLayout();
    }

    private Vector<Process> sortProcesses(Vector<Process> processes) {
        LinkedList<Process> sorted = new LinkedList<>();
        for (Process p:processes) {
            if (sorted.size()==0) sorted.addFirst(p);
            else {
                int index = 0;
                for (Process a:sorted) {
                    if (!p.getTimestamp().after(a.getTimestamp())) {
                        sorted.add(index, p);
                        break;
                    }
                    index++;
                }
                if (index == sorted.size()) sorted.addLast(p);
            }
        }
        return new Vector<>(sorted);
    }

    public void createLayout() {
        if (history == null) return;
        this.setRows(2);
        this.setRowExpandRatio(1,1);
        this.addStyleName("");
        vizSwitcher = new FlexibleOptionGroup();
        vizSwitcher.addItem("per actor");
        vizSwitcher.addItem("overall");
        vizSwitcher.addItem("activities");
        HorizontalLayout optionLayout = new HorizontalLayout();
        optionLayout.setSpacing(true);
        for (Iterator<FlexibleOptionGroupItemComponent> iter = vizSwitcher
                .getItemComponentIterator(); iter.hasNext();) {
            FlexibleOptionGroupItemComponent c = iter.next();
            optionLayout.addComponent(c);
            optionLayout.addComponent(new Label(c.getCaption()));
        }

        vizSwitcher.addValueChangeListener( e -> {
            this.removeAllComponents();
            tabSheet = new TabSheet();
            int count = 1;
            Process previous = null;
            for (Process p: history) {
                Panel panel = new Panel(createTabForProcess(p,previous));
                previous = p;
                panel.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-130)+"px");
                tabSheet.addTab(panel,""+count);
                count++;
            }
            this.addComponent(optionLayout);
            this.addComponent(tabSheet);
            this.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth()-200)+"px");
            this.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-20)+"px");
            this.setMargin(true);
            this.setSpacing(true);
        });
        vizSwitcher.setValue("per actor");
    }

    private GridLayout createTabForProcess(Process p, Process previous) {
        GridLayout gl = null;

        ModelDiff diff = null;
        if (previous != null) diff = new ModelDiff(previous,p);

        int availableWidth = UI.getCurrent().getPage().getBrowserWindowWidth()-200;
        int numberOfSubjects = p.getSubjects().size()+1;
        int widthOfColumns = (availableWidth-190) / 3;
        int numberOfRows = numberOfSubjects / 4;

        if (vizSwitcher.getValue().equals("per actor")) {
            gl = new GridLayout(3, numberOfRows);

            Panel panel = new Panel("Interaction");
            panel.setWidth(widthOfColumns + "px");
            panel.setHeight("350px");
            VisualizeModel model = new VisualizeModel("Interaction", null, widthOfColumns, 300);
            Set toBeMarked = new HashSet();
            if (diff != null) {
                toBeMarked.addAll(diff.getAddedSubjects());
                toBeMarked.addAll(diff.getAddedMessages());
            }
            model.showSubjectInteraction(p, toBeMarked);
            panel.setContent(model);
            gl.addComponent(panel);

            for (Subject s : p.getSubjects()) {
                panel = new Panel(s.toString());
                panel.setWidth(widthOfColumns + "px");
                panel.setHeight("350px");
                model = new VisualizeModel(s.toString(), null, widthOfColumns, 310);
                toBeMarked = new HashSet();
                if (diff != null) {
                    toBeMarked.addAll(diff.getAddedStates());
                    toBeMarked.addAll(diff.getAddedTransitions());
                }

                model.showSubject(s, toBeMarked);
/*            if (diff != null) {
                model.markProcessElements(diff.getAddedStates(),true);
                model.markProcessElements(diff.getAddedTransitions(),true);
            }
*/
                panel.setContent(model);
                gl.addComponent(panel);
            }
            gl.setMargin(true);
            gl.setSpacing(true);
        }
        if (vizSwitcher.getValue().equals("overall")) {
            gl = new GridLayout(1,1);
            Panel panel = new Panel("Overall");
            panel.setWidth((availableWidth-80) + "px");
            panel.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-150)+"px");
            VisualizeModel model = new VisualizeModel("Overall", null, availableWidth-80, (UI.getCurrent().getPage().getBrowserWindowHeight()-190));
            Set toBeMarked = new HashSet();
            if (diff != null) {
                toBeMarked.addAll(diff.getAddedSubjects());
                toBeMarked.addAll(diff.getAddedMessages());
                toBeMarked.addAll(diff.getAddedStates());
                toBeMarked.addAll(diff.getAddedTransitions());
            }
            model.showWholeProcess(p, toBeMarked);
            panel.setContent(model);
            gl.addComponent(panel);

        }
        if (vizSwitcher.getValue().equals("activities")) {
            gl = new GridLayout(1,1);
            Panel panel = new Panel("Activities");
            panel.setWidth((availableWidth-80) + "px");
            panel.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-150)+"px");
            VisualizeModel model = new VisualizeModel("Activities", null, availableWidth-80, (UI.getCurrent().getPage().getBrowserWindowHeight()-190));
            Set toBeMarked = new HashSet();
            if (diff != null) {
                toBeMarked.addAll(diff.getAddedSubjects());
                toBeMarked.addAll(diff.getAddedMessages());
                toBeMarked.addAll(diff.getAddedStates());
                toBeMarked.addAll(diff.getAddedTransitions());
            }
            model.showWholeProcessFlow(p, toBeMarked);
            panel.setContent(model);
            gl.addComponent(panel);

        }

        return gl;
    }


}
