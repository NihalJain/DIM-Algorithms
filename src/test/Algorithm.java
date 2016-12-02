package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import algorithm.FDIM.BitSetBased.AlgoDIMBitSetBased;
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
     * @param args
     * @throws FileNotFoundException If input file not found
     * @throws IOException throws IOException if any.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String input = null;
        Float minsupp = null;
        int parsed = 0;
        //Scanner sc = new Scanner(System.in);

        if (args.length != 2) {
            System.out.print("error: Missing argument!");
        }

        // ------------Input file --------------
        //System.out.print("Enter Input file path : ");
        try {
            input = fileToPath(args[0]); //use this for IDE(testing)
        } catch (Exception e) {
            System.err.println("error: Input file not found.");
            System.exit(-1);
        }

        // ------------Support --------------
        //System.out.print("Enter Support Threshold : ");
        try {
            minsupp = Float.valueOf(args[1]);
        } catch (Exception e) {
            System.err.println("error: Invalid input for minimum Support.");
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
        System.out.println("                      DIM Bit Set Based ALGORITHM");
        System.out.println("=======================================================================");
        System.out.println("                     MINING WITH BELOW PARAMETERS ");
        System.out.println("=======================================================================");
        System.out.println("DataSet : " + input);
        System.out.println("Support Threshold : " + minsupp);
        System.out.println("-----------------------------------------------------------------------");

        // PHASE 1: finding All frequent ORed itemsets
        long lStartTime = System.currentTimeMillis();

        AlgoDIMBitSetBased fpgrowth = new AlgoDIMBitSetBased();
        fpgrowth.runAlgorithm(input, minsupp);
        int databaseSize = fpgrowth.getDatabaseSize();

        long lMidTime = System.currentTimeMillis();

        // PHASE 2: Generating all rules from the set of frequent itemsets
        /*AlgoORedRules rules = new AlgoORedRules();
        rules.Generate_Rules(minconf, parsed);*/
        long lEndTime = System.currentTimeMillis();

        // summarizing results
        System.out.println("----------------------------------------------------------------------");
        System.out.println("DATABASE SIZE " + databaseSize + " Total items : " + AlgoDIMBitSetBased.total_singles);
        System.out.println("Elapsed milliseconds (Preprocessing): " + (parseEnd - parseStart));
        System.out.println("Elapsed milliseconds ( ORed Itemsets): " + (lMidTime - lStartTime));
        System.out.println("Elapsed milliseconds (Rules + ORed itemsets): " + (lEndTime - lStartTime));
        System.out.println("Elapsed milliseconds (preprocessing + Rules + ORed itemsets): " + (lEndTime - parseStart));

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
