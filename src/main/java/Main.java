import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Main class to set up the GUI for the GitHubViz application.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.3
 */
public class Main extends JFrame implements PropertyChangeListener {

    private JTextField urlField;
    private JLabel statusLabel;
    private Controller controller;

    public Main() {
        urlField = new JTextField();
        JButton okButton = new JButton("OK");
        controller = new Controller(urlField);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel(" GitHub Folder URL: "), BorderLayout.WEST);
        top.add(urlField, BorderLayout.CENTER);
        top.add(okButton, BorderLayout.EAST);

        FileTreePanel fileTreePanel = new FileTreePanel();

        JTabbedPane tabbedPane = new JTabbedPane();

        GridPanel gridPanel = new GridPanel();
        tabbedPane.addTab("Grid", gridPanel);

        MetricsPanel metricsPanel = new MetricsPanel();
        tabbedPane.addTab("Metrics", metricsPanel);

        // Diagram tab (placeholder)
        JPanel diagramPanel = new JPanel();
        diagramPanel.setBackground(Color.WHITE);
        tabbedPane.addTab("Diagram", diagramPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreePanel, tabbedPane);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.0);
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

        // Layout
        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        okButton.addActionListener(controller);
        okButton.setActionCommand("OK");

        Blackboard.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("statusMessage")) {
            statusLabel.setText(" " + evt.getNewValue());
        } else if (evt.getPropertyName().equals("blackboardCleared")) {
            statusLabel.setText(" Ready");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.setTitle("GitHub Code Visualizer");
            main.setSize(1000, 600);
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main.setVisible(true);
        });
    }
}