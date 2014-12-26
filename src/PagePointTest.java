import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;


public class PagePointTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String tableName="C2V-xmark-1.0-1";
		int step=2000;
		long timeTestStart=System.currentTimeMillis();
		ResultScanner rscanner=HbaseReader.getPagePoint(tableName, step);
		long timeTestEnd=System.currentTimeMillis();
		for(Result rs : rscanner){
			System.out.println(Bytes.toString(rs.getRow()));
	    }
		
		System.out.println("OVER");
		
	    //Print the running time.
	    System.out.println("The Query Running time is: "+(timeTestEnd-timeTestStart) + "ms");
	}

}
