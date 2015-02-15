package org.kesler.pvdstat.local.util;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.kesler.pvdstat.local.domain.Cause;


public class NumberedCellValueFactory implements Callback<TableColumn.CellDataFeatures<Cause,Cause>,ObservableValue<Cause>> {
    @Override
    public ObservableValue<Cause> call(TableColumn.CellDataFeatures<Cause, Cause> param) {
        return new ReadOnlyObjectWrapper<Cause>(param.getValue());
    }
}
