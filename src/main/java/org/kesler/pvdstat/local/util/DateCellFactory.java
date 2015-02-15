package org.kesler.pvdstat.local.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.kesler.pvdstat.local.domain.Cause;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateCellFactory implements Callback<TableColumn<Cause,Date>,TableCell<Cause,Date>> {
    @Override
    public TableCell<Cause, Date> call(TableColumn<Cause, Date> param) {
        return new TableCell<Cause, Date>(){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);

                if ( item != null) {
                    setText(simpleDateFormat.format(item));
                } else {
                    setText("");
                }
            }
        };
    }
}
