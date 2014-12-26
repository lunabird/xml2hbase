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

public class Query2 {
	
	private Map<String, List<String>> pctable;
	private String tableName;

	public Query2() {
		pctable = null;
		tableName = null;
	}
	
	/**
	 * Constructor with parameters.
	 * @param tableName : the HBase table name .
	 * @param pctable : the path to column mapping table.
	 */
	public Query2(String tableName, Map<String, List<String>> pctable){
		this.pctable = pctable;
		this.tableName = tableName;		
	}
	
	/**
	 * Run XQuery1
	 * @throws IOException
	 */
	public void query() throws IOException{
        String xpath = "/site/open_auctions/open_auction/bidder/increase";
        //Record the start time
        long timeTestStart=System.currentTimeMillis();
        List<String> pathColumns = new ArrayList<String>();
        pathColumns.addAll(pctable.get(xpath));
        List<String> columns = new ArrayList<String>();
        for(String str : pathColumns){
        	
        	if(str.matches("(\\d+[.])*1[.]\\d+[.]\\d+")){
        		columns.add(str);
        	}
        }
        pathColumns.clear();
        //ansList store the answers
        ArrayList<String> ansList = new ArrayList<String>();
        if(columns.size() > 0){
        	ResultScanner rs = HbaseReader.getSpecifiedQualifiers(tableName, "xmark", columns,null,null);
        	for(Result r : rs){
        		byte[][] ans = new byte[r.size()][];
        		r.getFamilyMap(Bytes.toBytes("xmark")).values().toArray(ans);
        		for(byte[] a : ans){
        			ansList.add("<increase>" + Bytes.toString(a) + "</increase>");
        		}
        	}
        	rs.close();
        }
		//Record the end time
	    long timeTestEnd=System.currentTimeMillis();
	    //Print the running time.
	    System.out.println("The Query #2 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tableName = "C2V-xmark1.0-4";
		String P2Ctable = "P2C-xmark1.0-4";
		Map<String, List<String>> pctable = null;
		Query2 q2 = null;
		try {
			pctable = HbaseRecreateMappingTable.RecP2CMap(P2Ctable, P2Ctable);
			//System.out.println("Got the Mapping Table");
			q2 = new Query2(tableName, pctable);
			for(int i = 0; i < 5; i++){
				q2.query();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
