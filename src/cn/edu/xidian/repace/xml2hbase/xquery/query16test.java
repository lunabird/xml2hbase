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
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseInfor;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseInsert;
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
public class query16test {
	private Map<String, List<String>> pctable;
	private String tableName;
	private String[] fileList;

	public query16test() {
		pctable = null;
		tableName = null;
	}
	
	
	public query16test(String tableName, Map<String, List<String>> pctable){
		this.pctable = pctable;
		this.tableName = tableName;	
	}
	
	
	
	public ArrayList<String> query16(String tableName) throws IOException
	{   
		//ArrayList<String> ans = new ArrayList<String>();
	
		//Record the start time
        long timeTestStart=System.currentTimeMillis();
        //the path need to query
        //String xpath="/TESTINFO/UPDATEINFO/CITYINFO_LIST/CITYINFO/@City";
       // String xpath2="/TESTINFO/UPDATEINFO/CITYINFO_LIST/CITYINFO/@Name";
       String xpath = "/site/closed_auctions/closed_auction/annotation/description/parlist/listitem/parlist/listitem/text/emph/keyword";
      String xpath2 = "/site/closed_auctions/closed_auction/seller/@person";
       // String xpath = "/site/people/person/homepage";
		//String xpath2 = "/site/people/person/name";
        List<String> columns = pctable.get(xpath);
        /*
        for(Iterator iter = columns.iterator(); iter.hasNext();)
		  {
        	System.out.println("column="+iter.next());
		  }
        */
        ArrayList<String> ans = new ArrayList<String>();     
        if(columns != null){
        	//获得所有对应列下的值
        	int count=0;
        	ResultScanner rs = HbaseReader.getSpecifiedQualifiers(tableName, "xmark", columns,null,null);
        	for(Result r : rs)
        	{
        		byte[][] text = new byte[r.size()][];
        		r.getFamilyMap(Bytes.toBytes("xmark")).values().toArray(text);
        		for(byte[] a : text)
        		{    
        			++count;
        			if(!Bytes.toString(a).equals("")&&(count==1)){
        				HbaseReader.getOneSpecifiedQualifiers(tableName, "xmark", new String(r.getRow()), xpath2,pctable, ans);
        				}
        				 
        		}
        		count=0;
        		}
        	}
        
       
		//Record the end time
	    long timeTestEnd=System.currentTimeMillis();
	    //Print the running time.
	   
	    System.out.println("The Query #16 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
	    System.out.println("anssize="+ans.size());
	   /*
	    Iterator iter=ans.iterator();
	    for(;iter.hasNext();)
	    	System.out.println("ans="+iter.next());
	    	*/
		return ans;
	} 
    

	public static void main(String[] argc) throws Exception{
		String tableName = "C2V-xmark-50.0-10-3";
		//String tableName = "C2V-test";
		//String rowKey="C2V-test";
		String P2Ctable = "P2C-xmark-50.0-10-3";
		//String P2Ctable = "P2C-test";
		// String xpath ="/TESTINFO/UPDATEINFO/CITYINFO_LIST/CITYINFO/@Name";
		Map<String, List<String>> pctable = null;
		ArrayList<String> ans = new ArrayList<String>();
		query16test  q16 = null;
		try {
			//HbaseInsert.addOneRecord(tableName, "2.xml", "xmark", "1.1.1.2.1.1.1.1.1", "ni hao");
			//HbaseInsert.addOneRecord(tableName, "3.xml", "xmark", "1.1.1.2.1.1.3.1.1", "xiao");
			
			pctable = HbaseRecreateMappingTable.RecP2CMap(P2Ctable, P2Ctable);
			//HbaseRecreateMappingTable.getRecP2CMap(pctable);
			System.out.println("Got the Mapping Table");
			q16 = new query16test(tableName, pctable);
			//HbaseReader.getOneSpecifiedQualifiers(tableName, "xmark", "1.xml", xpath, pctable, ans);
			//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
			//HbaseReader.getAllRecord(tableName);
			
			for(int j= 0; j < 5; j++){
				q16.query16(tableName);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
}

	
}

