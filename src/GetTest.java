import java.io.IOException;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;


public class GetTest {
	public static void main(String[] args){
		try {
			HTable table=new HTable(HbaseConf.conf,"C2V-xmark-10.0-1");
			
			Get g=new Get();
			//table.g
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
