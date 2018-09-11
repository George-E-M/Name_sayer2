package nameSayer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class provides the controller for the main menu of the NameSayer application
 */
public class MenuController implements Initializable {
    @FXML
    private Button createButton;
    @FXML
    private Button playButton;
    @FXML
    private Button deleteButton;
    @FXML
    private ListView<String> listView;
    @FXML
    private Button stopButton;
    @FXML
    private MediaView mediaView;
    @FXML
    private ImageView imageView;
    @FXML
    private Button exitButton;
    @FXML
    private ProgressBar videoBar;

    private ObservableList<String> items;
    private String selectedName;
    private String selectedItem;
    private File selectedFile;
    private MediaPlayer mediaPlayer;

    /**
     * This method is invoked when the play button is pressed. It then plays the currently selected item
     */
    @FXML
    private void playCreation() {
        //Set up all the media to play the video
        initialisePlaying();
        mediaView.toFront();
        Media video = new Media(Paths.get(selectedItem).toUri().toString());
        mediaPlayer =  new MediaPlayer(video);
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
            playingFinished();
        });
        mediaPlayer.setOnStopped(() ->
        {
            playingFinished();
        });
    }

    /**
     * Sets the correct button states before playing a video
     */
    private void initialisePlaying() {
        videoBar.setVisible(true);
        playButton.setDisable(true);
        deleteButton.setDisable(true);
        createButton.setDisable(true);
        listView.setDisable(true);
        mediaView.setVisible(true);
    }

    /**
     * Sets the correct button states after playing a video
     */
    private void playingFinished() {
        videoBar.setVisible(false);
        stopButton.setDisable(true);
        playButton.setDisable(false);
        deleteButton.setDisable(false);
        createButton.setDisable(false);
        listView.setDisable(false);
        imageView.toFront();
        mediaView.toBack();
        mediaView.setVisible(false);
    }

    /**
     * Sets up the ProgressBar to move with the mediaPlayer indicating how long is left. It does this by running
     * for five seconds, this should be fine as all recordings should only be five seconds.
     */
    private void setUpProgressBar() {
        KeyFrame keyFrameStart = new KeyFrame(Duration.ZERO, new KeyValue(videoBar.progressProperty(), 0));
        KeyFrame keyFrameEnd = new KeyFrame(Duration.seconds(5), new KeyValue(videoBar.progressProperty(), 1));
        Timeline timeLine = new Timeline(keyFrameStart, keyFrameEnd);
        timeLine.play();
    }

    /**
     * This method is invoked when the stop button is pressed. Stops the current video.
     */
    @FXML
    private void stopPlaying() {
        mediaPlayer.stop();
    }

    /**
     * This method is invoked when the create button is pressed. Swaps to the creation scene to begin the recording
     * process
     */
    @FXML
    private void createCreation() {
        UIManager UIManager = new UIManager();
        UIManager.swapScene("Create.fxml", createButton.getScene());
    }

    /**
     * This method is invoked when the delete button is pressed
     * Provides an alert to confirm they want to delete. If they want to, the file is deleted and the list gets updated
     */
    @FXML
    private void deleteCreation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recording");
        alert.setHeaderText("Are you sure you want to delete this recording?");
        alert.setContentText(selectedName);

        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            selectedFile.delete();
            //Updates the list
            updateList();
            listView.setItems(items);
            //makes the image empty again
            imageView.setImage(null);
        }
    }

    /**
     * This method is invoked when the exit button is pressed. It first checks they want to exit then exits.
     */
    @FXML
    private void exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit Name Sayer");

        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * Used to update the listView with the files in the AVStorage directory.
     */
    private void updateList() {
        //Gets the files in an array
        File folder = new File("AVStorage/");
        File[] ArrayOfFiles = folder.listFiles();
        //Initialises a list to store the files
        List<String> listOfFiles = new ArrayList<String>();
        if (ArrayOfFiles.length != 0) {
            for (File file : ArrayOfFiles) {
                //Validates the file
                if (file.isFile() && !file.toString().startsWith("AVStorage/.")) {
                    String fileString = file.getName();
                    //Removes extension
                    fileString = fileString.substring(0, fileString.lastIndexOf("."));
                    listOfFiles.add(fileString);
                }
            }
        }
        if(listOfFiles.isEmpty()) {
            //If the list is empty add a placeholder to inform the user
            listOfFiles.add("No Current Recordings");
        }
        items = FXCollections.observableArrayList(listOfFiles);
    }

    /**
     * This method is invoked when the user selects a valid file from the list view(not "No Current Recordings")
     * This method displays a preview of the video
     */
    @FXML
    private void creationSelected() {
        UIManager UIManager = new UIManager();
        UIManager.displayImage(imageView, selectedItem);
    }

    /**
     * Initialises a number of features
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Creates an AVStorage folder if it doesn't exist
        new File("AVStorage").mkdirs();

        //Sets buttons to initial state
        stopButton.setDisable(true);
        playButton.setDisable(true);
        deleteButton.setDisable(true);
        videoBar.setVisible(false);

        updateList();
        listView.setItems(items);

        //Makes the list observable
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectedName = newValue;
                selectedItem = "AVStorage/" + newValue + ".mp4";
                selectedFile = new File(selectedItem);

                //Used to check is "No Current Recordings" was selected
                if (selectedFile.exists()) {
                    creationSelected();
                    playButton.setDisable(false);
                    deleteButton.setDisable(false);
                } else {
                    playButton.setDisable(true);
                    deleteButton.setDisable(true);
                }
            }
        });
    }
}
