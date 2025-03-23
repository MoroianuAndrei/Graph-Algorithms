import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Node
{
    private int coordX;
    private int coordY;
    private int number;

    public Node(int coordX, int coordY, int number)
    {
        this.coordX = coordX;
        this.coordY = coordY;
        this.number = number;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Comparăm referințele
        if (obj == null || getClass() != obj.getClass()) return false; // Clase diferite
        Node other = (Node) obj;
        return this.getNumber() == other.getNumber(); // Comparăm pe baza identificatorului
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

    public void drawNode(Graphics g, int node_diam)
    {
        g.setColor(Color.RED);
        g.setFont(new Font("TimesRoman", Font.BOLD, 15));
        g.fillOval(coordX, coordY, node_diam, node_diam);
        g.setColor(Color.BLACK);
        g.drawOval(coordX, coordY, node_diam, node_diam);
        if(number < 10)
            g.drawString(((Integer)number).toString(), coordX+13, coordY+20);
        else
            g.drawString(((Integer)number).toString(), coordX+8, coordY+20);
    }
}
