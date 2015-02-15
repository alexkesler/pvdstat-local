package org.kesler.pvdstat.local.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.kesler.pvdstat.local.domain.Cause;


public class NumberedCellFactory implements Callback<TableColumn<Cause,Cause>,TableCell<Cause,Cause>>{
    @Override
    public TableCell<Cause, Cause> call(TableColumn<Cause, Cause> param) {
        return new TableCell<Cause, Cause>(){
            @Override
            protected void updateItem(Cause item, boolean empty) {
                super.updateItem(item, empty);

                if (this.getTableRow()!=null && item != null) {
                    setText(this.getTableRow().getIndex()+ 1 + "");
                } else {
                    setText("");
                }
            }
        };
    }
}
