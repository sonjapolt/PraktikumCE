package at.jku.ce.CoMPArE.process;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Created by oppl on 22/11/2016.
 */
public class Condition extends ProcessElement {

    private String condition;

//    @XStreamOmitField
//    State parentState;

    public Condition(String condition) {
        super();
        this.condition = condition;
//        this.parentState = null;
    }

    public Condition(Condition condition) { //, State newContainer) {
        super(condition);
        this.condition = condition.getCondition();
//        this.parentState = newContainer;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

//    public void setParentState(State parentState) {
//        this.parentState = parentState;
//    }

    @Override
    public String toString() {
        return condition;
    }

//    public void reconstructParentRelations(State state) {
//        this.parentState = state;
//    }

}
