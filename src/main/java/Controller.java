import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller class to handle the action of loading a URL from the text field.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.4
 */
public class Controller implements ActionListener {

    private JTextField urlField;

    public Controller(JTextField field) {
        this.urlField = field;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if ("OK".equals(command) || "Open from URL...".equals(command)) {
            loadUrl();
        } else if ("Reload".equals(command)) {
            reloadUrl();
        } else if ("Clear".equals(command)) {
            clearAll();
        } else if ("Exit".equals(command)) {
            System.exit(0);
        } else if ("About".equals(command)) {
            showAbout();
        }
    }

    private void loadUrl() {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            Blackboard.getInstance().setLoading(true);
            Blackboard.getInstance().loadFromUrl(url);
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a valid GitHub folder URL.",
                    "Invalid URL", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void reloadUrl() {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            Blackboard.getInstance().setLoading(true);
            Blackboard.getInstance().loadFromUrl(url);
        } else {
            JOptionPane.showMessageDialog(null, "No URL to reload. Please enter a URL first.",
                    "No URL", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearAll() {
        Blackboard.getInstance().clear();
        Blackboard.getInstance().setStatusMessage("");
        Blackboard.getInstance().setSelectedFileName("");
        urlField.setText("");
    }

    private void showAbout() {
        String message = "Assignment 04\n" +
                "GitHub Repository Visualizer\n\n" +
                "Authors: Aiden Rodriguez & Brandon Powell\n" +
                "Version 1.4";
        JOptionPane.showMessageDialog(null, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

}