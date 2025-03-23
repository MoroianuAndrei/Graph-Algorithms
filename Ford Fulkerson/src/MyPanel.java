import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
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
    private Node nodeBeingDragged = null; // Nodul care este tras cu mouse-ul
    boolean isLeft = true;
    int source = -1;
    int sink;
    int[][] residualGraph;

    public MyPanel()
    {
        listaNoduri = new Vector<Node>();
        listaArce = new Vector<Arc>();
        // borderul panel-ului
        setBorder(BorderFactory.createLineBorder(Color.black));

        // Create buttons
        JButton maxFlowButton = new JButton("Flux Maxim");

        maxFlowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sourceStr = JOptionPane.showInputDialog("Introduceți nodul sursă:");
                String sinkStr = JOptionPane.showInputDialog("Introduceți nodul destinație:");

                source = Integer.parseInt(sourceStr) - 1; // Indexurile nodurilor încep de la 0
                sink = Integer.parseInt(sinkStr) - 1;

                int[][] capacityMatrix = buildCapacityMatrix();
                int maxFlow = fordFulkerson(capacityMatrix, source, sink);

                JOptionPane.showMessageDialog(null, "Fluxul maxim este: " + maxFlow);

                repaint();
            }
        });

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(maxFlowButton);

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
                    addNode(e.getX(), e.getY());
                }
                else if (isDragging && nodeBeingDragged == null && isLeft) // Adaug arc
                {
                    Node startNode = getNodeAt(pointStart);
                    Node endNode = getNodeAt(pointEnd);
                    if (startNode != null && endNode != null && startNode != endNode)
                    {
                        // Cerem utilizatorului capacitatea arcului
                        String capacityStr = JOptionPane.showInputDialog("Introduceți capacitatea arcului:");
                        if (capacityStr != null && !capacityStr.isEmpty())
                        {
                            try {
                                int capacity = Integer.parseInt(capacityStr);
                                if (capacity > 0) {
                                    Arc arc = new Arc(startNode, endNode);
                                    arc.setCapacity(capacity); // Setăm capacitatea pe arc
                                    listaArce.add(arc);
                                    repaint();
                                } else {
                                    JOptionPane.showMessageDialog(null, "Capacitatea trebuie să fie un număr pozitiv.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Introduceți un număr valid pentru capacitate.", "Eroare", JOptionPane.ERROR_MESSAGE);
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

    private int[][] buildCapacityMatrix() {
        int numNodes = listaNoduri.size();
        int[][] capacityMatrix = new int[numNodes][numNodes];

        // Inițializează matricea cu 0
        for (Arc arc : listaArce) {
            Node startNode = getNodeAtP(arc.getStartPoint());
            Node endNode = getNodeAtP(arc.getEndPoint());
            if (startNode != null && endNode != null) {
                int startIndex = listaNoduri.indexOf(startNode);
                int endIndex = listaNoduri.indexOf(endNode);
                capacityMatrix[startIndex][endIndex] = arc.getCapacity();
            }
        }

        return capacityMatrix;
    }

    private Set<Node> findMinimumCut(int[][] residualGraph, int source) {
        Set<Node> reachableNodes = new HashSet<>();
        boolean[] visited = new boolean[residualGraph.length];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        // Perform BFS to find reachable nodes
        while (!queue.isEmpty()) {
            int u = queue.poll();
            reachableNodes.add(listaNoduri.get(u)); // Map index to Node

            for (int v = 0; v < residualGraph.length; v++) {
                if (!visited[v] && residualGraph[u][v] > 0) {
                    visited[v] = true;
                    queue.add(v);
                }
            }
        }

        return reachableNodes;
    }

    private int fordFulkerson(int[][] capacityMatrix, int source, int sink) {
        int numNodes = capacityMatrix.length;
        residualGraph = new int[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                residualGraph[i][j] = capacityMatrix[i][j];
            }
        }

        int[] parent = new int[numNodes]; // Păstrează drumul augmentativ
        int maxFlow = 0; // Rezultatul final

        // Cât timp există un drum augmentativ
        while (bfs(residualGraph, source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;

            // Găsim fluxul minim din drum
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
            }

            // Actualizăm capacitățile reziduale
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
            }

            // Actualizăm fluxurile pe arce pentru afișare
            updateArcFlows(residualGraph, capacityMatrix);

            maxFlow += pathFlow;
        }

        return maxFlow;
    }

    private void updateArcFlows(int[][] residualGraph, int[][] capacityMatrix) {
        for (Arc arc : listaArce) {
            Node startNode = getNodeAtP(arc.getStartPoint());
            Node endNode = getNodeAtP(arc.getEndPoint());

            if (startNode != null && endNode != null) {
                int startIndex = listaNoduri.indexOf(startNode);
                int endIndex = listaNoduri.indexOf(endNode);

                // Calculăm fluxul ca diferență între capacitate și graful rezidual
                int capacity = capacityMatrix[startIndex][endIndex];
                int residualCapacity = residualGraph[startIndex][endIndex];
                int flow = capacity - residualCapacity;

                arc.setFlow(flow); // Setăm fluxul pe arc
            }
        }
        repaint(); // Re-redesenează pentru a afișa noile fluxuri
    }


    private boolean bfs(int[][] residualGraph, int source, int sink, int[] parent) {
        boolean[] visited = new boolean[residualGraph.length];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (int v = 0; v < residualGraph.length; v++) {
                if (!visited[v] && residualGraph[u][v] > 0) {
                    if (v == sink) {
                        parent[v] = u;
                        return true;
                    }
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }

        return false;
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

    //metoda care se apeleaza la eliberarea mouse-ului
    private void addNode(int x, int y) {
        // Check if the new node is far enough from existing nodes
        for (Node node : listaNoduri) {
            double distance = Math.sqrt(Math.pow(node.getCoordX() - x, 2) + Math.pow(node.getCoordY() - y, 2));
            if (distance < node_diam + node_diam / 10) {
                return; // Nu adaugă nodul, dacă este prea aproape de un nod existent
            }
        }
        Node newNode = new Node(x - node_diam / 2, y - node_diam / 2, nodeNr++);
        listaNoduri.add(newNode);
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
                a.drawArcWithHead(g);
        }
        //deseneaza arcul curent; cel care e in curs de desenare
        if (pointStart != null && nodeBeingDragged == null)
        {
            g.setColor(Color.RED);
            g.drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            repaint();
        }
        //deseneaza lista de noduri
//        for(int i=0; i<listaNoduri.size(); i++)
//        {
//            listaNoduri.elementAt(i).drawNode(g, node_diam);
//        }
        // Căutarea tăieturii minime
        if (residualGraph != null) {
            Set<Node> minCutNodes = findMinimumCut(residualGraph, source);

// Colorează nodurile din tăietura minimă
            for (Node node : listaNoduri) {
                    if (minCutNodes.contains(node)) {
                        g.setColor(Color.GREEN);  // Colorare noduri parte din tăietura minimă
                    } else {
                        g.setColor(Color.BLUE);  // Colorare noduri care nu fac parte din tăietura minimă
                    }
                    node.drawNode(g, node_diam);
            }
        }
        else {
            for (int i = 0; i < listaNoduri.size(); i++) {
                g.setColor(Color.RED);
                listaNoduri.elementAt(i).drawNode(g, node_diam);
            }
        }

		/*for (Node nod : listaNoduri)
		{
			nod.drawNode(g, node_diam, node_Diam);
		}*/
    }
}