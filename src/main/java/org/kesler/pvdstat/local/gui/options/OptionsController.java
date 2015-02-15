package org.kesler.pvdstat.local.gui.options;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.controlsfx.dialog.Dialogs;
import org.kesler.pvdstat.local.gui.AbstractController;
import org.kesler.pvdstat.local.util.SpringOptionsUtil;

import java.util.Properties;
import java.util.StringJoiner;


public class OptionsController extends AbstractController {

    @FXML protected TextField branchTextField;
    @FXML protected TextField pvdIpTextField;
    @FXML protected TextArea pvdBooksTextArea;

    private Properties options;


    @Override
    public void showAndWait(Window owner) {
        options = SpringOptionsUtil.loadOptions();
        if (options.size() == 0) {
            options = SpringOptionsUtil.getDefaultOptions();
        }

        super.showAndWait(owner,"Настройки");
    }

    @Override
    protected void updateContent() {

        String branch = options.getProperty("app.branch");
        branchTextField.setText(branch==null?"":branch);

        String pvdIp = options.getProperty("pvd.ip");
        pvdIpTextField.setText(pvdIp==null?"":pvdIp);

        String pvdBooks = options.getProperty("pvd.books");
        if (pvdBooks!=null && !pvdBooks.isEmpty()) {
            String linedPvdBooks = pvdBooks.replaceAll(",","\n");
            pvdBooksTextArea.setText(linedPvdBooks);
        } else {
            pvdBooksTextArea.setText("");
        }
    }

    @Override
    protected void updateResult() {
        String branch = branchTextField.getText();
        options.setProperty("app.branch",branch==null?"":branch);

        String pvdIp = pvdIpTextField.getText();
        options.setProperty("pvd.ip",pvdIp==null?"":pvdIp);

        String linedPvdBooks = pvdBooksTextArea.getText();
        String pvdBooks = "";
        if (!linedPvdBooks.isEmpty()) {
            String[] booksArray = linedPvdBooks.split("\n");

            StringJoiner booksJoiner = new StringJoiner(",","","");
            for (String book : booksArray) {
                String clearBook = book.trim();
                if (!clearBook.isEmpty()) {
                    booksJoiner.add(clearBook);
                }
            }
            pvdBooks = booksJoiner.toString();
        }
        options.setProperty("pvd.books", pvdBooks);

        SpringOptionsUtil.saveOptions(options);

        Dialogs.create()
                .owner(stage)
                .title("Внимание")
                .message("Для применения настроек перезапустите приложение")
                .showWarning();


    }
}
