package algorithm.CIM.AncestorBitset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.StringTokenizer;

public class AlgoCIMAncestorBitset {

	/** the current level */
	protected int k; 


        /** variables for counting support of items */
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();

	/** the minimum support threshold */
	int minSuppRelative;

	/** Special parameter to set the maximum size of itemsets to be discovered */
	int maxItemsetSize = Integer.MAX_VALUE;

	/** start time of latest execution */
	long startTimestamp = 0; 
	
	/** end time of latest execution */
	long endTimeStamp = 0; 
	
	/** object for writing to file if the user choose to write to a file */
	BufferedWriter writer = null;
	
	/** variable to store the result if the user choose to save to memory instead of a file */
	protected Itemsets patterns = null;

	/** the number of frequent itemsets found */
	private int itemsetCount = 0;
	
	/** the number of transactions */
	private int databaseSize = 0;
	
	/** the current transaction database, if the user has provided one 
	   instead of an input file. */
	private TransactionDatabase database = null;

	/** indicate if the empty set should be added to the results */
	private boolean emptySetIsRequired = false;
	
	/** if true, transaction identifiers of each pattern will be shown*/
	boolean showTransactionIdentifiers = false;

        // Hashmap for storing frequencies of each item in dataset
    final Map<Integer, Integer> mapSupport = new HashMap<>();
         FPTree tree = null;
          // number of items in database
    public static int total_singles = 0;
    // all unique items present in dataset
    private Integer[] intKeys;
	/**
	 * Default constructor
	 */
	public AlgoCIMAncestorBitset() {
	}
	
