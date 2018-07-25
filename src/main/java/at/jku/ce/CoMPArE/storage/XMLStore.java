package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import com.thoughtworks.xstream.XStream;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import sun.rmi.runtime.Log;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by oppl on 29/11/2016.
 */
public class XMLStore {

    XStream xStream;
    FileDownloader fileDownloader;


    public XMLStore() {
        xStream = new XStream();
        xStream.alias("process", Process.class);
        xStream.alias("timestamp", Date.class);
        xStream.alias("subject", Subject.class);

        xStream.alias("state", State.class);
        xStream.alias("actionstate", ActionState.class);
        xStream.alias("recvstate", RecvState.class);
        xStream.alias("sendstate", SendState.class);

        xStream.alias("message", Message.class);

        xStream.alias("condition", Condition.class);
        xStream.alias("messagecondition", MessageCondition.class);

        xStream.useAttributeFor(ProcessElement.class,"uuid");

        xStream.registerConverter(new UUIDConverter());
        xStream.processAnnotations(Process.class);
    }


    public String convertToXML(Process p) {
        String xml = xStream.toXML(p);
        return xml;
    }

    public Process readXML(File f) {
        Process p = null;
        String xml = null;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
            xml = new String(encoded, Charset.defaultCharset());
        }
        catch (IOException e) {
//            LogHelper.logError("XMLStore: reading failed");
        }
        if (xml != null) {
            try {
                p = (Process) xStream.fromXML(xml);
            }
            catch (Exception e1) {
//                LogHelper.logError("XMLStore: XML conversion failed");
            }
        }
        if (p!=null) {
            p.reconstructParentRelations();
//            // LogHelper.logDebug("XMLStore: process "+p+" read successfully (original timestamp: "+p.getTimestamp());
        }
        return p;
    }
}
