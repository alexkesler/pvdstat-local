package org.kesler.pvdstat.local.report.support;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.kesler.pvdstat.local.domain.Cause;
import org.kesler.pvdstat.local.domain.cause.Applicant;
import org.kesler.pvdstat.local.report.Report;
import org.kesler.pvdstat.local.util.SpringOptionsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Отчет по количеству принятых заявителей
 */
public class ApplicantsReport extends Report {


    @Override
    public void createReport(Collection<Cause> causes, Date begDate, Date endDate) {

        wb = new SXSSFWorkbook(100);
        branch = SpringOptionsUtil.loadOptions().getProperty("app.branch");
        this.causes = causes;
        this.begDate = begDate;
        this.endDate = endDate;


        Sheet sh = wb.createSheet();

        sh.setColumnWidth(0, 256*5);
        sh.setColumnWidth(1, 256*28);
        sh.setColumnWidth(2, 256*28);
        sh.setColumnWidth(3, 256*15);
        sh.setColumnWidth(4, 256*28);

        // Создаем шапку

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerCellStyle.setFont(font);

        Row headerRow = sh.createRow(0);
        Cell cell = headerRow.createCell(0);
        headerRow.createCell(1);
        cell.setCellValue("Отчет");
        cell.setCellStyle(headerCellStyle);
        headerRow.createCell(2);
        headerRow.createCell(3);
        headerRow.createCell(4);
        CellRangeAddress range = new CellRangeAddress(0,0,0,4);
        sh.addMergedRegion(range);

        headerRow = sh.createRow(1);
        cell = headerRow.createCell(0);
        cell.setCellValue("о принятых заявителях ПК ПВД в филиале: ");
        cell.setCellStyle(headerCellStyle);
        headerRow.createCell(1);
        headerRow.createCell(2);
        headerRow.createCell(3);
        headerRow.createCell(4);
        range = new CellRangeAddress(1,1,0,4);
        sh.addMergedRegion(range);

        headerRow = sh.createRow(2);
        cell = headerRow.createCell(0);
        cell.setCellValue(branch);
        cell.setCellStyle(headerCellStyle);
        headerRow.createCell(1);
        headerRow.createCell(2);
        headerRow.createCell(3);
        headerRow.createCell(4);
        range = new CellRangeAddress(2,2,0,4);
        sh.addMergedRegion(range);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        headerRow = sh.createRow(3);
        cell = headerRow.createCell(0);
        cell.setCellValue("с " + dateFormat.format(begDate) + " по " + dateFormat.format(endDate));
        cell.setCellStyle(headerCellStyle);
        headerRow.createCell(1);
        headerRow.createCell(2);
        headerRow.createCell(3);
        headerRow.createCell(4);
        range = new CellRangeAddress(3,3,0,4);
        sh.addMergedRegion(range);





        int tableRowPos = 5;

        // Создаем заголовок
        Row titleRow = sh.createRow(tableRowPos);

        // Стиль

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);


        cell = titleRow.createCell(0);
        cell.setCellValue("№ п/п");
        cell.setCellStyle(cellStyle);

        cell = titleRow.createCell(1);
        cell.setCellValue("Номер дела (заявления)");
        cell.setCellStyle(cellStyle);

        cell = titleRow.createCell(2);
        cell.setCellValue("Заявитель");
        cell.setCellStyle(cellStyle);

        cell = titleRow.createCell(3);
        cell.setCellValue("Дата регистрации");
        cell.setCellStyle(cellStyle);

        cell = titleRow.createCell(4);
        cell.setCellValue("Действие (услуга)");
        cell.setCellStyle(cellStyle);


        // формируем список заявителей

        List<Applicant> applicants = new ArrayList<>();
        for (Cause cause : causes) {
            applicants.addAll(cause.getApplicants());
        }


        // Заполняем таблицу

        for (int rownum = 0; rownum < applicants.size(); rownum++) {
            Applicant applicant = applicants.get(rownum);
            Row row = sh.createRow(rownum + tableRowPos+1);
            for (int colnum = 0; colnum < 5; colnum++) {
                cell = row.createCell(colnum);
                String value = "";
                switch (colnum) {
                    case 0:
                        value += rownum+1;
                        break;
                    case 1:
                        value = applicant.getCause().getRegnum();
                        break;
                    case 2:
                        value = applicant.getCommonName();
                        break;
                    case 3:
                        value = dateFormat.format(applicant.getCause().getRegDate());
                        break;
                    case 4:
                        value = applicant.getCause().getPurposeString();
                        break;
                    default:
                        break;
                }

                cell.setCellValue(value);

                cell.setCellStyle(cellStyle);
            }
        }

        int footerRowPos = tableRowPos + applicants.size() + 4;

        Row footerRow = sh.createRow(footerRowPos);

        footerRow.createCell(0);
        cell = footerRow.createCell(1);
        cell.setCellValue("Начальник отдела");
        cell = footerRow.createCell(2);
        cell.setCellValue("_____________________");
        cell = footerRow.createCell(3);
        cell.setCellValue("__________");
        cell = footerRow.createCell(4);
        cell.setCellValue(dateFormat.format(new Date()));

//        footerRow = sh.createRow(footerRowPos+2);
//
//        footerRow.createCell(0);
//        cell = footerRow.createCell(1);
//        cell.setCellValue("принял________");
//        footerRow.createCell(2);
//
//        footerRow = sh.createRow(footerRowPos+4);
//
//        footerRow.createCell(0);
//        cell = footerRow.createCell(1);
//        cell.setCellValue("курьер________");
//        footerRow.createCell(2);

    }

    @Override
    public String toString() {
        return "Отчет о принятых заявителях";
    }
}
