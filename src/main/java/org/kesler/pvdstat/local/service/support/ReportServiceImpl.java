package org.kesler.pvdstat.local.service.support;

import org.kesler.pvdstat.local.report.Report;
import org.kesler.pvdstat.local.report.support.ApplicantsReport;
import org.kesler.pvdstat.local.service.ReportService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private final List<Report> reports;

    public ReportServiceImpl() {
        reports = new ArrayList<>();
        reports.add(new ApplicantsReport());
    }

    @Override
    public Collection<Report> getAllReports() {
        return reports;
    }
}
