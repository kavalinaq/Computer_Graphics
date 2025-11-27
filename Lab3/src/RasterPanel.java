import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class RasterPanel extends JPanel {
    private static final int GRID_SIZE = 40; 
    private static final int BASE_CELL_SIZE = 20;
    private List<Point> points;
    private Color drawColor;
    private double scale = 1.0;
    private JLabel scaleLabel;

    public RasterPanel(JLabel scaleLabel) {
        points = new ArrayList<>();
        drawColor = Color.RED;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 700));
        this.scaleLabel = scaleLabel;
        updateScaleLabel();
    }

    public void zoomIn() {
        scale *= 1.2;
        updateScaleLabel();
        repaint();
    }

    public void zoomOut() {
        scale /= 1.2;
        updateScaleLabel();
        repaint();
    }

    public void resetZoom() {
        scale = 1.0;
        updateScaleLabel();
        repaint();
    }

    private void updateScaleLabel() {
        scaleLabel.setText(String.format("Масштаб: %.1fx", scale));
    }

    private int getCellSize() {
        return (int) (BASE_CELL_SIZE * scale);
    }

    private int getVisibleGridSize() {
        return (int) (GRID_SIZE / scale);
    }

    public void clearPoints() {
        points.clear();
        repaint();
    }

    public void drawStepByStep(int x1, int y1, int x2, int y2) {
        points.clear();
        points.add(new Point(x1, y1, drawColor));

        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        double xIncrement = dx / (double) steps;
        double yIncrement = dy / (double) steps;

        double x = x1;
        double y = y1;

        for (int i = 0; i < steps; i++) {
            x += xIncrement;
            y += yIncrement;
            points.add(new Point((int) Math.round(x), (int) Math.round(y), drawColor));
        }

        repaint();
    }

    public void drawDDA(int x1, int y1, int x2, int y2) {
        points.clear();
        points.add(new Point(x1, y1, drawColor));

        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        float xIncrement = dx / (float) steps;
        float yIncrement = dy / (float) steps;

        float x = x1;
        float y = y1;

        for (int i = 0; i < steps; i++) {
            x += xIncrement;
            y += yIncrement;
            points.add(new Point(Math.round(x), Math.round(y), drawColor));
        }

        repaint();
    }

    public void drawBresenhamLine(int x1, int y1, int x2, int y2) {
        points.clear();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1, y = y1;

        while (true) {
            points.add(new Point(x, y, drawColor));

            if (x == x2 && y == y2) break;

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }

        repaint();
    }

    public void drawBresenhamCircle(int xc, int yc, int radius) {
        points.clear();

        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;

        drawCirclePoints(xc, yc, x, y);

        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            drawCirclePoints(xc, yc, x, y);
        }

        repaint();
    }

    private void drawCirclePoints(int xc, int yc, int x, int y) {
        points.add(new Point(xc + x, yc + y, drawColor));
        points.add(new Point(xc - x, yc + y, drawColor));
        points.add(new Point(xc + x, yc - y, drawColor));
        points.add(new Point(xc - x, yc - y, drawColor));
        points.add(new Point(xc + y, yc + x, drawColor));
        points.add(new Point(xc - y, yc + x, drawColor));
        points.add(new Point(xc + y, yc - x, drawColor));
        points.add(new Point(xc - y, yc - x, drawColor));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int originX = getWidth() / 2;
        int originY = getHeight() / 2;

        drawGrid(g2d, originX, originY);
        drawAxes(g2d, originX, originY);
        drawPoints(g2d, originX, originY);
    }

    private void drawGrid(Graphics2D g2d, int originX, int originY) {
        g2d.setColor(new Color(220, 220, 220));
        int width = getWidth();
        int height = getHeight();
        int cellSize = getCellSize();
        int visibleGridSize = getVisibleGridSize();

        for (int x = originX - visibleGridSize * cellSize; x <= originX + visibleGridSize * cellSize; x += cellSize) {
            g2d.drawLine(x, 0, x, height);
        }

        for (int y = originY - visibleGridSize * cellSize; y <= originY + visibleGridSize * cellSize; y += cellSize) {
            g2d.drawLine(0, y, width, y);
        }
    }

    private void drawAxes(Graphics2D g2d, int originX, int originY) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        g2d.drawLine(0, originY, getWidth(), originY);
        g2d.drawLine(originX, 0, originX, getHeight());

        drawArrow(g2d, getWidth() - 10, originY, getWidth(), originY);
        drawArrow(g2d, originX, 10, originX, 0);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        int cellSize = getCellSize();
        int visibleGridSize = getVisibleGridSize();

        for (int i = -visibleGridSize; i <= visibleGridSize; i++) {
            if (i != 0 && i % 5 == 0) {
                int x = originX + i * cellSize;
                g2d.drawString(String.valueOf(i), x - 5, originY + 15);
                g2d.drawLine(x, originY - 3, x, originY + 3);
            }
        }

        for (int i = -visibleGridSize; i <= visibleGridSize; i++) {
            if (i != 0 && i % 5 == 0) {
                int y = originY - i * cellSize;
                g2d.drawString(String.valueOf(i), originX + 10, y + 5);
                g2d.drawLine(originX - 3, y, originX + 3, y);
            }
        }

        g2d.drawString("0", originX + 5, originY + 15);
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int size = 10;
        g2d.drawLine(x2, y2,
                (int) (x2 - size * Math.cos(angle - Math.PI / 6)),
                (int) (y2 - size * Math.sin(angle - Math.PI / 6)));
        g2d.drawLine(x2, y2,
                (int) (x2 - size * Math.cos(angle + Math.PI / 6)),
                (int) (y2 - size * Math.sin(angle + Math.PI / 6)));
    }


    private void drawPoints(Graphics2D g2d, int originX, int originY) {
        int cellSize = getCellSize();

        for (Point p : points) {
            g2d.setColor(p.color);
            int px = originX + p.x * cellSize - cellSize / 2;
            int py = originY - p.y * cellSize - cellSize / 2;
            g2d.fillRect(px, py, cellSize, cellSize);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(px, py, cellSize, cellSize);
        }
    }

    private static class Point {
        int x, y;
        Color color;

        Point(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}