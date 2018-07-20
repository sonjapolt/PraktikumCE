package at.jku.ce.CoMPArE.visualize;

/**
 * Created by oppl on 24/11/2016.
 */
import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.InstanceHistoryStep;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import com.vaadin.pontus.vizcomponent.VizComponent;
import com.vaadin.pontus.vizcomponent.VizComponent.EdgeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickListener;
import com.vaadin.pontus.vizcomponent.client.ZoomSettings;
import com.vaadin.pontus.vizcomponent.model.Graph;
import com.vaadin.pontus.vizcomponent.model.Subgraph;
import com.vaadin.ui.*;
import sun.rmi.runtime.Log;

import java.util.*;

public class VisualizeModel extends VerticalLayout {

    String name;

    Panel panel;
    Graph graph;
    VizComponent component;
    private Graph.Node selectedNode;

    public VisualizeModel(String name, CoMPArEUI parent, int width, int height) {

        this.name = name;

        panel = new Panel("");
        panel.setWidth(""+width);
        panel.setHeight(""+height);

        component = new VizComponent();
        graph = new Graph(name, Graph.DIGRAPH);

        component.setCaption("");
        component.setWidth("100%");
        component.setHeight("100%");
        panel.setContent(component);
//        component.drawGraph(graph);

        selectedNode = null;

//        setSizeFull();
        addComponent(panel);
//        setComponentAlignment(component, Alignment.MIDDLE_CENTER);

        ZoomSettings zs = new ZoomSettings();
        zs.setPreventMouseEventsDefault(true);
        zs.setFit(true);
        zs.setMaxZoom(2.0f);
        zs.setMinZoom(0.5f);
        component.setPanZoomSettings(zs);

        component.addClickListener(new NodeClickListener() {

            @Override
            public void nodeClicked(NodeClickEvent e) {
                selectedNode = e.getNode();
                // LogHelper.logDebug("VizUI: selected node "+selectedNode.getId());
                if (parent != null) parent.informAboutSelectedNode(name,selectedNode.getId());
            }

        });


    }

    public void showSubject(Subject subject) {
        graph = new Graph(name, Graph.DIGRAPH);
        addStates(subject.getStates(), new HashSet());
        addTransitions(subject.getTransitions(),subject, new HashSet());
        component.drawGraph(graph);
        component.fitGraph();
    }

    public void showSubject(Subject subject, Set toBeMarked) {
        graph = new Graph(name, Graph.DIGRAPH);
        addStates(subject.getStates(),toBeMarked);
        addTransitions(subject.getTransitions(),subject, toBeMarked);
        component.drawGraph(graph);
        component.fitGraph();
    }


    public void addStates(Collection<State> states, Set toBeMarked) {
        for (State s: states) {
            Graph.Node node = new Graph.Node(s.getUUID().toString());
            node.setParam("shape", "box");
            node.setParam("label", "\""+s.toString()+"\"");
            if (toBeMarked.contains(s)) {
                node.setParam("style", "filled");
                node.setParam("fillcolor", "lightgreen");
                node.setParam("color", "darkgreen");
                node.setParam("penwidth", "3.0");
            }
            graph.addNode(node);
        }
    }

    public void addTransitions(Set<Transition> transitions, Subject subject, Set toBeMarked) {
        for (Transition t: transitions) {
            graph.addEdge(graph.getNode(t.getSource().toString()), graph.getNode(t.getDest().toString()));
            Graph.Edge edge = graph.getEdge(graph.getNode(t.getSource().toString()), graph.getNode(t.getDest().toString()));
            Condition c = t.getCondition();
            if (toBeMarked.contains(t)) {
                edge.setParam("color","darkgreen");
                edge.setParam("penwidth","3.0");
                edge.setParam("fontcolor","darkgreen");
            }
            if (c != null) {
                if (c instanceof MessageCondition) edge.setParam("label","\""+subject.getParentProcess().getMessageByUUID(((MessageCondition) c).getMessage()).toString()+"\"");
                else edge.setParam("label", "\""+c.toString()+"\"");
            }

        }
    }


    public void greyOutCompletedStates(LinkedList<InstanceHistoryStep> history, State currentState) {
        Set<State> currentStates = new HashSet<>();
        currentStates.add(currentState);
        greyOutCompletedStates(history,currentStates);
    }

