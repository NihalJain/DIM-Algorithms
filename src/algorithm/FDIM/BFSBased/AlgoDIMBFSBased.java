package algorithm.FDIM.BFSBased;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Implementation of FP tree based algorithm for finding Frequent ORed Itemsets.
 *
 * @author nihal jain
 * @version 1.0
 */
public class AlgoDIMBFSBased {

    // Number of transactions in the database
    private int transactionCount = 0;
    // PrintWriter object to write the output file
    PrintWriter writer = null;
    /**
     * number of transactions in the database
     */
    public static int databaseSize;
    // Hashmap for storing frequencies of each item in dataset
    final Map<Integer, Integer> mapSupport = new HashMap<>();
    /**
     * Number of items in dataset
     */
    public static int total_singles = 0;
    // all unique items present in dataset
    private Integer[] intKeys;
    // total candidate itemsets
    int candidateItemset = 0;
    FPTree tree = null;
    float minsup;
    int countitemsets = 0;
    int countt = 0;
    Integer preference[];
    // used by dfs for support counting
    private int sumOfSupport = 0;

    /**
     * Method to run the FP tree based ORed Itemset generation algorithm.
     *
     * @param input the path to an input file containing a transaction database.
     * @param minsupp the minimum support threshold.
     * @throws IOException exception if error reading or writing files.
     * @throws FileNotFoundException exception if input file not found.
     */
    public void runAlgorithm(String input, float minsupp) throws FileNotFoundException, IOException {

        // reset the transaction count
        databaseSize = 0;

        // (1) Initial database scan to determine the frequency of each item
        // The frequency is stored in a map:- key: item value: support
        long t1 = System.currentTimeMillis();
        scanDatabaseToDetermineFrequencyOfSingleItems(input);
        long t2 = System.currentTimeMillis();
        // Displaying frequency counting time
        System.out.println("Item Frequency counting time : " + (t2 - t1) + " ms");

        // assigning total unique items
        total_singles = (int) mapSupport.size();
        // adding all items name
        Integer[] X = new Integer[mapSupport.size()];
        int q = 0;
        for (int stringKey : mapSupport.keySet()) {
            X[q] = stringKey;
            q++;
        }

        List<Integer> t = new ArrayList<>();
        Collections.addAll(t, X);
        // sort item in the transaction by descending order of support
        Collections.sort(t, new Comparator<Integer>() {

            @Override
            public int compare(Integer item1, Integer item2) {
                // compare the frequency
                int compare = mapSupport.get(item2) - mapSupport.get(item1);
                // if the same frequency, we check the lexical ordering!
                if (compare == 0) {
                    return (item1 - item2);
                }
                // otherwise, just use the frequency
                return compare;
            }
        });
        intKeys = new Integer[X.length];
        for (int tmp = 0; tmp < t.size(); tmp++) {
            intKeys[tmp] = t.get(tmp);
        }
        intKeys = new Integer[mapSupport.size()];
        q = 0;
        for (int stringKey : mapSupport.keySet()) {
            intKeys[q] = stringKey;
            q++;
        }

        // (2) Scan the database again to build the initial FP-Tree
        // Before inserting a transaction in the FPTree, we sort the items
        // by descending order of support. We ignore items that
        // do not have the minimum support.
        tree = new FPTree();
        tree.root.nodeID = 0;
        // read the file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(input));
        } catch (Exception e) {
            System.err.println("error: Unable to open input file");
            System.exit(-1);
        }
        String line;
        // for each line (transaction) until the end of the file
        while (((line = reader.readLine()) != null)) {
            // if the line is a comment, is empty or is a kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }
            // Tokenizing input line
            StringTokenizer lineSplited = new StringTokenizer(line);
            List<Integer> transaction = new ArrayList<>();
            // for each item in the transaction
            while (lineSplited.hasMoreElements()) {
                Integer item = Integer.parseInt(lineSplited.nextToken());
                // only add items that have the minimum support
                // if(!transaction.contains(item))
                transaction.add(item);
            }

            // sort item in the transaction by descending order of support
            Collections.sort(transaction, new Comparator<Integer>() {
                @Override
                public int compare(Integer item1, Integer item2) {
                    // compare the frequency
                    int compare = mapSupport.get(item2) - mapSupport.get(item1);
                    // if the same frequency, we check the lexical ordering!
                    if (compare == 0) {
                        return (item1 - item2);
                    }
                    // otherwise, just use the frequency
                    return compare;
                }
            });

            // add the sorted transaction to the fptree.
            tree.addTransaction(transaction);
            // increase the transaction count
            databaseSize++;
        }
        // close the input file
        reader.close();

        t1 = System.currentTimeMillis();
        // calling FPOred function on TREE tree with minsupp.
        minsup = minsupp;
        FPORed();
        t2 = System.currentTimeMillis();

