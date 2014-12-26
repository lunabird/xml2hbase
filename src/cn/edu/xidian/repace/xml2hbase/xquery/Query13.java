package cn.edu.xidian.repace.xml2hbase.xquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;

public class Query13 {
	private Map<String, List<String>> pctable ;
	private String tableName;
	private String family;
	
	public Query13() {
		this.pctable = null;
		this.tableName = null;
		this.family = null;
	}
	
	Query13(String tableName, Map<String, List<String>> pctable,String family)
	{
		this.pctable = pctable;
		this.tableName = tableName;	
		this.family= family;
	}

	
	public static String getParentCol(String childCol)
	{
		int offset =childCol.lastIndexOf('.',childCol.length()-1);
		int offset2 = childCol.lastIndexOf('.',offset-1);
		return childCol.substring(0,offset2);				
	}
	
	public void query() throws IOException{
		
       // String xpath1 = "/site/regions/africa/item/name";
       // String xpath2 = "/site/regions/africa/item/description";
        String xpath1 = "/site/regions/australia/item/name";
        String xpath2 = "/site/regions/australia/item/description";
		// String xpath1 ="/TESTINFO/UPDATEINFO/PARAM_TIMESTAMP";
		// String xpath2 ="/TESTINFO/UPDATEINFO/CITYINFO_LIST";
      
       Map<String,String> cptable = new HashMap<String,String>();  
       
        List<String> columns = pctable.get(xpath1);    //get all the codes of /site/regions/australia/item/name
        Iterator it = columns.iterator();
        while(it.hasNext())
        	cptable.put((String)it.next(),xpath1);  
        
        //get all path codes that begin with /site/regions/australia/item/description                                        
       
        List<String> templist ;
        String pathKey ;
       
        Set<Map.Entry<String, List<String>>> set = pctable.entrySet(); 
        for (Iterator<Map.Entry<String, List<String>>> ite = set.iterator(); ite.hasNext();) {
        	Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>) ite.next();
        	pathKey = (String) entry.getKey();
        	if(pathKey.startsWith(xpath2))
        	{ 
        		templist = (List<String>)entry.getValue();
                String nextCode ;
                Iterator itr = templist.iterator();
                while (itr.hasNext()) {
                    nextCode = (String) itr.next();
                    cptable.put(nextCode, pathKey);                   
                }
               
        	}
        }
       // System.out.println("test print:cptable="+cptable);
        //Record the start time
        long timeTestStart=System.currentTimeMillis();       
        
      // lines that has the xpath1,each line only include the wanted qualifiers got from the first step
        ResultScanner rscanner = null ;
        if(columns != null){
			//Get the scan result of HBase table
			try
			{
				HTable table=new HTable(HbaseConf.getConf(),tableName);  
				Scan scan = new Scan(); 
				Set<String> key = cptable.keySet();
				for (Iterator itk = key.iterator(); itk.hasNext();) {					
					scan.addColumn(Bytes.toBytes(family), Bytes.toBytes((String)itk.next())); 			
				 }				
				rscanner = table.getScanner(scan);
							 
			}catch (IOException e) 
			{ 
				System.out.println(e.getMessage() + "scan failed"); 
			}
			
			
			}
        
		
		//Iterator on the ResultSet

		for(Result rs : rscanner){
			//System.out.println("The row of the result is :" + Bytes.toString(rs.getRow()));
			
			ArrayList<String> ansList = new ArrayList<String>();
			byte[][] ans = new byte[rs.size()][];
			rs.getFamilyMap(Bytes.toBytes("xmark")).keySet().toArray(ans);
			for(byte[] a : ans){
			        			ansList.add(Bytes.toString(a));
			        		}

			//print the results
			
			Iterator itr = ansList.iterator();
			while (itr.hasNext()) {
				String s = (String)(itr.next());
				if(cptable.get(s)!=null)
				{
					System.out.println("path="+cptable.get(s)+"  value="+Bytes.toString(rs.getValue(Bytes.toBytes(family), Bytes.toBytes(s))));
				}
				
			}
			

			System.out.println();
			System.out.println();
        }
		rscanner.close();
		//Record the end time
        long timeTestEnd=System.currentTimeMillis();
        //Print the running time.
        System.out.println("The Query #13 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
        
	}
	
	
	public static void main(String[] args) {
		String tableName = "C2V-xmark1.0-4"; 
		String P2Ctable = "P2C-xmark1.0-4";  
		//String tableName = "C2V-test4"; 
		//String P2Ctable = "P2C-test4";  
		String family =  "xmark";
		Map<String, List<String>> pctable = null;
		Query13 q13 = null;
		try {
			pctable = HbaseRecreateMappingTable.RecP2CMap(P2Ctable, P2Ctable);
			q13 = new Query13(tableName, pctable,family);
			for(int i = 0; i < 5; i++){
				q13.query();
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}