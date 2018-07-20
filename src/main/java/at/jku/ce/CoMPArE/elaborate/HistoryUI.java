package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.storage.DownloadWindow;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
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
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by oppl on 16/01/2017.
 */
public class HistoryUI extends Window {

    ProcessChangeHistory processChangeHistory;
    Table table;
    VerticalLayout layout;

    ProcessChangeTransaction selectedTransaction;

    public HistoryUI(ProcessChangeHistory processChangeHistory) {
        super("History of process changes");
        this.processChangeHistory = processChangeHistory;
        this.center();
        this.setWidth("90%");
        layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setMargin(true);
        layout.setSpacing(true);
        Label description = new Label("<p>Below you see an overview about all changes made to the process with the most current on top.</p>" +
                "<p>You can undo them in reverse order. This means that all changes that have been made after the selected " +
                "change will also be undone to assure consistency of the process.</p><p><b>Undo is permanent, changes cannot " +
                "be re-done automatically.</b></p>",ContentMode.HTML);
        buildTable();
        layout.addComponent(description);
        layout.addComponent(table);
        Button close = new Button("Close");
        close.addClickListener( e-> {
            this.close();
        });
        layout.addComponent(close);
        setContent(layout);
        this.selectedTransaction = null;
    }

    private void buildTable() {

        if (table != null) layout.removeComponent(table);

        table = new Table("Process Change History");
        table.addContainerProperty("ChangeID", Integer.class, null);
        table.addContainerProperty("ChangeName", Label.class, null);
        table.addContainerProperty("ButtonRollback",  Button.class, null);
        table.setWidth("90%");

        table.setColumnWidth("ChangeID",50);
        table.setColumnWidth("ButtonRollback",250);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.setColumnAlignment("ChangeID", Table.Align.CENTER);
        table.setColumnAlignment("ButtonRollback", Table.Align.CENTER);

        int itemID = processChangeHistory.getHistory().size();

        for (ProcessChangeTransaction transaction: processChangeHistory.getHistory()) {

            Button rollbackButton = new Button("undo this and all above");
            if (itemID == processChangeHistory.getHistory().size()) rollbackButton = new Button("undo this");
            rollbackButton.addClickListener( e-> {
                selectedTransaction = transaction;
                this.close();
            });

            table.addItem(new Object[]{
                    new Integer(itemID),
                    new Label(transaction.toString(), ContentMode.HTML),
                    rollbackButton
            }, itemID);
//                // LogHelper.logDebug("ResultView: added "+result.getName());
            itemID--;
        }
        table.setPageLength(table.size());
    }

    public ProcessChangeTransaction getSelectedTransaction() {
        return selectedTransaction;
    }
}
