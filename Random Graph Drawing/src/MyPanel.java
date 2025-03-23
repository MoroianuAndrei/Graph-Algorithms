import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.util.Vector;
import java.util.Random;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.event.*;
import java.awt.Graphics2D;

public class MyPanel extends JPanel {
    private int nodeNr;
    private int node_diam = 30;
    private Vector<Node> listaNoduri;
    private Vector<Arc> listaArce;
    private boolean isDirected = false;
    private double probabilitateArc = 0.5; // Probabilitate P
    private Random random = new Random();
    private double zoomFactor = 1.0; // Factorul de zoom
    private double translateX = 0.0, translateY = 0.0; // Translație pentru zoom

    public MyPanel() {
        listaNoduri = new Vector<>();
        listaArce = new Vector<>();

        // Create buttons
        JButton directedButton = new JButton("Graf Orientat");
        JButton undirectedButton = new JButton("Graf Neorientat");
        JButton randomGraphButton = new JButton("Generare Graf Aleator");
        JButton zoomInButton = new JButton("Zoom In");
        JButton zoomOutButton = new JButton("Zoom Out");

        // Add action listeners to buttons
        directedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isDirected = true;
                repaint();
            }
        });

        undirectedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isDirected = false;
                repaint();
            }
        });

        randomGraphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Introdu numărul de noduri:");
                try {
                    int numNoduri = Integer.parseInt(input);
                    generateRandomGraph(numNoduri);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Numărul introdus nu este valid.");
                }
            }
        });

        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomFactor *= 1.2;  // Creștem factorul de zoom
                repaint();
            }
        });

        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomFactor /= 1.2;  // Scădem factorul de zoom
                repaint();
            }
        });

        // Create a panel for buttons and slider for probability
        JPanel controlPanel = new JPanel();
        controlPanel.add(directedButton);
        controlPanel.add(undirectedButton);
        controlPanel.add(randomGraphButton);
        controlPanel.add(zoomInButton);  // Adăugăm butonul de zoom in
        controlPanel.add(zoomOutButton); // Adăugăm butonul de zoom out

        // Slider pentru setarea probabilității P
        JSlider probabilitateSlider = new JSlider(0, 100, 50);
        probabilitateSlider.setMajorTickSpacing(25);
        probabilitateSlider.setPaintTicks(true);
        probabilitateSlider.setPaintLabels(true);
        probabilitateSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                probabilitateArc = probabilitateSlider.getValue() / 100.0;
            }
        });
        controlPanel.add(new JLabel("Probabilitate P:"));
        controlPanel.add(probabilitateSlider);

        // Setăm layout-ul principal
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH); // Adăugăm zona de control (butoanele) deasupra
        add(new DrawingPanel(), BorderLayout.CENTER); // Zona de desenare separată
    }

    private void generateRandomGraph(int numNoduri) {
        listaNoduri.clear();
        listaArce.clear();

        // Calculăm dimensiunea diametrului nodurilor pe baza numărului total de noduri
        adjustNodeDiameter(numNoduri);

        // Generăm pozițiile aleatorii pentru noduri
        for (int i = 0; i < numNoduri; i++) {
            Node node = repositioning(new Node(random.nextInt(getWidth()), random.nextInt(getHeight()), i + 1));
            listaNoduri.add(node);
        }

        // Generăm arcele pe baza probabilității P
        for (int i = 0; i < numNoduri; i++) {
            for (int j = i + 1; j < numNoduri; j++) {
                if (random.nextDouble() < probabilitateArc) {
                    Arc arc = new Arc(listaNoduri.get(i), listaNoduri.get(j));
                    listaArce.add(arc);
                }
            }
        }

        repaint();
    }

    private void adjustNodeDiameter(int numNoduri) {
        // Ajustăm diametrul nodurilor în funcție de numărul total de noduri
        int maxNodeDiam = 30; // Dimensiunea maximă pentru diametrul nodurilor
        int med_maxNodeDiam = 25;
        int med1NodeDiam = 20;
        int med2NodeDiam = 15;
        int med_minNodeDiam = 10;
        int minNodeDiam = 5;  // Dimensiunea minimă pentru diametrul nodurilor

        // Calculăm diametrul nodurilor în funcție de numărul de noduri
        if (numNoduri <= 370) {
            node_diam = maxNodeDiam;  // Noduri mari pentru număr mic de noduri
        } else if (numNoduri <= 525) {
            node_diam = med_maxNodeDiam; // Reducem diametrul gradual
        } else if (numNoduri <= 840) {
            node_diam = med1NodeDiam;
        } else if (numNoduri <= 1550) {
            node_diam = med2NodeDiam;
        } else if (numNoduri <= 3000) {
            node_diam = med_minNodeDiam;
        } else {
            node_diam = minNodeDiam;
        }
    }

    private Node repositioning(Node node) {
        // Verificăm până găsim o poziție care nu se suprapune
        int maxAttempts = 1000; // Limitează numărul de încercări
        int minDistance = (int) (node_diam * 1.5); // Setăm o distanță minimă între noduri

        while (maxAttempts-- > 0) {
            boolean overlap = false;
            for (Node n : listaNoduri) {
                // Verificăm dacă distanța dintre noduri este mai mică decât distanța minimă
                if (distance(n, node) < minDistance) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                return node;
            }
            // Dacă există suprapunere, repoziționăm nodul într-o locație nouă aleatorie
            node.setCoordX(random.nextInt(getWidth() - node_diam));
            node.setCoordY(random.nextInt(getHeight() - node_diam));
        }
        return node; // Returnăm nodul în poziția găsită sau ultima poziție testată
    }

    private double distance(Node n1, Node n2) {
        return Math.sqrt(Math.pow(n1.getCoordX() - n2.getCoordX(), 2) + Math.pow(n1.getCoordY() - n2.getCoordY(), 2));
    }

    // Creăm o clasă internă pentru desenare
    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Cast la Graphics2D pentru a folosi metodele scale și translate
            Graphics2D g2d = (Graphics2D) g;

            // Translate + Zoom aplicate pe întreaga diagramă
            g2d.translate(translateX, translateY);  // Aplicați translația dacă este cazul
            g2d.scale(zoomFactor, zoomFactor);      // Aplicați zoom-ul

            int scaledNodeDiam = (int) (node_diam); // Dimensiune neschimbată pentru noduri, deoarece g2d.scale() aplică zoom

            for (Arc a : listaArce) {
                if (isDirected) a.drawArcWithHead(g2d);
                else a.drawArdWithoutHead(g2d);
            }

            for (Node nod : listaNoduri) {
                nod.drawNode(g2d, scaledNodeDiam);  // Desenăm nodurile cu dimensiunea scalată
            }
        }
    }
}