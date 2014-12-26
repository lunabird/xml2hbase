package cn.edu.xidian.repace.xml2hbase.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
/**
 * 现在创建和更新索引的策略是当一次性上传文件数大于5000是就更新表的索引，
 * 具体获得索引的类是cn.edu.xidian.repace.xml2hbase.model包下面的PagePointThread
 * @author Administrator
 *
 */
public class PagePointThread extends Thread{
	String tableName;
	public static HashMap<String,ArrayList<String>> tablePagePoints=new HashMap<String,ArrayList<String>>();
	public PagePointThread(String tableName){
		this.tableName=tableName;
	}
	public void run(){
		ArrayList<String> points=new ArrayList<String>();
		/*
		if(!HbaseData.isContained(tableName)){		
			int step=Global.step;
			ResultScanner rscanner=HbaseReader.getPagePoint("C2V-"+tableName, step);
			for(Result rs : rscanner){
				points.add(Bytes.toString(rs.getRow()));
		    }
			HbaseData.putPagePoints(tableName, points);
			//tablePagePoints.put(tableName, points);
			System.out.println(tableName+" "+HbaseData.tablePagePoints.get("books"));
		}else{
			points=HbaseData.getPagePoints(tableName);
		}
		*/
		int step=Global.step;
		ResultScanner rscanner=HbaseReader.getPagePoint("C2V-"+tableName, step);
		for(Result rs : rscanner){
			points.add(Bytes.toString(rs.getRow()));
	    }
		/*
		if(HbaseData.isContained(tableName)){
			HbaseData.tablePagePoints.remove(tableName);
		}
		*/
		HbaseData.putPagePoints(tableName, points);
		
		
		Put put = new Put(Bytes.toBytes(tableName));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("index"), Bytes.toBytes(points.toString()));
		HTable ht;
		try {
			ht = new HTable(HbaseConf.conf,"tables");
			ht.put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("Get points OK");
	}
}
