import javax.xml.stream.*;
import java.io.FileInputStream;
import java.util.*;

public class GraphParser {
    private final List<Node> nodes;
    private final List<Arc> arcs;

    public GraphParser() {
        nodes = new ArrayList<>();
        arcs = new ArrayList<>();
    }

    public void parseXML(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            Map<Integer, Node> nodeMap = new HashMap<>();

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();

                    if ("node".equals(elementName)) {
                        try {
                            int id = Integer.parseInt(reader.getAttributeValue(null, "id"));
                            int longitude = Integer.parseInt(reader.getAttributeValue(null, "longitude"));
                            int latitude = Integer.parseInt(reader.getAttributeValue(null, "latitude"));
                            Node node = new Node(longitude, latitude, id);
                            nodes.add(node);
                            nodeMap.put(id, node); // Stocare pentru arce
                        } catch (Exception e) {
                            System.err.println("Error parsing node: " + e.getMessage());
                        }
                    } else if ("arc".equals(elementName)) {
                        try {
                            int from = Integer.parseInt(reader.getAttributeValue(null, "from"));
                            int to = Integer.parseInt(reader.getAttributeValue(null, "to"));
                            int length = Integer.parseInt(reader.getAttributeValue(null, "length"));

                            Node startNode = nodeMap.get(from);
                            Node endNode = nodeMap.get(to);

                            if (startNode == null || endNode == null) {
                                System.err.println("Error: Node not found for Arc (From=" + from + ", To=" + to + ")");
                            } else {
                                arcs.add(new Arc(startNode, endNode, length));
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing arc: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing the XML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Arc> getArcs() {
        return arcs;
    }
}