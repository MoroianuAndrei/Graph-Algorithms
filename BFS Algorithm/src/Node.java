public class Node {
    private int x;
    private int y;
    private Integer position;
    private static Integer positionCounter = 0;

    public Node(int x, int y, int position) {
        this.x = x;
        this.y = y;
        this.position = position;
    }

    public Node() {
        position = Node.positionCounter++;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Integer getPosition() {
        return this.position;
    }

    public  void setPosition(Integer position) {
        this.position = position;
    }
}
