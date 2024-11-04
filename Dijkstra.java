import java.io.*;
import java.util.*;

public class Dijkstra {
    private static final int INF = Integer.MAX_VALUE;
    private static final Random random = new Random();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Dijkstra [-r n d x] [-l filename] [-f filename]");
            return;
        }

        switch (args[0]) {
            case "-r":
                if (args.length != 4) {
                    System.out.println("Random mode usage: -r n d x");
                    return;
                }
                int n = Integer.parseInt(args[1]);
                double d = Double.parseDouble(args[2]) / 100.0;
                int source = Integer.parseInt(args[3]);
                runRandomMode(n, d, source);
                break;
            case "-l":
                if (args.length != 2) {
                    System.out.println("Leftist tree mode usage: -l filename");
                    return;
                }
                runUserInputMode(args[1], true);
                break;
            case "-f":
                if (args.length != 2) {
                    System.out.println("Fibonacci heap mode usage: -f filename");
                    return;
                }
                runUserInputMode(args[1], false);
                break;
            default:
                System.out.println("Invalid mode");
        }
    }

    // Run Dijkstra's algorithm on a randomly generated graph
    private static void runRandomMode(int n, double density, int source) {
        List<List<Edge>> graph = generateRandomGraph(n, density);

        // Run with Leftist Tree
        long startTime = System.nanoTime();
        int[] distLeftist = dijkstraLeftistTree(graph, source);
        long leftistTime = System.nanoTime() - startTime;

        // Run with Fibonacci Heap
        startTime = System.nanoTime();
        int[] distFibonacci = dijkstraFibonacciHeap(graph, source);
        long fibonacciTime = System.nanoTime() - startTime;

        System.out.printf("Performance for n=%d, density=%.2f%%:\n", n, density * 100);
        System.out.printf("Leftist Tree Time: %.3f ms\n", leftistTime / 1_000_000.0);
        System.out.printf("Fibonacci Heap Time: %.3f ms\n", fibonacciTime / 1_000_000.0);
    }

    // Run Dijkstra's algorithm on a user-defined graph loaded from file
    private static void runUserInputMode(String filename, boolean useLeftistTree) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int source = Integer.parseInt(reader.readLine().trim());
            String[] nm = reader.readLine().trim().split(" ");
            int n = Integer.parseInt(nm[0]);
            int m = Integer.parseInt(nm[1]);

            List<List<Edge>> graph = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                graph.add(new ArrayList<>());
            }

            for (int i = 0; i < m; i++) {
                String[] edge = reader.readLine().trim().split(" ");
                int v1 = Integer.parseInt(edge[0]);
                int v2 = Integer.parseInt(edge[1]);
                int cost = Integer.parseInt(edge[2]);
                graph.get(v1).add(new Edge(v2, cost));
                graph.get(v2).add(new Edge(v1, cost));
            }

            int[] dist = useLeftistTree ?
                    dijkstraLeftistTree(graph, source) :
                    dijkstraFibonacciHeap(graph, source);

            // Output distances from source to each vertex
            for (int i = 0; i < n; i++) {
                System.out.println(dist[i] == INF ? "INF" : dist[i]);
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Generate a random connected graph with given density
    private static List<List<Edge>> generateRandomGraph(int n, double density) {
        List<List<Edge>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        int maxEdges = n * (n - 1) / 2;
        int targetEdges = (int) (maxEdges * density);
        Set<String> addedEdges = new HashSet<>();

        while (addedEdges.size() < targetEdges) {
            int v1 = random.nextInt(n);
            int v2 = random.nextInt(n);
            if (v1 == v2) continue;

            String edge = Math.min(v1, v2) + "," + Math.max(v1, v2);
            if (addedEdges.add(edge)) {
                int cost = random.nextInt(1000) + 1;
                graph.get(v1).add(new Edge(v2, cost));
                graph.get(v2).add(new Edge(v1, cost));
            }
        }

        // Ensure connectivity using DFS
        while (!isConnected(graph)) {
            int v1 = random.nextInt(n);
            int v2 = random.nextInt(n);
            if (v1 != v2) {
                String edge = Math.min(v1, v2) + "," + Math.max(v1, v2);
                if (addedEdges.add(edge)) {
                    int cost = random.nextInt(1000) + 1;
                    graph.get(v1).add(new Edge(v2, cost));
                    graph.get(v2).add(new Edge(v1, cost));
                }
            }
        }

        return graph;
    }

    // DFS to check if the graph is connected
    private static boolean isConnected(List<List<Edge>> graph) {
        boolean[] visited = new boolean[graph.size()];
        dfs(graph, 0, visited);
        for (boolean v : visited) {
            if (!v) return false;
        }
        return true;
    }

    private static void dfs(List<List<Edge>> graph, int v, boolean[] visited) {
        visited[v] = true;
        for (Edge e : graph.get(v)) {
            if (!visited[e.to]) {
                dfs(graph, e.to, visited);
            }
        }
    }

    // Dijkstra's algorithm using Leftist Tree
    private static int[] dijkstraLeftistTree(List<List<Edge>> graph, int source) {
        int n = graph.size();
        int[] dist = new int[n];
        Arrays.fill(dist, INF);
        dist[source] = 0;

        LeftistTree pq = new LeftistTree();
        pq.insert(new Node(source, 0));

        while (!pq.isEmpty()) {
            Node node = pq.deleteMin();
            int u = node.vertex;

            if (node.key > dist[u]) continue;

            for (Edge edge : graph.get(u)) {
                int v = edge.to;
                int newDist = dist[u] + edge.cost;

                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pq.insert(new Node(v, newDist));
                }
            }
        }

        return dist;
    }

    // Dijkstra's algorithm using Fibonacci Heap
    private static int[] dijkstraFibonacciHeap(List<List<Edge>> graph, int source) {
        int n = graph.size();
        int[] dist = new int[n];
        Arrays.fill(dist, INF);
        dist[source] = 0;

        FibonacciHeap fh = new FibonacciHeap();
        FibonacciHeap.Node[] nodes = new FibonacciHeap.Node[n];

        // Initialize and insert all nodes in the Fibonacci Heap
        for (int i = 0; i < n; i++) {
            nodes[i] = fh.insert(i == source ? 0 : INF, i); // Source has distance 0; others INF
        }

        while (!fh.isEmpty()) {
            FibonacciHeap.Node node = fh.extractMin();
            int u = node.data;

            // Process each neighbor of the extracted node
            for (Edge edge : graph.get(u)) {
                int v = edge.to;
                int newDist = dist[u] + edge.cost;

                // Update the distance if a shorter path is found
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    fh.decreaseKey(nodes[v], newDist); // Update the key in Fibonacci Heap
                }
            }
        }

        return dist;
    }

}
