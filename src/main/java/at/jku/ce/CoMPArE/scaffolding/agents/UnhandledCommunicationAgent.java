package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldGroup;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.ExpandingScaffold;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;
import javafx.scene.Scene;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 26/11/2016.
 */
public class UnhandledCommunicationAgent extends ScaffoldingAgent {

    public UnhandledCommunicationAgent(Process p, ScaffoldingManager manager) {
        super(p, manager);
        this.freq = ScaffoldingAgent.FREQ_EACHSTEP;
    }

    @Override
    public void updateScaffolds(Instance currentInstance, State finishedState) {
        clearAllScaffolds();
//        // LogHelper.logDebug("UnhandledCommunicationAgent: looking for issues to generate scaffolds for ...");
        for (Subject s : currentInstance.getProcess().getSubjects()) {
            ScaffoldGroup sgExp = generateScaffoldsForSubjectsExpectedMessages(s);
            ScaffoldGroup sgProv = generateScaffoldsForSubjectsProvidedMessages(s);
            if (sgExp != null) scaffoldGroups.add(sgExp);
            if (sgProv != null) scaffoldGroups.add(sgProv);
        }
    }

    public ScaffoldGroup generateScaffoldsForSubjectsExpectedMessages(Subject s) {
        ScaffoldGroup sg = new ScaffoldGroup();
        Set<Message> expectedMessages = s.getExpectedMessages();

        if (expectedMessages.isEmpty()) return null;

        sg.addScaffold(Scaffold.TYPE_METACOGNITIVE, new Scaffold(
                "There is input expected from "+s+", which is not currently provided.",
                this,
                "UCA"+expectedMessages.toString()+Scaffold.TYPE_METACOGNITIVE
        ));

        StringBuffer stratDescr = new StringBuffer();
        stratDescr.append("<p>The following inputs are currently expected from "+s+"<ol>");
        for (Message m : expectedMessages) {
            stratDescr.append("<li>Input \"" + m + "\" is expected by " + process.getRecipientOfMessage(m) + "</li>");
        }
        stratDescr.append("</ol></p>");

        sg.addScaffold(Scaffold.TYPE_STRATEGIC, new ExpandingScaffold(
                "Other actors require input from "+s+" which is not currently provided.",
                stratDescr.toString(),
                this,
                "UCA"+expectedMessages.toString()+Scaffold.TYPE_STRATEGIC
        ));

        StringBuffer procDescr = new StringBuffer();
        procDescr.append("<p>The following inputs are currently expected from "+s+"<ol>");
        for (Message m : expectedMessages) {
            procDescr.append("<li>Input \"" + m + "\" is expected by " + process.getRecipientOfMessage(m) + "</li>");
        }
        procDescr.append("</ol></p>");
        procDescr.append("<p>In order to provide this input, play through the behaviour of "+s+" until you have reached a " +
                "step, where the input would be available. Then click the \"I have a problem here\"-Button and select " +
                "\"It can't be done at the moment.\" or \"I rather need to do something else instead.\". You are then" +
                "able to specify a new step, in which you can provide the required input.</p>" +
                "<p>If there is no such step, where this input would be available, you can click the \"Add an additional step\"-Button " +
                "once you have finished playing through the actor's behaviour to add steps that enable to provide the requested input.</p>");

        sg.addScaffold(Scaffold.TYPE_PROCEDURAL, new ExpandingScaffold(
                s+" still requires additional behaviour to provide expected input to others.",
                procDescr.toString(),
                this,
                "UCA"+expectedMessages.toString()+Scaffold.TYPE_PROCEDURAL
        ));

        return sg;
    }

    public ScaffoldGroup generateScaffoldsForSubjectsProvidedMessages(Subject s) {
        ScaffoldGroup sg = new ScaffoldGroup();
        Set<Message> providedMessages = s.getProvidedMessages();

        if (providedMessages.isEmpty()) return null;

        sg.addScaffold(Scaffold.TYPE_METACOGNITIVE, new Scaffold(
                "There is input provided to "+s+", which is currently not used.",
                this,
                "UCA"+providedMessages.toString()+Scaffold.TYPE_METACOGNITIVE
        ));

        StringBuffer stratDescr = new StringBuffer();
        stratDescr.append("<p>The following inputs are currently provided to "+s+"<ol>");
        for (Message m : providedMessages) {
            stratDescr.append("<li>Input \"" + m + "\" is provided by " + process.getSenderOfMessage(m) + "</li>");
        }
        stratDescr.append("</ol></p>");

        sg.addScaffold(Scaffold.TYPE_STRATEGIC, new ExpandingScaffold(
                "Other actors provide input to "+s+" which is not used in its behaviour.",
                stratDescr.toString(),
                this,
                "UCA"+providedMessages.toString()+Scaffold.TYPE_STRATEGIC
        ));

        StringBuffer procDescr = new StringBuffer();
        procDescr.append("<p>The following inputs are currently provided to "+s+"<ol>");
        for (Message m : providedMessages) {
            procDescr.append("<li>Input \"" + m + "\" is provided by " + process.getSenderOfMessage(m) + "</li>");
        }
        procDescr.append("</ol></p>");
        procDescr.append("<p>In order to use this input, play through the behaviour of "+s+" until you have reached a " +
                "step, where the input would be of use. Then click the \"I have a problem here\"-Button and select " +
                "\"It can't be done at the moment.\" followed by \"I need more input to be able to do this activity.\". You are then" +
                "able to select the provided input and insert a step where "+s+" waits for this input to arrive.</p>" +
                "<p>If there is no such step, where this input would be of use, you can simply ignore it or you can click the \"Add an additional step\"-Button " +
                "once you have finished playing through the actor's behaviour to add steps that describe behaviour to handle this input.</p>");

        sg.addScaffold(Scaffold.TYPE_PROCEDURAL, new ExpandingScaffold(
                s+" might still require additional behaviour to handle input provided by others.",
                procDescr.toString(),
                this,
                "UCA"+providedMessages.toString()+Scaffold.TYPE_PROCEDURAL
        ));


        return sg;
    }

}