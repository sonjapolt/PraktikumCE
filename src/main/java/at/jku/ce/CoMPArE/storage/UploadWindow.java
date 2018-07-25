package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.ProcessSelectorUI;
import at.jku.ce.CoMPArE.process.Process;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.easyuploads.MultiFileUpload;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by oppl on 15/01/2017.
 */
public class UploadWindow extends Window {

    GridLayout gLayout = new GridLayout(2,5);
    Vector<Process> processes;
    FileStorageHandler fileStorageHandler = new FileStorageHandler();
    Label uploadedProcesses;

    public UploadWindow(ProcessSelectorUI manager) {
        super("Upload a new process");

        processes = new Vector<>();
        fileStorageHandler = new FileStorageHandler();

        uploadedProcesses = new Label("");
        uploadedProcesses.setVisible(false);

        this.center();
        gLayout.setWidth("100%");
        this.setWidth("500px");

        Label titleLabel = new Label("<b>Upload new Items</b>", ContentMode.HTML);
        Label descrLabel = new Label("Please select files or drop them below.<p/>If you upload a single process file, " +
                "the process will be loaded. If you upload a zip-archive or a number of process files representing different " +
                "evolution steps of a process, the process history will be stored on the server an the latest process version " +
                "will be loaded.", ContentMode.HTML);

        MultiFileUpload multiFileUpload = new MultiFileUpload() {
            @Override
            protected void handleFile(File file, String filename, String mimeType, long length) {
                if (filename.endsWith(".zip")) handleZIPFile(file,filename);
                if (filename.endsWith(".xml")) handleXMLFile(file,filename);
            }
        };

        Button close = new Button("Confirm to use uploaded files");
        close.addClickListener( e -> {
            if (!fileStorageHandler.isIDCookieAvailable()) {
                GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
                groupIDEntryWindow.addCloseListener( e1 -> {
                    fileStorageHandler.saveToServer();
                    manager.setSelectedProcess(getLatestProcess());
                });
                this.getUI().addWindow(groupIDEntryWindow);
            }
            else {
                fileStorageHandler.saveToServer();
                manager.setSelectedProcess(getLatestProcess());
            }
            this.close();
        });

        gLayout.addComponent(titleLabel,0,0,1,0);
        gLayout.addComponent(descrLabel,0,1,1,1);
        gLayout.addComponent(multiFileUpload,0,2,1,2);
        gLayout.addComponent(uploadedProcesses,0,3,1,3);
        gLayout.addComponent(close,0,4);

        gLayout.setRowExpandRatio(1,1);

        gLayout.setMargin(true);
        gLayout.setSpacing(true);
        setContent(gLayout);
    }

    private void handleZIPFile(File file, String filename) {
        Set<File> unzippedFiles = unzipFile(file);
        XMLStore xmlStore = new XMLStore();
        for (File f: unzippedFiles) {
            Process p = xmlStore.readXML(f);
            if (p == null) continue;
            processes.add(p);
            fileStorageHandler.addProcessToStorageBuffer(p);
        }
        updateUploadedProcessLabel();
    }

    private void handleXMLFile(File file, String filename) {
        XMLStore xmlStore = new XMLStore();
        Process p = xmlStore.readXML(file);
        if (p == null) return;
        processes.add(p);
        fileStorageHandler.addProcessToStorageBuffer(p);
        updateUploadedProcessLabel();
    }

    private Process getLatestProcess() {
        Process latest = null;
        for (Process p:processes) {
            if (latest == null) latest = p;
            if (!latest.getTimestamp().after(p.getTimestamp())) latest = p;
        }
        return latest;
    }

    private Set<File> unzipFile(File zipFile) {
        Set<File> unzippedFiles = new HashSet<>();
        byte[] buffer = new byte[2048];
        try {
            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
//                // LogHelper.logDebug("extracting file "+ze.getName());
                String fileName = ze.getName();
                if (!fileName.endsWith(".xml")) {
                    ze = zis.getNextEntry();
                    continue;
                }
                File newFile = File.createTempFile("VirtualEnactmentUpload",".xml");

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                unzippedFiles.add(newFile);
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return unzippedFiles;
    }

    void updateUploadedProcessLabel() {
        gLayout.removeComponent(uploadedProcesses);
        StringBuffer uploadList = new StringBuffer();
        uploadList.append("Uploaded Processes:<ul>");
        int i = 0;
        for (Process p:processes) {
            if (i == 5) {
                uploadList.append("<li>and "+(processes.size()-5)+" more ...</li>");
                break;
            }
            uploadList.append("<li>");
            uploadList.append(p.toString());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            LocalDateTime timestamp = LocalDateTime.ofInstant(p.getTimestamp().toInstant(), ZoneId.of("GMT+1"));
            uploadList.append(" "+dtf.format(timestamp));
            uploadList.append("</li>");
            i++;
        }
        uploadList.append("</ul>");
        uploadedProcesses = new Label(uploadList.toString(),ContentMode.HTML);
        gLayout.addComponent(uploadedProcesses,0,3,1,3);
        this.center();
    }
}
