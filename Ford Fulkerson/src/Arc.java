import java.awt.*;

public class Arc {
    private Point start;
    private Point end;
    private int capacity;  // Capacitatea arcului
    private int flow;      // Fluxul pe arc

    public Arc(Point start, Point end, int capacity, int flow) {
        this.start = start;
        this.end = end;
        this.capacity = capacity;
        this.flow = flow;
    }

    public Arc(Node start, Node end, int capacity) {
        this.start = new Point(start.getCoordX(), start.getCoordY());
        this.end = new Point(end.getCoordX(), end.getCoordY());
        this.capacity = capacity;
        this.flow = 0;  // Implicit fluxul este 0
    }

    public Arc(Node start, Node end) {
        this(start, end, 0); // Constructor implicit cu capacitate 0
    }

    public Point getStartPoint() {
        return start;
    }

    public Point getEndPoint() {
        return end;
    }

    public void setStartPoint(Point startPoint) {
        this.start = startPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.end = endPoint;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    // Pentru desenarea arcelor cu informațiile de capacitate și flux
    public void drawArcWithHead(Graphics g) {
        if (start != null) {
            g.setColor(Color.RED);
            g.drawLine(start.x + 15, start.y + 15, end.x + 15, end.y + 15);

            // Desenează săgeata și textul cu flux/capacitate
            drawArrowHead(g, new Point(start.x + 15, start.y + 15), new Point(end.x + 15, end.y + 15));

            drawFlowAndCapacity(g);
        }
    }

    private void drawFlowAndCapacity(Graphics g) {
        int midX = (start.x + end.x) / 2 + 15;
        int midY = (start.y + end.y) / 2 + 15;

        FontMetrics fm = g.getFontMetrics();
        String flowText = String.valueOf(flow);
        String capacityText = String.valueOf(capacity);
        int textWidth = fm.stringWidth(flowText + "/" + capacityText);
        int textHeight = fm.getHeight();

        int padding = 5; // Spațiu extra în jurul textului
        int rectWidth = textWidth + padding * 2;
        int rectHeight = textHeight + padding;
        int rectX = midX - rectWidth / 2;
        int rectY = midY - rectHeight / 2;

        g.setColor(Color.CYAN);
        g.fillRect(rectX, rectY, rectWidth, rectHeight);

        g.setColor(Color.BLACK);
        g.drawRect(rectX, rectY, rectWidth, rectHeight);

        g.setColor(Color.LIGHT_GRAY);
        int textX = midX - textWidth / 2;
        int textY = midY + fm.getAscent() / 2 - padding / 2;
        g.drawString(flowText + "/" + capacityText, textX, textY);
    }

    private void drawArrowHead(Graphics g, Point tip, Point tail) {
        final int ARROW_SIZE = 25;
        double angle = Math.atan2(tail.y - tip.y, tail.x - tip.x);
        int x1 = (int) (tail.x - ARROW_SIZE * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (tail.y - ARROW_SIZE * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (tail.x - ARROW_SIZE * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (tail.y - ARROW_SIZE * Math.sin(angle + Math.PI / 6));

        g.drawLine(tail.x, tail.y, x1, y1);
        g.drawLine(tail.x, tail.y, x2, y2);
        g.drawLine(x1, y1, x2, y2);
    }
}
