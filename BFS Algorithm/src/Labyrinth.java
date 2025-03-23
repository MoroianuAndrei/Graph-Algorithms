import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Labyrinth extends JPanel {
    private List<List<Integer>> matrix;
    private Point start = null;
    private Integer width;
    private Integer height;
    private Integer rows;
    private Integer columns;
    private JButton nextButton;
    private List<List<Point>> paths; // Lista drumurilor sub forma de coordonate Point
    private List<Point> inaccessibleEdges; // Lista pentru marginile inaccesibile
    private int currentPathIndex;
    private Graph graph;

    public Labyrinth() {
        matrix = new ArrayList<>();
        this.readFromFile();

        this.rows = matrix.size();
        this.columns = matrix.get(0).size();
        this.width = this.columns * 100;
        this.height = this.rows * 100;
        this.setPreferredSize(new Dimension(this.width, this.height));
        this.setLayout(new BorderLayout());

        JPanel labyrinthPanel = new JPanel();
        labyrinthPanel.setLayout(new GridLayout(this.rows, this.columns));

        paths = new ArrayList<>();
        inaccessibleEdges = new ArrayList<>(); // Inițializare lista de margini inaccesibile
        currentPathIndex = 0;

        findAllPaths();
        initializeLabyrinthPanels(labyrinthPanel);

        this.add(labyrinthPanel, BorderLayout.CENTER);

        nextButton = new JButton("Next");
        this.add(nextButton, BorderLayout.SOUTH);

        nextButton.addActionListener(e -> {
            if (paths.isEmpty()) {
                System.out.println("No paths to display.");
                return;
            }

            if (currentPathIndex < paths.size()) {
                colorizePath(labyrinthPanel, paths.get(currentPathIndex));
                currentPathIndex++;
            }

            // Resetăm la început când toate drumurile au fost afișate
            if (currentPathIndex == paths.size()) {
                currentPathIndex = 0;
            }
        });
    }

    private void initializeLabyrinthPanels(JPanel labyrinthPanel) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                JPanel panel = new JPanel();
                boolean isWall = matrix.get(i).get(j) == 0;
                Point point = new Point(i, j);

                if (!isWall && i == start.x && j == start.y) {
                    panel.setBackground(Color.BLUE); // Punct de start
                } else if (isWall) {
                    panel.setBackground(Color.BLACK); // Zid
                } else if (inaccessibleEdges.contains(point)) {
                    panel.setBackground(Color.RED); // Margine inaccesibilă
                } else {
                    panel.setBackground(Color.WHITE); // Drum
                }

                panel.setBorder(new LineBorder(Color.CYAN, 1));
                panel.setSize(100, 100);
                labyrinthPanel.add(panel);
            }
        }
    }

    private void findAllPaths() {
        graph = new Graph(matrix);
        int[] predecessors = graph.bfs(start);
        int pathCount = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Point point = new Point(i, j);
                Node node = graph.getNodeAt(i, j);

                if (node != null) {
                    if ((predecessors[node.getPosition()] != -1 && isOnEdge(point, rows, columns)) || (node.getX() == start.x && node.getY() == start.y && isOnEdge(new Point(node.getX(), node.getY()), rows, columns))) {
                        paths.add(constructPath(node, predecessors));
                        pathCount++;
                    } else if (predecessors[node.getPosition()] == -1 && isOnEdge(point, rows, columns)) {
                        inaccessibleEdges.add(point); // Adăugăm punctul inaccesibil la lista de margini inaccesibile
                    }
                }
            }
        }
        System.out.println("Found " + pathCount + " paths to the edge.");
    }

    private List<Point> constructPath(Node node, int[] predecessors) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(new Point(node.getX(), node.getY()));
            node = graph.getNodeP(predecessors[node.getPosition()]);
        }
        return path;
    }

    private void colorizePath(JPanel labyrinthPanel, List<Point> path) {
        // Resetăm culorile pentru a permite colorarea unui nou drum
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                JPanel panel = (JPanel) labyrinthPanel.getComponent(i * columns + j);
                boolean isWall = matrix.get(i).get(j) == 0;
                Point point = new Point(i, j);

                if (!isWall && i == start.x && j == start.y) {
                    panel.setBackground(Color.BLUE);
                } else if (isWall) {
                    panel.setBackground(Color.BLACK);
                } else if (inaccessibleEdges.contains(point)) {
                    panel.setBackground(Color.RED); // Colorăm marginile inaccesibile în roșu
                } else {
                    panel.setBackground(Color.WHITE);
                }
            }
        }

        // Colorăm drumul curent
        for (Point point : path) {
            if(!point.equals(start) || path.size() == 1) {
                int x = point.x;
                int y = point.y;
                JPanel panel = (JPanel) labyrinthPanel.getComponent(x * columns + y);
                panel.setBackground(Color.GREEN); // Colorăm drumul în verde
            }
        }
    }

    public boolean isOnEdge(Point point, int rows, int columns) {
        int x = point.x;
        int y = point.y;

        return (x == 0 || x == rows - 1 || y == 0 || y == columns - 1);
    }

    private void readFromFile() {
        String filename = "labyrinth.txt";
        this.start = readPointAndMatrix(filename, this.matrix);
    }

    public static Point readPointAndMatrix(String filename, List<List<Integer>> matrix) {
        Point startPoint = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            if (line != null) {
                String[] coordinates = line.trim().split("\\s+");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                startPoint = new Point(x, y);
            }

            String lineMatrix;
            while ((lineMatrix = br.readLine()) != null) {
                String[] values = lineMatrix.trim().split("\\s+");
                List<Integer> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Integer.parseInt(value));
                }
                matrix.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return startPoint;
    }
}
