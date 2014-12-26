import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;

/**
 * 在集群端创建一个表存放表的一些信息和表的索引，如果重新安装集群必须先执行这个，创建个表。
 * @author Administrator
 *
 */
public class CreateTableInfo {
	HBaseAdmin hBaseAdmin;
	public CreateTableInfo() throws MasterNotRunningException, ZooKeeperConnectionException{
		hBaseAdmin=new HBaseAdmin(HbaseConf.conf);
	}
	
	public void create() throws IOException{
		HTableDescriptor desc = new HTableDescriptor("tables");
		desc.addFamily(new HColumnDescriptor("info"));
		if(!hBaseAdmin.tableExists("tables")){
			hBaseAdmin.createTable(desc);
		}		
	}
	
	
	public void insertOne() throws IOException{
		ResultScanner rscanner=HbaseReader.getPagePoint("C2V-xmark4", 2000);//修改7
		List<String> points=new ArrayList<String>();
		for(Result rs : rscanner){
			points.add(Bytes.toString(rs.getRow()));
	    }
		//System.out.println(points.toString());
		//ArrayList<String> test=new ArrayList<String>();
		Put put = new Put(Bytes.toBytes("xmark4"));//修改8
		put.add(Bytes.toBytes("info"), Bytes.toBytes("creater"), Bytes.toBytes("admin"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("time"), Bytes.toBytes("2013-7-31 10:00:00"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(230001));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("index"), Bytes.toBytes(points.toString()));
		HTable ht=new HTable(HbaseConf.conf,"tables");
		ht.put(put);
		System.out.println("Insert OK");
	}
	
	public void getOne() throws IOException{
		Get get2 = new Get(Bytes.toBytes("xmark4"));//修改9
		get2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("index"));
		HTable ht=new HTable(HbaseConf.conf,"tables");
		Result r=ht.get(get2);
		String s=Bytes.toString(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("index")));
		System.out.println(s);
		
	}
	public static void main(String[] args) throws IOException{
		try {
			CreateTableInfo cti=new CreateTableInfo();
		//	cti.create();    //只在第一次运行   修改6
		//	System.out.println("Create OK");
			cti.insertOne();
			cti.getOne();//输出测试用
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
