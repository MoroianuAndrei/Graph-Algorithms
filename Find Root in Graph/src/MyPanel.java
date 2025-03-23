import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.util.Vector;
import java.util.Random;
import java.util.*;
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

    public MyPanel()
    {
        listaNoduri = new Vector<Node>();
        listaArce = new Vector<Arc>();
        // borderul panel-ului
        setBorder(BorderFactory.createLineBorder(Color.black));

        // Create buttons
        JButton directedButton = new JButton("Graf Orientat");
        JButton undirectedButton = new JButton("Graf Neorientat");

        // Add action listeners to buttons
        directedButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                isDirected = true;
                saveGraphToFile();
                repaint();
            }
        });

        undirectedButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                isDirected = false;
                saveGraphToFile();
                repaint();
            }
        });

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(directedButton);
        buttonPanel.add(undirectedButton);

        JButton rootButton = new JButton("Root");
        rootButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verifyAndIdentifyRoot();
            }
        });

        // Creează un panou pentru butonul "Topological Sort" și îl adaugă în partea de jos
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(rootButton);

        // Add button panel to the main panel (above the drawing area)
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

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
                    addNode(e.getX(), e.getY());
                }
                else if (isDragging && nodeBeingDragged == null && isLeft) // Adaug arc
                {
                    Node startNode = getNodeAt(pointStart);
                    Node endNode = getNodeAt(pointEnd);
                    if (startNode != null && endNode != null && startNode != endNode)
                    {
                        Arc arc = new Arc(startNode, endNode);
                        listaArce.add(arc);
                        saveGraphToFile();
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

    private Node getNodeAtP(Point p)
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
        for (Arc a : listaArce)
        {
            if (isDirected)
                a.drawArcWithHead(g);
            else
                a.drawArdWithoutHead(g);
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

    private int getNumber(Node n)
    {
        for(Node node : listaNoduri)
            if(n.getCoordX() == node.getCoordX() && n.getCoordY() == node.getCoordY())
                return node.getNumber();
        return -1;
    }

    public void verifyAndIdentifyRoot() {
        if (!isAcyclic()) {
            JOptionPane.showMessageDialog(this, "Graful are cicluri și nu este o arborescență.");
            for(Node node : listaNoduri){
                    node.setColor(Color.RED);
            }
            repaint();
            return;
        }

        if (!isQuasiStronglyConnected()) {
            JOptionPane.showMessageDialog(this, "Graful nu este quasi-tare conex și nu poate fi o arborescență.");
            for(Node node : listaNoduri){
                node.setColor(Color.RED);
            }
            repaint();
            return;
        }

        Node root = findRoot();
        if (root == null) {
            JOptionPane.showMessageDialog(this, "Rădăcina nu poate fi determinată.");
        } else {
            root.setColor(Color.BLUE); // Colorează rădăcina
            for(Node node : listaNoduri){
                if(!node.equals(root))
                    node.setColor(Color.RED);
            }
            repaint();
            JOptionPane.showMessageDialog(this, "Rădăcina arborescenței este nodul " + root.getNumber());
        }
    }

    private boolean isAcyclic() {
        Set<Node> visited = new HashSet<>();
        Set<Node> recStack = new HashSet<>();

        for (Node node : listaNoduri) {
            if (!visited.contains(node)) {
                if (isCyclic(node, visited, recStack)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCyclic(Node node, Set<Node> visited, Set<Node> recStack) {
        visited.add(node);
        recStack.add(node);

        for (Arc arc : listaArce) {
            if (arc.getStartNode().equalsByPosition(node)) {
                Node neighbor = arc.getEndNode();
                if (!visited.contains(neighbor) && isCyclic(neighbor, visited, recStack)) {
                    return true;
                } else if (recStack.contains(neighbor)) {
                    return true;
                }
            }
        }

        recStack.remove(node);
        return false;
    }

    private boolean isQuasiStronglyConnected() {
        // Verificăm dacă există cel puțin un nod din care putem ajunge la toate celelalte noduri
        for (Node node : listaNoduri) {
            if (canReachAllNodes(node)) {
                return true; // Dacă găsim un astfel de nod, graful este quasi-tare conex
            }
        }
        return false; // Dacă niciun nod nu îndeplinește condiția, graful nu este quasi-tare conex
    }

    private boolean canReachAllNodes(Node start) {
        // Parcurgere BFS pentru a verifica accesibilitatea tuturor nodurilor
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Arc arc : listaArce) {
                if (arc.getStartNode().equalsByPosition(current)) {
                    Node neighbor = arc.getEndNode();
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Returnăm true dacă toate nodurile au fost vizitate
        return visited.size() == listaNoduri.size();
    }

    private Node getNode(Node node){
        for(Node n : listaNoduri){
            if(node.equalsByPosition(n))
                return n;
        }
        return null;
    }

    private Node findRoot() {
        // Inițializăm un map pentru gradul de intrare al fiecărui nod
        Map<Node, Integer> inDegree = new HashMap<>();

        // Inițializăm toate nodurile cu gradul de intrare 0
        for (Node node : listaNoduri) {
            inDegree.put(node, 0);
        }

        // Actualizăm gradul de intrare pentru fiecare nod pe baza arcelor
        for (Arc arc : listaArce) {
            Node endNode = arc.getEndNode();
            endNode.setNumber(getNumber(endNode));
            endNode = getNode(endNode);
            inDegree.put(endNode, inDegree.getOrDefault(endNode, 0) + 1);
        }

        Node root = null;
        // Căutăm nodul care are gradul de intrare 0
        for (Map.Entry<Node, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                if (root != null) {
                    return null; // Mai mult de un nod fără intrări, nu putem determina rădăcina
                }
                root = entry.getKey(); // Setăm rădăcina
            }
        }

        return root;
    }
}