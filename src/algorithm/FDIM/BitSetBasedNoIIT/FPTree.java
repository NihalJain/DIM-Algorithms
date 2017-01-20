package algorithm.FDIM.BitSetBasedNoIIT;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a FPTree.
 *
 * @author nihal jain
 * @version 1.0
 */
public class FPTree {

    // List of pairs (item, frequency) of the header table
    Map<Integer, FPNode> mapItemNodes = new HashMap<>();
    // root of the tree
    FPNode root = new FPNode(); // null node
    int current_branch = 0;
    static int current_node = 1;
    //count of unique transactions
    static int countOfUniqueItems;

    //static BitSet bitMap;
    /**
     * Constructor
     */
    FPTree(int uniqueItemsCount) {
        countOfUniqueItems = uniqueItemsCount;
        //bitMap = new BitSet(uniqueItemsCount);
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

                newNode.nodeID = current_node;
                current_node++;
                //System.out.println("current_node: "+current_node+" item: "+item+" nodeID: "+newNode.nodeID);

                //init the bitSet
                newNode.bitMap = new BitSet(countOfUniqueItems);
                //set the element corresponding to parent's item
                if (currentNode != root) {
                    newNode.bitMap.set(currentNode.itemID);
                    newNode.bitMap.or(currentNode.bitMap);
                }

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
                //or the contents with the parent's bitMap
                if (currentNode != root) {
                    child.bitMap.or(currentNode.bitMap);
                }

                // there is a node already, we update it
                child.counter++;
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
