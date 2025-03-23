import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Arc
{
    private Point start;
    private Point end;

    public Arc(Point start, Point end)
    {
        this.start = start;
        this.end = end;
    }

    public Arc(Node start, Node end)
    {
        this.start = new Point(start.getCoordX(), start.getCoordY());
        this.end = new Point(end.getCoordX(), end.getCoordY());
    }

    // Metode pentru a obține punctele de start și end
    public Point getStartPoint()
    {
        return start;
    }

    public Point getEndPoint()
    {
        return end;
    }

    // Add setter methods to update the points
    public void setStartPoint(Point startPoint)
    {
        this.start = startPoint;
    }

    public void setEndPoint(Point endPoint)
    {
        this.end = endPoint;
    }

    public Node getStartNode(int number) {
        return new Node(start.x, start.y, number);
    }

    public Node getEndNode(int number) {
        return new Node(end.x, end.y, number);
    }

    public Node getStartNode() {
        return new Node(start.x, start.y);
    }

    public Node getEndNode() {
        return new Node(end.x, end.y);
    }

    public void drawArcWithHead(Graphics g)
    {
        if (start != null)
        {
            g.setColor(Color.RED);
            g.drawLine(start.x + 15, start.y + 15, end.x + 15, end.y + 15);

            // Draw arrowhead
            drawArrowHead(g, new Point(start.x+15, start.y+15), new Point(end.x+15, end.y+15));
        }
    }

    public void drawArdWithoutHead(Graphics g)
    {
        if (start != null)
        {
            g.setColor(Color.RED);
            g.drawLine(start.x + 15, start.y + 15, end.x + 15, end.y + 15);
        }
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
