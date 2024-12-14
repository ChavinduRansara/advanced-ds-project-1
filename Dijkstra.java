import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Dijkstra {
    private static final int INF = Integer.MAX_VALUE;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Dijkstra [-r n d x] [-l filename] [-f filename]");
            return;
        }

        switch (args[0]) {
            case "-r":
            // Run Dijkstra's algorithm on a randomly generated graph
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
            // Run Dijkstra's algorithm using Leftist Tree on a user-defined graph loaded from file
                if (args.length != 2) {
                    System.out.println("Leftist tree mode usage: -l filename");
                    return;
                }
                runUserInputMode(args[1], true);
                break;
            case "-f":
            // Run Dijkstra's algorithm using Fibonacci Heap on a user-defined graph loaded from file
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
        DijkstraAlgorithm graph = generateRandomGraph(n, density);

        // Run with Leftist Tree
        long leftistStartTime = System.nanoTime();
        List<Integer> leftistDistances = graph.dijkstraLeftist(source);
        long leftistTime = System.nanoTime() - leftistStartTime;

        System.out.printf("Performance for n=%d, density=%.2f%%:\n", n, density * 100);
        System.out.println();
        System.out.printf("Leftist Tree Time: %.3f ms\n", leftistTime / 1_000_000.0);

        // Run with Fibonacci Heap
        long fibonacciStartTime = System.nanoTime();
        List<Integer> fibDistances = graph.dijkstraFibonacci(source);
        long fibonacciTime = System.nanoTime() - fibonacciStartTime;

        System.out.printf("Fibonacci Heap Time: %.3f ms\n", fibonacciTime / 1_000_000.0);
        System.out.println();

        // Compare the results
        boolean success = true;
        for (int i = 0; i < n; i++) {
            if (!fibDistances.get(i).equals(leftistDistances.get(i))) {
                success = false;
                break;
            }
        }
        if (success) {
            System.out.println("The shortest path distances calculated using the Fibonacci Heap and Leftist Tree methods matched.");
        } else {
            System.out.println("The shortest path distances calculated using the Fibonacci Heap and Leftist Tree methods do not match.");
        }
    }

    // Run Dijkstra's algorithm on a user-defined graph loaded from file
    private static void runUserInputMode(String filename, boolean useLeftistTree) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int source = Integer.parseInt(reader.readLine().trim());
            String[] nm = reader.readLine().trim().split(" ");
            int n = Integer.parseInt(nm[0]);
            int m = Integer.parseInt(nm[1]);

            DijkstraAlgorithm graph = new DijkstraAlgorithm(n);

            // Add edges to the graph
            for (int i = 0; i < m; i++) {
                String[] edge = reader.readLine().trim().split(" ");
                int v1 = Integer.parseInt(edge[0]);
                int v2 = Integer.parseInt(edge[1]);
                int cost = Integer.parseInt(edge[2]);
                graph.addEdge(v1, v2, cost);
            }

            List<Integer> dist = useLeftistTree ?
                    graph.dijkstraLeftist(source) :
                    graph.dijkstraFibonacci(source);

            // Output distances from source to each vertex
            for (int i = 0; i < n; i++) {
                System.out.println(dist.get(i) == INF ? "INF" : dist.get(i) + " //cost from node "+source+" to node "+i);
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Generate a random connected graph with given density
    private static DijkstraAlgorithm generateRandomGraph(int n, double density) {
        DijkstraAlgorithm graph = new DijkstraAlgorithm(n);

        int maxEdges = n * (n - 1) / 2;
        int numEdges = (int) (density * maxEdges);

        Set<Pair<Integer, Integer>> edges = new HashSet<>();
        Random random = ThreadLocalRandom.current();

        // Generate a random connected graph
        while (edges.size() < numEdges) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            int weight = random.nextInt(1000) + 1;

            // Avoid self-loops and parallel edges
            if (u != v && !edges.contains(new Pair<>(u, v)) && !edges.contains(new Pair<>(v, u))) {
                graph.addEdge(u, v, weight);
                edges.add(new Pair<>(u, v));
            }
        }

        System.out.println("Generated random graph with " + n + " vertices and " + numEdges + " edges and graph is "+ (isConnected(graph) ? "connected." : " not connected."));
        return graph;
    }

    // DFS to check if the graph is connected
    private static boolean isConnected(DijkstraAlgorithm graph) {
       boolean[] visited = new boolean[graph.size()];
       dfs(graph, 0, visited);
       for (boolean v : visited) {
           if (!v) return false;
       }
       return true;
    }

    private static void dfs(DijkstraAlgorithm graph, int v, boolean[] visited) {
        visited[v] = true;
        for (Pair<Integer, Integer> edge : graph.getGraph().get(v)) {
            if (!visited[edge.getKey()]) {
                dfs(graph, edge.getKey(), visited);
            }
        }
    }

}
