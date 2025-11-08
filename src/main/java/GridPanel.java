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
 * @version 1.2
 */
public class GridPanel extends JPanel implements PropertyChangeListener {

    private boolean loading = false;
    private boolean ready = false;
    private Square selectedSquare = null;
    private Square hoveredSquare = null;

    public GridPanel() {
        setBackground(Color.WHITE);
        Blackboard.getInstance().addPropertyChangeListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
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
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (loading) {
            drawLoading(g);
        } else if (ready) {
            drawSquares(g);
        }
    }

    private void drawSquares(Graphics g) {
        java.util.List<Square> squares = Blackboard.getInstance().getSquares();
        if (squares.isEmpty()) return;

        int cols = (int) Math.ceil(Math.sqrt(squares.size()));
        int rows = (int) Math.ceil((double) squares.size() / cols);
        int squareWidth = getWidth() / cols;
        int squareHeight = getHeight() / rows;

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
        setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Loading...", getWidth() / 2 - 30, getHeight() / 2);
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
            repaint();
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
                setToolTipText(tooltip);
            } else {
                setToolTipText(null);
            }
        }
    }

    private Square getSquareAtPosition(int mouseX, int mouseY) {
        java.util.List<Square> squares = Blackboard.getInstance().getSquares();
        if (squares.isEmpty()) return null;

        int cols = (int) Math.ceil(Math.sqrt(squares.size()));
        int rows = (int) Math.ceil((double) squares.size() / cols);
        int squareWidth = getWidth() / cols;
        int squareHeight = getHeight() / rows;

        int col = mouseX / squareWidth;
        int row = mouseY / squareHeight;
        int index = row * cols + col;

        if (index >= 0 && index < squares.size()) {
            return squares.get(index);
        }
        return null;
    }

}