/*
package cn.edu.xidian.repace.xml2hbase.xquery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Query;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;





public class Query15 {
	//Mapping Table Manager
	private Map<String, Map<String, List<String>>> mtm;
	//private Map<String, Map<String, String>> c2pMt;
	//All files in HBase table
	private String[] fileList;
	
	Query15(){
		mtm = null;
		fileList = null;
	}
	
	Query15(Map<String, Map<String, List<String>>> omtm, Set<String> fileName){
		this.mtm = omtm;
		fileList = (String[])fileName.toArray(new String[0]);
	}
	
	public static String getColParent(String childCol){
		int offset = childCol.lastIndexOf('.', childCol.length() - 1);
		return childCol.substring(0, offset);
	}
	
	
	//query HBase tableName to get value of rowKey:xpath column
	public ArrayList<String> queryOne(String dataBase,String rowKey, String xPath, Map<String, List<String>> mt)
	throws IOException
	{
		List<String> columns = mt.get(xPath);
		ArrayList<String> ans = new ArrayList<String>();
		if(columns != null){
			int i;
			for(i = 0; i < columns.size(); ++i){
				ans.add(HbaseReader.getoneQualifier(dataBase, rowKey, "xmark", columns.get(i)));
			}
		}
		return ans;
	}
	
	
	
	public ArrayList<String> query15(String tableName) throws IOException
	{   

		ArrayList<String> ans = new ArrayList<String>();
		int i,j;
		//Record the start time
        long timeTestStart=System.currentTimeMillis();
        //the path need to query
        String xpath = "/site/closed_auctions/closed_auction/annotation/description/parlist/listitem/parlist/listitem/text/emph/keyword";
        //String xpath2 = "/site/closed_auctions/closed_auction/seller/@person";
		for(i = 0; i < fileList.length; i++)
		{
			Map<String, List<String>> mt = mtm.get(fileList[i]);
			ArrayList<String> text=queryOne(tableName, fileList[i], xpath , mt);
			
			Iterator iter= text.iterator();
			if(text.size()!=0)
			{
				//System.out.println("executing case#" + i);
				for(; iter.hasNext(); )	
				{   
					ans.add("<text>"+iter.next()+"</text>");
			     }
			}
			//else System.out.println("null");
			
       }//for
		//Record the end time
		long timeTestEnd=System.currentTimeMillis(); 
		
        //print the ans array
		
      
      //print the run time
        File filename= new File("Query#15TestRecord.log");
        try {
            FileOutputStream out=new FileOutputStream(filename,true);
			PrintStream p=new PrintStream(out);
            p.println("The Query #15 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
            p.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("The Query #15 Running time is: "+(timeTestEnd-timeTestStart)+"ms" );
        System.out.println("size="+ans.size());
	    return ans;
		
	}
	

	public static void main(String[] argc) throws IOException{
		Map<String, Map<String, List<String>>> mtP2C = null;
		Query15 q15 = null;
		String tableName = "C2V-xmark-1.0-1-1";
		String P2CTableName = "P2C-xmark-1.0-1-1";
		//System.out.println(getColParent("1.1.1.34.1"));
		try{
			System.out.println("Begin get mt");
			//mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			int i=mtP2C.size();
			System.out.println("mappingtable size="+i);
			//HbaseRecreateMappingTable.getRecP2CMapAll(mtP2C);
			System.out.println("got p2cmt");
			//mtC2P = HbaseRecreateMappingTable.RecC2PMapAll(C2VTableName);
			q15 = new Query15(mtP2C, mtP2C.keySet());
			//for(int i = 0; i < 6; i++){
				q15.query15(tableName);
			//}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	
}

	
}
*/

package cn.edu.xidian.repace.xml2hbase.xquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;


 


public class Query15 {
	private Map<String, List<String>> pctable;
	private String tableName;
	private String[] fileList;

	public Query15() {
		pctable = null;
		tableName = null;
	}
	
	
	public Query15(String tableName, Map<String, List<String>> pctable){
		this.pctable = pctable;
		this.tableName = tableName;	
	}
	
		

