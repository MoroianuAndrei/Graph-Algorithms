import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class Graph {
    private int nrNodes; // Numărul de noduri
    private List<Edge>[] graph; // Lista de adiacență pentru graf
    private List<Node> nodes; // Lista de noduri

    public int getNrNodes(){
        return nrNodes;
    }

    public Graph(List<List<Integer>> matrix) {
        this.nrNodes = 0;
        int rows = matrix.size();
        int cols = matrix.get(0).size();
        this.graph = new ArrayList[rows * cols];
        this.nodes = new ArrayList<>();

        int nodeIndex = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix.get(i).get(j) == 1) {
                    Node node = new Node(i, j, nodeIndex);
                    nodes.add(node);
                    graph[nodeIndex] = new ArrayList<>(); // Inițializare lista de adiacență

                    // Adăugare noduri adiacente
                    if (i > 0 && matrix.get(i - 1).get(j) == 1) { // sus
                        Node adjacentNode = getNodeAt(i - 1, j); // Obține nodul existent
                        addEdgeBetweenNodes(node, adjacentNode);
                        addEdgeBetweenNodes(adjacentNode, node);
                    }
                    if (j > 0 && matrix.get(i).get(j - 1) == 1) { // stânga
                        Node adjacentNode = getNodeAt(i, j - 1); // Obține nodul existent
                        addEdgeBetweenNodes(node, adjacentNode);
                        addEdgeBetweenNodes(adjacentNode, node);
                    }

                    nodeIndex++;
                }
            }
        }
        nrNodes = nodeIndex; // Actualizare nrNodes după procesarea matricei
    }

    public Node getNodeAt(int x, int y) {
        for (Node node : nodes) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }
        return null; // Dacă nodul nu este găsit
    }

    public Node getNodeP(int position){
        for (Node node : nodes) {
            if (node.getPosition() == position) {
                return node;
            }
        }
        return null; // Dacă nodul nu este găsit
    }

    public void addEdgeBetweenNodes(Node start, Node end) {
            Edge edge = new Edge(start, end);
            addEdge(edge);
    }

    public void addEdge(Edge edge) {
        int position = edge.getStart().getPosition();
        this.graph[position].add(edge);
        System.out.println("Arc adăugat: " + edge.getStart().getPosition() + " -> " + edge.getEnd().getPosition());
    }

    public void displayConnections() {
        System.out.println("Numărul total de noduri: " + nrNodes);
        for (int i = 0; i < nrNodes; i++) {
            System.out.print("Nodul " + i + " se leagă cu: ");
            if (graph[i] == null || graph[i].isEmpty()) {
                System.out.println("Niciun nod");
            } else {
                for (Edge edge : graph[i]) {
                    System.out.print(edge.getEnd().getPosition() + " ");
                }
                System.out.println();
            }
        }
    }

    public int[] bfs(Point startPoint) {
        Node startNode = getNodeAt(startPoint.x, startPoint.y); // Obține nodul din coordonatele punctului
        if (startNode == null) {
            System.out.println("Nodul specificat nu există în graf.");
            return null; // Iese din metodă dacă nodul nu există
        }

        boolean[] visited = new boolean[nrNodes]; // Marcăm nodurile vizitate
        int[] predecessors = new int[nrNodes]; // Vectorul de predecesori
        for (int i = 0; i < nrNodes; i++) {
            predecessors[i] = -1; // Inițializare predecesori cu -1
        }

        Queue<Node> queue = new LinkedList<>(); // Coada pentru BFS

        int startNodeIndex = startNode.getPosition();
        visited[startNodeIndex] = true; // Marcați nodul de început ca vizitat
        predecessors[startNodeIndex] = -1; // Predecesorul nodului de început este -1
        queue.add(startNode); // Adăugați nodul de început în coadă

        System.out.println("Parcurgerea în lățime (BFS) începând de la nodul (" + startPoint.x + ", " + startPoint.y + "):");

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll(); // Scoateți nodul din coadă
            System.out.print(currentNode.getPosition() + " "); // Afișați nodul curent

            // Adăugați nodurile adiacente în coadă
            int currentIndex = currentNode.getPosition();
            for (Edge edge : graph[currentIndex]) {
                Node adjacentNode = edge.getEnd();
                int adjacentIndex = adjacentNode.getPosition();
                if (!visited[adjacentIndex]) {
                    visited[adjacentIndex] = true; // Marcați ca vizitat
                    predecessors[adjacentIndex] = currentIndex; // Actualizați predecesorul
                    queue.add(adjacentNode); // Adăugați în coadă
                }
            }
        }

        System.out.println(); // Linia nouă după finalizarea BFS

        // Opțional: Afișați vectorul de predecesori
        System.out.println("Vectorul de predecesori:");
        for (int i = 0; i < nrNodes; i++) {
            System.out.println("Nodul " + i + ": Predecesor = " + predecessors[i]);
        }

        return predecessors;
    }
}
