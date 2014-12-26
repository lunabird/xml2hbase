package cn.edu.xidian.repace.xml2hbase;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.edu.xidian.repace.xml2hbase.hbase.*;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseCreate;

public class test {
	public static void main(String[] args) throws Exception
	{
		//HbaseDelete.deleteTable("C2V-xmark-50.0-1-3");
		//HbaseDelete.deleteTable("P2C-xmark-50.0-1-3");
		MappingTableManager MTmanager = new MappingTableManager();
		
		Map<String, String> code2Value= null;
		Map<String, List<String>> path2Code = new HashMap<String, List<String>>();
		//String tableName = "xmark-10.0-1-4";
		String tableName = "book-3";
		String family = "xmark";
		String P2CtableName ="P2C-"+tableName;
		String C2VtableName = "C2V-"+tableName;
		HbaseCreate.createTable(P2CtableName, family);
		HbaseCreate.createTable(C2VtableName, family);
		
		//open files,creating the map
		String C2ProwName = "";
		VirtualTable vt = new VirtualTable(P2CtableName);
		
		//File file = new File("/home/xdccl/workspace/XML2HBaseSimple/xmark-10.0-1");
		//File file = new File("/home/xdccl/workspace2/testdata/files2");
		File file = new File("D:\\XML2HBase\\testfile");
		if (file.isDirectory()) {
			File[] files = file.listFiles();
	        for (int i = 0; i < files.length; i++) {
	        	String path = files[i].getPath();//article path 
	        	C2ProwName =  files[i].getName();//article name as the C2VrowName
	        	code2Value= new HashMap<String, String>();
	        	MTmanager.createMappingTable(path,C2ProwName,vt,path2Code,code2Value);
	        	
	        	//print test
	    //    	System.out.println(C2ProwName);
	    //    	System.out.println("test print: path2Code ");
	    //    	System.out.println(path2Code);
	        	
	    //    	System.out.println("test print: code2Value ");
	    //    	System.out.println(code2Value);
	        	
	        	vt.flushCurrentNumber();
	        	//insert data
				HbaseInsert.addRecord(C2VtableName, C2ProwName, family, code2Value);
				code2Value = null;	 

	       }
	         HbaseInsert.addRecord(P2CtableName,path2Code, family);
	        
		}
		if (file.isFile()) {
			String path = file.getPath(); 
			C2ProwName =  file.getName();//article name as the C2VrowName
			code2Value= new HashMap<String, String>();
			
			MTmanager.createMappingTable(path,C2ProwName,vt,path2Code,code2Value);
			
        	//print test
		//	System.out.println(C2ProwName);
        //	System.out.println("test print: path2Code ");
        //	System.out.println(path2Code);
        //	
        //	System.out.println("test print: code2Value ");
        //	System.out.println(code2Value);
        	
        //	System.out.println();
       	
			//insert data
			HbaseInsert.addRecord(C2VtableName, C2ProwName, family, code2Value);
			HbaseInsert.addRecord(P2CtableName,path2Code, family);
			code2Value = null;	

		}
		
	}
	
}
