package org.kesler.pvdstat.local.service;

import org.kesler.pvdstat.local.report.Report;

import java.util.Collection;

/**
 * Created by alex on 13.02.15.
 */
public interface ReportService {
    public Collection<Report> getAllReports();
}
