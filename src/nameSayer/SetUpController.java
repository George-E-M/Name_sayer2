package nameSayer;

/**
 * This interface is used for controllers who need to be passed a name to process new Recordings
 * This Interface is used to avoid duplicate code in the switching of scenes.
 */
public interface SetUpController {
    /**
     * This method is used to provide the controllers with the means to take a name input from another scene
     * @param name the name of the Recording
     */
    public void setUp(String name);
}
