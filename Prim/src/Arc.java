import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.FontMetrics;

public class Arc {
    private Point start;
    private Point end;
    private int cost; // Costul arcului
    int startNumber;
    int endNumber;

    public Arc(Point start, Point end, int cost) {
        this.start = start;
        this.end = end;
        this.cost = cost;
    }

    public Arc(Node start, Node end, int cost) {
        this.start = new Point(start.getCoordX(), start.getCoordY());
        this.end = new Point(end.getCoordX(), end.getCoordY());
        this.cost = cost;
    }

    public Node getStartNode(){
        return new Node(start.x, start.y, startNumber);
    }

    public Node getEndNode(){
        return new Node(end.x, end.y, endNumber);
    }

    public int getStartNumber(){
        return startNumber;
    }

    public int getEndNumber(){
        return endNumber;
    }

    public Point getStartPoint() {
        return start;
    }

    public Point getEndPoint() {
        return end;
    }

    public int getCost() {
        return cost;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    public void setEndNumber(int endNumber) {
        this.endNumber = endNumber;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setStartPoint(Point startPoint) {
        this.start = startPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.end = endPoint;
    }

    public void drawArcWithHead(Graphics g) {
        if (start != null) {
            g.drawLine(start.x + 15, start.y + 15, end.x + 15, end.y + 15);

            // Desenează capătul de săgeată
            drawArrowHead(g, new Point(start.x + 15, start.y + 15), new Point(end.x + 15, end.y + 15));

            // Afișează costul
            drawCost(g);
        }
    }

    public void drawArcWithoutHead(Graphics g) {
        if (start != null) {
            g.drawLine(start.x + 15, start.y + 15, end.x + 15, end.y + 15);

            // Afișează costul
            drawCost(g);
        }
    }

    private void drawCost(Graphics g) {
        // Calculează poziția centrală a arcului
        int midX = (start.x + end.x) / 2 + 15;
        int midY = (start.y + end.y) / 2 + 15;

        // Obține dimensiunile textului
        FontMetrics fm = g.getFontMetrics();
        String costText = String.valueOf(cost);
        int textWidth = fm.stringWidth(costText);
        int textHeight = fm.getHeight();

        // Setează dimensiunile chenarului
        int padding = 4; // Spațiu extra în jurul textului
        int rectWidth = textWidth + padding * 2;
        int rectHeight = textHeight + padding;
        int rectX = midX - rectWidth / 2;
        int rectY = midY - rectHeight / 2;

        // Desenează chenarul galben
        g.setColor(Color.CYAN);
        g.fillRect(rectX, rectY, rectWidth, rectHeight);

        // Desenează conturul dreptunghiului
        g.setColor(Color.BLACK);
        g.drawRect(rectX, rectY, rectWidth, rectHeight);

        // Desenează textul centrat
        g.setColor(Color.LIGHT_GRAY);
        int textX = midX - textWidth / 2;
        int textY = midY + fm.getAscent() / 2 - padding / 2;
        g.drawString(costText, textX, textY);
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
