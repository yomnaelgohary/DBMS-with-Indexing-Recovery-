package DBMS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DBApp
{
	static int dataPageSize = 2;


	public static void createTable(String tableName, String[] columnsNames)
	{
		Table t = new Table(tableName, columnsNames);
		FileManager.storeTable(tableName, t);
	}

	public static void insert(String tableName, String[] record)
	{
		Table t = FileManager.loadTable(tableName);
		t.insert(record);
		FileManager.storeTable(tableName, t);
	}

	public static ArrayList<String []> select(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select();
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(pageNumber, recordNumber);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(cols, vals);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static String getFullTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		String res = t.getFullTrace();
		return res;
	}

	public static String getLastTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		String res = t.getLastTrace();
		return res;
	}
	
	
	public static ArrayList<String []> validateRecords( String tableName){
		Table t = FileManager.loadTable(tableName);
		String pageStr=null;
		 if (t == null) {
		        System.out.println("Table not found: " + tableName);
		        return new ArrayList<>();
		    }
		String fulltrace = getFullTrace(tableName);
		ArrayList<String[]> missingRecords = new ArrayList<>();
		ArrayList<String> insertedLines = new ArrayList<>();
		//int totalInsertions=0;
		//System.out.println (fulltrace);
		
		String[] lines = fulltrace.split("\n");
		//for (String line : lines) {
		  //  System.out.println(line);
		//}

		for (int i = 0; i<lines.length; i++){
			 String line = lines[i];
			 
			 if (line.contains("Inserted")){
				 //totalInsertions++;
				 insertedLines.add(line);
				// System.out.println(line);
			   }
			 }
		//System.out.println(insertedLines);
		  ArrayList<Integer> existingPages = getPagesFromTrace(tableName);
		  //System.out.println("Existing Pages: " + existingPages);
		  for (int i = 0; i < insertedLines.size(); i++) {
			    int pageIndex = insertedLines.get(i).indexOf("page number:");
			    if (pageIndex != -1) {
			        String after = insertedLines.get(i).substring(pageIndex + "page number:".length()).trim();
			        int commaIndex = after.indexOf(",");
			        if (commaIndex != -1) {
			            pageStr = after.substring(0, commaIndex);
			        }
			        try {
			            int pageNum = Integer.parseInt(pageStr.trim());
			            if (!existingPages.contains(pageNum)) {
			                int start = insertedLines.get(i).indexOf('[');
			                int end = insertedLines.get(i).indexOf(']');
			                if (start != -1 && end != -1 && end > start) {
			                    String res = insertedLines.get(i).substring(start + 1, end);
			                    String[] values = res.split(",\\s*");
			                    missingRecords.add(values);
			                }
			            }
			        } catch (NumberFormatException e) {
			//            System.out.println("Error parsing page number in line: " + insertedLines.get(i));
			        }
			    }
			}
		  t.getTrace().add("Validating records: " + missingRecords.size() + " records missing.");
		  FileManager.storeTable(tableName, t);

		return missingRecords;
			 
	}
		
	public static ArrayList<Integer> getPagesFromTrace(String tableName) {
	    ArrayList<Integer> existingPages = new ArrayList<>();
	    File tableDir = new File(FileManager.directory.getAbsolutePath() + File.separator + tableName);

	    if (!tableDir.exists()) return existingPages;

	    File[] files = tableDir.listFiles((dir, name) -> name.endsWith(".db"));
	    if (files == null) return existingPages;

	    for (File f : files) {
	        String fileName = f.getName().replace(".db", "");
	        try {
	            int pageIndex = Integer.parseInt(fileName);
	            existingPages.add(pageIndex);
	        } catch (NumberFormatException e) {
	            
	        }
	    }

	    return existingPages;
	}

	
	public static void recoverRecords(String tableName, ArrayList<String[]> missing) {
	    Table t = FileManager.loadTable(tableName);
	    ArrayList<Integer> existingPages = getPagesFromTrace(tableName);
	    ArrayList<Integer> recoveredPages = new ArrayList<>();
	    int pages = -1;
	    int recoveredCount = 0;

	    if (t != null) {
	        pages = t.getPageCount();
	    } else {
	        return;
	    }

	    ArrayList<String[]> tempMissing = new ArrayList<>(missing); 

	    for (int j = 0; j < pages; j++) {
	        if (!existingPages.contains(j)) {
	            Page p = new Page();
	            int k = 0;
	            while (k < dataPageSize && !tempMissing.isEmpty()) {
	                String[] record = tempMissing.remove(0);
	                p.insert(record);
	                recoveredCount++;
	                k++;
	            }
	            FileManager.storeTablePage(tableName, j, p);
	            recoveredPages.add(j);
	        }
	    }
	    t.getTrace().add("Recovering " + recoveredCount + " records in pages: " + recoveredPages.toString() + ".");
	    FileManager.storeTable(tableName, t);
	}

	
	


	public static void createBitMapIndex(String tableName, String colName) {
		long startTime = System.currentTimeMillis();
		Table t = FileManager.loadTable(tableName);
	    String [] cols= t.getColumnsNames();
	    int indexofcol=-1;
	    int totalpages = t.getPageCount();
	    ArrayList<String> uniquevalues = new ArrayList<>();
	    ArrayList<String[]> allrecords = new ArrayList<>();
	    for (int i =0;i< cols.length;i++){
	    	if (cols[i].equals(colName)){
	    		indexofcol=i;
	    		break;
	    	}	
	    }
	    if (indexofcol == -1) {
	        System.out.println("Column not found: " + colName);
	        return;
	    }
	    for (int i =0; i<totalpages;i++){
	    	Page p = FileManager.loadTablePage(tableName, i);
	    	for (int j=0;j<p.getRecords().size();j++){
	    		String [] record= p.getRecords().get(j);
	    		allrecords.add(record);
	    		String value = record[indexofcol];
	    		if (!uniquevalues.contains(value)){
	    		      uniquevalues.add(value);
	    		}	
	    	}
	    }
	    BitmapIndex bitmapIndex = new BitmapIndex();
	    for (int i = 0; i < allrecords.size(); i++) {
	        String[] record = allrecords.get(i);
	        String value = record[indexofcol];
	        for (int j = 0; j < uniquevalues.size(); j++) {
	            String unique = uniquevalues.get(j);
	            if (value.equals(unique)) {
	                bitmapIndex.add(unique, 1);
	            } else {
	                bitmapIndex.add(unique, 0);
	            }
	        }

	    }
	    FileManager.storeTableIndex(tableName, colName, bitmapIndex);
	    long stopTime = System.currentTimeMillis();
        t.getTrace().add("Index created for column: "+ colName+", execution time (mil):" + (stopTime - startTime));
        FileManager.storeTable(tableName, t);
	    
	}

	public static String getValueBits(String tableName, String colName, String value) {
	    
	    BitmapIndex index = FileManager.loadTableIndex(tableName, colName);

	    if (index == null) {
	        return "Index not found.";
	    }

	    ArrayList<Integer> bits = index.getIndex().get(value);
	    if (bits == null) {
	    	int bitLength = 0;
	        if (!index.getIndex().isEmpty()) {
	            bitLength = index.getIndex().values().iterator().next().size();
	        }

	        StringBuilder zeros = new StringBuilder();
	        for (int i = 0; i < bitLength; i++) {
	            zeros.append('0');
	        }
	        return zeros.toString();
	    }

	    String bitstream = "";
	    for (int i = 0; i < bits.size(); i++) {
	        bitstream = bitstream + bits.get(i); 
	    }

	    return bitstream;
	}
	

	public static ArrayList<String []> selectIndex(String tableName,String[] cols, String[] vals){
		if (cols.length!=vals.length){
			return null;
		}
		else{ 
		  Table t = FileManager.loadTable(tableName);
		 // BitmapIndex bitmap = t.getBitmapIndex();
		  ArrayList<BitmapIndex> colindex= new  ArrayList<>();
		  ArrayList<String> bitmapvalues= new  ArrayList<>();
		  ArrayList<String[]> allrecords = getallrecords(tableName);
		  ArrayList<String[]> res= new  ArrayList<>();
		  ArrayList<String[]> temp = new  ArrayList<>();
		  ArrayList<String> colnamesthathaveindex = new  ArrayList<>();
		  ArrayList<String> colnamesdonothaveindex = new  ArrayList<>();
		  int nullcount =0;
		  int indexcount =0;
		  for (int i =0; i<cols.length;i++){
	        String columnName= cols[i];
		    BitmapIndex indexavailable = FileManager.loadTableIndex(tableName, columnName);
		    if (indexavailable!=null){
			    colindex.add(indexavailable);
			    colnamesthathaveindex.add(columnName);
			    indexcount++;
		    }
		    else {
		    	colnamesdonothaveindex.add(columnName);
			    nullcount++;
		    }
	      }
		  if (indexcount==colindex.size()&&nullcount==0){
			  long startTime = System.currentTimeMillis();
			for (int i=0; i<colindex.size();i++){
				String x= getValueBits(tableName,cols[i],vals[i]);
				bitmapvalues.add(x);	
			}
			int[] combinedBitmap = bitmapToIntArray(bitmapvalues.get(0));

            for (int i = 1; i < bitmapvalues.size(); i++) {
                String currentBitmap = bitmapvalues.get(i);
                int[] currentBitmapArray = bitmapToIntArray(currentBitmap);
                combinedBitmap = bitwiseAnd(combinedBitmap, currentBitmapArray);
            }
            for (int i=0;i<combinedBitmap.length;i++){
            	if (combinedBitmap[i]==1){
            		res.add(allrecords.get(i));
            	}
            }
            long stopTime = System.currentTimeMillis();
            List<String> sortedColsList = new ArrayList<>(Arrays.asList(cols));
		    Collections.sort(sortedColsList);
		    Collections.sort(colnamesthathaveindex);
            t.getTrace().add("Select index condition:"+Arrays.toString(cols)+"->"+Arrays.toString(vals)+", Indexed columns: "+colnamesthathaveindex+","
            		+ "Indexed selection count: "+res.size()+", Final count: "+res.size()+", execution time (mil):"+(stopTime-startTime));
            FileManager.storeTable(tableName, t);
            return res;
		  }
		  else if (indexcount==1&&nullcount!=0){
			  long startTime = System.currentTimeMillis();
			  String colname = colnamesthathaveindex.get(0);
			  int location =0;
			  for (int i=0;i<cols.length;i++){
				 if (cols[i].equals(colname)){
					 location=i;
					 break;
				 }
			  }
			  String valuename = vals[location];
			  String indexofcol = getValueBits(tableName,colname,valuename);
			  int[] bitmapindex = bitmapToIntArray(indexofcol);
			  //System.out.println("Bitmap for indexed column " + colname + ": " + indexofcol);

			  for (int i=0;i<bitmapindex.length;i++){
	            	if (bitmapindex[i]==1){
	            		temp.add(allrecords.get(i));
	            	}
	            }
			  //System.out.println("Temp size after applying bitmap: " + temp.size());
			  for (int i = 0; i < temp.size(); i++) {
				    String[] record = temp.get(i);
				    int valcount = 0;
				    //System.out.println("Checking record: " + Arrays.toString(record));

				    for (int j = 0; j < colnamesdonothaveindex.size(); j++) {
				        String nameofcol = colnamesdonothaveindex.get(j);
				       // System.out.println("Checking column: " + nameofcol);

				        for (int k = 0; k < cols.length; k++) {
				            if (cols[k].equals(nameofcol)) {
				                int index = -1;
				                String[] fullCols = t.getColumnsNames();

				                
				                for (int m = 0; m < fullCols.length; m++) {
				                    if (fullCols[m].equals(nameofcol)) {
				                        index = m;
				                        break;
				                    }
				                }

				                if (index != -1) {
				                 //   System.out.println("Comparing " + record[index] + " with " + vals[k]);
				                    if (record[index].equals(vals[k])) {
				                        valcount++;
				                    }
				                }
				                break;
				            }
				        }
				    }

				    if (valcount == colnamesdonothaveindex.size()) {
				        res.add(record);
				    }
				}
			  long stopTime = System.currentTimeMillis();
			  List<String> sortedColsList = new ArrayList<>(Arrays.asList(cols));
			  Collections.sort(sortedColsList);
			  Collections.sort(colnamesthathaveindex);
			  Collections.sort(colnamesdonothaveindex);
			  t.getTrace().add("Select index condition:"+Arrays.toString(cols)+"->"+Arrays.toString(vals)+", Indexed columns: "+colnamesthathaveindex+","
	            		+ "Indexed selection count: "+temp.size()+", Non Indexed: "+colnamesdonothaveindex+", Final count: "+res.size()+", execution time (mil):"+(stopTime-startTime));
			  FileManager.storeTable(tableName, t);
			  return res;
		
		  }
		  else if (indexcount > 1 && nullcount != 0) {
			  long startTime = System.currentTimeMillis();
			   // System.out.println("Case: Some but not all selected columns are indexed");

			    
			    for (int i = 0; i < colnamesthathaveindex.size(); i++) {
			        String colname = colnamesthathaveindex.get(i);

			       
			        int colIndexInInput = -1;
			        for (int j = 0; j < cols.length; j++) {
			            if (cols[j].equals(colname)) {
			                colIndexInInput = j;
			                break;
			            }
			        }

			        if (colIndexInInput != -1) {
			            String valuename = vals[colIndexInInput];
			            String indexofcol = getValueBits(tableName, colname, valuename);
			        //    System.out.println("Bitmap for indexed column " + colname + ": " + indexofcol);
			            bitmapvalues.add(indexofcol);
			        } else {
			          //  System.out.println("ERROR: Column " + colname + " not found in cols[]");
			        }
			    }

			    
			    int[] combinedBitmap = bitmapToIntArray(bitmapvalues.get(0));
			    for (int i = 1; i < bitmapvalues.size(); i++) {
			        int[] currentBitmapArray = bitmapToIntArray(bitmapvalues.get(i));
			        combinedBitmap = bitwiseAnd(combinedBitmap, currentBitmapArray);
			    }

			    //System.out.println("Combined bitmap size after AND: " + combinedBitmap.length);

			    
			    for (int i = 0; i < combinedBitmap.length; i++) {
			        if (combinedBitmap[i] == 1) {
			            String[] rec = allrecords.get(i);
			      //      System.out.println("Bitmap matched record: " + Arrays.toString(rec));
			            temp.add(rec);
			        }
			    }

			    
			    String[] fullCols = t.getColumnsNames();
			    for (int i = 0; i < temp.size(); i++) {
			        String[] record = temp.get(i);
			        int valcount = 0;

			        for (int j = 0; j < colnamesdonothaveindex.size(); j++) {
			            String nameofcol = colnamesdonothaveindex.get(j);

			            
			            int inputIndex = -1;
			            for (int k = 0; k < cols.length; k++) {
			                if (cols[k].equals(nameofcol)) {
			                    inputIndex = k;
			                    break;
			                }
			            }

			            
			            int recordColIndex = -1;
			            for (int m = 0; m < fullCols.length; m++) {
			                if (fullCols[m].equals(nameofcol)) {
			                    recordColIndex = m;
			                    break;
			                }
			            }

			            if (inputIndex != -1 && recordColIndex != -1) {
			               // System.out.println("Checking record: " + Arrays.toString(record));
			                //System.out.println("Checking column: " + nameofcol);
			                if (record[recordColIndex].equals(vals[inputIndex])) {
			                    valcount++;
			                }
			            }
			        }

			        if (valcount == colnamesdonothaveindex.size()) {
			            res.add(record);
			        }
			    }
			    long stopTime = System.currentTimeMillis();
			    List<String> sortedColsList = new ArrayList<>(Arrays.asList(cols));
			    Collections.sort(sortedColsList);
			    Collections.sort(colnamesthathaveindex);
			    Collections.sort(colnamesdonothaveindex);
				  t.getTrace().add("Select index condition:"+Arrays.toString(cols)+"->"+Arrays.toString(vals)+", Indexed columns: "+colnamesthathaveindex+","
		            		+ "Indexed selection count: "+temp.size()+", Non Indexed: "+colnamesdonothaveindex+", Final count: "+res.size()+", execution time (mil):"+(stopTime-startTime));
				  FileManager.storeTable(tableName, t);
				  return res;
			}

		  else {
			  
			    long startTime = System.currentTimeMillis();
			    res = t.select(cols, vals);
			    long stopTime = System.currentTimeMillis();
			    List<String> sortedColsList = new ArrayList<>(Arrays.asList(cols));
			    Collections.sort(sortedColsList);
			    
			    t.getTrace().add("Select index condition:" + Arrays.toString(cols) + "->" + Arrays.toString(vals)
			            + ", Non Indexed: " + sortedColsList.toString()
			            + ", Final count: " + res.size()
			            + ", execution time (mil):" + (stopTime - startTime));

			    FileManager.storeTable(tableName, t);
			    return res;
		  }
	}
}	
	
	public static int[] bitmapToIntArray(String bitmap) {
	    int[] bitmapArray = new int[bitmap.length()];
	    for (int i = 0; i < bitmap.length(); i++) {
	    	if (bitmap.charAt(i) == '1') {
	    	    bitmapArray[i] = 1;
	    	} else {
	    	    bitmapArray[i] = 0;
	    	}
	    }
	    return bitmapArray;
	}	
	
	
	public static int[] bitwiseAnd(int[] bitmap1, int[] bitmap2) {
	    int[] result = new int[bitmap1.length];
	    for (int i = 0; i < bitmap1.length; i++) {
	        result[i] = bitmap1[i] & bitmap2[i];  
	    }
	    return result;
	}
	public static ArrayList<String []> getallrecords (String tablename){
		Table t =FileManager.loadTable(tablename);
		ArrayList<String []> res = new ArrayList<>();
		for(int i=0;i<t.getPageCount();i++){
			Page p = FileManager.loadTablePage(tablename, i);
			ArrayList<String []> pagerecords = p.getRecords();
			for(int j=0; j<pagerecords.size();j++){
				res.add(pagerecords.get(j));
			}
		}
		return res;
	}
	
	public static void main(String []args) throws IOException 
	{ 
	FileManager.reset(); 
	String[] cols = {"id","name","major","semester","gpa"}; 
	createTable("student", cols); 
	String[] r1 = {"1", "stud1", "CS", "5", "0.9"}; 
	insert("student", r1); 
	 
	 
	   
	  String[] r2 = {"2", "stud2", "BI", "7", "1.2"}; 
	  insert("student", r2); 
	   
	  String[] r3 = {"3", "stud3", "CS", "2", "2.4"}; 
	  insert("student", r3); 
	   
	  createBitMapIndex("student", "gpa"); 
	  createBitMapIndex("student", "major"); 
	   
	  System.out.println("Bitmap of the value of CS from the major index: "+getValueBits("student", "major", "CS")); 
	  System.out.println("Bitmap of the value of 1.2 from the gpa index: "+getValueBits("student", "gpa", "1.2")); 
	   
	   
	  String[] r4 = {"4", "stud4", "CS", "9", "1.2"}; 
	  insert("student", r4); 
	   
	  String[] r5 = {"5", "stud5", "BI", "4", "3.5"}; 
	  insert("student", r5); 
	   
	  System.out.println("After new insertions:");  
	  System.out.println("Bitmap of the value of CS from the major index: "+getValueBits("student", "major", "CS")); 
	  System.out.println("Bitmap of the value of 1.2 from the gpa index: "+getValueBits("student", "gpa", "1.2")); 
	     
	   
	  System.out.println("Output of selection using index when all columns of the select conditions are indexed:"); 
	  ArrayList<String[]> result1 = selectIndex("student", new String[] 
	{"major","gpa"}, new String[] {"CS","1.2"}); 
	        for (String[] array : result1) { 
	            for (String str : array) { 
	                System.out.print(str + " "); 
	            } 
	            System.out.println(); 
	        } 
	  System.out.println("Last trace of the table: "+getLastTrace("student")); 
	        System.out.println("--------------------------------"); 
	         
	  System.out.println("Output of selection using index when only one column of the columns of the select conditions are indexed:"); 
	  ArrayList<String[]> result2 = selectIndex("student", new String[] 
	{"major","semester"}, new String[] {"CS","5"}); 
	        for (String[] array : result2) { 
	            for (String str : array) { 
	                System.out.print(str + " "); 
	            } 
	            System.out.println(); 
	        } 
	  System.out.println("Last trace of the table: "+getLastTrace("student")); 
	        System.out.println("--------------------------------"); 
	         
	System.out.println("Output of selection using index when some of the columns of the select conditions are indexed:"); 
	ArrayList<String[]> result3 = selectIndex("student", new String[] 
	{"major","semester","gpa" }, new String[] {"CS","5", "0.9"}); 
	for (String[] array : result3) { 
	for (String str : array) { 
	System.out.print(str + " "); 
	} 
	System.out.println(); 
	} 
	System.out.println("Last trace of the table: "+getLastTrace("student")); 
	System.out.println("--------------------------------"); 
	System.out.println("Full Trace of the table:"); 
	System.out.println(getFullTrace("student")); 
	System.out.println("--------------------------------"); 
	System.out.println("The trace of the Tables Folder:"); 
	System.out.println(FileManager.trace()); 
	} 
	   
} 



	
	


