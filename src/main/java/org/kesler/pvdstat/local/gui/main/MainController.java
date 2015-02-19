package org.kesler.pvdstat.local.gui.main;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.controlsfx.dialog.*;
import org.kesler.pvdstat.local.domain.Cause;
import org.kesler.pvdstat.local.domain.cause.Applicant;
import org.kesler.pvdstat.local.gui.AbstractController;
import org.kesler.pvdstat.local.gui.about.AboutController;
import org.kesler.pvdstat.local.gui.options.OptionsController;
import org.kesler.pvdstat.local.report.Report;
import org.kesler.pvdstat.local.service.CauseService;
import org.kesler.pvdstat.local.service.ReportService;
import org.kesler.pvdstat.local.util.DateUtil;
import org.kesler.pvdstat.local.util.FXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

@Component
public class MainController extends AbstractController{
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @FXML protected DatePicker begDatePicker;
    @FXML protected DatePicker endDatePicker;
    @FXML protected Button readButton;
    @FXML protected ProgressIndicator readProgressIndicator;
    @FXML protected ComboBox<Report> reportComboBox;
    @FXML protected Button reportButton;
    @FXML protected TableView<Cause> causeTableView;
    @FXML protected Label taskMessageLabel;
    @FXML protected ProgressIndicator reportProgressIndicator;

    @FXML protected TextField causesCountTextField;
    @FXML protected TextField applicantsCountTextField;
    @FXML protected TextField flApplicantsCountTextField;
    @FXML protected TextField ulApplicantsCountTextField;


    @Autowired
    protected CauseService causeService;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected OptionsController optionsController;

    @Autowired
    protected AboutController aboutController;

    private final ObservableList<Cause> observableCauses = FXCollections.observableArrayList();

    private Task currentTask;

    @FXML
    protected void initialize() {
        SortedList<Cause> sortedCauses = new SortedList<Cause>(observableCauses,new CauseComparator());
        causeTableView.setItems(sortedCauses);
    }

    @Override
    protected void updateContent() {
        Calendar calendar  = new GregorianCalendar();
        calendar.add(Calendar.MONTH,-1);
        begDatePicker.setValue(DateUtil.dateToLocalDate(calendar.getTime()));
        endDatePicker.setValue(DateUtil.dateToLocalDate(new Date()));

        reportComboBox.getItems().clear();
        reportComboBox.getItems().addAll(reportService.getAllReports());
    }

    @FXML protected void handleCloseMenuItemAction(ActionEvent ev) {
        log.info("Close application");
        if (currentTask!=null && currentTask.isRunning()) {
            currentTask.cancel();
        }
        hideStage();
    }

    @FXML protected void handleOptionsMenuItemAction(ActionEvent ev) {
        optionsController.showAndWait(stage);
    }


    @FXML protected void handleAboutMenuItemAction(ActionEvent ev) {
        aboutController.showAndWait(stage);
    }

    @FXML
    protected void handleCauseTableViewMouseClicked(MouseEvent ev) {

    }


    @FXML protected void handleReadButtonAction(ActionEvent ev) {
        read();
    }

    @FXML protected void handleReportButtonAction(ActionEvent ev) {
        report();
    }


