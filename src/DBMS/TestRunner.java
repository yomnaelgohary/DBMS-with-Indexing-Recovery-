package DBMS;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TestRunner {
	public static void main(String[] args) {
	    try {
	        // Simulate the test case
	        FileManager.reset();
	        
	        DBApp.dataPageSize = 10;
	        String[] cols0 = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t"};
	        DBApp.createTable("t4", cols0);
	        
	        // Insert 320 records
	        System.out.println("Inserting 320 records...");
	        String[][] records_t4 = new String[320][cols0.length];
	        for(int i=0; i<320; i++) {
	            records_t4[i][0] = cols0[0]+i;
	            for(int j=1; j<cols0.length; j++) {
	                records_t4[i][j] = cols0[j]+((i%(j+1)));
	            }
	            DBApp.insert("t4", records_t4[i]);
	            if(i % 50 == 0) System.out.print("."); // Progress indicator
	        }
	        System.out.println("\nRecords inserted successfully.");
	        
	        // Perform the selectIndex operation
	        System.out.println("Performing selectIndex...");
	        String[] ConditionColumns0 = {"p","d","g","r","a","j","c"};
	        String[] ConditionColumnsValues0 = {"p5","d1","g2","r1","a37","j7","c1"};
	        ArrayList<String[]> result = DBApp.selectIndex("t4", ConditionColumns0, ConditionColumnsValues0);
	        
	        // Verify the results
	        System.out.println("\nSelect output size: " + result.size());
	        if(result.size() > 0) {
	            System.out.println("First record: " + Arrays.toString(result.get(0)));
	        }
	        
	        // Check the trace
	        String tableTrace0 = DBApp.getLastTrace("t4");
	        System.out.println("\nLast trace: " + tableTrace0);
	        
	        // Verify trace contains required information
	        Arrays.sort(ConditionColumns0);
	        System.out.println("\nVerification:");
	        System.out.println("1. Contains 'Select index': " + tableTrace0.contains("Select index"));
	        System.out.println("2. Doesn't contain 'Indexed columns:': " + !tableTrace0.contains("Indexed columns:"));
	        System.out.println("3. Contains 'Non Indexed:': " + tableTrace0.contains("Non Indexed:"));
	        System.out.println("4. Contains correct non-indexed columns: " + tableTrace0.contains(Arrays.toString(ConditionColumns0)));
	        System.out.println("5. Contains correct count (1): " + tableTrace0.contains("Final count: 1"));
	        
	    } catch (Exception e) {
	        System.err.println("\nException occurred:");
	        e.printStackTrace();
	        System.err.println("\nException message: " + e.getMessage());
	    }
	}
	}


