package nameSayer;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * This Class is to handle the UI of the application, specifically it swaps scenes and displays image previews.
 */
public final class UIManager {

    /**
     * Nothing is required to be initialised
     */
    public UIManager() {

    }

    /**
     * Swaps from the current scene to the new scene
     * @param newSceneName the scene to swap to, specifies a java fxml file
     * @param currentScene the scene to swap from
     */
    public void swapScene(String newSceneName, Scene currentScene) {
        try {
            Parent createScene = FXMLLoader.load(getClass().getResource(newSceneName));
            Scene newScene = new Scene(createScene, 800, 400);
            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {
            swapFailed();
        }
    }

    /**
     * Swaps from the current scene to the new scene while also passing the recordingName to the new scene
     * @param newSceneName the scene to swap to, specifies a java fxml file
     * @param recordingName the Name of the recording being created
     * @param currentScene the scene to swap from
     */
    public void swapSceneWithName(String newSceneName, String recordingName, Scene currentScene) {
        try {
            FXMLLoader fxmlLoader =  new FXMLLoader(getClass().getResource(newSceneName));
            Parent createScene = fxmlLoader.load();
            //Setupcontroller is a type made to allow this line to work with both Recording and ConfirmRecording
            //controllers
            SetUpController controller = fxmlLoader.getController();
            //Set the name in the new scene
            controller.setUp(recordingName);
            Scene newScene = new Scene(createScene, 800, 400);
            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {
            swapFailed();
        }
    }

    /**
     * This method is used to display a preview of the video that is either selected or being created.
     * @param imageView The view to display the image on
     * @param path The video to get the image from
     */
    public void displayImage(ImageView imageView, String path) {
        //Task to create image
        Task<Void> imageTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                //Allow files with spaces to be read in bash by replacing " " with "\ "
                String newPath = path.replaceAll("\\s", "\\\\ ");
                //Bash process to create image to display
                String cmd = "ffmpeg -y -i " + newPath + " -vf \"select=eq(n\\,0)\" -q:v 3 image.png";
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
                Process process = builder.start();
                process.waitFor();
                return null;
            }
            protected void succeeded() {
                //Creating java image and displaying it
                File file = new File("image.png");
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                imageView.toFront();
            }
            //shows an error if the image can't display
            protected void failed() {
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Error: Couldn't load image");
                alert.setHeaderText("Could not load the selected item image");
                alert.setContentText("The requested image could not be loaded");

                alert.showAndWait();
            }
        };
        //executing the thread
        Thread thread = new Thread(imageTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * This error is displayed if a scene fails to load.
     */
    private void swapFailed() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error: Failed to change scenes");
        alert.setHeaderText("Could not load the requested page");
        alert.setContentText("The requested page could not be found");

        alert.showAndWait();
    }
}
