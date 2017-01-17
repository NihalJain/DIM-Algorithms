package algorithm.FDIM.BitSet;

import com.rits.cloning.Cloner;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Implementation of bitset based algorithm for finding Frequent ORed Itemsets.
 *
 * @author nihal jain
 * @version 1.0
 */
public class AlgoDIMBitSet {

    Cloner cloner = new Cloner();

    // number of transactions in the database
    public static int databaseSize;
    // Hashmap for storing frequencies of each item in dataset
    final Map<Integer, Integer> mapSupport = new HashMap<>();
    // number of items in database
    public static int total_singles = 0;
    // all unique items present in dataset
    private Integer[] intKeys;

    List<BitSet> dT;

    float _minsupp;
    int _maxitems;
    // total candidate itemsets
    int candidateItemsetsCount = 0;
    // total frequent itemsets discovered
    int freqItemsetsCount = 0;
    // current level under inspection
    private int currLevel = 0;

    /**
     * Method to run the FP tree based ORed Itemset generation algorithm.
     *
     * @param input the path to an input file containing a transaction database.
     * @param minsupp the minimum support threshold.
     * @param maxitems the maximum pattern length.
     * @throws IOException exception if error reading or writing files.
     * @throws FileNotFoundException exception if input file not found.
     */
    public void runAlgorithm(String input, float minsupp, int maxitems) throws FileNotFoundException, IOException {

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
        // read the file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(input));
        } catch (Exception e) {
            System.err.println("error: Unable to open input file");
            System.exit(-1);
        }
        String line;

        dT = new ArrayList<>();
        for (int i = 0; i < total_singles; i++) {
            dT.add(new BitSet(databaseSize));
        }
        // for each line (transaction) until the end of the file
        int tID = 1;
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
                dT.get(item).set(tID);
            }

            // increase the transaction count
            tID++;
        }
        // close the input file
        reader.close();
        t2 = System.currentTimeMillis();
        System.out.println("Tree build time : " + (t2 - t1) + "ms");
        t1 = System.currentTimeMillis();
        // calling FPOred function on TREE tree with minsupp.
        _minsupp = minsupp;
        _maxitems = maxitems;
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
            //transactionCount++;
            databaseSize++;
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
        //preference = new Integer[total_singles];
        for (Integer i = 0; i < intKeys.length; i++) {
            list.add(intKeys[i]);
        }

        long time = 0;
        long t1, t2;
        List<List<Integer>> levelItemsets = new ArrayList<>();
        t1 = System.currentTimeMillis();

        if (_maxitems < total_singles) {
            levelItemsets = generateLevelOne(list, _maxitems);
        } else {
            freqItemsetsCount++;
            //SortedSet<Integer> set = new TreeSet<>();
            //set.addAll(list);
            //test.Algorithm.frequent_list_set.add(set.toArray(new Integer[list.size()]));
            //test.Algorithm.frequent_list.put(set.toString(), (float)1.0);
            levelItemsets = generateLevelOne(list, _maxitems - 1);
        }
        t2 = System.currentTimeMillis();
        System.out.println("\nMiddle lattice generation, Time: " + (t2 - t1));
        while (!levelItemsets.isEmpty() && !levelItemsets.get(0).isEmpty()) {
            candidateItemsetsCount += levelItemsets.size();
            System.out.println("\nLEVEL: " + currLevel);
            //System.out.println(freqItemsetsCount);
            //System.out.println("Level itemsets on ENTRY: " + levelItemsets);
            //System.out.println("ENTERED NEW LEVEL");
            t1 = System.currentTimeMillis();
            levelItemsets = processItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            //System.out.println("Level itemsets on PRUNING: " + levelItemsets);
            System.out.println("Processing Done, Time: " + (t2 - t1));
            time += t2 - t1;

            t1 = System.currentTimeMillis();
            levelItemsets = getSubsetItemsets(levelItemsets);
            t2 = System.currentTimeMillis();
            System.out.println("Subset Generation, Time: " + (t2 - t1));

            ++currLevel;
        }

        // summarizing result
        System.out.println("\nTime in support calculation:" + time);
        System.out.println("Total candidates " + candidateItemsetsCount);
        System.out.println("Total " + freqItemsetsCount + " frequent ORed Itemsets found.");
        //System.out.println("Total " + addedToIITItemset + " itemsets added to IIT.");
        //System.out.println("Total " + prunedItemset + " itemsets pruned without support count.");
        //System.out.println("Total " + supportCountedItemset + " itemsets processed for support count.");

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
        if (itemset.contains(0)) {
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
    public List<List<Integer>> generateLevelOne(List<Integer> input, int maxitems) {
        List<List<Integer>> subsets = new ArrayList<>();

        int[] s = new int[maxitems];                  // here we'll keep indices 
        // pointing to elements in input array

        if (maxitems <= input.size()) {
            // first index sequence: 0, 1, 2, ...
            for (int i = 0; (s[i] = i) < maxitems - 1; i++) {
                //empty-statement
            }
            subsets.add(getSubset(input, s));
            while (true) {
                int i;
                // find position of item that can be incremented
                for (i = maxitems - 1; i >= 0 && s[i] == input.size() - maxitems + i; i--) {
                    //empty-statement
                }
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
     * @param list candidate itemset
     * @return support of itemset
     */
    private float FindSupport(List<Integer> list) {
        BitSet temp = cloner.deepClone(dT.get(0));
        for (int k = 1; k < list.size(); k++) {
            temp.or(dT.get(k));

        }
        return ((float) temp.cardinality() / getDatabaseSize());
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
