

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
 * -- Q15. Print the keywords in emphasis in annotations of closed auctions.

let $auction := doc("auction.xml") return
for $a in
  $auction/site/closed_auctions/closed_auction/annotation/description/parlist/
   listitem/
   parlist/
   listitem/
   text/
   emph/
   keyword/
   text()
return <text>{$a}</text>


 */
public class Query15 {
	//Mapping Table Manager
	private Map<String, Map<String, List<String>>> mtm;
	//private Map<String, Map<String, String>> c2pMt;
	//All files in HBase table
	//private String[] fileList={"xmark-1.0-1-00000","xmark-1.0-1-00001","xmark-1.0-1-00002"};
	private List<String> fileList=null;
	Query15(){
		mtm = null;
		fileList = null;
	}
	
	Query15(Map<String, Map<String, List<String>>> omtm, List<String> fileName){
		this.mtm = omtm;
		//fileList = (String[])fileName.toArray(new String[0]);
		//fileList={"xmark-1.0-1-00000","xmark-1.0-1-00001","xmark-1.0-1-00002"};
		fileList=fileName;
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
        //String xpath = "/site/closed_auctions/closed_auction/annotation/description/parlist/listitem/parlist/listitem/text/emph/keyword";
        String xpath="/bib/book/@year";
        //String xpath2 = "/site/closed_auctions/closed_auction/seller/@person";
		for(i = 0; i < fileList.size(); i++)
		{
			Map<String, List<String>> mt = mtm.get("P2C-book");
			ArrayList<String> text=queryOne(tableName, fileList.get(i), xpath , mt);
			
			Iterator iter= text.iterator();
			if(text.size()!=0)
			{
				//System.out.println("executing case#" + i);
				for(; iter.hasNext(); )	
				{   
					ans.add("<text>"+iter.next()+"</text>");
					System.out.println(iter.next());
			     }
			}
			//else System.out.println("null");
			
       }//for
		//Record the end time
		long timeTestEnd=System.currentTimeMillis(); 
		
        //print the ans array
		/*
        for(i = 0; i < ans.size(); ++i)
        {
        	System.out.println(ans.get(i));
        }//for
        */
       //System.out.println("size="+ans.size());
      //print the run time
        File filename= new File("Query#15TestRecord.log");
        try {
            FileOutputStream out=new FileOutputStream(filename);
            @SuppressWarnings("resource")
			PrintStream p=new PrintStream(out);
            p.println("The Query #15 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("The Query #15 Running time is: "+(timeTestEnd-timeTestStart)+"ms" );
	    return ans;
		
	}
	

	public static void main(String[] argc) throws IOException{
		Map<String, Map<String, List<String>>> mtP2C = null;
		Map<String, Map<String, List<String>>> mtC2V = null;
		Query15 q15 = null;
		String tableName = "C2V-book";
		String P2CTableName = "P2C-book";
		try{
			System.out.println("Begin get mt");
			//mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			//mtC2V = HbaseRecreateMappingTable.RecC2PMapAll(P2CTableName);
			System.out.println("got p2cmt");
			//mtC2P = HbaseRecreateMappingTable.RecC2PMapAll(C2VTableName);
			List<String> fileList=HbaseReader.getAllRowKeys(tableName);
			for(int i=0;i<fileList.size();i++){
				System.out.println(fileList.get(i));
			}
			q15 = new Query15(mtP2C, fileList);
			for(int i = 0; i < 6; i++){
				q15.query15(tableName);
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	
}

	
}
