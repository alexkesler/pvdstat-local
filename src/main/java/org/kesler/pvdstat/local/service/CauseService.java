package org.kesler.pvdstat.local.service;


import org.kesler.pvdstat.local.domain.Cause;

import java.util.Collection;
import java.util.Date;

public interface CauseService {

    public Collection<Cause> getCausesByDates(Date begDate, Date endDate) throws Exception;
    public void cancel();

}
