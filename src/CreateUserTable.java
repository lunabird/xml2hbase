import java.io.IOException;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;

/**
 * 是在集群端创建个表存放用户信息，包括用户名和密码，重装集群时也要执行该文件
 * @author Administrator
 *
 */
public class CreateUserTable {
	HBaseAdmin hBaseAdmin;
	public CreateUserTable() throws MasterNotRunningException, ZooKeeperConnectionException{
		hBaseAdmin=new HBaseAdmin(HbaseConf.conf);
	}
	public void create() throws IOException{
		HTableDescriptor desc = new HTableDescriptor("users");
		desc.addFamily(new HColumnDescriptor("user"));
		desc.addFamily(new HColumnDescriptor("tables"));
		if(!hBaseAdmin.tableExists("users")){
			hBaseAdmin.createTable(desc);
		}		
	}
	
	public void insertOne() throws IOException{
		Put put = new Put(Bytes.toBytes("admin"));
		put.add(Bytes.toBytes("user"), Bytes.toBytes("username"), Bytes.toBytes("admin"));
		put.add(Bytes.toBytes("user"), Bytes.toBytes("password"), Bytes.toBytes("123456"));
		put.add(Bytes.toBytes("user"), Bytes.toBytes("isAdmin"), Bytes.toBytes("yes"));
		put.add(Bytes.toBytes("tables"), Bytes.toBytes("xmark4"), Bytes.toBytes(2));//修改4
		HTable ht=new HTable(HbaseConf.conf,"users");
		ht.put(put);
		System.out.println("Insert OK");
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CreateUserTable cut=new CreateUserTable(); 
	//	cut.create(); //值在第一次时运行这一行   修改5
		
		cut.insertOne();
	}

}
