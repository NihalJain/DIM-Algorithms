package algorithm.FDIM.MFPImproved;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a FPTree.
 *
 * @author shailesh prajapati
 * @version 1.0
 */
public class FPTree {

    // List of pairs (item, frequency) of the header table
    Map<Integer, FPNode> mapItemNodes = new HashMap<Integer, FPNode>();
    // root of the tree
    FPNode root = new FPNode(); // null node
    int current_branch = 0;

    /**
     * Constructor
     */
    FPTree() {
    }

    /**
     * Method for adding a transaction to the fp-tree (for the initial
     * construction of the FP-Tree).
     *
     * @param transaction transaction to be added
     */
    public void addTransaction(List<Integer> transaction) {
        FPNode currentNode = root;
        boolean flag = false;
        FPNode last = null;
        // For each item in the transaction
        for (Integer item : transaction) {
            // look if there is a node already in the FP-Tree
            FPNode child = currentNode.getChildWithID(item);
            if (child == null) {
                flag = true;
                // there is no node, we create a new one
                FPNode newNode = new FPNode();
                newNode.itemID = item;
                newNode.parent = currentNode;
                // we link the new node to its parrent
                currentNode.childs.add(newNode);

                // we take this node as the current node for the next for loop iteration
                currentNode = newNode;

                // We update the header table.
                // We check if there is already a node with this id in the header table
                FPNode headernode = mapItemNodes.get(item);
                if (headernode == null) { // there is not
                    mapItemNodes.put(item, newNode);
                } else { // there is
                    // we find the last node with this id.
                    while (headernode.nodeLink != null) {
                        headernode = headernode.nodeLink;
                    }
                    headernode.nodeLink = newNode;
                    last = newNode;
                }
            } else {
                // there is a node already, we update it
                child.counter++;
                last = child;
                currentNode = child;
            }
        }
        if (flag) {
            while (last != null) {
                last.branches.add(current_branch);
                last = last.parent;
            }
            current_branch++;
        }
    }
}
