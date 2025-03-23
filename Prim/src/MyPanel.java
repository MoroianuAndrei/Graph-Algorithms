import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MyPanel extends JPanel {
    private int nodeNr = 1;
    private int node_diam = 30;
    private Vector<Node> listaNoduri;
    private Vector<Arc> listaArce;
    Point pointStart = null;
    Point pointEnd = null;
    boolean isDragging = false;
    private boolean isDirected = false;
    private Node nodeBeingDragged = null; // Nodul care este tras cu mouse-ul
    boolean isLeft = true;
    List<Arc> Aprim = new ArrayList<>();

    public MyPanel()
    {
        listaNoduri = new Vector<Node>();
        listaArce = new Vector<Arc>();
        // borderul panel-ului
        setBorder(BorderFactory.createLineBorder(Color.black));

        // Crează butonul pentru a rula Prim
        JButton primButton = new JButton("Prim");
        primButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prim();

                // Poți și să desenezi MST-ul pe panou sau să faci orice altceva
                repaint();
            }
        });

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(primButton);

        // Add button panel to the main panel (above the drawing area)
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);

        addMouseListener(new MouseAdapter()
        {
            //evenimentul care se produce la apasarea mousse-ului
            public void mousePressed(MouseEvent e)
            {
                pointStart = e.getPoint();
                if(SwingUtilities.isLeftMouseButton(e))
                    isLeft = true;
                else
                    isLeft = false;

                if (!isLeft) // Click dreapta
                {
                    nodeBeingDragged = getNodeAt(pointStart);
                }
            }

            //evenimentul care se produce la eliberarea mousse-ului
            public void mouseReleased(MouseEvent e)
            {
                if(SwingUtilities.isLeftMouseButton(e))
                    isLeft = true;
                else
                    isLeft = false;

                if (!isDragging && isLeft) // Click stânga && Adaug nod
                {
                    Aprim.clear();
                    addNode(e.getX(), e.getY());
                }
                else if (isDragging && nodeBeingDragged == null && isLeft) // Adaug arc
                {
                    Aprim.clear();
                    Node startNode = getNodeAt(pointStart);
                    Node endNode = getNodeAt(pointEnd);
                    if (startNode != null && endNode != null && startNode != endNode)
                    {
                        // Solicită costul de la utilizator
                        String costInput = JOptionPane.showInputDialog(null, "Introduceți costul arcului:", "Cost Arc", JOptionPane.PLAIN_MESSAGE);
                        if (costInput != null && !costInput.isEmpty()) {
                            try {
                                int cost = Integer.parseInt(costInput); // Conversie la int (sau altă validare)
                                Arc arc = new Arc(startNode, endNode, cost); // Constructorul trebuie extins pentru a include costul
                                arc.setStartNumber(startNode.getNumber());
                                arc.setEndNumber(endNode.getNumber());
                                listaArce.add(arc);
                                saveGraphToFile();
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Costul trebuie să fie un număr valid!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
                else if (nodeBeingDragged != null && !isLeft) // Mutarea nodului
                {
                    if (verify(nodeBeingDragged))
                    {
                        updateArcs(nodeBeingDragged, e);
                        nodeBeingDragged.setCoordX(e.getX() - node_diam / 2);
                        nodeBeingDragged.setCoordY(e.getY() - node_diam / 2);
                        nodeBeingDragged = null;
                        repaint();
                    }
                    else
                    {
                        if (distanceE(e) > distance(nodeBeingDragged)) {
                            updateArcs(nodeBeingDragged, e);
                            nodeBeingDragged.setCoordX(e.getX() - node_diam / 2);
                            nodeBeingDragged.setCoordY(e.getY() - node_diam / 2);
                            repaint();
                        }
                    }
                }

                pointStart = null;
                isDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            //evenimentul care se produce la drag&drop pe mousse
            public void mouseDragged(MouseEvent e)
            {
                if (nodeBeingDragged != null) // Dacă mutăm un nod
                {
                    if(verify(nodeBeingDragged))
                    {
                        updateArcs(nodeBeingDragged, e);
                        nodeBeingDragged.setCoordX(e.getX() - node_diam / 2);
                        nodeBeingDragged.setCoordY(e.getY() - node_diam / 2);
                        repaint();
                    }
                    else
                    {
                        if (distanceE(e) > distance(nodeBeingDragged))
                        {
                            updateArcs(nodeBeingDragged, e);
                            nodeBeingDragged.setCoordX(e.getX() - node_diam / 2);
                            nodeBeingDragged.setCoordY(e.getY() - node_diam / 2);
                            repaint();
                        }
                    }
                }
                else
                {
                    pointEnd = e.getPoint();
                    isDragging = true;
                    repaint();
                }
            }
        });
    }

    private void updateArcs(Node movedNode, MouseEvent e) {
        Point movedPoint = new Point(movedNode.getCoordX(), movedNode.getCoordY());
        for (Arc arc : listaArce) {
            // Dacă nodul mutat este nodul de start al arcului, actualizăm punctul de start
            if (isSamePoint(arc.getStartPoint(), movedPoint))
            {
                arc.setStartPoint(new Point(e.getX() - node_diam / 2, e.getY() - node_diam / 2));
            }

            // Dacă nodul mutat este nodul de final al arcului, actualizăm punctul de final
            if (isSamePoint(arc.getEndPoint(), movedPoint))
            {
                arc.setEndPoint(new Point(e.getX() - node_diam / 2, e.getY() - node_diam / 2));
            }
        }
    }

    // Metodă auxiliară pentru a compara două puncte prin coordonatele lor
    private boolean isSamePoint(Point p1, Point p2)
    {
        return p1.x == p2.x && p1.y == p2.y;
    }

    private double distanceE(MouseEvent e)
    {
        double min = Integer.MAX_VALUE;

        for (Node node : listaNoduri)
        {
            double distance = Math.sqrt(Math.pow(node.getCoordX() - e.getX(), 2) + Math.pow(node.getCoordY() - e.getY(), 2));
            if (distance < min) // If the distance is less than the node diameter, they would overlap
            {
                min = distance;
            }
        }

        return min;
    }

    private double distance(Node thisNode)
    {
        double min = Integer.MAX_VALUE;

        for (Node node : listaNoduri)
        {
            if(node != thisNode)
            {
                double distance = Math.sqrt(Math.pow(node.getCoordX() - thisNode.getCoordX(), 2) + Math.pow(node.getCoordY() - thisNode.getCoordY(), 2));
                if (distance < min) // If the distance is less than the node diameter, they would overlap
                {
                    min=distance;
                }
            }
        }

        return min;
    }

    private boolean verify(Node thisNode)
    {
        // Check if the new node is far enough from existing nodes
        for (Node node : listaNoduri)
        {
            if(node != thisNode)
            {
                double distance = Math.sqrt(Math.pow(node.getCoordX() - thisNode.getCoordX(), 2) + Math.pow(node.getCoordY() - thisNode.getCoordY(), 2));
                if (distance < node_diam + node_diam / 10) // If the distance is less than the node diameter, they would overlap
                {
                    return false;
                }
            }
        }
        return true;
    }

    private Node getNodeAt(Point p)
    {
        for (Node node : listaNoduri)
        {
            double distance = Math.sqrt(Math.pow(node.getCoordX() - p.x + node_diam/2, 2) + Math.pow(node.getCoordY() - p.y + node_diam/2, 2));
            if (distance <= node_diam / 2)
            { // Verificăm dacă punctul este în interiorul diametrului nodului
                return node;
            }
        }
        return null;
    }

    public Node getNodeAtP(Point p)
    {
        for (Node node : listaNoduri)
        {
            double distance = Math.sqrt(Math.pow(node.getCoordX() - p.x + node_diam/2, 2) + Math.pow(node.getCoordY() - p.y + node_diam/2, 2));
            if (distance <= node_diam) // Aici nu impartim la 2, deoarece ne intereseaza mijlocul pentru arce
            { // Verificăm dacă punctul este în interiorul diametrului nodului
                return node;
            }
        }
        return null;
    }

    private int[][] buildAdjacencyMatrix() {
        int numNodes = listaNoduri.size();
        int[][] adjacencyMatrix = new int[numNodes][numNodes];

        // Inițializează matricea cu 0
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }

        // Populează matricea cu 1 acolo unde există un arc
        for (Arc arc : listaArce)
        {
            Node startNode = getNodeAtP(arc.getStartPoint());
            Node endNode = getNodeAtP(arc.getEndPoint());
            if (startNode != null && endNode != null)
            {
                int startIndex = listaNoduri.indexOf(startNode);
                int endIndex = listaNoduri.indexOf(endNode);
                adjacencyMatrix[startIndex][endIndex] = 1;
                if(!isDirected)
                {
                    adjacencyMatrix[endIndex][startIndex] = 1; // Pentru graf neorientat
                }
            }
        }

        return adjacencyMatrix;
    }

    // Adauga numarul de noduri si matricea de adiacenta in fisier
    private void saveGraphToFile()
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("graph_data.txt")))
        {
            // Scrie numărul de noduri
            writer.write("Numar de noduri: " + (nodeNr - 1));
            writer.newLine();
            writer.newLine();

            // Obține matricea de adiacență pentru un graf neorientat
            int[][] adjacencyMatrix = buildAdjacencyMatrix();

            // Scrie matricea de adiacență pentru un graf neorientat
            writer.write(isDirected ? "Matricea de adiacenta pentru graf orientat:" : "Matricea de adiacenta pentru graf neorientat:");
            writer.newLine();
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                for (int j = 0; j < adjacencyMatrix[i].length; j++)
                {
                    writer.write(adjacencyMatrix[i][j] + " ");
                }
                writer.newLine(); // Trecem la linia următoare după fiecare rând
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //metoda care se apeleaza la eliberarea mouse-ului
    private void addNode(int x, int y)
    {
        // Check if the new node is far enough from existing nodes
        for (Node node : listaNoduri)
        {
            double distance = Math.sqrt(Math.pow(node.getCoordX() - x, 2) + Math.pow(node.getCoordY() - y, 2));
            if (distance < node_diam + node_diam/10) // If the distance is less than the node diameter, they would overlap
            {
                return; // Don't add the node
            }
        }

        // Add the new node if there's no overlap
        Node node = new Node(x, y, nodeNr);
        listaNoduri.add(node);
        nodeNr++;
        saveGraphToFile(); // Salvează graful după adăugarea unui nod nou
        repaint();
    }

    private void prim() {
        List<Node> N1 = new ArrayList<>();
        List<Node> N1hat = new ArrayList<>(listaNoduri);

        List<Integer> v1 = new ArrayList<>();
        v1.add(0);
        for(int i=0; i<listaNoduri.size() - 1; i++)
            v1.add(Integer.MAX_VALUE);

        List<Arc> e = new ArrayList<>();
        for(int i=0; i<listaNoduri.size(); i++) {
            e.add(null);
        }

        while(N1.size() != listaNoduri.size()){
            int y = minKey(v1, N1hat);

            N1.add(listaNoduri.get(y - 1));

            N1hat.remove(listaNoduri.get(y - 1));

            if(y != 1){
                Aprim.add(e.get(y - 1));
            }

            for (Arc arc : listaArce) {
                Node startNode = arc.getStartNode();
                Node endNode = arc.getEndNode();

                if (N1.contains(startNode) && N1hat.contains(endNode)) {
                    int cost = arc.getCost();
                    if (v1.get(endNode.getNumber() - 1) > cost) {
                        v1.set(endNode.getNumber() - 1, cost);
                        e.set(endNode.getNumber() - 1, arc);
                    }
                }
                if (N1.contains(endNode) && N1hat.contains(startNode)) {
                    int cost = arc.getCost();
                    if (v1.get(startNode.getNumber() - 1) > cost) {
                        v1.set(startNode.getNumber() - 1, cost);
                        e.set(startNode.getNumber() - 1, arc);
                    }
                }
            }
        }

        int total_cost = 0;

        for(int i=0; i<Aprim.size(); i++){
            total_cost = total_cost + Aprim.get(i).getCost();
        }

        System.out.println("Costul total este: " + total_cost);
    }

    private int minKey(List<Integer> v1, List<Node> N1hat){
        int min = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int  i= 0;  i< N1hat.size(); i++) {
            if (v1.get(N1hat.get(i).getNumber() - 1) < min) {
                min = v1.get(N1hat.get(i).getNumber() - 1);
                minIndex = i;
            }
        }

        return N1hat.get(minIndex).getNumber();
    }

    //se executa atunci cand apelam repaint()
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);//apelez metoda paintComponent din clasa de baza
        g.drawString("This is my Graph!", 10, 20);
        //deseneaza arcele existente in lista
		/*for(int i=0;i<listaArce.size();i++)
		{
			listaArce.elementAt(i).drawArc(g);
		}*/

        if (Aprim.size() == 0) {
            for (Arc a : listaArce) {
                g.setColor(Color.RED);
                if (isDirected)
                    a.drawArcWithHead(g);
                else
                    a.drawArcWithoutHead(g);
            }
        } else {
            for (Arc arc : Aprim) {
                g.setColor(Color.BLUE);
                arc.drawArcWithoutHead(g);  // Sau drawArcWithoutHead pentru arce neorientate
            }
        }

        //deseneaza arcul curent; cel care e in curs de desenare
        if (pointStart != null && nodeBeingDragged == null)
        {
            g.setColor(Color.RED);
            g.drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            repaint();
        }
        //deseneaza lista de noduri
        for(int i=0; i<listaNoduri.size(); i++)
        {
            listaNoduri.elementAt(i).drawNode(g, node_diam);
        }
		/*for (Node nod : listaNoduri)
		{
			nod.drawNode(g, node_diam, node_Diam);
		}*/
    }
}