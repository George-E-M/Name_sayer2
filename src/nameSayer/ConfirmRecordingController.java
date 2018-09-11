package nameSayer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class is used to provide the controller for the ConfirmRecording Scene of the application
 */
public class ConfirmRecordingController implements SetUpController, Initializable{
    @FXML
    private Button cancelButton;
    @FXML
    private Button playButton;
    @FXML
    private Button keepButton;
    @FXML
    private Button redoButton;
    @FXML
    private MediaView mediaView;
    @FXML
    private Button stopButton;
    @FXML
    private ImageView imageView;
    @FXML
    private ProgressBar videoBar;

    private String name;
    private MediaPlayer mediaPlayer;

    /**
     * This method is called when the cancel button is pressed, it confirms the user wants to leave then swaps scenes
     * back to the main menu.
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
     * This method is called when the play button is pressed, it then plays the current recording.
     */
    @FXML
    private void playRecording() {
        //Set up media to play Recording
        setUpPlayButtons();
        mediaView.toFront();
        Media video = new Media(Paths.get( "video.mp4").toUri().toString());
        mediaPlayer = new MediaPlayer(video);
        mediaView.setMediaPlayer(mediaPlayer);
        setUpProgressBar();
        mediaPlayer.play();

        //Controlling button states
        mediaPlayer.setOnPlaying(() ->
        {
            stopButton.setDisable(false);
        });
        mediaPlayer.setOnEndOfMedia(() ->
        {
            finishedPlaying();
        });
        mediaPlayer.setOnStopped(() ->
        {
            finishedPlaying();
        });
    }

    /**
     * Sets the correct button states before playing a video. Invoked in playRecording
     */
    private void setUpPlayButtons() {
        cancelButton.setDisable(true);
        keepButton.setDisable(true);
        redoButton.setDisable(true);
        playButton.setDisable(true);
        videoBar.setVisible(true);
    }

    /**
     * Sets the correct button states once a video is finished or stops. Invoked in playRecording
     */
    private void finishedPlaying() {
        cancelButton.setDisable(false);
        keepButton.setDisable(false);
        redoButton.setDisable(false);
        stopButton.setDisable(true);
        playButton.setDisable(false);
        videoBar.setVisible(false);
    }

    /**
     * Sets up the progressBar to move with the video indicating how long is left. Invoked in playRecording. It does
     * this by running for five seconds, this should be fine as all recordings should only be five seconds.
     */
    private void setUpProgressBar() {
        KeyFrame keyFrameStart = new KeyFrame(Duration.ZERO, new KeyValue(videoBar.progressProperty(), 0));
        KeyFrame keyFrameEnd = new KeyFrame(Duration.seconds(5), new KeyValue(videoBar.progressProperty(), 1));
        Timeline timeLine = new Timeline(keyFrameStart, keyFrameEnd);
        timeLine.play();
        videoBar.toFront();
    }

    /**
     * This method is invoked when the stop button is pressed. Stops the video.
     */
    @FXML
    private void stopPlaying() {
        mediaPlayer.stop();
    }

    /**
     * This method is invoked when the keep button is pressed. This method first moves the combined video into the
     * AVStorage finalising its creation, then it swaps back to the main menu.
     */
    @FXML
    private void keepRecording() {
        //If we are overwriting delete the old file
        File file = new File("AVStorage/" + name + ".mp4");
        if (file.exists()) {
            file.delete();
        }
        try {
            Files.move(Paths.get("video.mp4"), Paths.get("AVStorage/" + name + ".mp4"));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error: Failed to finalise recording");
            alert.setHeaderText("There was an error while finishing the process of your recording");
            alert.setContentText("Please cancel out of the process and try again");

            alert.showAndWait();
        }
        UIManager UIManager = new UIManager();
        UIManager.swapScene("Menu.fxml", keepButton.getScene());
    }

    /**
     * This method is invoked when the redo button is pressed. This methods swaps back to the recording scene.
     */
    @FXML
    private void redoRecording() {
        UIManager UIManager = new UIManager();
        UIManager.swapSceneWithName("Record.fxml", name, redoButton.getScene());
    }

    /**
     * This takes the input name from the RecordController
     * @param name the name of the new recording
     */
    public void setUp(String name) {
        this.name = name;
    }

    /**
     * This method displays a preview of the recording the user is creating.
     */
    private void displayImage() {
        UIManager UIManager = new UIManager();
        UIManager.displayImage(imageView, "video.mp4");
    }

    /**
     * This method initialises the stop button to the desired value and displays the image preview.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stopButton.setDisable(true);
        displayImage();
    }
}
