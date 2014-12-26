package cn.edu.xidian.repace.xml2hbase.model;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.ipc.HMasterInterface;
import org.apache.hadoop.hbase.master.HMaster;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.MappingTableManager;
import cn.edu.xidian.repace.xml2hbase.VirtualTable;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseCreate;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseDelete;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseInsert;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;

public class HbaseData {
	public static HTableDescriptor[] hTableDescriptors=null;
	public static HBaseAdmin hBaseAdmin; 
	public static ClusterStatus cs;
	public static boolean isRunning=true; 
	public static ArrayList<String> tableList=new ArrayList<String>();
	public static HashMap<String,ArrayList<String>> tablePagePoints=new HashMap<String,ArrayList<String>>();
	public static User user;
	public static ArrayList<String> userList;
	//public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 15, 2,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	public static boolean isContained(String tableName){
		return tablePagePoints.containsKey(tableName);
	}
	public static void putPagePoints(String tableName,ArrayList<String> points){
		tablePagePoints.put(tableName, points);
	}
	public static ArrayList<String> getPagePoints(String tableName){
		return tablePagePoints.get(tableName);
	}
	
	public static void discon(){
		hTableDescriptors=null;
		hBaseAdmin=null; 
		cs=null;
		isRunning=true;
		tableList=new ArrayList<String>();
		tablePagePoints=new HashMap<String,ArrayList<String>>();
		user=null;
		userList=null;
	}
	
	public static void getPoints(String tableName){
		Get get2 = new Get(Bytes.toBytes(tableName));
		get2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("index"));
		HTable ht;
		try {
			ht = new HTable(HbaseConf.conf,"tables");
			Result r=ht.get(get2);
			String s="";
			s=Bytes.toString(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("index")));
			//s="111";
			int n=s.length();
			if(n>2){
				String [] tmp=(s.substring(1, n-2)).split(", ");
				//ArrayList<String> list=new ArrayList<String>();
				//list=(ArrayList<String>) Arrays.asList(tmp);
				ArrayList<String> list = new ArrayList<String>(Arrays.asList(tmp));
				/*
				for(int i=0;i<tmp.length;i++){
					list.add(tmp[i]);
				}
				*/
				putPagePoints(tableName,list);
			}else{
				putPagePoints(tableName,new ArrayList<String>());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static ArrayList<String> listTables(){
		
		try {
			hBaseAdmin=new HBaseAdmin(HbaseConf.conf);
			hTableDescriptors=hBaseAdmin.listTables();
			for(HTableDescriptor htd:hTableDescriptors){
				if(htd.getNameAsString().startsWith("C2V-")&&user.getValue(htd.getNameAsString().substring(4))>0){
				//if(htd.getNameAsString().startsWith("C2V-")){
					tableList.add(htd.getNameAsString().substring(4));
					//new PagePointThread(htd.getNameAsString().substring(4)).start();
					getPoints(htd.getNameAsString().substring(4));
				}				
			}
			HMasterInterface hmaster=hBaseAdmin.getMaster();
			cs=hmaster.getClusterStatus();
			System.out.println("Test ID :"+cs.getClusterId());
			
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			isRunning=false;
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tableList;
	}
	
	public static HTableDescriptor getP2CByName(String name){
		name="P2C-"+name;
		for(int i=0;i<hTableDescriptors.length;i++){
			if(name.equals(hTableDescriptors[i].getNameAsString())){
				return hTableDescriptors[i];
			}
		}
		return new HTableDescriptor();
	}
	
	public static HTableDescriptor getC2VByName(String name){
		name="C2V-"+name;
		for(int i=0;i<hTableDescriptors.length;i++){
			if(name.equals(hTableDescriptors[i].getNameAsString())){
				return hTableDescriptors[i];
			}
		}
		return new HTableDescriptor();
	}
	
	public static boolean isTableExists(String tableName) throws IOException{
		return hBaseAdmin.tableExists("P2C-"+tableName)||hBaseAdmin.tableExists("C2V-"+tableName);
	}
	
	public static void newUser(String username,String password) throws IOException{
		HTable ht=new HTable(HbaseConf.conf,"users");
		Put put=new Put(Bytes.toBytes(username));
		put.add(Bytes.toBytes("user"), Bytes.toBytes("username"), Bytes.toBytes(username));
		put.add(Bytes.toBytes("user"),Bytes.toBytes("password"), Bytes.toBytes(password));
		put.add(Bytes.toBytes("user"),Bytes.toBytes("isAdmin"), Bytes.toBytes("no"));
		ht.put(put);
	}
	
	public static void newDatabase(String tableName,String family){
		//String family = "xmark";
		String P2CtableName ="P2C-"+tableName;
		String C2VtableName = "C2V-"+tableName;
		try {
			HbaseCreate.createTable(P2CtableName, family);
			HbaseCreate.createTable(C2VtableName, family);
			
			
			
			long t=System.currentTimeMillis();
			SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dt = new Date(t);  
			String sDateTime = sdf.format(dt); 
			Put put = new Put(Bytes.toBytes(tableName));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("creater"), Bytes.toBytes(user.username));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("time"), Bytes.toBytes(sDateTime));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(0));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("index"), Bytes.toBytes(""));
			HTable ht=new HTable(HbaseConf.conf,"tables");
			ht.put(put);
			
			HTable ht1=new HTable(HbaseConf.conf,"users");
			Put put1=new Put(Bytes.toBytes(user.username));
			put1.add(Bytes.toBytes("tables"), Bytes.toBytes(tableName), Bytes.toBytes(2));
			ht1.put(put1);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Fail to Create "+tableName);
			e.printStackTrace();
		}
		try {
			hTableDescriptors=hBaseAdmin.listTables();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tableList.add(tableName);
		tablePagePoints.put(tableName, new ArrayList<String>());
	}
	
	public static boolean deleteDatabase(String tableName) throws Exception{
		boolean success=false;
		String p2cName="P2C-"+tableName;
		String c2vName="C2V-"+tableName;
		HbaseDelete.deleteTable(c2vName);
		HbaseDelete.deleteTable(p2cName);
		
		
		HTable ht=new HTable(HbaseConf.conf,"tables");
		Delete deleteAll = new Delete(Bytes.toBytes(tableName));
		ht.delete(deleteAll);
		
		
		success=true;
		return success;
	}
	
	public static void importFile(String tableName,File file){
		String path = file.getPath(); 
		String C2ProwName = "";
		MappingTableManager MTmanager = new MappingTableManager();		
		Map<String, String> code2Value= null;
		Map<String, List<String>> path2Code = new HashMap<String, List<String>>();
		C2ProwName =  file.getName();//article name as the C2VrowName
		code2Value= new HashMap<String, String>();
		VirtualTable vt = new VirtualTable("P2C-"+tableName);
		HTableDescriptor p2c=getP2CByName(tableName);
		String family=p2c.getColumnFamilies()[0].getNameAsString();
		MTmanager.createMappingTable(path,C2ProwName,vt,path2Code,code2Value);
		HbaseInsert.addRecord("C2V-"+tableName, C2ProwName, family, code2Value);
		HbaseInsert.addRecord("P2C-"+tableName,path2Code, family);
		code2Value = null;	
		
		Get get2 = new Get(Bytes.toBytes(tableName));
		get2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("count"));
		HTable ht;
		try {
			ht = new HTable(HbaseConf.conf,"tables");
			Result r=ht.get(get2);
			int s=Bytes.toInt(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("count")));
			s++;
			Put put = new Put(Bytes.toBytes(tableName));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(s));
			ht.put(put);
			System.out.println(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void addCount(int n){
		
	}
	/**
	 * 具体执行查询的方法是HbaseData类中的query，该方法调用cn.edu.xidian.repace.xml2hbase.model包中的查询线程
	 * QueryThread.java完成操作
	 * @param tableName1
	 * @param query
	 * @param queue
	 * @param pt
	 * @throws IOException
	 */
	public static void query(String tableName1,String query,ConcurrentLinkedQueue<String> queue,PrintThread pt) throws IOException{
		String P2CTableName = "P2C-"+tableName1;
		String tableName="C2V-"+tableName1;	
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 15, 2,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		Map<String, List<String>> pctable = null;
		pctable = HbaseRecreateMappingTable.RecP2CMap(P2CTableName, P2CTableName);
		List<String> points=new ArrayList<String>();
		
		while(!isContained(tableName1)){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		points=tablePagePoints.get(tableName1);
		
		String family=HbaseData.getP2CByName(tableName1).getColumnFamilies()[0].getNameAsString();
		System.out.println("queryyyy "+tableName+points.size()+" "+family);
		String oldStart=null;
		for(int i=0;i<points.size();i++){
			threadPool.execute(new QueryThread(tableName,family,pctable,oldStart,points.get(i),query,queue));
			oldStart=points.get(i);
		}
		threadPool.execute(new QueryThread(tableName,family,pctable,oldStart,null,query,queue));
		pt.start();
		threadPool.shutdown();
		System.out.println("Run HeartBeat");
		new HeartBeat(threadPool,pt).start();
		Global.isQuering=false;		
	}
}
