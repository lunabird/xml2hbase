import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;

import com.fatdog.xmlEngine.ResultList;
import com.fatdog.xmlEngine.TreeWalker;
import com.fatdog.xmlEngine.exceptions.InvalidQueryException;
import com.fatdog.xmlEngine.javacc.ParseException;
import com.fatdog.xmlEngine.javacc.SimpleNode;
import com.fatdog.xmlEngine.javacc.XQueryParser;


public class QueryThread extends Thread{
	String tableName;
	String startRow;
	String stopRow;
	Map<String, List<String>> pctable;
	String query;
	public QueryThread(String tableName,Map<String, List<String>> pctable,String startRow,String stopRow,String query){
		this.tableName=tableName;
		this.pctable=pctable;
		this.startRow=startRow;
		this.stopRow=stopRow;
		this.query=query;
	}
	
	public void run(){
		System.out.println("The QueryThread is running");
		long timeTestStart=System.currentTimeMillis();
		XQueryParser parser = new XQueryParser( new StringReader( query ));
		TreeWalker treeWalk=new TreeWalker(tableName,pctable,startRow,stopRow);
		ResultList results;
		try {
			SimpleNode root = parser.getXQueryAST();
			results=treeWalk.walk( root );
			int i;
			if(results.type!=1){
				int validNum=results.columns.size();
	            for(i = 0; i < validNum; ++i){
					System.out.println(HbaseReader.getoneQualifier(tableName, results.file.get(i), "xmark", results.columns.get(i)));	
				}
	            System.out.println("This thread number is "+validNum);
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
            
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long timeTestEnd=System.currentTimeMillis();
	    //Print the running time.
	    System.out.println("This Thread Running time is: "+(timeTestEnd-timeTestStart) + "ms");
	}

}
