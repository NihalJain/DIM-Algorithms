package algorithm.FDIM.BitSetBased;

import com.google.common.collect.Ordering;
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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
    float minsup;
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
        t2 = System.currentTimeMillis();
        System.out.println("Tree build time : " + (t2 - t1) + "ms");
        t1 = System.currentTimeMillis();
        // calling FPOred function on TREE tree with minsupp.
        minsup = minsupp;
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
        List<List<Integer>> levelItemsets = new ArrayList<>();

        //Itemsets(list, list, levelItemsets, 0, total_singles);
        if (maxitems < total_singles) {
            levelItemsets = generateLevelOne(list, maxitems);
        } else {
            countitemsets++;
            //SortedSet<Integer> set = new TreeSet<>();
            //set.addAll(list);
            //test.Algorithm.frequent_list_set.add(set.toArray(new Integer[list.size()]));
            //test.Algorithm.frequent_list.put(set.toString(), (float)1.0);
            levelItemsets = generateLevelOne(list, maxitems - 1);
        }
        long time = 0;
        long t1, t2;
        while (!levelItemsets.isEmpty()) {
            //System.out.println("Level itemsets: " + levelItemsets);
            System.out.println(countitemsets);
            System.out.println("ENTERED NEW LEVEL");
            
            t1 = System.currentTimeMillis();
            levelItemsets = processItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            System.out.println("Processing Done, Time: " + (t2 - t1));
            
            //System.out.println("Level itemsets: " + levelItemsets);
            time += t2 - t1;
            
            t1 = System.currentTimeMillis();
            List<List<Integer>> complimentItemsets = new ArrayList<>();
            for (int i = 0; i < levelItemsets.size(); i++) {
                List<Integer> currItemset = levelItemsets.get(i);
                List<Integer> complimentItemset = findComplimentItemset(currItemset);
                //Collections.sort(complimentItemset);
                complimentItemsets.add(complimentItemset);
            }
            t2 = System.currentTimeMillis();
            System.out.println("Complimented, Time: " + (t2 - t1));
            //System.out.println("Compliment itemsets: " + complimentItemsets);
            
            t1 = System.currentTimeMillis();
            Ordering ordering = Ordering.natural();
            Collections.sort(complimentItemsets, ordering.lexicographical());
            t2 = System.currentTimeMillis();
            System.out.println("Compliment Sort, Time: " + (t2 - t1));
            //System.out.println("Sorted Compliment itemsets: " + complimentItemsets);
            
            t1 = System.currentTimeMillis();
            List<List<Integer>> genItemsets = generateCandidateSizeK(complimentItemsets);
            t2 = System.currentTimeMillis();
            //System.out.println("Generated itemsets: " + genItemsets);
            System.out.println("Ap-Generation, Time: " + (t2 - t1));
            
            t1 = System.currentTimeMillis();
            levelItemsets = new ArrayList<>();
            for (int i = 0; i < genItemsets.size(); i++) {
                List<Integer> currItemset = genItemsets.get(i);
                List<Integer> itemset = findComplimentItemset(currItemset);
                Collections.sort(itemset);
                levelItemsets.add(itemset);
            }
            t2 = System.currentTimeMillis();
            System.out.println("Complimented, Time: " + (t2 - t1));
            //System.out.println("Level itemsets: " + levelItemsets);

        }
        // writer.close();
        // summarizing result
        System.out.println("Time in processsing:" + time);
        System.out.println("Total candidates " + candidateItemset);
        System.out.println("Total " + countitemsets + " frequent ORed Itemsets found.");
    }

    public List<Integer> findComplimentItemset(List<Integer> currItemset) {
        List<Integer> complimentItemset = new ArrayList<>();
        boolean[] included = new boolean[total_singles];

        for (int j = 0; j < currItemset.size(); j++) {
            included[currItemset.get(j)] = true;
        }

        //for (int j = included.length - 1; j >= 0; j--) {
        for (int j = 0; j < included.length; j++) {
            if (included[j] == false) {
                complimentItemset.add(j);
            }
        }
        return complimentItemset;
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

    /**
     * Enumerate from middle recursively: VERY FAST
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

    public List<List<Integer>> processItemsets(List<List<Integer>> itemsets) {
        List<List<Integer>> levelItemsets = new ArrayList<>();
        for (int i = 0; i < itemsets.size(); i++) {
            List<Integer> currItemset = itemsets.get(i);

            float val = FindSupport(currItemset);
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
            }
        }
        return levelItemsets;
    }

    /* Generating candidate itemsets of size k from frequent itemsets of size
     * k-1. This is called "apriori-gen" in the paper by agrawal. This method is
     * also used by the Apriori algorithm for generating candidates.
     * Note that this method is very optimized. It assumed that the list of
     * itemsets received as parameter are lexically ordered.
     * 
     * @param levelK_1  a set of itemsets of size k-1
     * @return a set of candidates
     */
    protected List<List<Integer>> generateCandidateSizeK(List<List<Integer>> levelK_1) {
        // create a variable to store candidates
        List<List<Integer>> candidates = new ArrayList<>();

        // For each itemset I1 and I2 of level k-1
        loop1:
        for (int i = 0; i < levelK_1.size(); i++) {
            List<Integer> itemset1 = levelK_1.get(i);
            //System.out.println("1:" + itemset1);
            loop2:
            for (int j = i + 1; j < levelK_1.size(); j++) {
                List<Integer> itemset2 = levelK_1.get(j);
                //System.out.println("2:" + itemset2);
                // we compare items of itemset1 and itemset2.
                // If they have all the same k-1 items and the last item of
                // itemset1 is smaller than
                // the last item of itemset2, we will combine them to generate a
                // candidate
                for (int k = 0; k < itemset1.size(); k++) {
                    // if they are the last items
                    if (k == itemset1.size() - 1) {
                        // the one from itemset1 should be smaller (lexical
                        // order)
                        // and different from the one of itemset2
                        if (itemset1.get(k) >= itemset2.get(k)) {
                            continue loop1;
                        }
                    } // if they are not the last items, and
                    else if (itemset1.get(k) < itemset2.get(k)) {
                        continue loop2; // we continue searching
                    } else if (itemset1.get(k) > itemset2.get(k)) {
                        continue loop1; // we stop searching: because of lexical
                        // order
                    }
                }

                // Create a new candidate by combining itemset1 and itemset2
                int lastItem1 = itemset1.get(itemset1.size() - 1);
                int lastItem2 = itemset2.get(itemset2.size() - 1);
                //System.out.println(lastItem1 + " " + lastItem2);
                Cloner cloner = new Cloner();
                if (lastItem1 < lastItem2) {
                    // Create a new candidate by combining itemset1 and itemset2  
                    List<Integer> newItemset = cloner.deepClone(itemset1);
                    newItemset.add(lastItem2);
                    candidates.add(newItemset);
                } else {
                    // Create a new candidate by combining itemset1 and itemset2
                    List<Integer> newItemset = cloner.deepClone(itemset2);
                    newItemset.add(lastItem1);
                    candidates.add(newItemset);
                }

            }
        }
        // return the set of candidates
        return candidates;
    }

    /**
     * Enumerate from middle recursively: VERY SLOW
     *
     * @param list list of items for candidates generation
     * @param currList
     * @param levelItemsets
     * @param depth
     * @param skip
     */
    public void Itemsets(List<Integer> list, List<Integer> currList, List<List<Integer>> levelItemsets, int depth, int skip) {
        if (total_singles - depth <= maxitems) {
            //float val = FindSupport(currList);
            //System.out.println("--> depth: " + depth + "Itemset: "+ currList.toString() ); 
            //System.out.println("--> " + currList.toString()+ " val: " + val + " tnr: " + getDatabaseSize());
            //if (val >= minsup){
            //countitemsets++;
            //SortedSet<Integer> set = new TreeSet<>();
            //set.addAll(currList);
            levelItemsets.add(currList);
            //test.Algorithm.frequent_list_set.add(set.toArray(newInteger[currList.size()]));
            //test.Algorithm.frequent_list.put(set.toString(), val); //prints the freq ored itemsets 
            //System.out.println("--> " + currList.toString() + " val:" + val + " tnr: " + getDatabaseSize()); } 
            return;
        }

        for (int i = skip - 1; i >= 0; i--) {
            Cloner cloner = new Cloner();
            List<Integer> newList = cloner.deepClone(currList);
            //System.out.println("--> depth: "+i); 
            newList.remove(list.get(i));
            // System.out.println("--> depth: " + depth + "Itemset: " + newList.toString());
            Itemsets(list, newList, levelItemsets, depth + 1, i);
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
        for (int k = 0; k < indexOfItem; k++) {
            if (X_node.bitMap.get(list.get(k))) {
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
