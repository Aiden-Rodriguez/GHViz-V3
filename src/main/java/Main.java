import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Main class to set up the GUI for the GitHubViz application.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5186
 * @version 1.2
 */
public class Main extends JFrame implements PropertyChangeListener {

    private JTextField urlField;
    private JTextField selectedFileField;
    private JLabel statusLabel;
    private Controller controller;

    public Main() {
        urlField = new JTextField();
        JButton okButton = new JButton("OK");
        JPanel gridPanel = new GridPanel();
        controller = new Controller(urlField);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel(" GitHub Folder URL: "), BorderLayout.WEST);
        top.add(urlField, BorderLayout.CENTER);
        top.add(okButton, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JLabel(" Selected File Name: "), BorderLayout.WEST);
        selectedFileField = new JTextField();
        selectedFileField.setEditable(false);
        selectedFileField.setBackground(Color.WHITE);
        bottom.add(selectedFileField, BorderLayout.CENTER);

        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open from URL...");
        openItem.setActionCommand("Open from URL...");
        openItem.addActionListener(controller);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setActionCommand("Exit");
        exitItem.addActionListener(controller);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu actionMenu = new JMenu("Action");
        JMenuItem reloadItem = new JMenuItem("Reload");
        reloadItem.setActionCommand("Reload");
        reloadItem.addActionListener(controller);
        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.setActionCommand("Clear");
        clearItem.addActionListener(controller);
        actionMenu.add(reloadItem);
        actionMenu.add(clearItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setActionCommand("About");
        aboutItem.addActionListener(controller);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(actionMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(bottom, BorderLayout.NORTH);
        bottomContainer.add(statusLabel, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        okButton.addActionListener(controller);
        okButton.setActionCommand("OK");

        Blackboard.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("selectedFileName")) {
            selectedFileField.setText((String) evt.getNewValue());
        } else if (evt.getPropertyName().equals("statusMessage")) {
            statusLabel.setText(" " + evt.getNewValue());
        } else if (evt.getPropertyName().equals("blackboardCleared")) {
            selectedFileField.setText("");
            statusLabel.setText(" Ready");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.setTitle("Assignment 03");
            main.setSize(800, 600);
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main.setVisible(true);
        });
    }

}