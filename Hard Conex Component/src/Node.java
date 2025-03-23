import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Node
{
    private int coordX;
    private int coordY;
    private int number;
    private Color color = Color.RED;

    public Node(int coordX, int coordY, int number)
    {
        this.coordX = coordX;
        this.coordY = coordY;
        this.number = number;
    }

    public Node(int coordX, int coordY)
    {
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public int getCoordX()
    {
        return coordX;
    }
    public void setCoordX(int coordX)
    {
        this.coordX = coordX;
    }
    public int getCoordY()
    {
        return coordY;
    }
    public void setCoordY(int coordY)
    {
        this.coordY = coordY;
    }
    public int getNumber()
    {
        return number;
    }
    public void setNumber(int number)
    {
        this.number = number;
    }
    public Color getColor(){return color;}
    public void setColor(Color color) {this.color = color;}

    public boolean equalsByPosition(Node other) {
        if (other == null) {
            return false;
        }
        return this.coordX == other.coordX && this.coordY == other.coordY;
    }

    public void drawNode(Graphics g, int node_diam)
    {
        g.setColor(color);
        g.setFont(new Font("TimesRoman", Font.BOLD, 15));
        g.fillOval(coordX, coordY, node_diam, node_diam);
        g.setColor(Color.BLACK);
        g.drawOval(coordX, coordY, node_diam, node_diam);
        if(number < 10)
            g.drawString(((Integer)number).toString(), coordX+13, coordY+20);
        else
            g.drawString(((Integer)number).toString(), coordX+8, coordY+20);
    }

    @Override
    public String toString() {
        return String.valueOf(number); // Returnează numărul nodului ca un șir de caractere
    }
}
