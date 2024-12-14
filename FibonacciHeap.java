import java.util.ArrayList;
import java.util.Collections;
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
        this.min = null;
        this.size = 0;
    }

    // Insert a new node with the given key and data into the Fibonacci heap
    public Node insert(int key, int value) {
        Node node = new Node(key, value);
        if (min == null) {
            min = node;
        } else {
            mergeWithRootList(node); // Add the new node to the root list
            if (node.key < min.key) {
                min = node;
            }
        }
        size++;
        return node;
    }

    // Merge the node with the root list
    private void mergeWithRootList(Node node) {
        if(min == null){
            min = node;
        }else{
            // Insert the node into the root list
            node.left = min;
            node.right = min.right;
            min.right.left = node;
            min.right = node;
            if (node.key < min.key) {
                min = node;
            }
        }
    }

    // Extract the node from the root list 
    private void removeFromRootList(Node node) {
        if (node.right == node) {
            min = null;
        } else {
            node.left.right = node.right;
            node.right.left = node.left;
            if (min == node) {
                min = node.right;
            }
        }
    }

    // Remove the node from the child list
    private void removeFromChildList(Node parent, Node node) {
        if (node.right == node) {
            parent.child = null;
        } else {
            if (parent.child == node) {
                parent.child = node.right;
            }
            node.left.right = node.right;
            node.right.left = node.left;
        }
        node.left = node.right = node;
    }

    // Merge the node with the child list
    private Node mergeWithChildList(Node child, Node node) {
        if (child == null) {
            return node;
        } else {
            node.left = child;
            node.right = child.right;
            child.right.left = node;
            child.right = node;
        }
        return child;
    }

    // Extract the node with the minimum key from the Fibonacci heap
    public Node extractMin() {
        Node z = min;
        if (z != null) {
            if (z.child != null) {
                for (Node x : iterate(z.child)) {
                    mergeWithRootList(x);
                    x.parent = null;
                }
            }
            removeFromRootList(z);
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

    // Decrease the key of the node to the new key
    public void decreaseKey(Node x, int k) {
        if (k > x.key) {
            throw new IllegalArgumentException("New key is greater than current key");
        }

        x.key = k;
        Node y = x.parent;

        // Cut the node from its parent if it violates the heap property
        if (y != null && x.key < y.key) {
            cut(x, y);
            cascadingCut(y);
        }

        if (x.key < min.key) {
            min = x;
        }
    }


    // Remove the node from the Fibonacci heap
    private void cut(Node x, Node y) {
        removeFromChildList(y, x);
        y.degree--;
        mergeWithRootList(x);
        x.parent = null;
        x.childCut = false;
    }

    // Perform cascading cut on the node
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

    // Iterate through the nodes in the Fibonacci heap
    private List<Node> iterate(Node head) {
        List<Node> nodes = new ArrayList<>();
        if (head == null) return nodes;

        Node current = head;
        do {
            nodes.add(current);
            current = current.right;
        } while (current != head);

        return nodes;
    }

    // Consolidate the Fibonacci heap
    private void consolidate() {
        int maxDegree = (int) (Math.log(size) / Math.log(2)) + 10;
        List<Node> A = new ArrayList<>(Collections.nCopies(maxDegree, null));

        List<Node> nodes = iterate(min);

        // Combine nodes with the same degree
        for (Node w : nodes) {
            Node x = w;
            int d = x.degree;
            while (A.get(d) != null) {
                Node y = A.get(d);
                if (x.key > y.key) {
                    Node temp = x;
                    x = y;
                    y = temp;
                }
                link(y, x);
                A.set(d, null);
                d++;
            }
            A.set(d, x);
        }

        // Reconstruct the root list
        min = null;
        for (Node node : A) {
            if (node != null) {
                if (min == null || node.key < min.key) {
                    min = node;
                }
            }
        }
    }

    // Link the node y to the node x
    private void link(Node y, Node x) {
        removeFromRootList(y);
        y.left = y.right = y;
        x.child = mergeWithChildList(x.child, y);
        y.parent = x;
        x.degree++;
        y.childCut = false;
    }

    public boolean isEmpty() {
        return min == null;
    }
}