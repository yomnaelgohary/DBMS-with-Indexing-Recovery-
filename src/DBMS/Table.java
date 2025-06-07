package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Table implements Serializable
{
	private String name;
	private String[] columnsNames;
	private int pageCount;
	private int recordsCount;
	private ArrayList<String> trace;
	private BitmapIndex bitmapIndex;

	
	
	
	
	//public BitmapIndex getBitmapIndex() {
	//	return bitmapIndex;
//	}

	//public ArrayList<String> getIndexedColumns() {
	  //  if (bitmapIndex == null)
	    //    return new ArrayList<>();
	    //return new ArrayList<>(bitmapIndex.getIndex().keySet());
	//}

	
	
	
	public ArrayList<String> getTrace() {
		return trace;
	}


	public Table(String name, String[] columnsNames) 
	{
		super();
		this.name = name;
		this.columnsNames = columnsNames;
		this.trace = new ArrayList<String>();
		this.trace.add("Table created name:" + name + ", columnsNames:"
				+ Arrays.toString(columnsNames));
	}


	@Override
	public String toString() 
	{
		return "Table [name=" + name + ", columnsNames="
				+ Arrays.toString(columnsNames) + ", pageCount=" + pageCount
				+ ", recordsCount=" + recordsCount + "]";
	}
	
	
	public void insert(String[] record) {
	    long startTime = System.currentTimeMillis();

	    int targetPageIndex = pageCount - 1;
	    Page current = FileManager.loadTablePage(this.name, targetPageIndex);

	    if (current == null || !current.insert(record)) {
	        current = new Page();
	        current.insert(record);
	        targetPageIndex = pageCount; 
	        pageCount++;
	    }

	   
	    FileManager.storeTablePage(this.name, targetPageIndex, current);
	    recordsCount++;

	    for (int colIndex = 0; colIndex < columnsNames.length; colIndex++) {
	        String colName = columnsNames[colIndex];
	        BitmapIndex index = FileManager.loadTableIndex(this.name, colName);

	        if (index == null) continue;

	        String insertedValue = record[colIndex];
	        for (String val : index.getIndex().keySet()) {
	            if (val.equals(insertedValue)) {
	                index.add(val, 1);
	            } else {
	                index.add(val, 0);
	            }
	        }
	        if (!index.contains(insertedValue)) {   
	            ArrayList<Integer> newBitmap = new ArrayList<>();
	            for (int i = 0; i < recordsCount - 1; i++) {
	                newBitmap.add(0);
	            }
	            newBitmap.add(1);  
	            index.putBitmap(insertedValue, newBitmap);
	        }

	        
	        FileManager.storeTableIndex(this.name, colName, index);
	    }

	    long stopTime = System.currentTimeMillis();
	    this.trace.add("Inserted:" + Arrays.toString(record) +
	        ", at page number:" + targetPageIndex +
	        ", execution time (mil):" + (stopTime - startTime));
	}

	public String[] fixCond(String[] cols, String[] vals)
	{
		String[] res = new String[columnsNames.length];
		for(int i=0;i<res.length;i++)
		{
			for(int j=0;j<cols.length;j++)
			{
				if(columnsNames[i].equals(cols[j]))
				{
					res[i]=vals[j];
				}
			}
		}
		return res;
	}
		
	public ArrayList<String []> select(String[] cols, String[] vals)
	{
		String[] cond = fixCond(cols, vals);
		String tracer ="Select condition:"+Arrays.toString(cols)+"->"+Arrays.toString(vals);
		ArrayList<ArrayList<Integer>> pagesResCount = new ArrayList<ArrayList<Integer>>();
		ArrayList<String []> res = new ArrayList<String []>();
		long startTime = System.currentTimeMillis();
		for(int i=0;i<pageCount;i++)
		{
			Page p = FileManager.loadTablePage(this.name, i);
			ArrayList<String []> pRes = p.select(cond);
			if(pRes.size()>0)
			{
				ArrayList<Integer> pr = new ArrayList<Integer>();
				pr.add(i);
				pr.add(pRes.size());
				pagesResCount.add(pr);
				res.addAll(pRes);
			}
		}
		long stopTime = System.currentTimeMillis();
		tracer +=", Records per page:" + pagesResCount+", records:"+res.size()
				+", execution time (mil):"+(stopTime - startTime);
		this.trace.add(tracer);
		return res;
	}
	
	public ArrayList<String []> select(int pageNumber, int recordNumber)
	{
		String tracer ="Select pointer page:"+pageNumber+", record:"+recordNumber;
		ArrayList<String []> res = new ArrayList<String []>();
		long startTime = System.currentTimeMillis();
		Page p = FileManager.loadTablePage(this.name, pageNumber);
		ArrayList<String []> pRes = p.select(recordNumber);
		if(pRes.size()>0)
		{
			res.addAll(pRes);
		}
		long stopTime = System.currentTimeMillis();
		tracer+=", total output count:"+res.size()
				+", execution time (mil):"+(stopTime - startTime);
		this.trace.add(tracer);
		return res;
	}
	
	
	public ArrayList<String []> select()
	{
		ArrayList<String []> res = new ArrayList<String []>();
		long startTime = System.currentTimeMillis();
		for(int i=0;i<pageCount;i++)
		{
			Page p = FileManager.loadTablePage(this.name, i);
			res.addAll(p.select());
		}
		long stopTime = System.currentTimeMillis();
		this.trace.add("Select all pages:" + pageCount+", records:"+recordsCount
				+", execution time (mil):"+(stopTime - startTime));
		return res;
	}
	
	
	
	
	
	
	public int getPageCount() {
		return pageCount;
	}


	public int getRecordsCount() {
		return recordsCount;
	}


	public String[] getColumnsNames() {
		return columnsNames;
	}


	public String getFullTrace() 
	{
		String res = "";
		for(int i=0;i<this.trace.size();i++)
		{
			res+=this.trace.get(i)+"\n";
		}
		return res+ "Pages Count: " + pageCount + ", Records Count: " + recordsCount + ", Indexed Columns: " + this.indexedcol();
	}
	
	public String getLastTrace() 
	{
		return this.trace.get(this.trace.size()-1);
	}
	public ArrayList<String> indexedcol (){
		ArrayList<String> colnamesthathaveindex = new  ArrayList<>();
	for (int i =0; i<columnsNames.length;i++){		
		String columnName= columnsNames[i];
	    BitmapIndex indexavailable = FileManager.loadTableIndex(name, columnName);
	    if (indexavailable!=null){
		    colnamesthathaveindex.add(columnName);
	    }
	    }
	return colnamesthathaveindex;
	
}
}