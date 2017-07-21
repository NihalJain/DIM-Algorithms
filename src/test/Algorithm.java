package test;

import algorithm.FDIM.BFSBased.AlgoDIMBFSBased;
import algorithm.FDIM.DFSBased.AlgoDIMDFSBased;
import algorithm.FDIM.AncestorBitsetBased.AlgoDIMAncestorBitsetBased;
import algorithm.FDIM.TidSetBased.AlgoDIMTidSetBased;

import algorithm.FDIM.BFS.AlgoDIMBFS;
import algorithm.FDIM.DFS.AlgoDIMDFS;
import algorithm.FDIM.AncestorBitset.AlgoDIMAncestorBitset;
import algorithm.FDIM.TidSet.AlgoDIMTidSet;

import algorithm.FDIM.MFPImproved.AlgoDIMMFPImproved;

import algorithm.FDCIM.TidSet.AlgoDCIMTidSet;
import algorithm.FDCIM.AncestorBitset.AlgoDCIMAncestorBitset;
import algorithm.FDCIM.BFS.AlgoDCIMBFS;
import algorithm.FDCIM.DFS.AlgoDCIMDFS;

import algorithm.CIM.AncestorBitset.AlgoCIMAncestorBitset;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoEclat;
import algorithm.CIM.FPGrowth.AlgoFPGrowth;
import algorithm.FDCIM.TidSetPS.AlgoDCIMTidSetPS;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nihal jain
 * @version 1.0
 */
public class Algorithm {

    /**
     * Hash Map for Frequent itemset to its Support mapping.
     */
    public static Map<String, Float> frequent_list = new HashMap<>();
    /**
     * ArrayList for storing frequent ORed itemsets.
     */
    public static ArrayList<Integer[]> frequent_list_set = new ArrayList<>();

    /**
     * main function
     *
     * @param args input_dataset_name minimum_support_value algorithm_number
     * @throws FileNotFoundException If input file not found
     * @throws IOException throws IOException if any.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String input = null;
        Float minsupp = null, maxsupp = null;
        int whichAlgo = 0, maxitem = 0;
        int parsed = 0;
        //Scanner sc = new Scanner(System.in);

        //System.out.println(args.length);
        if (args.length != 5) {
            System.err.print("error: Missing argument!");
            System.exit(-1);
        }

        // ------------Input file --------------
        //System.out.print("Enter Input file path : ");
        try {
            input = args[1]; //jar mode
            //System.out.println(input);
            //input = fileToPath(args[1]); //use this for IDE(testing)
        } catch (Exception e) {
            System.out.println("error: Input file not found.");
            System.exit(-1);
        }

        //assigns which algorithm to run
        try {
            whichAlgo = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println("error: No algorithm specified.");
            System.exit(-1);
        }

        // ------------Support --------------
        //System.out.print("Enter Support Threshold : ");
        try {
            minsupp = Float.valueOf(args[3]);
        } catch (Exception e) {
            System.out.println("error: Invalid input for minimum Support.");
            System.exit(-1);
        }

        /*try {
            maxsupp = Float.valueOf(args[3]);
        } catch (Exception e) {
            System.out.println("error: Invalid input for minimum Support.");
            System.exit(-1);
        }*/
        //assigns maximum length of the pattern to search forto run
        try {
            maxitem = Integer.parseInt(args[4]);
        } catch (Exception e) {
            System.out.println("error: No maxitem specified.");
            System.exit(-1);
        }

        // preprocess dataset
        long parseStart = System.currentTimeMillis();
        ParseDataset parse = new ParseDataset();
        input = parse.ParseData(input);
        parsed = 1;
        long parseEnd = System.currentTimeMillis();

        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        switch (whichAlgo) {
            case 1:
                System.out.println("                       DIM BFS Based ALGORITHM");
                break;
            case 2:
                System.out.println("                       DIM DFS Based ALGORITHM");
                break;
            case 3:
                System.out.println("                       DIM AncestorBitset ALGORITHM");
                break;
            case 4:
                System.out.println("                       DIM TidSet Based ALGORITHM");
                break;
            case 5:
                System.out.println("                       DIM BFS (bitset) ALGORITHM");
                break;
            case 6:
                System.out.println("                       DIM DFS (bitset) ALGORITHM");
                break;
            case 7:
                System.out.println("                       DIM AncestorBitset (bitset) ALGORITHM");
                break;
            case 8:
                System.out.println("                       DIM TidSet (bitset) ALGORITHM");
                break;
            case 9:
                System.out.println("                        DCIM BFS (bitset) ALGORITHM");
                break;
            case 10:
                System.out.println("                        DCIM DFS (bitset) ALGORITHM");
                break;
            case 11:
                System.out.println("                        DCIM AncestorBitset (bitset) ALGORITHM");
                break;
            case 12:
                System.out.println("                        DCIM TidSet (bitset) ALGORITHM");
                break;
            case 13:
                System.out.println("                        CIM AncestorBitet (bitset) ALGORITHM");
                break;
            
            default:
                System.out.println("                       DIM MFP-Improved");
                break;
        }

        System.out.println("=======================================================================");
        System.out.println("                     MINING WITH BELOW PARAMETERS ");
        System.out.println("=======================================================================");
        System.out.println("DataSet : " + input);
        System.out.println("Min. Support Threshold : " + minsupp);
        System.out.println("Maximum pattern size : " + maxitem);
        System.out.println("-----------------------------------------------------------------------");

        // PHASE 1: finding All frequent ORed itemsets
        long lStartTime = System.currentTimeMillis();
        int databaseSize, totalSingles;

