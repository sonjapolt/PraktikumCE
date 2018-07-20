package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class Message extends ProcessElement {

    private String name;
    private Object content;

    public Message(String name) {
        super();
        this.name = name;
        this.content = null;
    }

    public Message(String name, Object content) {
        this(name);
        this.content = content;
    }

    public Message(Message m) {
        super(m);
        this.name = m.toString();
        this.content = m.getContent();
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return name;
    }

}
