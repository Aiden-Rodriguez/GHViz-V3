import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * GridPanel class to display squares based on the Blackboard state.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.3
 */
public class GridPanel extends JPanel implements PropertyChangeListener {

    private boolean loading = false;
    private boolean ready = false;
    private Square selectedSquare = null;
    private Square hoveredSquare = null;
    private JPanel visualizationPanel;
    private JTextField selectedFileField;

    public GridPanel() {
        setLayout(new BorderLayout());

        visualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (loading) {
                    drawLoading(g);
                } else if (ready) {
                    drawSquares(g);
                }
            }
        };
        visualizationPanel.setBackground(Color.WHITE);

        visualizationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        visualizationPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel(" Selected File Name: "), BorderLayout.WEST);
        selectedFileField = new JTextField();
        selectedFileField.setEditable(false);
        selectedFileField.setBackground(Color.WHITE);
        bottomPanel.add(selectedFileField, BorderLayout.CENTER);

        add(visualizationPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        Blackboard.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("blackboardLoading")) {
            loading = (boolean) evt.getNewValue();
            if (loading) ready = false;
        } else if (evt.getPropertyName().equals("blackboardReady")) {
            loading = false;
            ready = true;
        } else if (evt.getPropertyName().equals("blackboardCleared")) {
            selectedSquare = null;
            hoveredSquare = null;
            selectedFileField.setText("");
        } else if (evt.getPropertyName().equals("selectedFileName")) {
            selectedFileField.setText((String) evt.getNewValue());
        } else if (evt.getPropertyName().equals("selectedFolderPath")) {
            // When folder selection changes, clear the selected file
            selectedSquare = null;
            selectedFileField.setText("");
        }
        visualizationPanel.repaint();
    }

    private void drawSquares(Graphics g) {
        java.util.List<Square> squares = Blackboard.getInstance().getFilteredSquares();
        if (squares.isEmpty()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            String message = "Select a folder from the tree to view files";
            FontMetrics fm = g.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            g.drawString(message,
                    (visualizationPanel.getWidth() - messageWidth) / 2,
                    visualizationPanel.getHeight() / 2);
            return;
        }

        int cols = (int) Math.ceil(Math.sqrt(squares.size()));
        int rows = (int) Math.ceil((double) squares.size() / cols);
        int squareWidth = visualizationPanel.getWidth() / cols;
        int squareHeight = visualizationPanel.getHeight() / rows;

        int maxLines = squares.stream().mapToInt(Square::getLinesOfCode).max().orElse(1);

        for (int i = 0; i < squares.size(); i++) {
            Square square = squares.get(i);
            int row = i / cols;
            int col = i % cols;
            int x = col * squareWidth;
            int y = row * squareHeight;

            Color color = calculateColor(square.getComplexity(), square.getLinesOfCode(), maxLines);
            g.setColor(color);
            g.fillRect(x, y, squareWidth - 2, squareHeight - 2);

            // Highlight selected square
            if (square == selectedSquare) {
                g.setColor(Color.BLUE);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(3));
                g.drawRect(x, y, squareWidth - 2, squareHeight - 2);
                g2d.setStroke(new BasicStroke(1));
            } else {
                g.setColor(Color.BLACK);
                g.drawRect(x, y, squareWidth - 2, squareHeight - 2);
            }
        }
    }

    private void drawLoading(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Loading...", visualizationPanel.getWidth() / 2 - 30, visualizationPanel.getHeight() / 2);
    }

    private Color calculateColor(int complexity, int lines, int maxLines) {
        Color baseColor;

        // Determine base color by complexity
        if (complexity > 10) {
            baseColor = new Color(240, 140, 140); // Red
        } else if (complexity > 5) {
            baseColor = new Color(255, 245, 150); // Yellow
        } else {
            baseColor = new Color(180, 240, 180); // Green
        }

        // Calculate alpha based on lines
        int alpha;
        if (lines == 0) {
            alpha = 0;
        } else if (maxLines == 0) {
            alpha = 255;
        } else {
            alpha = (int) (255.0 * lines / maxLines);
        }

        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        Square clickedSquare = getSquareAtPosition(mouseX, mouseY);
        if (clickedSquare != null) {
            selectedSquare = clickedSquare;
            Blackboard.getInstance().setSelectedFileName(clickedSquare.getName());
            visualizationPanel.repaint();
        }
    }

    private void handleMouseMove(int mouseX, int mouseY) {
        Square newHoveredSquare = getSquareAtPosition(mouseX, mouseY);
        if (newHoveredSquare != hoveredSquare) {
            hoveredSquare = newHoveredSquare;
            if (hoveredSquare != null) {
                String tooltip = String.format("<html>%s<br>Lines: %d<br>Complexity: %d</html>",
                        hoveredSquare.getName(),
                        hoveredSquare.getLinesOfCode(),
                        hoveredSquare.getComplexity());
                visualizationPanel.setToolTipText(tooltip);
            } else {
                visualizationPanel.setToolTipText(null);
            }
        }
    }

    private Square getSquareAtPosition(int mouseX, int mouseY) {
        java.util.List<Square> squares = Blackboard.getInstance().getFilteredSquares();
        if (squares.isEmpty()) return null;

        int cols = (int) Math.ceil(Math.sqrt(squares.size()));
        int rows = (int) Math.ceil((double) squares.size() / cols);
        int squareWidth = visualizationPanel.getWidth() / cols;
        int squareHeight = visualizationPanel.getHeight() / rows;

        int col = mouseX / squareWidth;
        int row = mouseY / squareHeight;
        int index = row * cols + col;

        if (index >= 0 && index < squares.size()) {
            return squares.get(index);
        }
        return null;
    }
}