import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * MetricsPanel class to display Instability vs Abstractness metrics.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.3
 */
public class MetricsPanel extends JPanel implements PropertyChangeListener {

    private boolean loading = false;
    private boolean ready = false;
    private JPanel chartPanel;
    private List<Square> displayedSquares;
    private Square hoveredSquare = null;
    private static final int MARGIN = 60;
    private static final int POINT_RADIUS = 8;

    public MetricsPanel() {
        setLayout(new BorderLayout());
        displayedSquares = new ArrayList<>();

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (loading) {
                    drawLoading(g2d);
                } else if (ready && !displayedSquares.isEmpty()) {
                    drawChart(g2d);
                } else {
                    drawEmptyMessage(g2d);
                }
            }
        };
        chartPanel.setBackground(Color.WHITE);

        chartPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });

        add(chartPanel, BorderLayout.CENTER);
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
            updateDisplayedSquares();
        } else if (evt.getPropertyName().equals("blackboardCleared")) {
            displayedSquares.clear();
            hoveredSquare = null;
        } else if (evt.getPropertyName().equals("selectedFolderPath")) {
            updateDisplayedSquares();
        }
        chartPanel.repaint();
    }

    private void updateDisplayedSquares() {
        displayedSquares.clear();
        displayedSquares.addAll(Blackboard.getInstance().getFilteredSquares());
    }

    private void drawChart(Graphics2D g2d) {
        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        int chartWidth = width - 2 * MARGIN;
        int chartHeight = height - 2 * MARGIN;

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN); // X-axis
        g2d.drawLine(MARGIN, MARGIN, MARGIN, height - MARGIN); // Y-axis

        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        int x1 = MARGIN;
        int y1 = MARGIN;
        int x2 = width - MARGIN;
        int y2 = height - MARGIN;
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Useless", width - MARGIN - 80, MARGIN + 30);
        g2d.drawString("Painful", MARGIN + 10, height - MARGIN - 20);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Instability", width / 2 - 30, height - MARGIN + 40);

        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Abstractness", -height / 2 - 40, MARGIN - 40);
        g2d.rotate(Math.PI / 2);

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= 10; i++) {
            int x = MARGIN + (i * chartWidth / 10);
            int y = height - MARGIN + (i * chartHeight / 10);

            g2d.drawLine(x, height - MARGIN, x, height - MARGIN + 5);
            String xLabel = String.format("%.1f", i / 10.0);
            g2d.drawString(xLabel, x - 10, height - MARGIN + 20);

            g2d.drawLine(MARGIN - 5, y, MARGIN, y);
            String yLabel = String.format("%.1f", 1.0 - (i / 10.0));
            g2d.drawString(yLabel, MARGIN - 30, y + 5);
        }

        for (Square square : displayedSquares) {
            // Calculate metrics for individual square
            double abstractness = (square.isAbstract() || square.isInterface()) ? 1.0 : 0.0;

            int ce = square.getEfferentCoupling();
            int ca = square.getAfferentCoupling();
            double instability = 0.0;
            if (ce + ca > 0) {
                instability = (double) ce / (ce + ca);
            }

            double distance = Math.abs(abstractness + instability - 1.0);

            int px = MARGIN + (int) (instability * chartWidth);
            int py = height - MARGIN - (int) (abstractness * chartHeight);

            Color pointColor;
            if (distance < 0.3) {
                pointColor = new Color(100, 180, 100);
            } else if (distance < 0.5) {
                pointColor = new Color(255, 200, 100);
            } else {
                pointColor = new Color(220, 100, 100);
            }

            if (square == hoveredSquare) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(px - POINT_RADIUS - 2, py - POINT_RADIUS - 2,
                        (POINT_RADIUS + 2) * 2, (POINT_RADIUS + 2) * 2);
            }

            g2d.setColor(pointColor);
            g2d.fillOval(px - POINT_RADIUS, py - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawOval(px - POINT_RADIUS, py - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
        }
    }

    private void drawLoading(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Loading...", chartPanel.getWidth() / 2 - 30, chartPanel.getHeight() / 2);
    }

    private void drawEmptyMessage(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String message = "Select a folder from the tree to view metrics";
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        g2d.drawString(message,
                (chartPanel.getWidth() - messageWidth) / 2,
                chartPanel.getHeight() / 2);
    }

    private void handleMouseMove(int mouseX, int mouseY) {
        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        int chartWidth = width - 2 * MARGIN;
        int chartHeight = height - 2 * MARGIN;

        Square newHovered = null;
        for (Square square : displayedSquares) {
            double abstractness = (square.isAbstract() || square.isInterface()) ? 1.0 : 0.0;

            int ce = square.getEfferentCoupling();
            int ca = square.getAfferentCoupling();
            double instability = 0.0;
            if (ce + ca > 0) {
                instability = (double) ce / (ce + ca);
            }

            int px = MARGIN + (int) (instability * chartWidth);
            int py = height - MARGIN - (int) (abstractness * chartHeight);

            double distance = Math.sqrt(Math.pow(mouseX - px, 2) + Math.pow(mouseY - py, 2));
            if (distance <= POINT_RADIUS + 2) {
                newHovered = square;
                break;
            }
        }

        if (newHovered != hoveredSquare) {
            hoveredSquare = newHovered;
            if (hoveredSquare != null) {
                double abstractness = (hoveredSquare.isAbstract() || hoveredSquare.isInterface()) ? 1.0 : 0.0;
                int ce = hoveredSquare.getEfferentCoupling();
                int ca = hoveredSquare.getAfferentCoupling();
                double instability = 0.0;
                if (ce + ca > 0) {
                    instability = (double) ce / (ce + ca);
                }
                double dist = Math.abs(abstractness + instability - 1.0);

                String tooltip = String.format(
                        "<html><b>%s</b><br>Abstractness: %.2f<br>Instability: %.2f<br>Distance: %.2f<br>Ce: %d, Ca: %d</html>",
                        hoveredSquare.getName(),
                        abstractness,
                        instability,
                        dist,
                        ce,
                        ca
                );
                chartPanel.setToolTipText(tooltip);
            } else {
                chartPanel.setToolTipText(null);
            }
            chartPanel.repaint();
        }
    }
}