	/**
	 * This method run the algorithm on a transaction database already in memory.
	 * @param database  the transaction database
	 * @param minsup the minimum support threshold as a percentage (double)
	 * @return the method returns frequent itemsets
	 * @throws IOException  exception if error reading/writing the file
	 */
	public Itemsets runAlgorithm(TransactionDatabase database, double minsup)
			throws NumberFormatException, IOException {
		// remember the transaction database received as parameter
		this.database = database;
		// call the real "runAlgorithm() method
		Itemsets result = runAlgorithm(null, null, minsup);
		// forget the database
		this.database = null;
		
		// return the result
		return result;
	}
		int suppTime = 0;	
	/**
	 * This method run the algorithm.
	 * @param input  the file path of an input file.  if null, the result is returned by the method.
	 * @param output  the output file path
	 * @param minsup the minimum support threshold as a percentage (double)
	 * @return if no output file path is provided, the method return frequent itemsets, otherwise null
	 * @throws IOException  exception if error reading/writing the file
	 */
	public Itemsets runAlgorithm(String input, String output, double minsup)
			throws NumberFormatException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// reset number of itemsets found
		itemsetCount = 0;

		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		
		
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
                int compare = mapSupport.get(item1) - mapSupport.get(item2);
                // if the same frequency, we check the lexical ordering!
                if (compare == 0) {
                    return (item2 - item1);
                }
                // otherwise, just use the frequency
                return compare;
            }
        });
        /*intKeys = new Integer[X.length];
        for (int tmp = 0; tmp < t.size(); tmp++) {
            intKeys[tmp] = t.get(tmp);
        }
        intKeys = new Integer[mapSupport.size()];
        q = 0;
        for (int stringKey : mapSupport.keySet()) {
            intKeys[q] = stringKey;
            q++;
        }*/
        Integer[] revMap = new Integer[total_singles + 100];
        intKeys = new Integer[X.length];
        for (int tmp = 0; tmp < t.size(); tmp++) {
            intKeys[tmp] = t.get(tmp);
            //System.out.println("intKeys["+tmp+"] :"+intKeys[tmp]);
            if (t.get(tmp) != null) {
                revMap[t.get(tmp)] = tmp+1;
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
        
        
        
        
        // (1) count the tid set of each item in the database in one database
		// pass
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); // id item, count
                
		// read the input file line by line until the end of the file
		// (each line is a transaction)
		
		databaseSize = 0; 
		// if the database is in memory
		if(database != null){
			// for each transaction
			for(List<Integer> transaction : database.getTransactions()){ // for each transaction
				// for each token (item)
				for (int item : transaction) {
					// get the set of tids for this item until now
					Set<Integer> tids = mapItemTIDS.get(revMap[item]);
					// if null, create a new set
					if (tids == null) {
						tids = new HashSet<Integer>();
						 mapItemTIDS.put(revMap[item], tids);
					}
					// add the current transaction id (tid) to the set of the current item
					tids.add(databaseSize);
				}
				databaseSize++; // increment the tid number
			}
		}else{
			BufferedReader reader = new BufferedReader(new FileReader(input));
			
			String line;
			while (((line = reader.readLine()) != null)) { // for each transaction
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (line.isEmpty() == true ||
						line.charAt(0) == '#' || line.charAt(0) == '%'
								|| line.charAt(0) == '@') {
					continue;
				}
				
				// split the line into tokens according to spaces
				String[] lineSplited = line.split(" ");
				// for each token (item)
				for (String token : lineSplited) {
					// convert from string item to integer
					int item = Integer.parseInt(token);
					// get the set of tids for this item until now
					Set<Integer> tids = mapItemTIDS.get(revMap[item]);
					// if null, create a new set
					if (tids == null) {
						tids = new HashSet<Integer>();
						mapItemTIDS.put(revMap[item], tids);
					}
					// add the current transaction id (tid) to the set of the current item
					tids.add(databaseSize);
				}
				databaseSize++; // increment the tid number
			}
			reader.close(); // close the input file
		}
                //System.out.println("tset:"+mapItemTIDS);
                
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
        int tID = 1;
        while (((line = reader.readLine()) != null)) {
            // if the line is a comment, is empty or is a kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }
            // Tokenizing input line
            StringTokenizer lineSplited = new StringTokenizer(line);
            List<Integer> atransaction = new ArrayList<>();
            
            List<Integer> transaction = new ArrayList<>();
            // for each item in the transaction
            while (lineSplited.hasMoreElements()) {
                Integer item = Integer.parseInt(lineSplited.nextToken());
                // only add items that have the minimum support
                // if(!transaction.contains(item))
  //              atransaction.add(item);
                transaction.add(revMap[item]);
            }
            
//System.out.println("ATransaction: "+atransaction);
//System.out.println("Transaction: "+transaction);
            // sort item in the transaction by descending order of support
            Collections.sort(transaction);/*, new Comparator<Integer>() {
                @Override
                public int compare(Integer item1, Integer item2) {
                    // compare the frequency
                    int compare = mapSupport.get(revMap[item2]) - mapSupport.get(revMap[item1]);
                    // if the same frequency, we check the lexical ordering!
                    if (compare == 0) {
                        return (revMap[item1] - revMap[item2]);
                    }
                    // otherwise, just use the frequency
                    return compare;
                }
            });*/
          //  System.out.println("Transaction: "+transaction);
            // add the sorted transaction to the fptree.
            tree.addTransaction(transaction);
           // increase the transaction count
            tID++;
        }
        // close the input file
        reader.close();
        t2 = System.currentTimeMillis();
        System.out.println("Tree build time : " + (t2 - t1) + "ms");
        t1 = System.currentTimeMillis();
        
        
       
		
		
		// if the user want the empty set
		if(emptySetIsRequired ){
			// add the empty set to the set of patterns
			patterns.addItemset(new Itemset(new int[]{}), 0);
		}
		

		// convert the support from a relative minimum support (%) to an 
		// absolute minimum support
		this.minSuppRelative = (int) Math.ceil(minsup * databaseSize);

		// To build level 1, we keep only the frequent items.
		// We scan the database one time to calculate the support of each
		// candidate.
		k = 1;
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();
		while (iterator.hasNext()) {
			// check memory usage
			MemoryLogger.getInstance().checkMemory();
			
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator
					.next();
			// if the item is frequent
			if (entry.getValue().size() >= minSuppRelative) { 
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
                                //System.out.println("added:"+item);
				//itemset.setTIDs(mapItemTIDS.get(item));
				level.add(itemset);
				// save the itemset
				saveItemset(itemset);
			} else {
				iterator.remove(); // if the item is not frequent we don't
				// need to keep it into memory.
			}
		}

		// sort itemsets of size 1 according to lexicographical order.
		Collections.sort(level, new Comparator<Itemset>() {
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});

		// Generate candidates with size k = 1 (all itemsets of size 1)
		k = 2;
		// While the level is not empty
		while (!level.isEmpty() && k <= maxItemsetSize) {
			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = generateCandidateSizeK(level);
			k++;
		}


		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		// save the end time
		endTimeStamp = System.currentTimeMillis();
		// return frequent itemsets
		return patterns;
	}

	/**
	 * Method to generate itemsets of size k from frequent itemsets of size K-1.
	 * @param levelK_1  frequent itemsets of size k-1
	 * @return itemsets of size k
     * @throws java.io.IOException
	 */
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1)
			throws IOException {
		// create a variable to store candidates
		List<Itemset> candidates = new ArrayList<>();

		// For each itemset I1 and I2 of level k-1
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			Itemset itemset1 = levelK_1.get(i);
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				Itemset itemset2 = levelK_1.get(j);

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
						if (itemset1.getItems()[k] >= itemset2.get(k)) {
							continue loop1;
						}
					}
					// if the k-th items is smalle rinn itemset1
					else if (itemset1.getItems()[k] < itemset2.getItems()[k]) {
						continue loop2; // we continue searching
					} else if (itemset1.getItems()[k] > itemset2.getItems()[k]) {
						continue loop1; // we stop searching: because of lexical
										// order
					}
				}
                                
                                /*
				// create list of common tids 
				Set<Integer> list = new HashSet<Integer>();
				// for each tid from the tidset of itemset1
				for (Integer val1 : itemset1.getTransactionsIds()) {
					// if it appears also in the tidset of itemset2
					if (itemset2.getTransactionsIds().contains(val1)) {
						// add it to common tids
						list.add(val1);
					}
				}*/
                                int newItemset[] = new int[itemset1.size()+1];
                                System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
				newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
				BitSet bitsetItemset = new BitSet();
                                bitsetItemset.clear();
                                for(int item: newItemset)
                                    bitsetItemset.set(item);
                                 long t1 = System.currentTimeMillis();
                                int support = FindSupport(bitsetItemset);
                                long t2 = System.currentTimeMillis();
                                suppTime += t2 - t1;
				// if the combination of itemset1 and itemset2 is frequent
				if (support >= minSuppRelative) {
//System.out.println("Itemset:"+bitsetItemset+" Supp:"+support);                                    
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset(newItemset);
					//candidate.setTIDs(list);
					// add it to the list of candidates
					candidates.add(candidate);
					// save it 
					saveItemset(candidate);
				}
			}
		}
		return candidates;
	}

	/**
	 * Set the maximum itemset size of itemsets to be found
	 * @param maxItemsetSize maximum itemset size.
	 */
	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}

	/**
	 * Save a frequent itemset to the output file or memory,
	 * depending on what the user chose.
	 * @param itemset the itemset
	 * @throws IOException exception if error writing the output file.
	 */
	void saveItemset(Itemset itemset) throws IOException {
		itemsetCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			writer.write(itemset.toString() + " #SUP: "
					+ itemset.getTransactionsIds().size() );
			if(showTransactionIdentifiers) {
	        	writer.append(" #TID:");
	        	for (Integer tid: itemset.getTransactionsIds()) {
	        		writer.append(" " + tid); 
	        	}
			}
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			patterns.addItemset(itemset, itemset.size());
		}
	}

	/**
	 * Method to indicate if the empty set should be included in results
	 * or not.
	 * @param emptySetIsRequired  if true the empty set will be included.
	 */
	public void setEmptySetIsRequired(boolean emptySetIsRequired) {
		this.emptySetIsRequired = emptySetIsRequired;
	}
	
	/**
	 * Set that the transaction identifiers should be shown (true) or not (false) for each
	 * pattern found, when writing the result to an output file.
	 * @param showTransactionIdentifiers true or false
	 */
	public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
		this.showTransactionIdentifiers = showTransactionIdentifiers;
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  APRIORI TID v2.12 - STATS =============");
		System.out.println(" Transactions count from database : " + databaseSize);
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + 
				MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
                System.out.println("STime:"+ suppTime);
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
        System.out.println(mapSupport);
        // close the input file
        reader.close();
    }
     /**
     *
     * @param list candidate itemset
     * @return support of itemset
     */
    private int FindSupport(BitSet list) {
        int sum = 0;
        //System.out.println("Itemset:" + list);
        int k = list.previousSetBit(list.size());
        //System.out.println("Bitset:"+list+" K:" +k);
            //for (int k = list.nextSetBit(0); k >= 0; k = list.nextSetBit(k + 1)) {
            /*if (k == Integer.MAX_VALUE) {
         break; // or (k+1) would overflow
            }*/
            FPNode X_node = tree.mapItemNodes.get(k);
            // find the first/head node of the header list corresponding to item k
            while (X_node != null) {
                if (!Check_path(list, k, X_node)) {
                    //System.out.println("s:"+sum);
                    sum = sum + X_node.counter;
                }
                X_node = X_node.nodeLink;
            }
        
        return sum;
    }

    int c = 0;

    /**
     * @param list candidate itemset
     * @param i index
     * @param X_node FP-tree node
     * @param tree FP-tree
     * @return 1 if exist otherwise 0
     */
    private boolean Check_path(BitSet list, int indexOfItem, FPNode X_node) {
        boolean result = false;
        //System.out.println("Agaiin");
        //for (int k = list.previousSetBit(list.size()); k >= indexOfItem; k = list.previousSetBit(k - 1)) {
        for (int k = list.previousSetBit(indexOfItem - 1); k >= 0; k = list.previousSetBit(k - 1)) {
            //c++;
            // operate on index i here
            if (!X_node.bitMap.get(k)) {
                //System.out.println("Item " + k + " is parent of " + X_node.itemID);
                result = true;
                break;
            }
        }
        
        /*for (int k = list.nextSetBit(0); k < indexOfItem; k = list.nextSetBit(k+1)) {
           System.out.println("k:"+k+"->"+list.get(k));
            if (X_node.bitMap.get(k)) {
                System.out.println("Item "+ list.get(k) + " is parent of "+ X_node.nodeID);
                result = true;
                break;
            }
        }*/
        //System.out.println("Result:" + result);
        return result;
    }
	/**
	 * Get the number of transactions in the last database read.
	 * @return number of transactions.
	 */
	public int getDatabaseSize() {
		return databaseSize;
	}
}