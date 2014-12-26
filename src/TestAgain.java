import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;


public class TestAgain {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String tableName = "C2V-books";
		String P2CTableName = "P2C-books";
		List<String> points=new ArrayList<String>();
		int step=1000;
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 15, 2,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		ResultScanner rscanner=HbaseReader.getPagePoint(tableName, step);
		for(Result rs : rscanner){
			points.add(Bytes.toString(rs.getRow()));
	    }
		Map<String, List<String>> pctable = null;
		pctable = HbaseRecreateMappingTable.RecP2CMap(P2CTableName, P2CTableName);//where $a/@id=\"person1964\"
		//String query="for $a in /site/regions/africa/item where $a/location=\"United States\"return $a/@id";
		//String query="for $a in /site/closed_auctions/closed_auction/annotation/description/parlist/listitem/text/emph/keyword return $a";
		String query="for $a in /bib/book where $a/price >32.2 return $a/title";
		String oldStart=null;
		for(int i=0;i<points.size();i++){
			threadPool.execute(new QueryThread(tableName,pctable,oldStart,points.get(i),query));
			oldStart=points.get(i);
		}
		threadPool.execute(new QueryThread(tableName,pctable,oldStart,null,query));
		
		threadPool.shutdown();
		while(!threadPool.isTerminated()){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Hello World");
	}

}
