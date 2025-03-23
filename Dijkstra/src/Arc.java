import java.awt.Graphics;

public class Arc {
    private final Node startNode;
    private final Node endNode;
    private final int length;

    public Arc(Node startNode, Node endNode, int length) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.length = length;
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public int getLength() {
        return length;
    }

    public void drawArc(Graphics g) {
        int x1 = startNode.getLongitude();
        int y1 = startNode.getLatitude();
        int x2 = endNode.getLongitude();
        int y2 = endNode.getLatitude();

        // Desenează linia
        g.drawLine(x1, y1, x2, y2);
    }
}
