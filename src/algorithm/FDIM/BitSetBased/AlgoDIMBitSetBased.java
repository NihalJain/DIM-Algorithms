package algorithm.FDIM.BitSetBased;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
public class AlgoDIMBitSetBased {

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
    float minsup, maxsup;
    int maxitems, countitemsets = 0;
    int countt = 0;
    Integer preference[];
    // used by dfs for support counting
    private final int sumOfSupport = 0;

    /**
     * Method to run the FP tree based ORed Itemset generation algorithm.
     *
     * @param input the path to an input file containing a transaction database.
     * @param minsupp the minimum support threshold.
     * @param maxsupp the maximum support threshold.
     * @param maxitem the maximum pattern length.
     * @throws IOException exception if error reading or writing files.
     * @throws FileNotFoundException exception if input file not found.
     */
    public void runAlgorithm(String input, float minsupp, float maxsupp, int maxitem) throws FileNotFoundException, IOException {

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
        tree = new FPTree(mapSupport.size());
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
        maxsup = maxsupp;
        maxitems = maxitem;
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
     */
    public void Itemsets(List<Integer> list, int start, int end, int depth) {
        for (int i = start; i >= end; i--) {
            if (depth == end + 1) {
                return;
            }
            List<Integer> newlist = new ArrayList<>(list);
            newlist.remove(list.get(i));
            if(newlist.size() <= maxitems){
                float val = FindSupport(newlist);
                //System.out.println("--> start: " + i + " depth: " + depth + "Itemset: "+ newlist.toString() );
                if (val >= minsup) {
                    if(val <= maxsup){
                        countitemsets++;
                        SortedSet<Integer> set = new TreeSet<>();
                        set.addAll(newlist);
                        test.Algorithm.frequent_list_set.add(set.toArray(new Integer[newlist.size()]));
                        test.Algorithm.frequent_list.put(set.toString(), val);
                        //prints the freq ored itemsets
                        //System.out.println("--> " + newlist.toString() + " val: " + val + " tnr: " + getDatabaseSize());
                    }
                    Itemsets(newlist, i - 1, end, depth - 1);
                }
            }
            else{
                Itemsets(newlist, i - 1, end, depth - 1);
            }
        }
    }

    /**
     *
     * @param list candidate itemset
     * @return support of itemset
     */
    private float FindSupport(List<Integer> list) {
        long sum = 0;
  
        for (int k = 0; k < list.size(); k++) {
            
            // find the first/head node of the header list corresponding to item k
            FPNode X_node = tree.mapItemNodes.get(list.get(k));
            while (X_node != null) {
                int temp = Check_path(list, k, X_node);
                if (temp == 0) {
                    sum = sum + X_node.counter;
                }
                X_node = X_node.nodeLink;
            }
        }
        return ((float) sum / getDatabaseSize());
    }
  
    /**
     * @param list candidate itemset
     * @param i index
     * @param X_node FP-tree node
     * @param tree FP-tree
     * @return 1 if exist otherwise 0
     */
    private int Check_path(List<Integer> list, int indexOfItem, FPNode X_node) {
        int result = 0;
        for(int k = 0; k < indexOfItem; k++) {
            if(X_node.bitMap.get(list.get(k))){
                //System.out.println("Item "+ list.get(k) + " is parent of "+ X_node.nodeID);
                result = 1;
                break;
            }
        }
        return result;
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
