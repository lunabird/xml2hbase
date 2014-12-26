import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Query;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import com.fatdog.xmlEngine.ResultList;
import com.fatdog.xmlEngine.TreeWalker;
import com.fatdog.xmlEngine.exceptions.InvalidQueryException;
import com.fatdog.xmlEngine.javacc.ParseException;
import com.fatdog.xmlEngine.javacc.SimpleNode;
import com.fatdog.xmlEngine.javacc.TokenMgrError;
import com.fatdog.xmlEngine.javacc.XQueryParser;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;
public class FuckTest {

	/**
	 * @param args
	 * @throws InvalidQueryException 
	 */
	public static void main(String[] args) throws InvalidQueryException {
		Map<String, Map<String, List<String>>> mtP2C = null;
		//Query15 q15 = null;//xmark-1.0-1
		String tableName = "C2V-xmark-10.0-1";
		String P2CTableName = "P2C-xmark-10.0-1";
		//System.out.println(getColParent("1.1.1.34.1"));
		Map<String, List<String>> pctable = null;
		long timeTestStart = 0;
		try{
			pctable = HbaseRecreateMappingTable.RecP2CMap(P2CTableName, P2CTableName);
			
			System.out.println("Begin get mt");
			//mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			//mtP2C = HbaseRecreateMappingTable.RecP2CMapAll(P2CTableName);
			System.out.println("got p2cmt");
			//mtC2P = HbaseRecreateMappingTable.RecC2PMapAll(C2VTableName);Leszek Schrott  let $b:=$a/title where $a/price>43.33
			//q15 = new Query15(mtP2C, mtP2C.keySet()); where $a/name>\"Zaaaa\" where $a/@id=\"person1964\"    and $a/name=\"Mohamadou Iciki\"
			String query="for $a in /site/closed_auctions/closed_auction/annotation/description/parlist/listitem/text/emph/keyword return $a";
			//String query="for $a in /bib/book  where $a/price>43.33 return $a/title";
			//String query="for $a in /site/regions/africa/item where $a/location=\"United States\"return $a/@id";
			ResultList results;
	        XQueryParser parser = new XQueryParser( new StringReader( query ));
			TreeWalker treeWalk=new TreeWalker(tableName,pctable,null,null);
			try {
	            SimpleNode root = parser.getXQueryAST();                      
	       
	            //TreeWalker walker = new TreeWalker( m_indexer );
	            //TreeWalker walker = new TreeWalker();
	            
	            //m_indexer.setTreeWalker( walker );
	            timeTestStart=System.currentTimeMillis();
	            results=treeWalk.walk( root );
	            /*
	            Set set = results.resultMap.entrySet();
	    		Iterator it = set.iterator();
    			HTable table=new HTable(HbaseConf.getConf(),tableName); 
	    		while(it.hasNext()) {
	    			Map.Entry me = (Map.Entry)it.next();
	    			List<String> columnsTmpTmp=(List<String>) me.getValue();
	    			String key=(String) me.getKey();
	    			//System.out.println("The key is "+key+"and "+columnsTmpTmp);
	    			Get g = new Get(key.getBytes());
	    			Result rs = table.get(g);
	    			for(int i=0;i<columnsTmpTmp.size();i++){
	    				//rs.
	    				//System.out.println(columnsTmpTmp.get(i));
	    				String result=Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columnsTmpTmp.get(i))));
	    				System.out.println("The result is "+result);
	    			}
	    		}
	    		*/
	            
	            int i;
	            if(results.type!=1){
	            	int validNum=results.columns.size();
		            for(i = 0; i < validNum; ++i){
						System.out.println(HbaseReader.getoneQualifier(tableName, results.file.get(i), "xmark", results.columns.get(i)));	
					}
		            System.out.println("Total number is "+validNum);
				
	            }
	            else{
					if(results.columns != null){
			        	ResultScanner rs = HbaseReader.getSpecifiedQualifiers(results.m_treeWalker.tableName, "xmark", results.columns,results.m_treeWalker.startRow,results.m_treeWalker.stopRow);
			        	for(Result r : rs){
			        		byte[][] ans = new byte[r.size()][];
			        		r.getFamilyMap(Bytes.toBytes("xmark")).values().toArray(ans);
			        		
			        		for(byte[] a : ans){
			        			//String row=new String(r.getRow());
			        			System.out.println(Bytes.toString(a));
			        		  
			        			//ansList.add("<increase>" + Bytes.toString(a) + "</increase>");
			        		}
			        	}
			        }
				}
	            
	        }
	        catch( ParseException ex ) {
	            throw new InvalidQueryException( ex.getMessage() );
	        }
	        catch( TokenMgrError error ) {
	            throw new InvalidQueryException( error.getMessage() );
	        }
			//treeWalk.
			//for(int i = 0; i < 6; i++){
				//q15.query15(tableName);
			//}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		long timeTestEnd=System.currentTimeMillis();
	    //Print the running time.
	    System.out.println("The Query Running time is: "+(timeTestEnd-timeTestStart) + "ms");
	}
	

}
