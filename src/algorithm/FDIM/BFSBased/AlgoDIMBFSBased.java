package algorithm.FDIM.BFSBased;

import com.rits.cloning.Cloner;
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
import java.util.StringTokenizer;

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
    int prunedItemset = 0;
    int supportCountedItemset = 0;
    int addedToIITItemset = 0;
    FPTree tree = null;
    InfrequentItemsetsTree iiTree = null;
    float minsup;
    int maxitems, countitemsets = 0;
    int countt = 0;
    Integer preference[];
    // used by dfs for support counting
    //private final int sumOfSupport = 0;
    private int currLevel;

    /**
     * Method to run the FP tree based ORed Itemset generation algorithm.
     *
     * @param input the path to an input file containing a transaction database.
     * @param minsupp the minimum support threshold.
     * @param maxitem the maximum pattern length.
     * @throws IOException exception if error reading or writing files.
     * @throws FileNotFoundException exception if input file not found.
     */
    public void runAlgorithm(String input, float minsupp, int maxitem) throws FileNotFoundException, IOException {

        // reset the transaction count
        databaseSize = 0;

        // (1) Initial database scan to determine the frequency of each item
        // The frequency is stored in a map:- key: item value: support
        long t1 = System.currentTimeMillis();
        scanDatabaseToDetermineFrequencyOfSingleItems(input);
        long t2 = System.currentTimeMillis();
        // Displaying frequency counting time
        System.out.println("Item Frequency counting time : " + (t2 - t1) + " ms");
        t1 = System.currentTimeMillis();
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
        t2 = System.currentTimeMillis();
        System.out.println("Tree build time : " + (t2 - t1) + "ms");
        t1 = System.currentTimeMillis();
        // calling FPOred function on TREE tree with minsupp.
        minsup = minsupp;
        maxitems = maxitem;
        FPORed();
        t2 = System.currentTimeMillis();

        // itemset finding time
        System.out.println("TOTAL Itemset Finding Time : " + (t2 - t1) + "ms");
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
                //System.out.println(item);
            }
            // increase the transaction count
            transactionCount++;
        }
        //System.out.println(mapSupport);
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

        List<Integer> list = new ArrayList<>();
        //List<Integer> origList = new ArrayList<>();
        preference = new Integer[total_singles];
        for (Integer i = 0; i < intKeys.length; i++) {
            list.add(intKeys[i]);
        }

        /*Cloner cloner = new Cloner();
        origList = cloner.deepClone(list);
        System.out.println("Original List: " + origList.toString());*/
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

        //System.out.println("Mapped List: " + list.toString());
        long time = 0;
        long t1, t2;
        List<List<Integer>> levelItemsets = new ArrayList<>();
        t1 = System.currentTimeMillis();

        iiTree = new InfrequentItemsetsTree();

        //ICombinatoricsVector<Integer> initialVector = Factory.createVector(list);
        currLevel = 0;
        if (maxitems < total_singles) {
            levelItemsets = generateLevelOne(list, maxitems);
            //levelItemsets =  Generator.combination(list).simple(maxitems).stream().collect(Collectors.<List<Integer>>toList());
        } else {
            countitemsets++;
            //SortedSet<Integer> set = new TreeSet<>();
            //set.addAll(list);
            //test.Algorithm.frequent_list_set.add(set.toArray(new Integer[list.size()]));
            //test.Algorithm.frequent_list.put(set.toString(), (float)1.0);
            levelItemsets = generateLevelOne(list, maxitems - 1);
            //levelItemsets = (List<List<Integer>>) Generator.combination(list).simple(maxitems-1).stream().collect(Collectors.<List<Integer>>toList());;
        }
        t2 = System.currentTimeMillis();
        System.out.println("\nMiddle lattice generation, Time: " + (t2 - t1));
        while (!levelItemsets.isEmpty() && !levelItemsets.get(0).isEmpty()) {
            candidateItemset += levelItemsets.size();
            System.out.println("\nLEVEL: " + currLevel);
            //System.out.println(countitemsets);
            //System.out.println("Level itemsets on ENTRY: " + levelItemsets);
            //System.out.println("ENTERED NEW LEVEL");
            t1 = System.currentTimeMillis();
            levelItemsets = processItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            //System.out.println("Level itemsets on PRUNING: " + levelItemsets);
            System.out.println("Processing Done, Time: " + (t2 - t1));
            time += t2 - t1;

            t1 = System.currentTimeMillis();
            for (List<Integer> itemset : levelItemsets) {
                Collections.sort(itemset);
            }
            t2 = System.currentTimeMillis();
            System.out.println("Itemsets PreSort, Time: " + (t2 - t1));

            t1 = System.currentTimeMillis();
            levelItemsets = getSubsetItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            System.out.println("Subset Generation, Time: " + (t2 - t1));

            t1 = System.currentTimeMillis();
            for (List<Integer> itemset : levelItemsets) {
                Collections.sort(itemset, new Comparator<Integer>() {
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
            }
            t2 = System.currentTimeMillis();
            System.out.println("Itemsets PostSort, Time: " + (t2 - t1));
            //System.out.println("Level itemsets on EXIT: " + levelItemsets);
            ++currLevel;
        }
        // writer.close();
        // summarizing result
        System.out.println("\nTime in support calculation:" + time);
        System.out.println("Total candidates " + candidateItemset);
        System.out.println("Total " + countitemsets + " frequent ORed Itemsets found.");
        System.out.println("Total " + addedToIITItemset + " itemsets added to IIT.");
        System.out.println("Total " + prunedItemset + " itemsets pruned without support count.");
        System.out.println("Total " + supportCountedItemset + " itemsets processed for support count.");
    }

    /**
     * Generate actual subset by index sequence
     *
     * @param input
     * @param subset
     * @return
     */
    public List<Integer> getSubset(List<Integer> input, int[] subset) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < subset.length; i++) {
            result.add(input.get(subset[i]));
        }
        return result;
    }

    private boolean checkSeq(List<Integer> itemset, int k) {
        while (k > 0) {
            if (itemset.get(k) - itemset.get(k - 1) == 1) {
                k -= 1;
            } else {
                return false;
            }
        }

        return k == 0;
    }

    /**
     * Geneates all subset itemsets of the given itemset
     *
     * @param itemset the itemset whose subset itemsets is required
     * @return all the subsets of the passed itemset
     */
    private List<List<Integer>> generateSubsets(List<Integer> itemset) {
        List<List<Integer>> subsetItemsets = new ArrayList<>();
        Cloner cloner = new Cloner();
        if (itemset.get(0) == 0) {
            for (int k = itemset.size() - 1; k > 0; k--) {
                if (checkSeq(itemset, k)) {
                    List<Integer> newItemset = cloner.deepClone(itemset);
                    newItemset.remove(k);
                    subsetItemsets.add(newItemset);
                }
            }

            //if (itemset.get(0) == 0) {
            List<Integer> newItemset = cloner.deepClone(itemset);
            newItemset.remove(0);
            subsetItemsets.add(newItemset);
            //}
        }
        //System.out.println(itemset+"---->"+subsetItemsets);
        return subsetItemsets;
    }

    private List<List<Integer>> getSubsetItemsets(List<List<Integer>> itemsets) {
        List<List<Integer>> subsetItemsets = new ArrayList<>();
        for (List<Integer> itemset : itemsets) {
            subsetItemsets.addAll(generateSubsets(itemset));
        }
        return subsetItemsets;
    }

    /**
     * Enumerate from middle non-recursively: VERY FAST
     *
     * @param input
     * @param maxitems
     * @return
     */
    @SuppressWarnings("empty-statement")
    public List<List<Integer>> generateLevelOne(List<Integer> input, int maxitems) {
        List<List<Integer>> subsets = new ArrayList<>();

        int[] s = new int[maxitems];                  // here we'll keep indices 
        // pointing to elements in input array

        if (maxitems <= input.size()) {
            // first index sequence: 0, 1, 2, ...
            for (int i = 0; (s[i] = i) < maxitems - 1; i++);
            subsets.add(getSubset(input, s));
            for (;;) {
                int i;
                // find position of item that can be incremented
                for (i = maxitems - 1; i >= 0 && s[i] == input.size() - maxitems + i; i--);
                if (i < 0) {
                    break;
                } else {
                    s[i]++;                    // increment this item
                    for (++i; i < maxitems; i++) {    // fill up remaining items
                        s[i] = s[i - 1] + 1;
                    }
                    subsets.add(getSubset(input, s));
                }
            }
        }

        return subsets;
    }

    private boolean checkIITPath(List<Integer> list, int i, IITNode X_node) {
        if (i >= 0) {
            IITNode Y_node = X_node.parent;

            while (Y_node != iiTree.root) {
                //System.out.println("y_node: " + Y_node.itemID + " item: " + list.get(i));
                if (Y_node.itemID == list.get(i)) {
                    if (i == 0) {
                        //System.out.println("RETURN true, MID");
                        return true;
                    } else {
                        i--;
                    }
                } else if (preference[Y_node.itemID] <= preference[list.get(i)]) {
                    //System.out.println(preference[Y_node.itemID]+" "+preference[list.get(i)]);
                    //System.out.println("RETURN false, MID");
                    return false;
                }
                Y_node = Y_node.parent;
            }
        }
        //System.out.println("RETURN false, END ");
        return false;
    }

    public boolean isSubset(List<Integer> itemset) {
        int N = itemset.size() - 1;
        boolean temp = false;
        IITNode X_node = iiTree.mapItemNodes.get(itemset.get(N));

        if (N == 0 && X_node != null) {
            return true;
        }

        while (X_node != null) {
            if (N - 1 >= 0) {
                temp = checkIITPath(itemset, N - 1, X_node);
            }

            if (temp) {
                return true;
            }
            X_node = X_node.nodeLink;
        }
        return false;
    }

    public List<List<Integer>> processItemsets(List<List<Integer>> itemsets) {
        List<List<Integer>> levelItemsets = new ArrayList<>();
        for (int i = 0; i < itemsets.size(); i++) {
            List<Integer> currItemset = itemsets.get(i);

            // check whether infrequent
            if (currLevel != 0) {
                if (isSubset(currItemset)) {
                    prunedItemset++;
                    //System.out.println("PRUNED: " + currItemset);
                    continue;
                }
            }
            supportCountedItemset++;
            float val = FindSupportBFS(currItemset);
            //System.out.println("--> " + currItemset.toString() + " val: " + val + " tnr: " + getDatabaseSize());

            if (val >= minsup) {
                countitemsets++;
                /*SortedSet<Integer> set = new TreeSet<>();
                set.addAll(currItemset);
                
                test.Algorithm.frequent_list_set.add(set.toArray(new Integer[currItemset.size()]));
                test.Algorithm.frequent_list.put(set.toString(), val);*/
                //prints the freq ored itemsets
                //System.out.println("--> " + currItemset.toString() + " val: " + val + " tnr: " + getDatabaseSize());
                levelItemsets.add(currItemset);
            } else {// add the itemset to IIT
                addedToIITItemset++;
                iiTree.addTransaction(currItemset);
                //System.out.println("ADDED " + currItemset + " to IIT");
            }
        }
        return levelItemsets;
    }

 /**
     *
     * @param list
     * @return
     */
    private float FindSupportBFS(List<Integer> list) {
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
                @Override
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
        return sumOfSupport/(float)getDatabaseSize();
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
