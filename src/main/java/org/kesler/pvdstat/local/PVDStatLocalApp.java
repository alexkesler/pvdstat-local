package org.kesler.pvdstat.local;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.kesler.pvdstat.local.gui.AbstractController;
import org.kesler.pvdstat.local.gui.main.MainController;
import org.kesler.pvdstat.local.gui.options.OptionsController;
import org.kesler.pvdstat.local.util.SpringOptionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Properties;

/**
 * Main entry point for the client-side JavaFX REST application. This loads the JavaFX UI via a Spring-based application
 * context and presents it to the user.
 */
public class PVDStatLocalApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(org.kesler.pvdstat.local.PVDStatLocalApp.class);

    /**
     * Main entry point called when the application starts. This follows the typical JavaFX pattern of delegating
     * straight to the Application.launch method, which then triggers the start() method below.
     *
     * @param args any line arguments passed to the application at startup. This may be from the command line or from
     *             the the launch file if called from Webstart or an Applet, etc.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Main JavaFX initialisation method which is called indirectly by the main() method above on startup. This method
     * loads the user interface from a Spring-based application context and adds it to the provided Stage.
     *
     * @param stage the main Stage (i.e. Window) that the application is to run within.
     */
    public void start(Stage stage) {

        log.info("Starting PVDStat-local application");

        boolean defaultOptions = false;
        Properties options = SpringOptionsUtil.loadOptions();
        if (options.size() == 0) {
            options = SpringOptionsUtil.getDefaultOptions();
            SpringOptionsUtil.saveOptions(options);
            defaultOptions = true;
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PVDStatLocalAppFactory.class);
        MainController mainController = context.getBean(MainController.class);
        mainController.showMain(stage,
                "Статистика ПК ПВД",
                new Image(org.kesler.pvdstat.local.PVDStatLocalApp.class.getResourceAsStream("/images/Bibble.png")),
                800, 700
        );

        if (defaultOptions) {
            OptionsController optionsController = context.getBean(OptionsController.class);
            Dialogs.create()
                    .owner(stage)
                    .title("Внимание")
                    .message("Необходимо провести настройку приложения")
                    .showWarning();
            optionsController.showAndWait(stage);
            if (optionsController.getResult() == AbstractController.Result.CANCEL) {
                Action response = Dialogs.create()
                        .owner(stage)
                        .title("Внимание")
                        .message("Закрываем приложение?")
                        .showConfirm();
                if (response == Dialog.ACTION_YES) {
                    mainController.hideStage();
                }

            }
        }

    }
}
