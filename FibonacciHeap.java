import java.util.ArrayList;
import java.util.List;

class FibonacciHeap {
    public static class Node {
        int key;
        int data;
        int degree;
        boolean childCut;
        Node parent;
        Node child;
        Node left;
        Node right;

        Node(int key, int data) {
            this.key = key;
            this.data = data;
            this.degree = 0;
            this.childCut = false;
            this.parent = null;
            this.child = null;
            this.left = this;
            this.right = this;
        }
    }

    private Node min;
    private int size;

    public FibonacciHeap() {
        min = null;
        size = 0;
    }

    public Node insert(int key, int data) {
        Node node = new Node(key, data);
        min = mergeLists(min, node);
        size++;
        return node;
    }

    public Node extractMin() {
        Node z = min;
        if (z != null) {
            if (z.child != null) {
                Node child = z.child;
                do {
                    Node next = child.right;
                    min = mergeLists(min, child);
                    child.parent = null;
                    child = next;
                } while (child != z.child);
            }

            z.left.right = z.right;
            z.right.left = z.left;

            if (z == z.right) {
                min = null;
            } else {
                min = z.right;
                consolidate();
            }

            size--;
        }
        return z;
    }

    public void decreaseKey(Node x, int k) {
        if (k > x.key) {
            throw new IllegalArgumentException("New key is greater than current key");
        }

        x.key = k;
        Node y = x.parent;

        if (y != null && x.key < y.key) {
            cut(x, y);
            cascadingCut(y);
        }

        if (min == null || x.key < min.key) { // Ensure min is updated correctly
            min = x;
        }
    }


    private void cut(Node x, Node y) {
        x.left.right = x.right;
        x.right.left = x.left;
        y.degree--;

        if (y.child == x) {
            y.child = x.right;
        }

        if (y.degree == 0) {
            y.child = null;
        }

        x.left = x;
        x.right = x;
        min = mergeLists(min, x);
        x.parent = null;
        x.childCut = false;
    }

    private void cascadingCut(Node y) {
        Node z = y.parent;
        if (z != null) {
            if (!y.childCut) {
                y.childCut = true;
            } else {
                cut(y, z);
                cascadingCut(z);
            }
        }
    }

    private void consolidate() {
        int maxDegree = (int) (Math.log(size) / Math.log(1.618)) + 1;  // Using golden ratio
        maxDegree = Math.max(maxDegree, 45); // Safety margin
        
        Node[] A = new Node[maxDegree];  // Degree array - temporary storage
    
        // Collect all roots into a list
        List<Node> nodes = new ArrayList<>();
        if (min != null) {
            Node current = min;
            do {
                nodes.add(current);
                current = current.right;
            } while (current != min);
        }
    
        // Main consolidation loop
        for (Node w : nodes) {
            Node x = w;
            int d = x.degree;
    
            // Keep linking trees of same degree
            while (d < maxDegree && A[d] != null) {
                Node y = A[d];  // Found another tree of same degree
                if (x.key > y.key) {  // Ensure x has smaller key
                    Node temp = x;
                    x = y;
                    y = temp;
                }
                link(y, x);  // Make y a child of x
                A[d] = null;
                d++;
            }
            if (d < maxDegree) {
                A[d] = x;  // Store tree in degree array
            }
        }
    
        // Reconstruct the root list
        min = null;
        for (Node a : A) {
            if (a != null) {
                min = mergeLists(min, a);
            }
        }
    }

    private void link(Node y, Node x) {
        y.left.right = y.right;
        y.right.left = y.left;

        y.parent = x;
        if (x.child == null) {
            x.child = y;
            y.right = y;
            y.left = y;
        } else {
            y.left = x.child;
            y.right = x.child.right;
            x.child.right = y;
            y.right.left = y;
        }

        x.degree++;
        y.childCut = false;
    }

    private Node mergeLists(Node a, Node b) {
        if (a == null) return b;
        if (b == null) return a;

        Node temp = a.right;
        a.right = b.right;
        b.right.left = a;
        b.right = temp;
        temp.left = b;

        return (a.key < b.key) ? a : b;
    }

    public boolean isEmpty() {
        return min == null;
    }
}