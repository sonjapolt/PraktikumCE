package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.InstanceHistoryStep;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;

import java.util.*;

/**
 * Created by oppl on 16/01/2017.
 */
public class ProcessChangeTransaction {
    Vector<ProcessChangeCommand> commands;
    State newActiveState;
    InstanceHistoryStep affectedInstanceHistoryState;

    public ProcessChangeTransaction() {
        commands = new Vector<>();
    }

    public ProcessChangeTransaction(List<ProcessChangeCommand> commands) {
        this();
        add(commands);
    }

    public void add(ProcessChangeCommand processChangeCommand) {
        commands.add(processChangeCommand);
    }

    public void add(List<ProcessChangeCommand> commands) {
        Iterator<ProcessChangeCommand> i = commands.iterator();
        while (i.hasNext()) {
            this.commands.add(i.next());
        }
    }

    public boolean perform(Process p) {
        boolean successful = true;
        Vector<ProcessChangeCommand> rollbackBuffer = new Vector<>();
        for (ProcessChangeCommand processChangeCommand: commands) {
           successful = processChangeCommand.perform(p);
           if (!successful) break;
           else {
               rollbackBuffer.add(processChangeCommand);
               State s = processChangeCommand.getNewActiveState();
               if (s != null && !s.getParentSubject().getPredecessorStates(s).contains(newActiveState)) newActiveState = s;
           }
        }
        if (!successful) {
            Collections.reverse(rollbackBuffer);
            for (ProcessChangeCommand rollbackCommand:rollbackBuffer) {
                rollbackCommand.undo(p);
                State s = rollbackCommand.getNewActiveState();
                if (s != null && !s.getParentSubject().getPredecessorStates(s).contains(newActiveState)) newActiveState = s;
            }
        }
        return successful;
    }

    public boolean undo(Process p) {
        Vector<ProcessChangeCommand> reverseCommands = new Vector<>(commands);
        Collections.reverse(reverseCommands);

        boolean successful = true;
        Vector<ProcessChangeCommand> rollbackBuffer = new Vector<>();
        for (ProcessChangeCommand processChangeCommand: reverseCommands) {
            successful = processChangeCommand.undo(p);
//            // LogHelper.logDebug(processChangeCommand.getClass().getSimpleName());
            if (!successful) break;
            else {
                rollbackBuffer.add(processChangeCommand);
                State s = processChangeCommand.getNewActiveState();
                if (s != null && !s.getParentSubject().getPredecessorStates(s).contains(newActiveState)) newActiveState = s;

            }
        }
        if (!successful) {
            Collections.reverse(rollbackBuffer);
            for (ProcessChangeCommand rollbackCommand:rollbackBuffer) {
                rollbackCommand.perform(p);
                State s = rollbackCommand.getNewActiveState();
                if (s != null && !s.getParentSubject().getPredecessorStates(s).contains(newActiveState)) newActiveState = s;

            }
        }
        return successful;
    }

    public State getNewActiveState() {
        return newActiveState;
    }

    public InstanceHistoryStep getAffectedInstanceHistoryState() {
        return affectedInstanceHistoryState;
    }

    public void setAffectedInstanceHistoryState(InstanceHistoryStep affectedInstanceHistoryState) {
        this.affectedInstanceHistoryState = affectedInstanceHistoryState;
    }

    @Override
    public String toString() {
        StringBuffer changeList = new StringBuffer();
        Iterator<ProcessChangeCommand> i = commands.iterator();
        while (i.hasNext()) {
            changeList.append(i.next().toString());
            if (i.hasNext()) changeList.append("<br>");
        }
        return changeList.toString();
    }
}