		public void query15(String tableName) throws IOException{
			//ArrayList<String> ans = new ArrayList<String>();
			//String xpath = "/site/closed_auctions/closed_auction/annotation/description/parlist/listitem/parlist/listitem/text/emph/keyword";
			String xpath="/site/people/person/name";
			long timeTestStart=System.currentTimeMillis();
	        //List<String> pathColumns = pctable.get(xpath);
	        List<String> columns = pctable.get(xpath);
	       /* for(String str : pathColumns){
	        	
	        	if(str.matches("(\\d+[.])*1[.]\\d+[.]\\d+")){
	        		columns.add(str);
	        	}
	        }*/
	        //pathColumns.clear();
	        //ansList store the answers
	        ArrayList<String> ansList = new ArrayList<String>();
	        if(columns != null){
	        	ResultScanner rs = HbaseReader.getSpecifiedQualifiers(tableName, "xmark", columns,null,null);
	        	for(Result r : rs){
	        		byte[][] ans = new byte[r.size()][];
	        		r.getFamilyMap(Bytes.toBytes("xmark")).values().toArray(ans);
	        		
	        		for(byte[] a : ans){
	        			//String row=new String(r.getRow());
	        		//	System.out.println(Bytes.toString(a));
	        		  
	        			ansList.add("<increase>" + Bytes.toString(a) + "</increase>");
	        		}
	        	}
	        }
	        
			//Record the end time
		    long timeTestEnd=System.currentTimeMillis();
		    //Print the running time.
		  //  System.out.println("anssize="+ansList.size());
		    System.out.println("The Query #15 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
		} 
	        //Record the start time
			/*
	        long timeTestStart=System.currentTimeMillis();
	    
	        List<String> columns= pctable.get(xpath);
			ArrayList<String> text = new ArrayList<String>();
			 HbaseInfor object=new  HbaseInfor();
			 object.tableName=tableName;
			 object.column="xmark";
	        if(columns!= null)
	        {
				int i;
				for(i = 0; i < columns.size(); ++i)
				{
					 object.qualifier=columns.get(i);
					 // ArrayList<String> value = new ArrayList<String>();	
				
					 ArrayList <String> value =new ArrayList<String>();
					// ArrayList <String> rowkey =new ArrayList<String>();
					 HbaseReader.getallQualifier(object,value);
					 //ArrayList<String> value=HbaseReader.getallQualifier(object);
				
					 for(int j=0;j<value.size();j++)
					 {
						 String temp=value.get(j).toString();
						 if(!temp.equals(""))
						 {
						 text.add(temp);
						 //System.out.println(rowkey.get(j));
						//System.out.println("temp="+temp);
						 }
					 }
					
				}
	       
	        Iterator iter= text.iterator();
					//System.out.println("executing case#" + i);
					for(; iter.hasNext(); )	
					{   
						ans.add("<text>"+iter.next()+"</text>");
				    }//for
			
			
			
			  long timeTestEnd=System.currentTimeMillis();
		
				
				
				//Record the end time
		      
		        System.out.println("anssize="+ans.size());
		        //Print the running time.
		        System.out.println("The Query #15 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
		     //System.out.println("count="+count);
	        }  	
	        
			 
		}                          

*/
	

		public static void main(String[] argc) throws Exception{
		String tableName = "C2V-xmark1.0-4";
		//String rowKey="C2V-xmark-1.0-1-3";
		String P2Ctable = "P2C-xmark1.0-4";
		Map<String, List<String>> pctable = null;
		Query15 q15 = null;
		try {
			
			pctable = HbaseRecreateMappingTable.RecP2CMap(P2Ctable, P2Ctable);
			//System.out.println("Got the Mapping Table");
			q15 = new Query15(tableName, pctable);
			//HbaseReader.getAllRecord(tableName);
			for(int j= 0; j < 5; j++){
				q15.query15(tableName);
			
			}
			
			
		}
	
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