    private void read() {
        Date begDate = FXUtils.localDateToDate(begDatePicker.getValue());
        Date endDate = FXUtils.localDateToDate(endDatePicker.getValue());
        currentTask = new CausesReader(begDate,endDate);

        BooleanBinding runningBinding = currentTask.stateProperty().isEqualTo(Task.State.RUNNING);
        readProgressIndicator.visibleProperty().bind(runningBinding);
        readButton.disableProperty().bind(runningBinding);
        begDatePicker.disableProperty().bind(runningBinding);
        endDatePicker.disableProperty().bind(runningBinding);

        reportComboBox.disableProperty().bind(runningBinding);
        reportButton.disableProperty().bind(runningBinding);

        taskMessageLabel.textProperty().bind(currentTask.messageProperty());

        Thread thread = new Thread(currentTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateStat(Collection<Cause> causes) {
        causesCountTextField.setText(causes.size()+"");
        int applicantsCount = 0;
        int applicantsFlCount = 0;
        int applicantsUlCount = 0;
        for (Cause cause : causes) {
            applicantsCount += cause.getApplicants().size();
            for (Applicant applicant : cause.getApplicants()) {
                if (applicant.getType() == Applicant.Type.FL) {
                    applicantsFlCount++;
                } else if (applicant.getType() == Applicant.Type.UL) {
                    applicantsUlCount++;
                }

            }
        }

        applicantsCountTextField.setText(applicantsCount+"");
        flApplicantsCountTextField.setText(applicantsFlCount+"");
        ulApplicantsCountTextField.setText(applicantsUlCount+"");

    }

    private void report() {
        log.info("Handle report action");
        Report report = reportComboBox.getValue();
        if (report != null) {
            log.info("Selected report: " + report);
            Date begDate = DateUtil.localDateToDateBegDay(begDatePicker.getValue());
            Date endDate = DateUtil.localDateToDateEndDay(endDatePicker.getValue());
            currentTask = new Reporter(report, observableCauses, begDate, endDate);

            BooleanBinding runningBinding = currentTask.stateProperty().isEqualTo(Task.State.RUNNING);

            reportProgressIndicator.visibleProperty().bind(runningBinding);
            readButton.disableProperty().bind(runningBinding);
            begDatePicker.disableProperty().bind(runningBinding);
            endDatePicker.disableProperty().bind(runningBinding);

            reportComboBox.disableProperty().bind(runningBinding);
            reportButton.disableProperty().bind(runningBinding);

            taskMessageLabel.textProperty().bind(currentTask.messageProperty());

            Thread thread = new Thread(currentTask);
            thread.setDaemon(true);
            thread.start();
        }
    }


    // Вспомогательные классы

    class CausesReader extends Task<Collection<Cause>> {
        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private Date begDate;
        private Date endDate;

        public CausesReader(Date begDate, Date endDate) {
            this.begDate = begDate;
            this.endDate = endDate;
        }



        @Override
        protected Collection<Cause> call() throws Exception {
            log.info("Getting causes from " + begDate + " to " + endDate);
            updateMessage("Читаем данные из ПК ПВД");
            return causeService.getCausesByDates(begDate, endDate);
        }

        @Override
        protected void succeeded() {
            updateMessage("Готово");
            super.succeeded();
            try {
                Collection<Cause> causes = get();
                observableCauses.clear();
                observableCauses.addAll(causes);
                updateStat(causes);
            } catch (InterruptedException e) {
                log.error("Interrupted",e);
            } catch (ExecutionException e) {
                log.error("Error reading causes: " + e,e);
                Dialogs.create()
                        .owner(stage)
                        .title("Ошибка")
                        .message("Ошибка при чтении данных")
                        .showException(e);
            }
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            causeService.cancel();
        }

        @Override
        protected void failed() {
            updateMessage("Ошибка при чтении данных");
            super.failed();
            Throwable e = getException();
            Dialogs.create()
                    .owner(stage)
                    .title("Ошибка")
                    .message("Ошибка при чтении данных")
                    .showException(e);
        }
    }

    class Reporter extends Task<Void> {
        private final Logger log = LoggerFactory.getLogger(this.getClass());
        private Report report;
        private Collection<Cause> causes;
        private Date begDate;
        private Date endDate;

        public Reporter(Report report, Collection<Cause> causes, Date begDate, Date endDate) {
            this.report = report;
            this.causes = causes;
            this.begDate = begDate;
            this.endDate = endDate;
        }

        @Override
        protected Void call() throws Exception {
            log.info("Preparing report...");
            updateMessage("Заполянем отчет");
            report.createReport(causes,begDate,endDate);
            return null;
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            log.info("Report ready, saving..");
            updateMessage("Сохраняем..");
            report.save(stage);
            log.info("Report saved");
            updateMessage("Готово");
        }

        @Override
        protected void failed() {
            super.failed();
            Throwable e = getException();
            log.error("Error preparing report: "+ e,e);
            Dialogs.create()
                    .owner(stage)
                    .title("Ошибка")
                    .message("Ошибка при формировании отчета")
                    .showException(e);
        }
    }

}
