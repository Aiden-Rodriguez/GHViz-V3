import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Shows a live PlantUML class diagram of the loaded files.
 * Updates automatically via Blackboard.
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.4
 *
 */
public class DiagramPanel extends JPanel implements PropertyChangeListener {

    private final JLabel imageLabel = new JLabel();
    private boolean ready = false;

    public DiagramPanel() {
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(imageLabel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);

        imageLabel.setText("<html><body style='text-align:center; padding:50px; font-size:14px; color:#666'>" +
                "Load a GitHub folder to see the class diagram</body></html>");
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        Blackboard.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("blackboardReady")) {
            ready = true;
            renderDiagram();
        } else if (evt.getPropertyName().equals("blackboardCleared")) {
            ready = false;
            imageLabel.setIcon(null);
            imageLabel.setText("<html><body style='text-align:center; padding:50px; font-size:14px; color:#666'>" +
                    "Load a GitHub folder to see the class diagram</body></html>");
        } else if (evt.getPropertyName().equals("selectedFolderPath")) {
            if (ready) {
                renderDiagram();
            }
        }
    }

    private void renderDiagram() {
        List<Square> squares = Blackboard.getInstance().getFilteredSquares();

        if (squares.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("<html><body style='text-align:center; padding:50px; font-size:14px; color:#666'>" +
                    "Select a folder from the tree to view its class diagram</body></html>");
            return;
        }

        imageLabel.setText("Generating diagram...");
        imageLabel.setIcon(null);

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                String plantUmlCode = PlantUmlGenerator.generateDiagram(squares);

                SourceStringReader reader = new SourceStringReader(plantUmlCode);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                reader.outputImage(os, new FileFormatOption(FileFormat.PNG));
                os.close();

                byte[] imageBytes = os.toByteArray();
                return ImageIO.read(new ByteArrayInputStream(imageBytes));
            }

            @Override
            protected void done() {
                try {
                    BufferedImage image = get();
                    ImageIcon icon = new ImageIcon(image);
                    imageLabel.setIcon(icon);
                    imageLabel.setText(null);

                    Blackboard.getInstance().setStatusMessage(
                            "Diagram ready • " + squares.size() + " classes");
                } catch (Exception ex) {
                    imageLabel.setIcon(null);
                    imageLabel.setText("<html><body style='color:red; padding:20px'>Failed to render diagram:<br>"
                            + ex.getMessage() + "</body></html>");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}