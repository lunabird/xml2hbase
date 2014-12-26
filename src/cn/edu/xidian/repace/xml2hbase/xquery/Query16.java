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

/*
 * -- -- Q16. Return the IDs of those auctions
--      that have one or more keywords in emphasis. (cf. Q15)

let $auction := doc("auction.xml") return
for $a in $auction/site/closed_auctions/closed_auction
where
  not(
    empty(
      $a/annotation/description/parlist/listitem/parlist/listitem/text/emph/
       keyword/
       text()
    )
  )
return <person id="{$a/seller/@person}"/>



 */
public class Query16 {
	//Mapping Table Manager
	private Map<String, Map<String, List<String>>> mtm;
	//private Map<String, Map<String, String>> c2pMt;
	//All files in HBase table
	private String[] fileList;
	
	Query16(){
		mtm = null;
		fileList = null;
	}
	
	Query16(Map<String, Map<String, List<String>>> omtm, Set<String> fileName){
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
	
	
	
	public ArrayList<String> query16(String tableName) throws IOException
	{   

		ArrayList<String> ans = new ArrayList<String>();
		int i,j;
		//Record the start time
        long timeTestStart=System.currentTimeMillis();
        //the path need to query
        String xpath = "/site/closed_auctions/closed_auction/annotation/description/parlist/listitem/parlist/listitem/text/emph/keyword";
        String xpath2 = "/site/closed_auctions/closed_auction/seller/@person";
       
    
		for(i = 0; i < fileList.length; i++)
		{
			Map<String, List<String>> mt = mtm.get(fileList[i]);
			ArrayList<String> text=queryOne(tableName, fileList[i], xpath , mt);
			if(text.size()!=0)
			{
				ArrayList<String> person=queryOne(tableName, fileList[i], xpath2 , mt);
				Iterator iter= person.iterator();
				for(;iter.hasNext();)
				{
					//System.out.println(iter.next().toString());
			       ans.add(iter.next().toString());
				}
			}
			//else System.out.println("null");
			
       }//for
		//Record the end time
		long timeTestEnd=System.currentTimeMillis(); 
		
        //print the ans array
		
        for(i = 0; i < ans.size(); ++i)
        {
        	System.out.println(ans.get(i));
        }//for
       
        System.out.println("size="+ans.size());
        File filename= new File("Query#16TestRecord.log");
        try {
            FileOutputStream out=new FileOutputStream(filename,true);
			PrintStream p=new PrintStream(out);
            p.println("The Query #16 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
            p.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
      //print the run time
        System.out.println("The Query #16 Running time is: "+(timeTestEnd-timeTestStart)+"ms" );
	    return ans;
		
	}
    /*
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
			 //ArrayList<String> value=HbaseReader.getallQualifier(object£¬value);
			 HbaseReader.getallQualifier(object,value);
			 for(int j=0;j<value.size();j++)
			 {
				 String temp=value.get(j).toString();
				 if(!temp.equals(""))
				 {
				   text.add(temp);
				 }
				 //System.out.println("temp="+temp);
			 }
		}
		
		if(text.size()!=0)
		{
			 List<String> column= pctable.get(xpath2);
			 HbaseInfor infor=new  HbaseInfor();
			 infor.tableName=tableName;
			 infor.column="xmark";
			 if(column!= null)
		       {
					int k;
					for(k = 0; k< columns.size(); ++k)
					{
						 object.qualifier=columns.get(k);
						 ArrayList<String> value = new ArrayList<String>();	      
						 //ArrayList<String> value=HbaseReader.getallQualifier(object);
						 HbaseReader.getallQualifier(object,value);
						 for(int j=0;j<value.size();j++)
						 {
							 String temp=value.get(j).toString();	 
						     ans.add(temp);
						 }
					}
		       }
		}
   }
			 
			
	 long timeTestEnd=System.currentTimeMillis(); 
    System.out.println("The Query #16 Running time is: "+(timeTestEnd-timeTestStart)+"ms" );
    System.out.println("ans.size="+ans.size());
    return ans;
    
	
}
*/

	public static void main(String[] argc) throws IOException{
		Map<String, Map<String, List<String>>> mtP2C = null;
		Query16 q16 = null;
		String tableName = "C2V-xmark-1.0-1-1";
		String P2CTableName = "P2C-xmark-1.0-1-1";
		System.out.println(getColParent("1.1.1.34.1"));
		try{
			System.out.println("Begin get mt");
			//mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			System.out.println("got p2cmt");
			//mtC2P = HbaseRecreateMappingTable.RecC2PMapAll(C2VTableName);
			q16 = new Query16(mtP2C, mtP2C.keySet());
			//for(int i = 0; i < 6; i++){
				q16.query16(tableName);
			//}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	
}

	
}

