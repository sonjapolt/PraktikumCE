package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;

import java.util.Date;

/**
 * Created by oppl on 15/12/2016.
 */
public abstract class ProcessChangeCommand {

    protected State newActiveState;

    public ProcessChangeCommand() {
        newActiveState = null;
    }

    public abstract boolean perform(Process p);

    public abstract boolean undo(Process p);

    public State getNewActiveState() { return newActiveState; }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
