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


public class Query15test {
	

	



	
		//Mapping Table Manager
		private Map<String, Map<String, List<String>>> mtm;
		//private Map<String, Map<String, String>> c2pMt;
		//All files in HBase table
		private String[] fileList;
		
		Query15test(){
			mtm = null;
			fileList = null;
		}
		
		Query15test(Map<String, Map<String, List<String>>> omtm, Set<String> fileName){
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
	        String xpath = "/site/closed_auctions/closed_auction/annotation/description/parlist/listitem/parlist/listitem/text/emph/keyword/ ";
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
	       for(int k=0;k<ans.size();k++)
	       {
	    	   System.out.println(ans.get(k));
	       }
		    return ans;
			
		}
		

		public static void main(String[] argc) throws IOException{
			Map<String, Map<String, List<String>>> mtP2C = null;
			Query15test q15 = null;
			String tableName = "C2V-xmark-1.0-1";
			String P2CTableName = "P2C-xmark-1.0-1";
			System.out.println("Begin get mt");
			HbaseReader.getAllRecord(P2CTableName);
			//mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
	}

		
}


