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

        // Creează butonul "Topological Sort"
        JButton conexComponentButton = new JButton("Paint Conex Component");
        conexComponentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                conexComponent();
            }
        });

        // Creează un panou pentru butonul "Topological Sort" și îl adaugă în partea de jos
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(conexComponentButton);

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

    boolean verifyYinU(Arc arc, Vector<Node> U)
    {
        for(Node node : U)
            if(arc.getEndNode().getCoordX() == node.getCoordX() && arc.getEndNode().getCoordY() == node.getCoordY())
                return true;
        return false;
    }

    boolean verifyXinU(Arc arc, Vector<Node> U)
    {
        for(Node node : U)
            if(arc.getStartNode().getCoordX() == node.getCoordX() && arc.getStartNode().getCoordY() == node.getCoordY())
                return true;
        return false;
    }

    private int Uindex(Vector<Node> U, int number){
        for(int i=0; i<U.size(); i++){
            if(U.get(i).getNumber() == number)
                return i;
        }
        return -1;
    }

    private boolean verify2() {
        int numNodes = listaNoduri.size();
        int[] inDegree = new int[numNodes];
        Arrays.fill(inDegree, 0);

        // Calculăm gradele de intrare pentru fiecare nod
        for (Arc arc : listaArce) {
            Node endNode = getNodeAtP(arc.getEndPoint());
            if (endNode != null) {
                int endIndex = listaNoduri.indexOf(endNode);
                inDegree[endIndex]++;
            }
        }

        // Coada pentru nodurile cu grad de intrare 0
        Queue<Node> queue = new LinkedList<>();
        for (int i = 0; i < numNodes; i++) {
            if (inDegree[i] == 0) {
                queue.add(listaNoduri.get(i));
            }
        }

        // Contor pentru nodurile procesate
        int processedNodes = 0;

        // Procesăm nodurile
        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            processedNodes++;

            // Scădem gradul de intrare al nodurilor vecine
            for (Arc arc : listaArce) {
                if (getNodeAtP(arc.getStartPoint()) == currentNode) {
                    Node endNode = getNodeAtP(arc.getEndPoint());
                    if (endNode != null) {
                        int endIndex = listaNoduri.indexOf(endNode);
                        inDegree[endIndex]--;
                        if (inDegree[endIndex] == 0) {
                            queue.add(endNode);
                        }
                    }
                }
            }
        }

        // Verificăm dacă am procesat toate nodurile (dacă nu, există un ciclu)
        return processedNodes == numNodes;
    }

    private boolean verify(Vector<Integer> t1, Vector<Integer> t2){
        for (Arc arc : listaArce) {
            int startNumber = getNumber(arc.getStartNode()) - 1;
            int endNumber = getNumber(arc.getEndNode()) - 1;

            if (t1.get(startNumber) > t1.get(endNumber) ||
                    t1.get(endNumber) > t2.get(endNumber) ||
                    t2.get(endNumber) > t2.get(startNumber)) {
                return false;
            }
        }
        return true;
    }

    protected void printConex(Vector<Node> W){
        Graphics g = getGraphics();
        for(int i=0; i<W.size(); i++){
            W.get(i).drawNode(g, node_diam);
        }
    }

    private Node getNodeAtNumber(int number){
        for(Node node:listaNoduri){
            if(node.getNumber() == number)
                return node;
        }
        return null;
    }

    // SORTAREA TOPOLOGICA EXEMPLUL 2
    public void conexComponent() {
        Color[] culori = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.MAGENTA};
        int indexCuloare = 0;

        Integer S = 0;

        int contor = -1;

        Vector<Node> U = new Vector<>(listaNoduri);
        U.remove(listaNoduri.get(S));

        // Adăugăm primul element în V
        Stack<Node> V =new Stack<>();
        V.add(listaNoduri.get(S));

        Vector<Node> W = new Vector<>();
        W.add(listaNoduri.get(0));

        Vector<Integer> t1 = new Vector<>(listaNoduri.size());

        Vector<Integer> t2 = new Vector<>(listaNoduri.size());

        Vector<Integer> p = new Vector<>(listaNoduri.size());

        for(int i=0; i<listaNoduri.size(); i++)
        {
            t1.add(Integer.MAX_VALUE);
            t2.add(Integer.MAX_VALUE);
            p.add(S,0);
        }

        t1.set(S, 1);
        int t = 1;

        while (!W.isEmpty()) {
            contor++;
            W.clear();
            while (!V.isEmpty()) {
                Node x = V.peek();
                boolean exist = false;

                for (Arc arc : listaArce) {
                    if (arc.getStartNode().equalsByPosition(x) && verifyYinU(arc, U) && !exist) {
//                        System.out.println("EXISTA ARC DE LA: " + getNumber(arc.getStartNode()) + " LA " + getNumber(arc.getEndNode()));
                        Node endNode = arc.getEndNode();
                        int number = getNumber(endNode);
                        endNode.setNumber(number);
                        U.remove(Uindex(U, getNumber(endNode)));
                        V.add(endNode);
                        t++;
                        t1.set(getNumber(endNode) - 1, t);
                        p.set(getNumber(endNode) - 1, x.getNumber());
                        exist = true;
                    } else if (arc.getEndNode().equalsByPosition(x) && verifyXinU(arc, U) && !exist) {
//                        System.out.println("EXISTA ARC DE LA: " + getNumber(arc.getStartNode()) + " LA " + getNumber(arc.getEndNode()));
                        Node startNode = arc.getStartNode();
                        int number = getNumber(startNode);
                        startNode.setNumber(number);
                        U.remove(Uindex(U, getNumber(startNode)));
                        V.add(startNode);
                        t++;
                        t1.set(getNumber(startNode) - 1, t);
                        p.set(getNumber(startNode) - 1, x.getNumber());
                        exist = true;
                    }
                }
                if (!exist) {
                    V.pop();
                    W.add(x);
                    t++;
                    t2.set(x.getNumber() - 1, t);
                }
            }

            for(int i=0; i<W.size(); i++){
                W.get(i).setColor(culori[indexCuloare % culori.length]);
            }

            // Colorează componenta conexă
            printConex(W);
            System.out.println("W: " + W);
            for(int i=0; i<W.size(); i++) {
                listaNoduri.get(W.get(i).getNumber()-1).setColor(culori[indexCuloare % culori.length]);
//                System.out.println("NODUL: " + W.get(i).getNumber() + " " + W.get(i).getColor() + " ");
            }

            indexCuloare++;

            if (!U.isEmpty()) {
                S = U.get(0).getNumber() - 1;
                V.add(U.get(0));
                U.remove(0);
                t++;
                t1.set(S, t);
            }
        }
        if(contor == 1)
            System.out.println("EXISTA O COMPONENTA CONEXA");
        else
            System.out.println("EXISTA " + contor + " COMPONENETE CONEXE");
    }
}