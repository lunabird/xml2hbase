package cn.edu.xidian.repace.xml2hbase.model;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	String family;
	ConcurrentLinkedQueue<String> queue;
	public QueryThread(String tableName,String family,Map<String, List<String>> pctable,String startRow,String stopRow,String query,ConcurrentLinkedQueue<String> queue){
		this.tableName=tableName;
		this.pctable=pctable;
		this.startRow=startRow;
		this.stopRow=stopRow;
		this.query=query;
		this.queue=queue;
		this.family=family;
	}
	
	public void run(){
		System.out.println("The QueryThread is running");
		//long timeTestStart=System.currentTimeMillis();
		XQueryParser parser = new XQueryParser( new StringReader( query ));
		TreeWalker treeWalk=new TreeWalker(tableName,pctable,startRow,stopRow);
		ResultList results;
		String result;
		try {
			SimpleNode root = parser.getXQueryAST();
			results=treeWalk.walk( root );
			int i;
			if(results.type!=1){
				int validNum=results.columns.size();
	            for(i = 0; i < validNum; ++i){
	            	result=HbaseReader.getoneQualifier(tableName, results.file.get(i), family, results.columns.get(i));
					//System.out.println(result);	
					queue.add(result);
				}
	            //System.out.println("This thread number is "+validNum);
			}
			else{
				if(results.columns != null){
		        	ResultScanner rs = HbaseReader.getSpecifiedQualifiers(results.m_treeWalker.tableName, family, results.columns,results.m_treeWalker.startRow,results.m_treeWalker.stopRow);
		        	for(Result r : rs){
		        		byte[][] ans = new byte[r.size()][];
		        		r.getFamilyMap(Bytes.toBytes(family)).values().toArray(ans);
		        		
		        		for(byte[] a : ans){
		        			queue.add(Bytes.toString(a));
		        		}
		        	}
		        }
			}
            
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			if(!Global.hasException){
				Global.setE(e);
			}
			e.printStackTrace();
		} catch (InvalidQueryException e) {
			// TODO Auto-generated catch block
			if(!Global.hasException){
				Global.setE(e);
			}
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(!Global.hasException){
				Global.setE(e);
			}
			e.printStackTrace();
		}
	}

}
