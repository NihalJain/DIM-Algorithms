package algorithm.FDIM.DFS;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Math.ceil;
import java.util.ArrayList;
import java.util.BitSet;
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
public class AlgoDIMDFS {

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
    int candidateItemsetsCount = 0;
    FPTree tree = null;
    int _minsupp;
    int _maxitems, freqItemsetsCount = 0;
    Integer preference[];
    // used by dfs for support counting
    //private final int sumOfSupport = 0;
    private int currLevel;

    //int sumOfSupport=0;
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
            //System.out.println("intKeys["+q+"] :"+X[q]);
            q++;
        }

        List<Integer> t = new ArrayList<>();
        Collections.addAll(t, X);
        // sort item in the transaction by descending order of support
        Collections.sort(t, new Comparator<Integer>() {

            @Override
            public int compare(Integer item1, Integer item2) {
                // compare the frequency
                int compare = mapSupport.get(item1) - mapSupport.get(item2);
                // if the same frequency, we check the lexical ordering!
                if (compare == 0) {
                    return (item2 - item1);
                }
                // otherwise, just use the frequency
                return compare;
            }
        });
        Integer[] revMap = new Integer[total_singles + 1];
        intKeys = new Integer[X.length];
        for (int tmp = 0; tmp < t.size(); tmp++) {
            intKeys[tmp] = t.get(tmp);
            //System.out.println("intKeys["+tmp+"] :"+intKeys[tmp]);
            if (t.get(tmp) != null) {
                revMap[t.get(tmp)] = tmp;
                //System.out.println("revMap["+t.get(tmp)+"] :"+revMap[t.get(tmp)]);
            }
        }
        intKeys = new Integer[mapSupport.size()];
        q = 0;

        for (int stringKey : mapSupport.keySet()) {
            intKeys[q] = stringKey;
            //System.out.println("intKeys["+q+"] :"+intKeys[q]);
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
            List<Integer> newTransaction = new ArrayList<>();
            for (int i = 0; i < transaction.size(); i++) {
                newTransaction.add(revMap[transaction.get(i)]);
            }
            //System.out.println("Transaction: "+transaction);
            //System.out.println("NewTransaction: "+newTransaction);

            // add the sorted transaction to the fptree.
            tree.addTransaction(newTransaction);
            // increase the transaction count
            databaseSize++;
        }
        // close the input file
        reader.close();
        t2 = System.currentTimeMillis();
        System.out.println("Tree build time : " + (t2 - t1) + "ms");
        t1 = System.currentTimeMillis();
        // calling FPOred function on TREE tree with minsupp.
        _minsupp = (int) (ceil(minsupp * databaseSize));
        System.out.println("minsupp:" + _minsupp);
        _maxitems = maxitem;
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

        //List<Integer> list = new ArrayList<>();
        BitSet list = new BitSet(total_singles);
        //List<Integer> origList = new ArrayList<>();
        //preference = new Integer[total_singles];
        //BUG: IF only frequent items are kept, 
        //then all generateed itemsets are always frequent
        /*int pruned_singles = 0;
        for (Integer i = 0; i < intKeys.length; i++) {
            //System.out.println(mapSupport.get(intKeys[i]));
            if (mapSupport.get(intKeys[i]) >= _minsupp) {
                list.add(intKeys[i]);
            } else {
                //System.out.println("Pruned "+intKeys[i]+"single item infrequency.");
                pruned_singles++;
            }
        }
        int remainingSingles = total_singles - pruned_singles;
        //System.out.println(remainingSingles);
        if (remainingSingles == 0) {
            System.out.println("NO candidates satsify the min supp condition!");
            return;
        } else if (remainingSingles < _maxitems) {
            _maxitems = remainingSingles;
        }*/

        for (Integer i = 0; i < intKeys.length; i++) {
            //System.out.println(mapSupport.get(intKeys[i]));
            list.set(intKeys[i]);
            //list.add(intKeys[i]);
        }

        if (total_singles == 0) {
            System.out.println("NO candidates satsify the min supp condition!");
            return;
        } else if (total_singles < _maxitems) {
            _maxitems = total_singles;
        }

        long time = 0;
        long t1, t2;
        List<BitSet> levelItemsets = new ArrayList<>();
        t1 = System.currentTimeMillis();

        if (_maxitems < total_singles) {

            levelItemsets = generateLevelOne(list, _maxitems);
        } else {
            System.out.println("\nLEVEL: " + currLevel + "\nCurrent itemset size: " + list.cardinality() + "\nLevel candidate itemsets on ENTRY: " + 1);
            candidateItemsetsCount++;
            currLevel++;
            processItemset(list);
            levelItemsets = generateLevelOne(list, _maxitems - 1);
        }
        t2 = System.currentTimeMillis();
        System.out.println("\nMiddle lattice generation, Time: " + (t2 - t1));
        while (!levelItemsets.isEmpty() && !levelItemsets.get(0).isEmpty()) {
            candidateItemsetsCount += levelItemsets.size();
            System.out.println("\nLEVEL: " + currLevel + "\nCurrent itemset size: " + levelItemsets.get(0).cardinality() + "\nLevel candidate itemsets on ENTRY: " + levelItemsets.size());
            //System.out.println(countitemsets);
            //System.out.println("Level itemsets on ENTRY: " + levelItemsets.size());
            //System.out.println("ENTERED NEW LEVEL");
            t1 = System.currentTimeMillis();
            levelItemsets = processItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            System.out.println("Level frequent itemsets: " + levelItemsets.size() + "\nProcessing Done, Time: " + (t2 - t1));
            time += t2 - t1;

            /*t1 = System.currentTimeMillis();
            for (List<Integer> itemset : levelItemsets) {
                Collections.sort(itemset);
            }
            t2 = System.currentTimeMillis();
            System.out.println("Itemsets PreSort, Time: " + (t2 - t1));*/
            t1 = System.currentTimeMillis();
            levelItemsets = getSubsetItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            System.out.println("Subset Generation, Time: " + (t2 - t1));

            /*t1 = System.currentTimeMillis();
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
            System.out.println("Itemsets PostSort, Time: " + (t2 - t1));*/
            //System.out.println("Level itemsets on EXIT: " + levelItemsets);
            ++currLevel;
        }
        // writer.close();
        // summarizing result
        System.out.println("\nTime in support calculation:" + time);
        System.out.println("Total candidates " + candidateItemsetsCount);
        System.out.println("Total " + freqItemsetsCount + " frequent ORed Itemsets found.");
    }

    private boolean checkSeq(BitSet itemset, int k) {
        //List<Long> items = new ArrayList<>();
        //int firstBit = itemset.nextSetBit(0);
        //System.out.println("Here "+ firstBit);
        //k = itemset.previousSetBit(total_singles);
        int k_1 = itemset.previousSetBit(k - 1);
        //while (k > firstBit) {
        while (k > 0) {
            //if (itemset.get(k) - itemset.get(k - 1) == 1) {
            if (k - k_1 == 1) {
                k = k_1;
                k_1 = itemset.previousSetBit(k_1 - 1);
                //System.out.println("k:"+k);
            } else {
                return false;
            }
        }

        return true;//k == 0;
    }

    /**
     * Generates all subset itemsets of the given itemset
     *
     * @param itemset the itemset whose subset itemsets is required
     * @return all the subsets of the passed itemset
     */
    private List<BitSet> generateSubsets(BitSet itemset) {
        List<BitSet> subsetItemsets = new ArrayList<>();

        //int firstBit = itemset.nextSetBit(0);
        //if (itemset.get(firstBit)) {
        if (itemset.get(0)) {
            //System.out.println("Here1");
            for (int k = itemset.previousSetBit(total_singles); k > 0; k = itemset.previousSetBit(k - 1)) {
                //System.out.println("Here2");
                if (checkSeq(itemset, k)) {
                    //System.out.println("Here3");
                    //List<Integer> newItemset = cloner.deepClone(itemset);
                    BitSet newItemset = new BitSet(total_singles);
                    newItemset.or(itemset);
                    //newItemset.addAll(itemset);
                    newItemset.clear(k);
                    subsetItemsets.add(newItemset);
                }
            }

            //if (itemset.get(0) == 0) {
            //List<Integer> newItemset = cloner.deepClone(itemset);
            BitSet newItemset = new BitSet(total_singles);
            newItemset.or(itemset);
            //newItemset.addAll(itemset);
            //newItemset.clear(firstBit);
            newItemset.clear(0);
            subsetItemsets.add(newItemset);
            //}
        }
        //System.out.println(itemset+"---->"+subsetItemsets);
        return subsetItemsets;
    }

    private List<BitSet> getSubsetItemsets(List<BitSet> itemsets) {
        List<BitSet> subsetItemsets = new ArrayList<>();
        for (BitSet itemset : itemsets) {
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
    public List<BitSet> generateLevelOne(BitSet input, int maxitems) {
        List<BitSet> subsets = new ArrayList<>();

        //int[] s = new int[maxitems];                  // here we'll keep indices 
        List<Integer> s = new ArrayList<>();
        for (int i = 0; i < maxitems; i++) {
            s.add(i);
        }

        // pointing to elements in input array
        if (maxitems <= input.cardinality()) {
            // first index sequence: 0, 1, 2, ...
            for (int i = 0; (s.set(i, i)) < maxitems - 1; i++) {
                //empty-statement
            }
            BitSet b = new BitSet(total_singles);
            //subsets.add(getSubset(input, s));
            for (int item : s) {
                b.set(item);
            }
            subsets.add(b);

            while (true) {
                int i;
                // find position of item that can be incremented
                for (i = maxitems - 1; i >= 0 && s.get(i) == input.cardinality() - maxitems + i; i--) {
                    //empty-statement
                }
                if (i < 0) {
                    break;
                } else {
                    //s[i]++;                    // increment this item
                    s.set(i, s.get(i) + 1);
                    for (++i; i < maxitems; i++) {    // fill up remaining items
                        s.set(i, s.get(i - 1) + 1);
                    }
                    //subsets.add(getSubset(input, s));
                    BitSet x = new BitSet(total_singles);
                    for (int item : s) {
                        x.set(item);
                    }
                    subsets.add(x);
                    //subsets.add(new ArrayList<>(s));

                }
            }
        }

        return subsets;
    }

    public void processItemset(BitSet currItemset) {
        int val = FindSupportDFS(currItemset);
        //System.out.println("--> " + currItemset.toString() + " val: " + val + " tnr: " + getDatabaseSize());

        if (val >= _minsupp) {
            freqItemsetsCount++;
            //checkClosed(currItemset, currTidset);
            /*SortedSet<Integer> set = new TreeSet<>();
                set.addAll(currItemset);
                
                test.Algorithm.frequent_list_set.add(set.toArray(new Integer[currItemset.size()]));
                test.Algorithm.frequent_list.put(set.toString(), val);*/
            //prints the freq ored itemsets
            //System.out.println("--> " + currItemset.toString() + " val: " + val + " tnr: " + getDatabaseSize());

        }

    }

    public List<BitSet> processItemsets(List<BitSet> itemsets) {
        List<BitSet> levelItemsets = new ArrayList<>();
        //for (int i = 0; i < itemsets.size(); i++) {
        for (BitSet currItemset : itemsets) {
            //List<Integer> currItemset = itemsets.get(i);

            int val = FindSupportDFS(currItemset);
           // System.out.println("--> " + currItemset.toString() + " val: " + val + " tnr: " + _minsupp);

            if (val >= _minsupp) {
                freqItemsetsCount++;
                /*SortedSet<Integer> set = new TreeSet<>();
                set.addAll(currItemset);
                
                test.Algorithm.frequent_list_set.add(set.toArray(new Integer[currItemset.size()]));
                test.Algorithm.frequent_list.put(set.toString(), val);*/
                //prints the freq ored itemsets
                //System.out.println("--> " + currItemset.toString() + " val: " + val + " tnr: " + getDatabaseSize());
                levelItemsets.add(currItemset);
            }
        }
        return levelItemsets;
    }

    /**
     *
     * @param list
     * @return
     */
    private int FindSupportDFS(BitSet list) {
        int sumOfSupport = 0;
        // Mark all the vertices as not visited(By default
        // set as false)
        //boolean visited[] = new boolean[FPTree.current_node + 1];
        //Arrays.fill(visited, false);
        /*for (int i = 0; i < visited.length; ++i) {
            visited[i] = false;
        }*/
        int indexOfMin = list.nextSetBit(0);
        // Create a stack for DFS+
        LinkedList<FPNode> stack = new LinkedList<>();
        //Stack<FPNode> stack = new Stack<>();
        // Mark the current node as visited and enqueue it
        //visited[0] = true;
        //stack.addFirst(tree.root);
        for (FPNode child : tree.root.childs) {
            int id = child.itemID;
            if (id >= indexOfMin) {
                if (!list.get(id)) {
                    stack.add(child);
                } else {
                    //visited[child.nodeID] = true;
                    sumOfSupport += child.counter;
                }
            }
        }

        //stack.addAll(0, tree.root.childs);
        FPNode src;
        //System.out.print("Reached "+tree.root.nodeID);
        while (!stack.isEmpty()) {
            // Dequeue a vertex from queue and print it
            src = stack.poll();
            //src = stack.pop();
            //System.out.print("Dequed "+src+" ");

            // Get all adjacent vertices of the dequeued vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it
            //stack.addAll(0, adj);
            /*Collections.sort(adj, new Comparator<FPNode>() {
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
            });*/
            //System.out.println("list size: "+src.childs.size());
            //for (int i = 0; i < adj.size(); i++) {
            //for(FPNode n: adj){   
            //boolean foundFlag = false;
            //FPNode n = adj.get(i);
            //System.out.println("Visited node: "+n.nodeID +" visited: "+visited[n.nodeID]); 
            //if (!visited[src.nodeID]) //n.nodeID != -1 && 
            //{
                /*for (Integer item : list) {
                        if (item.equals(n.itemID)) {
                            sumOfSupport += n.counter;
                            foundFlag = true;
                            break;
                        }
                    }*/

                if (list.get(src.itemID)) {
                    //System.out.println("count: "+src.counter+"\nbefore: "+sumOfSupport);
                    sumOfSupport += src.counter;
                    //System.out.println("after: "+sumOfSupport);
                    //stack.addAll(0, src.childs);
                    //foundFlag = true;
                }
                else
                //if (!foundFlag) 
                {
                    for (FPNode child : src.childs) {
                        int id = child.itemID;
                        if (id >= indexOfMin) {
                            if (!list.get(id)) {
                                stack.add(child);
                            } else {
                                //visited[child.nodeID] = true;
                                sumOfSupport += child.counter;
                            }
                        }
                    }
                    // stack.addAll(0, src.childs);

                }
                //System.out.println("Visited: "+src.nodeID);
                //visited[src.nodeID] = true;
                //}
            //}
        }
        return sumOfSupport; // (float) getDatabaseSize();
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
