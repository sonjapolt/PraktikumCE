package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by oppl on 15/01/2017.
 */
public class DownloadWindow extends Window {

        FileStorageHandler manager;
        Table table;
        VerticalLayout layout;


        public DownloadWindow(FileStorageHandler manager) {
            super("Download process files");
            this.center();
            this.setWidth("90%");
            layout = new VerticalLayout();
            layout.setWidth("100%");
            layout.setMargin(true);
            layout.setSpacing(true);
            this.manager = manager;
            buildTable();
            layout.addComponent(table);
            Button close = new Button("Close");
            close.addClickListener( e-> {
                this.close();
            });
            layout.addComponent(close);
            setContent(layout);

        }

        private void buildTable() {

            List<File> resultFiles = loadResults(new File(CoMPArEUI.CoMPArEServlet.getResultFolderName()),manager.getGroupID());
            if (table != null) layout.removeComponent(table);

            table = new Table("Available Results");
            table.addContainerProperty("ResultName", String.class, null);
            table.addContainerProperty("ResultDate", String.class, null);
            table.addContainerProperty("ButtonShow",  Button.class, null);
            table.addContainerProperty("ButtonDownload",  Button.class, null);
            table.addContainerProperty("SortDate",Long.class,null);
            table.setSortContainerPropertyId("SortDate");
            table.setSortAscending(false);
            table.setWidth("90%");

            table.setColumnWidth("ResultDate",200);
            table.setColumnWidth("ButtonShow",150);
            table.setColumnWidth("ButtonDownload",150);
            table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
            table.setColumnAlignment("ButtonShow", Table.Align.CENTER);
            table.setColumnAlignment("ButtonDownload", Table.Align.CENTER);

            int itemID = 0;
  //          // LogHelper.logDebug("ResultView: "+resultFiles.size()+" results available.");

            for (File result: resultFiles) {

                Button showButton = new Button("show content");
                showButton.addClickListener( e-> {
                    this.getUI().addWindow(new ShowFolderContentWindow(result));
                });


                Button downloadButton = new Button ("download");

                StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
                    @Override
                    public InputStream getStream() {
                        List<File> requestedFiles = new LinkedList<File>();
                        assert result.exists() && result.isDirectory();

                        File[] files = result.listFiles(f -> f.isFile()
                                && f.getName().endsWith(".xml"));

                        requestedFiles = Arrays.asList(files);
                        try {
                            BufferedInputStream origin = null;
                            final int BUFFER = 2048;
                            byte data[] = new byte[BUFFER];
                            File zipFile = new File(new File(CoMPArEUI.CoMPArEServlet.getResultFolderName()),result.getName()+".zip");
                            FileOutputStream dest = new FileOutputStream(zipFile);
                            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                            for (File f : requestedFiles) {
                                FileInputStream fi = new FileInputStream(f);
                                origin = new BufferedInputStream(fi, BUFFER);
                                ZipEntry entry = new ZipEntry(f.getName());
                                out.putNextEntry(entry);
                                int count;
                                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                                    out.write(data, 0, count);
                                    out.flush();
                                }
                            }
                            origin.close();
                            out.flush();
                            out.close();
                            ByteArrayInputStream zipstream = new ByteArrayInputStream(FileUtils.readFileToByteArray(zipFile));
                            return zipstream;
                        }
                        catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        return null;
                    }
                }, result.getName()+".zip");

                FileDownloader fd = new FileDownloader(resource);
                fd.extend(downloadButton);

                BasicFileAttributes attr = null;
                try {
                    attr = Files.readAttributes(result.toPath(), BasicFileAttributes.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (attr == null) continue;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                LocalDateTime creationTime = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.of("GMT+1"));
                table.addItem(new Object[]{
                        result.getName(),
                        dtf.format(creationTime),
                        showButton,
                        downloadButton,
                        new Long(attr.lastModifiedTime().toMillis())
                }, itemID);
//                // LogHelper.logDebug("ResultView: added "+result.getName());
                itemID++;
            }
            table.sort();
            table.setVisibleColumns(new String[] { "ResultName", "ResultDate", "ButtonShow", "ButtonDownload"});
            table.setPageLength(table.size());
        }

        public List<File> loadResults(File containingFolder, String groupID) {

            assert containingFolder.exists() && containingFolder.isDirectory();

            File[] files = containingFolder.listFiles(f -> f.isDirectory()
                    && f.getName().startsWith(groupID));

//            // LogHelper.logDebug("Found "+files.length+" results");
            return Arrays.asList(files);
        }

        private class ShowFolderContentWindow extends Window {

            public ShowFolderContentWindow(File folder) {
                super("Contained Process Files");
                VerticalLayout vLayout = new VerticalLayout();
                this.center();
                this.setWidth("500px");
                vLayout = new VerticalLayout();
                vLayout.setWidth("100%");
                vLayout.setMargin(true);
                vLayout.setSpacing(true);
                vLayout.addComponent(createTable(folder));
                Button close = new Button("Close");
                close.addClickListener( e-> {
                    this.close();
                });
                vLayout.addComponent(close);
                setContent(vLayout);

            }

            private Table createTable(File folder) {
                Table table = new Table("Contained Results");
                table.addContainerProperty("ResultName", String.class, null);
                table.addContainerProperty("SortDate",Long.class,null);
                table.setSortContainerPropertyId("SortDate");
                table.setSortAscending(false);
                table.setWidth("90%");

                table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);

                assert folder.exists() && folder.isDirectory();

                File[] files = folder.listFiles(f -> f.isFile()
                        && f.getName().endsWith(".xml"));

//                // LogHelper.logDebug("Found "+files.length+" results");
                List<File> resultFiles = Arrays.asList(files);

                int itemID = 0;
//                // LogHelper.logDebug("ResultView: "+resultFiles.size()+" results available.");

                for (File result: resultFiles) {

                    BasicFileAttributes attr = null;
                    try {
                        attr = Files.readAttributes(result.toPath(), BasicFileAttributes.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (attr == null) continue;
                    table.addItem(new Object[]{
                            result.getName(),
                            new Long(attr.lastModifiedTime().toMillis())
                    }, itemID);
//                    // LogHelper.logDebug("ResultView: added "+result.getName());
                    itemID++;
                }
                table.sort();
                table.setVisibleColumns(new String[] { "ResultName"});
                table.setPageLength(Math.min(table.size(),5));

                return table;
            }

        }
}
