package at.jku.ce.CoMPArE.execute;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.Subject;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by oppl on 18/01/2017.
 */
public class InstanceHistory {

    private LinkedList<InstanceHistoryStep> history;

    public InstanceHistory() {
        this.history = new LinkedList<>();
    }

    public void addHistoryStep(InstanceHistoryStep step) {
        history.addFirst(step);
    }

    public LinkedList<InstanceHistoryStep> getWholeHistory() {
        return history;
    }

    public LinkedList<InstanceHistoryStep> getHistoryForSubject(Subject s) {
        LinkedList<InstanceHistoryStep> listForSubject = new LinkedList<>();
        for (InstanceHistoryStep step:history) {
            if (step.getAffectedSubject().equals(s)) listForSubject.add(step);
        }
        return listForSubject;
    }

    public InstanceHistoryStep getHistoryStepById(UUID id) {
        for (InstanceHistoryStep step:history) {
            if (step.getId().equals(id)) return step;
        }
        return null;
    }

    public void removeAllStepsUntil(InstanceHistoryStep step) {
        if (!history.contains(step)) return;
        while (!history.getFirst().equals(step)) {
            history.removeFirst();
        }
        history.removeFirst();
    }
    public void removeLatestStepForSubject(Subject s) {
        InstanceHistoryStep toBeRemoved = null;
        for (InstanceHistoryStep step:history) {
            if (step.getAffectedSubject().equals(s)) toBeRemoved = step;
        }
        if (toBeRemoved != null) history.remove(toBeRemoved);
    }

}
