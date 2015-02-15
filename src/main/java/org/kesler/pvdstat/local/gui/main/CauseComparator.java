package org.kesler.pvdstat.local.gui.main;

import org.kesler.pvdstat.local.domain.Cause;

import java.util.Comparator;

public class CauseComparator implements Comparator<Cause> {
    @Override
    public int compare(Cause o1, Cause o2) {
        return o1.getRegDate().compareTo(o2.getRegDate());
    }

}