    public void greyOutCompletedStates(LinkedList<InstanceHistoryStep> history, Collection<State> currentStates) {
        Graph.Node currentNode = null;
        Graph.Node previousNode = null;
        LinkedList<InstanceHistoryStep> reverseHistory = new LinkedList<>(history);
        Collections.reverse(reverseHistory);
        for (InstanceHistoryStep s : reverseHistory) {
            currentNode = graph.getNode(s.getState().getUUID().toString());
            if (currentNode == null) {
                for (Graph.Node node: graph.getNodes()) {
                    if (node instanceof Subgraph.GraphNode) {
                        currentNode = ((Subgraph.GraphNode) node).getGraph().getNode(s.getState().getUUID().toString());
                        if (currentNode != null) break;
                    }
                }
            }
            if (currentNode != null) {
                currentNode.setParam("style", "filled");
                currentNode.setParam("fillcolor", "lightgrey");
                currentNode.setParam("color", "darkgreen");
                currentNode.setParam("penwidth", "3.0");
                currentNode.setParam("fontcolor","darkgreen");

            }
            if (previousNode!=null && currentNode != null) {
                Graph.Edge edge = graph.getEdge(previousNode, currentNode);
                if (edge != null) {
                    edge.setParam("color", "darkgreen");
                    edge.setParam("fontcolor","darkgreen");

                }
            }
            previousNode = currentNode;
        }
        for (State currentState:currentStates) {
            if (currentState instanceof RecvState) continue;
            if (currentState != null) currentNode = graph.getNode(currentState.getUUID().toString());
            if (currentNode == null) {
                for (Graph.Node node: graph.getNodes()) {
                    if (node instanceof Subgraph.GraphNode) {
                        if (currentState != null) currentNode = ((Subgraph.GraphNode) node).getGraph().getNode(currentState.getUUID().toString());
                        if (currentNode != null) break;
                    }
                }
            }
            if (currentNode != null) {
                currentNode.setParam("style", "filled");
                currentNode.setParam("fillcolor", "lightgreen");

                component.addTextCss(currentNode, "font-weight", "bold");
                if (previousNode != null) {
                    Graph.Edge edge = graph.getEdge(previousNode, currentNode);
                    if (edge != null) {
                        edge.setParam("color", "darkgreen");
                        edge.setParam("fontcolor", "darkgreen");
                        component.addTextCss(edge, "fill", "darkgreen");
                    }
                }
            }
        }
        component.drawGraph(graph);
        component.fitGraph();
    }


    public void showSubjectInteraction(Process p) {
        showSubjectInteraction(p, new HashSet());
    }

    public void showSubjectInteraction(Process p, Set toBeMarked) {
//        // LogHelper.logDebug("creating subject interaction");
        graph = new Graph("", Graph.DIGRAPH);
        for (Message m: p.getMessages()) {
            Graph.Node sender = new Graph.Node(p.getSenderOfMessage(m).getUUID().toString());
            Graph.Node recipient = new Graph.Node(p.getRecipientOfMessage(m).getUUID().toString());
            sender.setParam("label", "\""+p.getSenderOfMessage(m).toString()+"\"");
            recipient.setParam("label", "\""+p.getRecipientOfMessage(m).toString()+"\"");

            if (toBeMarked.contains(p.getSenderOfMessage(m))) {
                sender.setParam("style", "filled");
                sender.setParam("fillcolor", "lightgreen");
                sender.setParam("color", "darkgreen");
                sender.setParam("penwidth", "3.0");
            }

            if (toBeMarked.contains(p.getRecipientOfMessage(m))) {
                recipient.setParam("style", "filled");
                recipient.setParam("fillcolor", "lightgreen");
                recipient.setParam("color", "darkgreen");
                recipient.setParam("penwidth", "3.0");
            }

            Graph.Node message = new Graph.Node((m.getUUID().toString()));
            message.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
            message.setParam("fontsize","10");
            message.setParam("shape", "note");
            if (toBeMarked.contains(m)) {
                message.setParam("style", "filled");
                message.setParam("fillcolor", "lightgreen");
                message.setParam("color", "darkgreen");
                message.setParam("penwidth", "3.0");
            }


            graph.addEdge(sender, message);
            graph.addEdge(message,recipient);

        }
        component.drawGraph(graph);
    }

    public void showWholeProcess(Process p) {
        showWholeProcess(p, new HashSet());
    }

