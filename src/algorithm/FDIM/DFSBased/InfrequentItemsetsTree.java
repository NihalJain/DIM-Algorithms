package algorithm.FDIM.DFSBased;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a Infrequent Itemsets Tree.
 *
 * @author nihal jain
 * @version 1.0
 */
public class InfrequentItemsetsTree {

    // List of pairs (item, frequency) of the header table
    Map<Integer, IITNode> mapItemNodes = new HashMap<>();
    // root of the tree
    IITNode root = new IITNode(); // null node
    int current_branch = 0;


    /**
     * Method for adding a transaction to the fp-tree (for the initial
     * construction of the FP-Tree).
     *
     * @param transaction transaction to be added
     */
    public void addTransaction(List<Integer> transaction) {
        IITNode currentNode = root;

        boolean flag = false;
        IITNode last = null;
        // For each item in the transaction
        for (Integer item : transaction) {
            // look if there is a node already in the FP-Tree
            IITNode child = currentNode.getChildWithID(item);
            if (child == null) {
                flag = true;
                // there is no node, we create a new one
                IITNode newNode = new IITNode();
                newNode.itemID = item;
                newNode.parent = currentNode;

                //newNode.nodeID = current_node;
                //current_node++;
                //System.out.println("current_node: "+current_node+" item: "+item+" nodeID: "+newNode.nodeID);
                // we link the new node to its parrent
                currentNode.childs.add(newNode);

                // we take this node as the current node for the next for loop iteration
                currentNode = newNode;

                // We update the header table.
                // We check if there is already a node with this id in the header table
                IITNode headernode = mapItemNodes.get(item);
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
                //child.counter++;
                //child.nodeID = currentNode.nodeID;
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
