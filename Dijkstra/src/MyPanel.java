import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MyPanel extends JPanel {
    private List<Node> nodes;
    private List<Arc> arcs;
    private Node selectedNode1 = null;
    private Node selectedNode2 = null;
    Map<Node, List<Pair<Node, Integer>>> adjacencyList = new HashMap<>();
    private List<Integer> shortestPath = new ArrayList<>(); // Listă pentru drumul cel mai scurt

    public MyPanel() {
        GraphParser parser = new GraphParser();
        parser.parseXML("src/hartaLuxembourg.xml");

        this.nodes = parser.getNodes();
        this.arcs = parser.getArcs();

        // Adaugă ascultător pentru clicuri de mouse
        addMouseListener(new MouseAdapter() {
            // În cadrul metodei mouseClicked
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                // Găsește nodul cel mai apropiat
                Node closestNode = findClosestNode(mouseX, mouseY);

                // Setează nodurile selectate
                if (selectedNode1 == null) {
                    selectedNode1 = closestNode;
                } else if (selectedNode2 == null) {
                    selectedNode2 = closestNode;
                } else {
                    // Resetează dacă ambele noduri sunt deja selectate
                    selectedNode1 = closestNode;
                    selectedNode2 = null;
                }

                // Resetează drumul cel mai scurt înainte de a recalcula
                shortestPath.clear(); // Curăță drumul anterior

                // Re-desenează panoul pentru a afișa nodurile selectate și drumul
                repaint();

                // Afișează nodul selectat
                System.out.println("Nod selectat: ID=" + closestNode.getNumber());
            }

        });

        adjacencyList = makeList();

        // Crearea unui buton pentru a apela metoda Dijkstra
        JButton dijkstraButton = new JButton("Calculare Dijkstra");
        dijkstraButton.setBounds(10, 10, 150, 30); // Setează poziția și dimensiunea butonului
        dijkstraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dijkstra(); // Apelarea metodei Dijkstra
                repaint();  // Re-desenează panoul pentru a afișa drumul cel mai scurt
            }
        });

        // Adaugă butonul în panou
        this.setLayout(null); // Setează layout-ul pentru a permite poziționarea manuală
        this.add(dijkstraButton);
    }

    private Node findClosestNode(int x, int y) {
        Node closestNode = null;
        double minDistance = Double.MAX_VALUE;

        // Obține dimensiunea panoului
        Dimension size = getSize();
        int panelWidth = size.width;
        int panelHeight = size.height;

        // Calculăm limitele coordonatelor
        int minX = nodes.stream().mapToInt(Node::getLongitude).min().orElse(0);
        int maxX = nodes.stream().mapToInt(Node::getLongitude).max().orElse(1);
        int minY = nodes.stream().mapToInt(Node::getLatitude).min().orElse(0);
        int maxY = nodes.stream().mapToInt(Node::getLatitude).max().orElse(1);

        double scaleX = panelWidth / (double) (maxX - minX);
        double scaleY = panelHeight / (double) (maxY - minY);

        int offsetX = (int) ((panelWidth - scaleX * (maxX - minX)) / 2);
        int offsetY = (int) ((panelHeight - scaleY * (maxY - minY)) / 2);

        // Găsește nodul cel mai apropiat
        for (Node node : nodes) {
            int nodeX = (int) ((node.getLongitude() - minX) * scaleX + offsetX);
            int nodeY = (int) ((node.getLatitude() - minY) * scaleY + offsetY);

            double distance = Math.sqrt(Math.pow(x - nodeX, 2) + Math.pow(y - nodeY, 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestNode = node;
            }
        }

        return closestNode;
    }

    private void Dijkstra() {
        shortestPath.clear();

        if (selectedNode1 == null || selectedNode2 == null) {
            return;
        }

        Map<Integer, Integer> d = new HashMap<>();
        Map<Integer, Integer> p = new HashMap<>();
        for (Node node : nodes) {
            d.put(node.getNumber(), Integer.MAX_VALUE);
            p.put(node.getNumber(), 0);
        }

        PriorityQueue<Node> W = new PriorityQueue<>(Comparator.comparingInt(n -> d.get(n.getNumber())));
        W.add(selectedNode1);

        while (!W.isEmpty()) {
            Node x = W.poll();
            if (x.equals(selectedNode2)) break;

            for (Pair<Node, Integer> y : adjacencyList.getOrDefault(x, Collections.emptyList())) {
                int newDist = d.get(x.getNumber()) + y.getSecond();
                if (d.get(y.getFirst().getNumber()) > newDist) {
                    d.put(y.getFirst().getNumber(), newDist);
                    p.put(y.getFirst().getNumber(), x.getNumber());
                    W.remove(y.getFirst());
                    W.add(y.getFirst());
                }
            }
        }

        Deque<Integer> path = new ArrayDeque<>();
        int k = selectedNode2.getNumber();
        while (k != selectedNode1.getNumber()) {
            path.addFirst(k);
            k = p.get(k);
        }
        path.addFirst(selectedNode1.getNumber());
        shortestPath.addAll(path);
    }

    private Map<Node, List<Pair<Node, Integer>>> makeList()  {
        Map<Node, List<Pair<Node, Integer>>> adjacencyList = new HashMap<>();

        // Inițializare liste goale pentru fiecare nod
        for (Node node : nodes) {
            adjacencyList.put(node, new ArrayList<>());
        }

        // Parcurge toate arcele și adaugă nodurile adiacente cu costul
        for (Arc arc : arcs) {
            Node startNode = arc.getStartNode();
            Node endNode = arc.getEndNode();
            int cost = arc.getLength(); // presupunând că costul este lungimea arcului

            // Adaugă nodul de destinație și costul în lista de adiacență a nodului de start
            adjacencyList.get(startNode).add(new Pair<>(endNode, cost));
        }

        return adjacencyList;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (nodes == null || nodes.isEmpty() || arcs == null || arcs.isEmpty()) {
            System.out.println("Nu există noduri sau arce de desenat!");
            return;
        }

        // Obține dimensiunea panoului
        Dimension size = getSize();
        int panelWidth = size.width;
        int panelHeight = size.height;

        // Calculăm limitele coordonatelor nodurilor
        int minX = nodes.stream().mapToInt(Node::getLongitude).min().orElse(0);
        int maxX = nodes.stream().mapToInt(Node::getLongitude).max().orElse(1);
        int minY = nodes.stream().mapToInt(Node::getLatitude).min().orElse(0);
        int maxY = nodes.stream().mapToInt(Node::getLatitude).max().orElse(1);

        // Calculăm dimensiunile graficului
        int graphWidth = maxX - minX;
        int graphHeight = maxY - minY;

        // Calculăm scalarea pe lățime și înălțime
        double scaleX = panelWidth / (double) graphWidth;  // scalare pe lățime
        double scaleY = panelHeight / (double) graphHeight;  // scalare pe înălțime

        // Calculăm offset-urile pentru a centra graficul
        int offsetX = (int) ((panelWidth - scaleX * graphWidth) / 2);
        int offsetY = (int) ((panelHeight - scaleY * graphHeight) / 2);

        // Desenează arcele fără a le scala
        for (Arc arc : arcs) {
            g.setColor(Color.BLACK);
            int x1 = arc.getStartNode().getLongitude();
            int y1 = arc.getStartNode().getLatitude();
            int x2 = arc.getEndNode().getLongitude();
            int y2 = arc.getEndNode().getLatitude();

            // Aplicăm doar scalarea pentru pozițiile nodurilor
            x1 = (int) ((x1 - minX) * scaleX + offsetX);
            y1 = (int) ((y1 - minY) * scaleY + offsetY);
            x2 = (int) ((x2 - minX) * scaleX + offsetX);
            y2 = (int) ((y2 - minY) * scaleY + offsetY);

            // Desenăm linia între noduri
            g.drawLine(x1, y1, x2, y2);
        }

        // Desenează nodurile selectate (dacă sunt)
        if (selectedNode1 != null) {
            int x = (int) ((selectedNode1.getLongitude() - minX) * scaleX + offsetX);
            int y = (int) ((selectedNode1.getLatitude() - minY) * scaleY + offsetY);
            g.setColor(Color.RED);
            g.fillOval(x - 5, y - 5, 10, 10);
        }

        if (selectedNode2 != null) {
            int x = (int) ((selectedNode2.getLongitude() - minX) * scaleX + offsetX);
            int y = (int) ((selectedNode2.getLatitude() - minY) * scaleY + offsetY);
            g.setColor(Color.RED);
            g.fillOval(x - 5, y - 5, 10, 10);
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setStroke(new BasicStroke(2));

        if(shortestPath != null){
            for(int i=0;i<shortestPath.size()-1;i++){
                for (Arc arc : arcs) {
                    if (arc.getStartNode().getNumber() == shortestPath.get(i) && arc.getEndNode().getNumber() == shortestPath.get(i+1)) {
                        g2d.setColor(Color.RED);
                        int x1 = arc.getStartNode().getLongitude();
                        int y1 = arc.getStartNode().getLatitude();
                        int x2 = arc.getEndNode().getLongitude();
                        int y2 = arc.getEndNode().getLatitude();

                        // Aplicăm doar scalarea pentru pozițiile nodurilor
                        x1 = (int) ((x1 - minX) * scaleX + offsetX);
                        y1 = (int) ((y1 - minY) * scaleY + offsetY);
                        x2 = (int) ((x2 - minX) * scaleX + offsetX);
                        y2 = (int) ((y2 - minY) * scaleY + offsetY);

                        // Desenăm linia între noduri
                        g2d.drawLine(x1, y1, x2, y2);
                        break; // Am găsit arcul, nu mai este necesar să căutăm în continuare
                    }
                }
            }
        }
    }
}