    public void showWholeProcess(Process p, Set toBeMarked) {
        graph = new Graph("", Graph.DIGRAPH);
        int cnt = 0;
        HashMap<UUID, Subgraph.GraphNode> uuid2Cluster = new HashMap<>();
        for (Subject subject: p.getSubjects()) {
            Subgraph sub = new Subgraph();
            Collection<State> states = subject.getStates();
            Set<Transition> transitions = subject.getTransitions();

            if (states.size() == 0) {
                Graph.Node node = new Graph.Node(subject.toString()+"_stateProxy");
                node.setParam("shape", "box");
                node.setParam("label", "\"no activity yet\"");
                node.setParam("style", "dashed");
                sub.addNode(node);
            }
            if (subject.getExpectedMessages().size() > 0) {
                Graph.Node node = new Graph.Node(subject.getUUID()+"_sendProxy");
                node.setParam("shape", "rarrow");
                node.setParam("label", "\"not yet provided\"");
                sub.addNode(node);
            }
            if (subject.getProvidedMessages().size() > 0) {
                Graph.Node node = new Graph.Node(subject.getUUID()+"_recvProxy");
                node.setParam("label", "\"not yet used\"");
                node.setParam("shape", "larrow");
                sub.addNode(node);
            }
            for (State s: states) {
                Graph.Node node = new Graph.Node(s.getUUID().toString());
                node.setParam("shape", "box");
                node.setParam("label", "\"" + s.toString() + "\"");
                if (toBeMarked.contains(s)) {
                    node.setParam("style", "filled");
                    node.setParam("fillcolor", "lightgreen");
                    node.setParam("color", "darkgreen");
                    node.setParam("penwidth", "3.0");
                }
                sub.addNode(node);
            }
            for (Transition t: transitions) {
                sub.addEdge(sub.getNode(t.getSource().toString()), sub.getNode(t.getDest().toString()));
                Graph.Edge edge = sub.getEdge(sub.getNode(t.getSource().toString()), sub.getNode(t.getDest().toString()));
                Condition c = t.getCondition();
                if (toBeMarked.contains(t)) {
                    edge.setParam("color", "darkgreen");
                    edge.setParam("penwidth", "3.0");
                    edge.setParam("fontcolor", "darkgreen");
                }
                if (c != null) {
                    if (c instanceof MessageCondition)
                        edge.setParam("label", "\"" + subject.getParentProcess().getMessageByUUID(((MessageCondition) c).getMessage()).toString() + "\"");
                    else edge.setParam("label", "\"" + c.toString() + "\"");
                }
            }
            Subgraph.GraphNode subNode = new Subgraph.GraphNode("cluster_"+cnt, sub);
            uuid2Cluster.put(subject.getUUID(),subNode);
            cnt++;
            subNode.setParam("label", "\""+subject.toString()+"\"");
            if (toBeMarked.contains(subject)) {
                subNode.setParam("style", "filled");
                subNode.setParam("fillcolor", "lightgreen");
                subNode.setParam("color", "darkgreen");
                subNode.setParam("penwidth", "3.0");
            }

            graph.addNode(subNode);
        }
        for (Message m: p.getMessages()) {
//            // LogHelper.logDebug("adding information for Message "+m.toString());
            Subgraph senderNode = uuid2Cluster.get(p.getSenderOfMessage(m).getUUID()).getGraph();
            Subgraph recipientNode = uuid2Cluster.get(p.getRecipientOfMessage(m).getUUID()).getGraph();
            Set<Graph.Node> senders = new HashSet<>();
            if (p.getSenderOfMessage(m).getExpectedMessages().contains(m))
                senders.add(senderNode.getNode(p.getSenderOfMessage(m).getUUID()+"_sendProxy"));
            else {
                for (State sendState: p.getSenderOfMessage(m).getSendStates(m)) senders.add(senderNode.getNode(sendState.getUUID().toString()));
            }
            Set<Graph.Node> recipients = new HashSet<>();
            if (p.getRecipientOfMessage(m).getProvidedMessages().contains(m))
                recipients.add(recipientNode.getNode(p.getRecipientOfMessage(m).getUUID()+"_recvProxy"));
            else {
                for (State recvState: p.getRecipientOfMessage(m).getRecvStates(m)) recipients.add(recipientNode.getNode(recvState.getUUID().toString()));
            }

            Graph.Node message = new Graph.Node((m.getUUID().toString()));
            message.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
            message.setParam("fontsize","10");
            message.setParam("shape", "note");
            if (toBeMarked.contains(m)) {
                message.setParam("style", "filled");
                message.setParam("fillcolor", "lightgreen");
                message.setParam("color", "darkgreen");
                message.setParam("penwidth", "3.0");
            }

            graph.addNode(message);
            for (Graph.Node sender: senders) {
                senderNode.addEdge(sender, message);
                Graph.Edge edge = senderNode.getEdge(sender,message);
                edge.setParam("style","dashed");
            }
            for (Graph.Node recipient: recipients) {
                graph.addEdge(message,recipient);
                Graph.Edge edge = graph.getEdge(message,recipient);
                edge.setParam("style","dashed");

            }

        }
        component.drawGraph(graph);
    }

