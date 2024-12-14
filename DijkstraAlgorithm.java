import java.util.*;

public class DijkstraAlgorithm {
    private List<List<Pair<Integer, Integer>>> graph;
    private int numVertices;

    public DijkstraAlgorithm(int n) {
        this.numVertices = n;
        this.graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
    }

    // Get the number of vertices in the graph
    public int size() {
        return numVertices;
    }

    // Get the adjacency list of the graph
    public List<List<Pair<Integer, Integer>>> getGraph() {
        return graph;
    }

    // Add an edge to the graph
    public void addEdge(int u, int v, int weight) {
        graph.get(u).add(new Pair<>(v, weight));
        graph.get(v).add(new Pair<>(u, weight));
    }

    // Dijkstra's algorithm using Fibonacci heap or Leftist tree
    public List<Integer> dijkstraFibonacci(int source) {
        FibonacciHeap fh = new FibonacciHeap();
        List<Integer> minDist = new ArrayList<>(Collections.nCopies(numVertices, Integer.MAX_VALUE));
        minDist.set(source, 0);

        List<FibonacciHeap.Node> nodes = new ArrayList<>(numVertices);
        // Insert all vertices into the Fibonacci heap
        for (int i = 0; i < numVertices; i++) {
            nodes.add(fh.insert(minDist.get(i), i));
        }

        // Perform Dijkstra's algorithm
        while (!fh.isEmpty()) {
            FibonacciHeap.Node minNode = fh.extractMin();
            int u = minNode.data;

            for (Pair<Integer, Integer> edge : graph.get(u)) {
                int v = edge.getKey();
                int weight = edge.getValue();
                if (minDist.get(u) + weight < minDist.get(v)) {
                    minDist.set(v, minDist.get(u) + weight);
                    fh.decreaseKey(nodes.get(v), minDist.get(v));
                }
            }
        }
        return minDist;
    }

    // Dijkstra's algorithm using Leftist tree
    public List<Integer> dijkstraLeftist(int source) {
        LeftistTree lt = new LeftistTree();
        List<Integer> minDist = new ArrayList<>(Collections.nCopies(numVertices, Integer.MAX_VALUE));
        minDist.set(source, 0);

        Map<Integer, Node> nodes = new HashMap<>();
        // Insert all vertices into the Leftist tree
        for (int i = 0; i < numVertices; i++) {
            nodes.put(i, new Node(minDist.get(i), i));
            Node newNode = new Node(i,minDist.get(i));
            lt.insert(newNode);
        }

        // Perform Dijkstra's algorithm
        while (!lt.isEmpty()) {
            Node minNode = lt.deleteMin();
            int u = minNode.vertex;

            for (Pair<Integer, Integer> edge : graph.get(u)) {
                int v = edge.getKey();
                int weight = edge.getValue();
                if (minDist.get(u) + weight < minDist.get(v)) {
                    minDist.set(v, minDist.get(u) + weight);
                    lt.insert(new Node(v,minDist.get(v)));
                }
            }
        }
        return minDist;
    }
}
