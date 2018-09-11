package nameSayer;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * This class provides the controller for the create scene of the NameSayer application
 */
public class CreateController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private Button nextButton;
    @FXML
    private TextField textField;

    /**
     * This method is invoked when the cancel button is selected. It returns to the menu.
     */
    @FXML
    private void cancel() {
        UIManager UIManager = new UIManager();
        UIManager.swapScene("Menu.fxml", cancelButton.getScene());
    }

    /**
     * This method is invoked when the record button is selected. It begins the swaps to the recording scene after
     * validating the input.
     */
    @FXML
    private void beginRecording() {
        String name = textField.getText();
        if (checkIllegalCharacters()) {
            if (overwriting()) {
                //Alerts the user they are overwriting
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Overwrite Recording");
                alert.setHeaderText("A recording with the name " + name + " already exists");
                alert.setContentText("If you keep this new recording it will be overwritten" +
                        "\nAre you sure you want to continue?");

                Optional<ButtonType> option = alert.showAndWait();

                if (option.get() == ButtonType.OK) {
                    //If they want to overwrite go to record scene
                    UIManager UIManager = new UIManager();
                    UIManager.swapSceneWithName("Record.fxml", name, nextButton.getScene());
                }
            } else {
                UIManager UIManager = new UIManager();
                UIManager.swapSceneWithName("Record.fxml", name, nextButton.getScene());
            }
        }
    }

    /**
     * This method is used to validate the input the user gives, ensuring it fits the requirements
     * @return true if the input is ok.
     */
    private Boolean checkIllegalCharacters() {
        //Checks for atleast one non-space character
        String trimmedInput = textField.getText().trim();
        //Checks for a valid length
        if (trimmedInput.matches("[a-zA-Z0-9_\\s-]+") && textField.getText().length() <= 25) {
            return true;
        } else {
            //Sends an alert if the input is invalid
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Illegal Characters Used");
            alert.setHeaderText("Please use only the allowed characters\nand atleast one non-space character");
            alert.setContentText("The allowed characters are letters,\ndigits, underscores, hyphens and spaces\n" +
                    "Max 25 characters");
            alert.showAndWait();
            return false;
        }
    }

    /**
     * This method is used to check if the user is overwriting a recording
     * @return true if the user is overwriting
     */
    private Boolean overwriting() {
        File folder = new File("AVStorage/");
        File[] ArrayOfFiles = folder.listFiles();
        List<String> listOfFiles = new ArrayList<String>();
        if (ArrayOfFiles.length != 0) {
            for (File file : ArrayOfFiles) {
                if (file.isFile()) {
                    String fileString = file.getName();
                    fileString = fileString.substring(0, fileString.lastIndexOf("."));
                    if (Objects.equals(fileString, textField.getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Binds the next button to the textfield
     * The Button is disabled when the textfield is empty
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BooleanBinding binding = new BooleanBinding() {
            {
                super.bind(textField.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (textField.getText().isEmpty());
            }
        };
        nextButton.disableProperty().bind(binding);
    }
}