        // itemset finding time
        System.out.println("Support Counting Time : " + (t2 - t1) + "ms");
    }

    /**
     * This method scans the input dataset to calculate the support of single
     * items.
     *
     * @param input the path of the input file.
     * @throws IOException exception if error while writing the file.
     * @throws FileNotFoundException exception if input file not found.
     */
    private void scanDatabaseToDetermineFrequencyOfSingleItems(String input) throws FileNotFoundException, IOException {
        // Create object for reading the input file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(input));
        } catch (Exception e) {
            System.err.println("error: Unable to open input file");
            System.exit(-1);
        }
        String line;
        // for each line (transaction) until the end of file
        while (((line = reader.readLine()) != null)) {
            // if the line is a comment, is empty or is a kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            // spliting line
            StringTokenizer lineSplited = new StringTokenizer(line);
            // for each item
            while (lineSplited.hasMoreElements()) {
                // increase the support count of the item
                Integer item = Integer.parseInt(lineSplited.nextToken());
                // increase the support count of the item
                Integer count = mapSupport.get(item);
                if (count == null) {
                    mapSupport.put(item, 1);
                } else {
                    mapSupport.put(item, ++count);
                }
            }
            // increase the transaction count
            transactionCount++;
        }
        // close the input file
        reader.close();
    }

    /**
     * This method finds all frequent ORed itemsets.
     *
     * @param minsupp the minimum support threshold.
     * @param tree original FP tree.
     */
    private void FPORed() {

        List<Integer> list = new ArrayList<Integer>();
        preference = new Integer[total_singles];
        for (Integer i = 0; i < intKeys.length; i++) {
            list.add(intKeys[i]);
        }
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer item1, Integer item2) {
                // compare the frequency
                int compare = mapSupport.get(item2) - mapSupport.get(item1);
                // if the same frequency, we check the lexical ordering!
                if (compare == 0) {
                    return (item1 - item2);
                }
                // otherwise, just use the frequency
                return compare;
            }
        });
        for (int i = 0; i < intKeys.length; i++) {
            preference[list.get(i)] = i;
        }
        Itemsets(list, total_singles - 1, 0, total_singles);

        // writer.close();
        // summarizing result
        System.out.println("Total candidates " + candidateItemset);
        System.out.println("Total " + countitemsets + " frequent ORed Itemsets found.");
    }

    /**
     *
     * @param list list of items for candidates generation
     * @param start start index
     * @param end end index
     * @param depth depth of combination tree
     * @throws InterruptedException
     */
    public void Itemsets(List<Integer> list, int start, int end, int depth) {
        for (int i = start; i >= end; i--) {
            if (depth == end + 1) {
                return;
            }
            List<Integer> newlist = new ArrayList<>(list);
            newlist.remove(list.get(i));
            int sum = BFS(newlist);
            float val = ((float) sum / getDatabaseSize());
            if (val >= minsup) {
                countitemsets++;
                SortedSet<Integer> set = new TreeSet<>();
                set.addAll(newlist);
                test.Algorithm.frequent_list_set.add(set.toArray(new Integer[newlist.size()]));
                test.Algorithm.frequent_list.put(set.toString(), val);
                //System.out.println("--> " + newlist.toString() + " val: " + val + " tnr: " + getDatabaseSize());
                Itemsets(newlist, i - 1, end, depth - 1);
            }
        }
    }

   /**
    * 
    * @param list
    * @return 
    */
    private int BFS(List<Integer> list) {
        int sumOfSupport = 0;
        // Mark all the vertices as not visited(By default
        // set as false)
        boolean visited[] = new boolean[FPTree.current_node + 1];
        //Arrays.fill(visited, false);
        for (int i = 0; i < visited.length; ++i) {
            visited[i] = false;
        }

        // Create a queue for BFS+
        LinkedList<FPNode> queue = new LinkedList<>();

        // Mark the current node as visited and enqueue it
        visited[0] = true;
        queue.add(tree.root);
        FPNode src;
        //System.out.print("Reached "+tree.root.nodeID);
        while (!queue.isEmpty()) {
            // Dequeue a vertex from queue and print it
            src = queue.poll();
            //System.out.print("Dequed "+src+" ");

            // Get all adjacent vertices of the dequeued vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it
            List<FPNode> adj = src.childs;
            Collections.sort(adj, new Comparator<FPNode>() {
                public int compare(FPNode node1, FPNode node2) {
                    // compare the frequency
                    int compare = mapSupport.get(node2.itemID) - mapSupport.get(node1.itemID);
                    // if the same frequency, we check the lexical ordering!
                    if (compare == 0) {
                        return (node1.itemID - node2.itemID);
                    }
                    // otherwise, just use the frequency
                    return compare;
                }
            });
            //System.out.println("list size: "+src.childs.size());
            for (int i = 0; i < adj.size(); i++) {
                boolean foundFlag = false;
                FPNode n = adj.get(i);
                //System.out.println("Visited node: "+n.nodeID +" visited: "+visited[n.nodeID]); 

                if (!visited[n.nodeID]) //n.nodeID != -1 && 
                {
                    for (Integer item : list) {
                        if (item.equals(n.itemID)) {
                            sumOfSupport += n.counter;
                            foundFlag = true;
                            break;
                        }
                    }
                    if (foundFlag == true) {
                        continue;
                    }
                    //System.out.println("Visited: "+n.nodeID);
                    visited[n.nodeID] = true;
                    queue.add(n);
                }
            }
        }
        return sumOfSupport;
    }

    /**
     * Get the number of transactions in the last transaction database read.
     *
     * @return the number of transactions.
     */
    public int getDatabaseSize() {
        return databaseSize;
    }
}
