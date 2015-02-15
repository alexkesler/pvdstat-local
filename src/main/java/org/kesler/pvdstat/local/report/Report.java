package org.kesler.pvdstat.local.report;


import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.kesler.pvdstat.local.domain.Cause;
import org.kesler.pvdstat.local.util.SpringOptionsUtil;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public abstract class Report {

    protected SXSSFWorkbook wb;

    protected String branch;
    protected Date begDate;
    protected Date endDate;
    protected Collection<Cause> causes;


    public abstract void createReport(Collection<Cause> causes, Date begDate, Date endDate);

    public void save(Window stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для сохранения");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Файл Excel","*.xlsx")
        );
        fileChooser.setInitialFileName(toString() + " " +
                new SimpleDateFormat("dd.MM.yyyy").format(new Date()));

        File file = null;

        file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        String filePath = file.getPath();
        if(filePath.indexOf(".xlsx") == -1) {
            filePath += ".xlsx";
            file = new File(filePath);
        }

        if (file.exists()) {

        }

        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            wb.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }

        //Открытие файла:

        try {
            desktop.open(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
    @Override
    public abstract String toString();
}
