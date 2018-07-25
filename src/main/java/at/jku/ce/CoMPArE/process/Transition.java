package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.process.Condition;
import at.jku.ce.CoMPArE.process.State;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.UUID;

/**
 * Created by oppl on 10/02/2017.
 */
public class Transition extends ProcessElement {
    private UUID source;
    private UUID dest;
    private Condition condition;

//    @XStreamOmitField
    private String name;

    @XStreamOmitField
    Subject parentSubject;

    public Transition(State source, State dest) {
        super();
        this.source = source.getUUID();
        this.dest = dest.getUUID();
        this.condition = null;
        name = new String("Transition between "+source+" and "+dest);
        this.parentSubject = source.parentSubject;
    }

    public Transition(State source, State dest, Condition condition) {
        this(source,dest);
        this.condition = condition;
    }

    public Transition(Transition transition) {
        super(transition);
        this.source = transition.getSource();
        this.dest = transition.getDest();
        this.name = new String(transition.toString());
        if (transition.getCondition() instanceof MessageCondition)
            this.condition = new MessageCondition(((MessageCondition) transition.getCondition()).getMessage());
        else if (transition.getCondition() == null) this.condition = null;
        else this.condition = new Condition(transition.getCondition());
    }

    public UUID getSource() {
        return source;
    }

    public UUID getDest() {
        return dest;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return name;
    }

    public Subject getParentSubject() {
        return parentSubject;
    }

    public void reconstructParentRelations(Subject subject) {
        this.parentSubject = subject;
    }

}