    public void showWholeProcessFlow(Process p) {
        showWholeProcessFlow(p,new HashSet());
    }

    public void showWholeProcessFlow(Process p, Set toBeMarked) {
        graph = new Graph("", Graph.DIGRAPH);
        for (Subject subject: p.getSubjects()) {
            Collection<State> states = subject.getStates();
            Set<Transition> transitions = subject.getTransitions();

            if (subject.getExpectedMessages().size() > 0) {
                Graph.Node node = new Graph.Node(subject.getUUID()+"_sendProxy");
                node.setParam("shape", "rarrow");
                node.setParam("label", "\"not yet provided by "+subject+"\"");
                graph.addNode(node);
            }
            if (subject.getProvidedMessages().size() > 0) {
                Graph.Node node = new Graph.Node(subject.getUUID()+"_recvProxy");
                node.setParam("label", "\"not yet used by "+subject+"\"");
                node.setParam("shape", "larrow");
                graph.addNode(node);
            }
            for (State s: states) {
                Graph.Node node = new Graph.Node(s.getUUID().toString());
                node.setParam("shape", "box");
                node.setParam("label", "\"" + s.toString() +"\n(" + subject+ ")\"");
                if (toBeMarked.contains(s)) {
                    node.setParam("style", "filled");
                    node.setParam("fillcolor", "lightgreen");
                    node.setParam("color", "darkgreen");
                    node.setParam("penwidth", "3.0");
                }
                graph.addNode(node);
            }
            for (Transition t: transitions) {
                graph.addEdge(graph.getNode(t.getSource().toString()), graph.getNode(t.getDest().toString()));
                Graph.Edge edge = graph.getEdge(graph.getNode(t.getSource().toString()), graph.getNode(t.getDest().toString()));
                Condition c = t.getCondition();
                if (toBeMarked.contains(t)) {
                    edge.setParam("color", "darkgreen");
                    edge.setParam("penwidth", "3.0");
                    edge.setParam("fontcolor", "darkgreen");
                }
                if (c != null) {
                    if (c instanceof MessageCondition)
                        edge.setParam("label", "\"" + subject.getParentProcess().getMessageByUUID(((MessageCondition) c).getMessage()).toString() + "\"");
                    else edge.setParam("label", "\"" + c.toString() + "\"");
                }
            }
        }
        for (Message m: p.getMessages()) {
//            // LogHelper.logDebug("adding information for Message "+m.toString());
            Set<Graph.Node> senders = new HashSet<>();
            if (p.getSenderOfMessage(m).getExpectedMessages().contains(m))
                senders.add(graph.getNode(p.getSenderOfMessage(m).getUUID()+"_sendProxy"));
            else {
                for (State sendState: p.getSenderOfMessage(m).getSendStates(m)) senders.add(graph.getNode(sendState.getUUID().toString()));
            }
            Set<Graph.Node> recipients = new HashSet<>();
            if (p.getRecipientOfMessage(m).getProvidedMessages().contains(m))
                recipients.add(graph.getNode(p.getRecipientOfMessage(m).getUUID()+"_recvProxy"));
            else {
                for (State recvState: p.getRecipientOfMessage(m).getRecvStates(m)) recipients.add(graph.getNode(recvState.getUUID().toString()));
            }

            Graph.Node message = new Graph.Node((m.getUUID().toString()));
            message.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
            message.setParam("fontsize","10");
            message.setParam("shape", "note");
            if (toBeMarked.contains(m)) {
                message.setParam("style", "filled");
                message.setParam("fillcolor", "lightgreen");
                message.setParam("color", "darkgreen");
                message.setParam("penwidth", "3.0");
            }

            graph.addNode(message);
            for (Graph.Node sender: senders) {
                graph.addEdge(sender, message);
                Graph.Edge edge = graph.getEdge(sender,message);
                edge.setParam("style","dashed");
            }
            for (Graph.Node recipient: recipients) {
                graph.addEdge(message,recipient);
                Graph.Edge edge = graph.getEdge(message,recipient);
                edge.setParam("style","dashed");

            }

        }
        component.drawGraph(graph);
    }

    public String getSelectedNodeName() {
        if (selectedNode == null) return null;
        return selectedNode.getId();
    }

}

