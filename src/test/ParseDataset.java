package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class preprocesses the dataset. It reads the input dataset and creates a
 * new dataset with items represented by an integer ID.
 *
 * @author nihal jain
 * @version 1.0
 */
public class ParseDataset {

    /**
     * HashMap for storing Mapping of assigned integer ID to item
     */
    public static Map<Integer, String> map = new HashMap<>();

    /**
     * Method to Preprocess dataset.
     *
     * @param input the path to an input file containing a transaction database.
     * @return New input path to dataset.
     */
    public String ParseData(String input) {
        // Mapping of item to assigned integer ID
        Map<String, Integer> mapitem = new HashMap<>();
        // BufferedReader object to read transations of input file.
        BufferedReader reader = null;
        // PrintWriter object to write mapped transaction to new input file.
        PrintWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(input));
        } catch (Exception e) {
            System.err.println("error: Unable to open input file");
            System.exit(-1); // exit
        }

        // Creating new input file
        int len = input.length();
        int i = len - 1;
        for (; i > len - 7; i--) {
            if (input.charAt(i) == '.') { // extension checking
                break;
            }
        }
        if (i == len - 7) { // if no extension provided
            i = len;
        }
        String out = (String) input.subSequence(0, i);
        out += "_int.txt";
        System.out.println("Mapped Input file: " + out);
        try {
            writer = new PrintWriter(out, "UTF-8");

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.err.println("error: Unable to open item to integer ID mapping file");
            System.exit(-1); // exit
        }
        String line;
        Integer count = 0; // integer ID

        try {
            // for each line (transaction) until the end of the file
            while (((line = reader.readLine()) != null)) {
                // if the line is a comment, is empty or is a kind of metadata
                if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }
                // splitting transaction using delimeter space or tab
                StringTokenizer lineSplited = new StringTokenizer(line);
                // new transaction to be written in file
                String transaction = "";
                // for each item in the input transaction
                while (lineSplited.hasMoreElements()) {
                    Integer temp = null;
                    String s = lineSplited.nextToken();
                    // checking wheather item is mapped with integer ID or not
                    if ((temp = mapitem.get(s)) == null) {
                        // assign integer ID to item
                        mapitem.put(s, count);
                        // reverse mapping for final output
                        map.put(count, s);
                        temp = count;
                        count++;
                    }
                    // adding integer ID correspond to intem
                    transaction = transaction + temp + " ";
                }
                if (transaction != "") {
                    // writing to new input file
                    writer.println(transaction);
                }
            }
        } catch (Exception e) {
            System.err.println("error: Unable to read transaction from input file");
        }
        try {
            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println("error: Unable to close input or output file");
        }
	
        // System.out.println(mapitem);

        // returning new input path
        return out;
    }
}
