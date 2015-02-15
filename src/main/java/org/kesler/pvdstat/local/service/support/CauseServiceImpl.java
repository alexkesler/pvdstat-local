package org.kesler.pvdstat.local.service.support;


import org.kesler.pvdstat.local.domain.Cause;
import org.kesler.pvdstat.local.domain.cause.Applicant;
import org.kesler.pvdstat.local.domain.cause.applicant.ApplicantFL;
import org.kesler.pvdstat.local.domain.cause.applicant.ApplicantUL;
import org.kesler.pvdstat.local.domain.cause.applicant.FL;
import org.kesler.pvdstat.local.domain.cause.applicant.UL;
import org.kesler.pvdstat.local.service.CauseService;
import org.kesler.pvdstat.local.util.OracleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Component
public class CauseServiceImpl implements CauseService {
    private static final Logger log = LoggerFactory.getLogger(CauseServiceImpl.class);

    private final String serverIp;
    private final String serverUser;
    private final String serverPassword;
    private final String books;

    private boolean cancelled = false;

    public CauseServiceImpl(String serverIp, String books) {
        this.serverIp = serverIp;
        this.serverUser = "admin";
        this.serverPassword = "admin";
        this.books = books;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public Collection<Cause> getCausesByDates(Date begDate, Date endDate) throws Exception{

        Map<String,Cause> causesMap = new HashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy", Locale.US);

        String andDateSqlString = "";
        if (begDate!=null) {
            andDateSqlString= " and RBI.REGDATE >= '" +  dateFormat.format(begDate) + "' ";
        }
        if (endDate!=null) {
            andDateSqlString += " and RBI.REGDATE <= '" +  dateFormat.format(endDate) + "' ";
        }

        String andBooksSqlString = "";
        if (books != null && !books.isEmpty()) {
            String[] booksArray = books.split(",");
            StringJoiner booksJoiner = new StringJoiner("','","'","'");
            for (String book : booksArray) {
                booksJoiner.add(book);
            }
            andBooksSqlString = " and RB.NUM in (" + booksJoiner.toString() +")";
        }


        String causesQuery = "SELECT RBI.ID_CAUSE, RBI.REGNUM, RBI.REGDATE, C.PURPOSE, " +
                "C.STATE, C.STATUSMD " +
                "from DPS$RECBOOKITEM RBI " +
                "join DPS$D_CAUSE C on C.ID = RBI.ID_CAUSE " +
                "join DPS$RECBOOK RB on RB.ID = RBI.ID_RECBOOK " +
                "where RBI.FROMCAUSE=1" + andDateSqlString + andBooksSqlString;


        String applicantsQuery = "SELECT A.ID_CAUSE, SA.ID_CLSTYPE AS A_CLSTYPE, " +
                "SA.SURNAME AS A_SURNAME, SA.FIRSTNAME AS A_FIRSTNAME, SA.PATRONYMIC AS A_PATRONYMIC, SA.SHORTNAME AS A_SHORTNAME, " +
                "SR.ID AS R_ID, SR.SURNAME AS R_SURNAME, SR.FIRSTNAME AS R_FIRSTNAME, SR.PATRONYMIC AS R_PATRONYMIC " +
                "FROM DPS$D_CAUSE C " +
                "LEFT JOIN DPS$APPLICANT A ON A.ID_CAUSE=C.ID " +
                "LEFT JOIN DPS$SUBJECT SA ON SA.ID=A.ID_SUBJECT " +
                "LEFT JOIN DPS$SUBJECT SR ON SR.ID=A.ID_AGENT " +
                "JOIN DPS$RECBOOKITEM RBI ON RBI.ID_CAUSE=C.ID " +
                "join DPS$RECBOOK RB on RB.ID = RBI.ID_RECBOOK " +
                "where RBI.FROMCAUSE=1 " + andDateSqlString + andBooksSqlString;


        log.info("Reading causes from server ");

        log.debug("Connecting >>>");
        Connection conn;
        try {
            conn = OracleUtil.createConnection(serverIp, serverUser, serverPassword);
        } catch (SQLException e) {
            log.error("Error connecting server " + serverIp + ": " + e, e);
            throw new RuntimeException("Error connecting PVD server " ,e);
        } catch (ClassNotFoundException e) {
            log.error("Oracle driver not registered: " + e, e);
            throw new RuntimeException(e);
        }
        log.debug("Connected.");

        try {

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(causesQuery)) {
                    log.debug("Processing causes >>>");
                    processCausesRs(rs, causesMap);
                    log.debug("Processing causes complete");
                }
            }


            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(applicantsQuery)) {
                    log.debug("Processing applicants >>>");
                    processApplicantsRs(rs, causesMap);
                    log.debug("Processing applicants complete");
                }

            }


        } catch (SQLException sqle) {
            log.error("Error getting cause from server " + sqle, sqle);
            throw new Exception(sqle);
        } finally {
            log.debug("Closing connection");
            OracleUtil.closeConnection(conn);
        }

        return causesMap.values();
    }

    private void processCausesRs(ResultSet rs, Map<String,Cause> causesMap) throws SQLException, InterruptedException {
        if (rs==null) return;

        while (rs.next()) {
            if (cancelled) {
                log.warn("Cancelled");
                throw new InterruptedException("Cancelled");
            }
            Cause cause = new Cause();

            cause.setCauseId(rs.getString("ID_CAUSE"));
            cause.setRegnum(rs.getString("REGNUM"));
            cause.setRegDate(rs.getDate("REGDATE"));
            cause.setPurpose(rs.getInt("PURPOSE"));
            cause.setState(rs.getInt("STATE"));
            cause.setStatusMd(rs.getString("STATUSMD"));

            causesMap.put(cause.getCauseId(), cause);

        }

    }


    private void processApplicantsRs(ResultSet rs, Map<String,Cause> causesMap) throws SQLException, InterruptedException {

        while (rs.next()) {
            if (cancelled) {
                log.warn("Cancelled");
                throw new InterruptedException("Cancelled");
            }

            String clstype = rs.getString("A_CLSTYPE");
            if (clstype == null) {
                continue;
            }
            String cls = clstype.substring(0,3);

            Applicant applicant=null;

            FL repres = null;
            String represId = rs.getString("R_ID");
            if (represId != null) {
                repres = new FL();

                String represSurName = rs.getString("R_SURNAME");
                String represFirstName = rs.getString("R_FIRSTNAME");
                String represPatronymic = rs.getString("R_PATRONYMIC");

                repres.setSurName(represSurName);
                repres.setFirstName(represFirstName);
                repres.setParentName(represPatronymic);
            }


            if (cls.equals("7.3")) { // физ лицо
                ApplicantFL applicantFL = new ApplicantFL();
                FL subject = new FL();
                String subjectSurName = rs.getString("A_SURNAME");
                String subjectFirstName = rs.getString("A_FIRSTNAME");
                String subjectPatronymic = rs.getString("A_PATRONYMIC");

                subject.setFirstName(subjectFirstName);
                subject.setParentName(subjectPatronymic);
                subject.setSurName(subjectSurName);

                applicantFL.setFl(subject);

                applicantFL.setRepres(repres);

                applicant = applicantFL;
            } else if (cls.equals("7.1") || cls.equals("7.2")) { // юр лицо или огв
                ApplicantUL applicantUL = new ApplicantUL();

                UL subject = new UL();
                String subjectShortName = rs.getString("A_SHORTNAME");
                subject.setShortName(subjectShortName);

                applicantUL.setUl(subject);

                applicantUL.setRepres(repres);

                applicant = applicantUL;
            }

            String causeId = rs.getString("ID_CAUSE");

            Cause cause = causesMap.get(causeId);
            if (cause != null && applicant != null) {
                cause.getApplicants().add(applicant);
                applicant.setCause(cause);
            }
        }

    }


}