        //run the chosen algorithm
        switch (whichAlgo) {
            case 0: {
                AlgoDCIMTidSetPS dimAlgo = new AlgoDCIMTidSetPS();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDCIMTidSetPS.total_singles;
                break;
            }
            case 1: {
                AlgoDIMBFSBased dimAlgo = new AlgoDIMBFSBased();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMBFSBased.total_singles;
                break;
            }
            case 2: {
                AlgoDIMDFSBased dimAlgo = new AlgoDIMDFSBased();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMDFSBased.total_singles;
                break;
            }
             case 3: {
                AlgoDIMAncestorBitsetBased dimAlgo = new AlgoDIMAncestorBitsetBased();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMAncestorBitsetBased.total_singles;
                break;
            }
            
            case 4: {
                AlgoDIMTidSetBased dimAlgo = new AlgoDIMTidSetBased();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMTidSetBased.total_singles;
                break;
            }
            
            /*case 1: {
                AlgoDIMBitSetBased dimAlgo = new AlgoDIMBitSetBased();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMBitSetBased.total_singles;
                break;
            }*/
            case 5: {
                AlgoDIMBFS dimAlgo = new AlgoDIMBFS();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMBFS.total_singles;
                break;
            }
            case 6: {
                AlgoDIMDFS dimAlgo = new AlgoDIMDFS();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMDFS.total_singles;
                break;
            }
            case 7: {
                AlgoDIMAncestorBitset dimAlgo = new AlgoDIMAncestorBitset();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMAncestorBitset.total_singles;
                break;
            }
            case 8: {
                AlgoDIMTidSet dimAlgo = new AlgoDIMTidSet();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMTidSet.total_singles;
                break;
            }
            case 9: {
                AlgoDCIMBFS dimAlgo = new AlgoDCIMBFS ();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDCIMBFS.total_singles;
                break;
            }
            case 10 : {
                AlgoDCIMDFS dimAlgo = new AlgoDCIMDFS ();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDCIMDFS.total_singles;
                break;
            }
            case 11: {
                AlgoDCIMAncestorBitset dimAlgo = new AlgoDCIMAncestorBitset ();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDCIMAncestorBitset .total_singles;
                break;
            }
            
            case 12: {
                AlgoDCIMTidSet dimAlgo = new AlgoDCIMTidSet();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDCIMTidSet.total_singles;
                break;
            }
            
            case 13: {
                input = fileToPath(args[1]);
                //input = args[1];
                AlgoCIMAncestorBitset cimAlgo = new AlgoCIMAncestorBitset();
                cimAlgo.runAlgorithm(input, null, minsupp);
                cimAlgo.printStats();
                databaseSize = 0;
                totalSingles = 0;
                break;
            }
            case 14:{
                TransactionDatabase database = new TransactionDatabase();
		try {
			//database.loadFile(fileToPath(args[1]));
                        database.loadFile(args[1]);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		context.printContext();
		
		// Applying the ECLAT algorithm
		AlgoEclat algo = new AlgoEclat();
		Itemsets patterns = algo.runAlgorithm(null, database, minsupp, true);
		// NOTE 0: We use "null" as output file path, because in this
		// example, we want to save the result to memory instead of
		// saving to a file
		
		// NOTE 1: if you  use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		//patterns.printItemsets(database.size());
		algo.printStats();
                 databaseSize = 0;
                totalSingles = 0;
                break;
            }
            case 15:{
                input = fileToPath(args[1]);
                //input = args[1];
		// Applying the FPGROWTH algorithmMainTestFPGrowth.java
		AlgoFPGrowth algo = new AlgoFPGrowth();
		// Run the algorithm
		// Note that here we use "null" as output file path because we want to keep the results into memory instead of saving to a file
		Itemsets patterns = algo.runAlgorithm(input, null, minsupp);  
		// show the execution time and other statistics
		algo.printStats();
		// print the patterns to System.out
		//patterns.printItemsets(algo.getDatabaseSize());
                databaseSize = 0;
                totalSingles = 0;
                break;
            }
            /*case 4: {
                AlgoDIMFPOR dimAlgo = new AlgoDIMFPOR();
                dimAlgo.runAlgorithm(input, minsupp, maxsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMFPOR.total_singles;
                break;            
            }*/

            default: {
                AlgoDIMMFPImproved dimAlgo = new AlgoDIMMFPImproved();
                dimAlgo.runAlgorithm(input, minsupp, maxitem);
                databaseSize = dimAlgo.getDatabaseSize();
                totalSingles = AlgoDIMMFPImproved.total_singles;
                break;
            }
        }

        long lEndTime = System.currentTimeMillis();

        // summarizing results
        System.out.println("----------------------------------------------------------------------");
        System.out.println("DATABASE SIZE " + databaseSize + " Total items : " + totalSingles);
        //System.out.println("Elapsed milliseconds (Preprocessing): " + (parseEnd - parseStart));
        System.out.println("Elapsed milliseconds ( FPTree + ORed Itemsets): " + (lEndTime - lStartTime));
        System.out.println("Elapsed milliseconds (Total time => Preprocessing + ORed itemsets): " + (lEndTime - parseStart));

        // linux command for getting memory utiliation of this process
        try {
            String s;
            Process p = Runtime.getRuntime().exec(" ps -C java -O rss");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null) {
                System.out.println(s);
            }

        } catch (Exception ex) {
            System.out.println("unable to get memory Utilization.");
        }
        System.out.println("======================================================================\n\n");
    }

    /**
     * Method to find complete path of input file.
     *
     * @param filename input file name given by user.
     * @return complete input path to dataset.
     * @throws UnsupportedEncodingException Exception if file encoding is not
     * supported.
     */
    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = Algorithm.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }
}
