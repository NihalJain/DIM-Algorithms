package algorithm.FDIM.BFSBased;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a FPTree node.
 *
 * @author nihal jain
 * @version 1.0
 */
public class FPNode {
    int nodeID = -1;
    int itemID = -1; // item id
    int counter = 1; // frequency counter
    // the parent node of that node or null if it is the root
    FPNode parent = null;
    // the child nodes of that node
    List<FPNode> childs = new ArrayList<>();
    FPNode nodeLink = null; // link to next node with the same item id (for the header table).
    List<Integer> branches = new ArrayList<>();

    /**
     * Return the immediate child of this node having a given ID. If there is no
     * such child, return null;
     *
     * @param id of the given item
     * @return immediate child having given item ID
     */
    public FPNode getChildWithID(int id) {
        // for each child node
        for (FPNode child : childs) {
            // if the id is the one that we are looking for
            if (child.itemID == id) {
                // return that node
                return child;
            }
        }
        // if not found, return null
        return null;
    }
}
