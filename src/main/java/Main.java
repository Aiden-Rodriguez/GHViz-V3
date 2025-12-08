import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to set up the GUI for the GitHubViz application.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.5
 */
public class Main extends JFrame implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private JTextField urlField;
    private JLabel statusLabel;
    private Controller controller;

    public Main() {
        logger.info("Initializing GitHub Code Visualizer application");

        urlField = new JTextField();
        JButton okButton = new JButton("OK");
        controller = new Controller(urlField);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel(" GitHub Folder URL: "), BorderLayout.WEST);
        top.add(urlField, BorderLayout.CENTER);
        top.add(okButton, BorderLayout.EAST);

        FileTreePanel fileTreePanel = new FileTreePanel();
        logger.debug("FileTreePanel created");

        JTabbedPane tabbedPane = new JTabbedPane();

        GridPanel gridPanel = new GridPanel();
        tabbedPane.addTab("Grid", gridPanel);
        logger.debug("GridPanel added to tabs");

        MetricsPanel metricsPanel = new MetricsPanel();
        tabbedPane.addTab("Metrics", metricsPanel);
        logger.debug("MetricsPanel added to tabs");

        DiagramPanel diagramPanel = new DiagramPanel();
        tabbedPane.addTab("Diagram", diagramPanel);
        logger.debug("DiagramPanel added to tabs");

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String tabName = tabbedPane.getTitleAt(selectedIndex);
            logger.debug("Switched to tab: {}", tabName);
            Blackboard.getInstance().setStatusMessage("Viewing: " + tabName);
        });

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

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        okButton.addActionListener(controller);
        okButton.setActionCommand("OK");

        Blackboard.getInstance().addPropertyChangeListener(this);

        logger.info("GUI initialization complete");
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
        logger.info("=== GitHub Code Visualizer Starting ===");

        SwingUtilities.invokeLater(() -> {
            try {
                Main main = new Main();
                main.setTitle("GitHub Code Visualizer");
                main.setSize(1000, 600);
                main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                main.setVisible(true);
                logger.info("Application window displayed successfully");
            } catch (Exception e) {
                logger.error("Failed to start application", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}