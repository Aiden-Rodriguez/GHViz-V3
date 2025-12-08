import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class to handle the action of loading a URL from the text field.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.5
 */
public class Controller implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private JTextField urlField;

    public Controller(JTextField field) {
        this.urlField = field;
        logger.info("Controller initialized");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        logger.debug("Action performed: {}", command);

        if ("OK".equals(command) || "Open from URL...".equals(command)) {
            loadUrl();
        } else if ("Reload".equals(command)) {
            reloadUrl();
        } else if ("Clear".equals(command)) {
            clearAll();
        } else if ("Exit".equals(command)) {
            exitApplication();
        } else if ("About".equals(command)) {
            showAbout();
        }
    }

    private void loadUrl() {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            logger.info("Loading URL: {}", url);
            Blackboard.getInstance().setStatusMessage("Loading from URL...");
            Blackboard.getInstance().setLoading(true);
            Blackboard.getInstance().loadFromUrl(url);
        } else {
            logger.warn("Attempted to load empty URL");
            Blackboard.getInstance().setStatusMessage("Error: No URL provided");
            JOptionPane.showMessageDialog(null, "Please enter a valid GitHub folder URL.",
                    "Invalid URL", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void reloadUrl() {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            logger.info("Reloading URL: {}", url);
            Blackboard.getInstance().setStatusMessage("Reloading...");
            Blackboard.getInstance().setLoading(true);
            Blackboard.getInstance().loadFromUrl(url);
        } else {
            logger.warn("Attempted to reload with no URL");
            Blackboard.getInstance().setStatusMessage("Error: No URL to reload");
            JOptionPane.showMessageDialog(null, "No URL to reload. Please enter a URL first.",
                    "No URL", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearAll() {
        logger.info("Clearing all data");
        Blackboard.getInstance().clear();
        Blackboard.getInstance().setSelectedFileName("");
        urlField.setText("");
        Blackboard.getInstance().setStatusMessage("Ready");
    }

    private void exitApplication() {
        logger.info("Application exit requested");
        Blackboard.getInstance().setStatusMessage("Exiting...");
        System.exit(0);
    }

    private void showAbout() {
        logger.debug("Showing about dialog");
        Blackboard.getInstance().setStatusMessage("About dialog opened");
        String message = "Assignment 04\n" +
                "GitHub Repository Visualizer\n\n" +
                "Authors: Aiden Rodriguez & Brandon Powell\n" +
                "Version 1.4\n\n";
        JOptionPane.showMessageDialog(null, message, "About", JOptionPane.INFORMATION_MESSAGE);
        Blackboard.getInstance().setStatusMessage("Ready");
    }
}