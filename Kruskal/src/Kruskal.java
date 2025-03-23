import java.awt.Point;
import java.util.*;

public class Kruskal {
    private List<Arc> arcs;
    private UnionFind uf;
    private List<Node> nodes;
    int node_diam;
    int total_cost;

    public Kruskal(List<Arc> arcs, int n, List<Node> nodes, int node_diam) {
        this.arcs = arcs;
        this.uf = new UnionFind(n);  // n este numărul de noduri
        this.nodes = nodes;
        this.node_diam = node_diam;
        total_cost = 0;
        addNumberToArcs();
    }

    public List<Arc> execute() {
        List<Arc> mst = new ArrayList<>();

        // Sortează muchiile în ordine crescătoare a costului
        Collections.sort(arcs, Comparator.comparingInt(Arc::getCost));

        for (Arc arc : arcs) {
            int u = arc.getStartNumber() - 1;
            int v = arc.getEndNumber() - 1;

            if (uf.union(u, v)) {
                mst.add(arc); // Adăugăm arc în MST
                total_cost += arc.getCost();
            }
        }

        printCost();

        return mst;
    }

    private Node getNodeAtP(Point p)
    {
        for (Node node : nodes)
        {
            double distance = Math.sqrt(Math.pow(node.getCoordX() - p.x + node_diam/2, 2) + Math.pow(node.getCoordY() - p.y + node_diam/2, 2));
            if (distance <= node_diam) // Aici nu impartim la 2, deoarece ne intereseaza mijlocul pentru arce
            { // Verificăm dacă punctul este în interiorul diametrului nodului
                return node;
            }
        }
        return null;
    }

    private void addNumberToArcs(){
        for(Arc arc : arcs){
            arc.setStartNumber(getNodeAtP(arc.getStartPoint()).getNumber());
            arc.setEndNumber(getNodeAtP(arc.getEndPoint()).getNumber());
        }
    }

    public void printCost(){
        System.out.println("Costul total este: " + total_cost);
    }
}