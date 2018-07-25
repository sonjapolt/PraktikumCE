package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class ActionState extends State {

    private String description;

    public ActionState(String name) {
        super(name);
    }
    public ActionState(String name, boolean isEndState) {
        super(name,isEndState);
    }

    public ActionState(String name, String description) {
        super(name);
        this.description = description;
    }

    public ActionState(ActionState s, Subject container) {
        super(s, container);
        description = s.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
