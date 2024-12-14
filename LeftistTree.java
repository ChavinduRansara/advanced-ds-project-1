class Node {
    int vertex;
    int key;
    Node left, right;
    int npl;  // null path length

    Node(int vertex, int key) {
        this.vertex = vertex;
        this.key = key;
        this.left = null;
        this.right = null;
        this.npl = 0;
    }
}

public class LeftistTree {
    public Node root;

    public LeftistTree() {
        root = null;
    }

    // Insert a Node into the Leftist Tree
    public void insert(Node x) {
        root = meld(root, x);
    }

    // Delete and return the node with the minimum key (the root)
    public Node deleteMin() {
        if (root == null) return null;
        Node min = root;
        root = meld(root.left, root.right);
        return min;
    }

    // Check if the Leftist Tree is empty
    public boolean isEmpty() {
        return root == null;
    }

    // Meld two Leftist Trees
    private Node meld(Node h1, Node h2) {
        if (h1 == null) return h2;
        if (h2 == null) return h1;

        // Ensure the smaller key is the root
        if (h1.key > h2.key) {
            Node temp = h1;
            h1 = h2;
            h2 = temp;
        }

        // Meld h2 with the right child of h1
        h1.right = meld(h1.right, h2);

        // Maintain the leftist property by adjusting npl and swapping children if necessary
        if (h1.left == null) {
            h1.left = h1.right;
            h1.right = null;
        } else {
            if (h1.right != null && h1.left.npl < h1.right.npl) {
                Node temp = h1.left;
                h1.left = h1.right;
                h1.right = temp;
            }
            h1.npl = (h1.right != null ? h1.right.npl : 0) + 1;
        }

        return h1;
    }

    // Simulate decrease key by deleting the node and re-inserting it with a new key
    public void decreaseKey(Node oldNode, int newKey) {
        delete(oldNode.vertex);  
        insert(new Node(oldNode.vertex, newKey));  
    }

    // Delete node based on vertex (for decrease key simulation)
    private void delete(int vertex) {
        root = delete(root, vertex);
    }

    // Recursive helper to find and delete the node with the specified vertex
    private Node delete(Node root, int vertex) {
        if (root == null) return null;
        if (root.vertex == vertex) {
            return meld(root.left, root.right);  // Meld the left and right subtrees of the found node
        } else {
            root.left = delete(root.left, vertex);
            root.right = delete(root.right, vertex);
            return root;
        }
    }
}
