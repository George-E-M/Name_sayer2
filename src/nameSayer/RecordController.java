package nameSayer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class is used to provide the controller for the record scene of the application
 */
public class RecordController implements Initializable, SetUpController {
    @FXML
    private Button cancelButton;
    @FXML
    private Button recordButton;
    @FXML
    private ProgressIndicator indicator;
    @FXML
    private Button nextButton;
    @FXML
    private Label label;

    private String name;
    private Task<Void> recordTask;

    /**
     * This method is invoked if the cancel button is pressed. It asks the user if they wish to continue then returns
     * to the main menu.
     */
    @FXML
    private void cancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Confirmation");
        alert.setHeaderText("If you cancel now you will lose all your progress");
        alert.setContentText("Are you sure you wish to continue");

        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            UIManager UIManager = new UIManager();
            UIManager.swapScene("Menu.fxml", cancelButton.getScene());
        }
    }

    /**
     * This method is invoked if the next button is pressed. It swaps to the confirm recording scene.
     */
    @FXML
    private void next() {
        UIManager UIManager = new UIManager();
        UIManager.swapSceneWithName("ConfirmRecording.fxml", name, nextButton.getScene());
    }

    /**
     * This method is invoked if the record button is pressed. It begins the recording process.
     */
    @FXML
    private void record() {
        recordButton.setDisable(true);
        recordTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                //Record Bash process
                String recordCMD = "ffmpeg -y -f alsa -loglevel quiet -t 5 -i default audio.wav";
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", recordCMD);
                Process process = builder.start();
                process.waitFor();
                return null;
            }
            protected void succeeded() {
                label.setText("Select Next to continue, processing may take a couple of seconds. You may cancel at any " +
                        "time\n\n\t\tIf you want to redo your recording you can do so from the next page");
                processRecording();
            }
        };
        Thread thread = new Thread(recordTask);
        thread.setDaemon(true);
        startIndicator();
        thread.start();
    }

    /**
     * This method is invoked once the user is finished recording, it creates the video then merges the video with
     * the audio to get the combined video.
     */
    private void processRecording() {
        Task<Void> createVideo = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                buildVideo();
                combineVideo();
                return null;
            }
            protected void succeeded() {
                nextButton.setDisable(false);
            }
            @Override
            protected void failed() {
                //Alerts the user if the processing fails
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Error: Couldn't process Recording");
                alert.setHeaderText("Could not combine the video");

                alert.showAndWait();
            }
        };
        Thread thread = new Thread(createVideo);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * This method builds the video with the name on it.
     * @throws Exception
     */
    private void buildVideo() throws Exception{
        //Create video bash process
        String cmd = "ffmpeg -y -f lavfi -i color=c=black:s=400x300:d=5 -loglevel quiet -vf drawtext=\"fontfile=/usr"
                + "/shar/fonts/truetype/liberation/LiberationSans-Regular.ttf:fontsize=18:fontcolor=white:"
                + "x=(w-text_w)/2:y=(h-text_h)/2:text='" + name + "'\" video.mp4";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
        Process process = builder.start();
        process.waitFor();
    }

    /**
     * This method combines the video and audio to give the final recording video.
     * @throws Exception
     */
    private void combineVideo() throws Exception {
        //Combine the audio and video bash process.
        String combineCMD = "ffmpeg -y -loglevel quiet -i video.mp4 -i audio.wav -acodec aac -shortest -strict -2 " +
                "video.mp4";
        ProcessBuilder combinationBuilder = new ProcessBuilder("/bin/bash", "-c", combineCMD);
        Process process = combinationBuilder.start();
        process.waitFor();
    }

    /**
     * This method starts the ProgresssIndicator in time with the recording to show the user how long they have to
     * record.
     */
    private void startIndicator() {
        KeyFrame keyFrameStart = new KeyFrame(Duration.ZERO, new KeyValue(indicator.progressProperty(), 0));
        KeyFrame keyFrameEnd = new KeyFrame(Duration.seconds(5), new KeyValue(indicator.progressProperty(), 1));
        Timeline timeLine = new Timeline(keyFrameStart, keyFrameEnd);
        timeLine.play();
    }

    /**
     * This takes the input name from the createController
     * @param name the name of the new recording
     */
    public void setUp(String name) {
        this.name = name;
    }

    /**
     * Sets the next button to it's initial state, disabled
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nextButton.setDisable(true);
    }
}
