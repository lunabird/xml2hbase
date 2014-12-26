package cn.edu.xidian.repace.xml2hbase.xquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;

public class Query1 {
	
	private Map<String, List<String>> pctable;
	private String tableName;

	public Query1() {
		pctable = null;
		tableName = null;
	}
	
	/**
	 * Constructor with parameters.
	 * @param tableName : the HBase table name .
	 * @param pctable : the path to column mapping table.
	 */
	public Query1(String tableName, Map<String, List<String>> pctable){
		this.pctable = pctable;
		this.tableName = tableName;		
	}
	
	/**
	 * get the parent column of childCol
	 * @param childCol
	 * @return parent column
	 */
	public static String getParentCol(String childCol){
		int offset = childCol.lastIndexOf('.', childCol.length() - 1);
		int offset2 = childCol.lastIndexOf('.', offset - 1);
		return childCol.substring(0, offset2);
	}
	
	/**
	 * Run XQuery1
	 * @throws IOException
	 */
	public void query() throws IOException{
        String xpath = "/site/people/person/@id";
        String xpath2 = "/site/people/person/name";
        String eqName = "person1964";
        //Record the start time
        long timeTestStart=System.currentTimeMillis();
        List<String> columns = pctable.get(xpath);
        for(int i=0;i<columns.size();i++){
        	System.out.println(columns.get(i)+"\t"+columns.size());
        }
        //ansList store the answers
        ArrayList<String> ansList = new ArrayList<String>();
        if(columns != null){
			int i;
			List<Filter> filters = new ArrayList<Filter>();
			for(i = 0; i < columns.size(); ++i){
				//New a SingleColumnValueFilter to filter on a qualifier
				SingleColumnValueFilter filter = new SingleColumnValueFilter(
							  Bytes.toBytes("xmark"),
							  Bytes.toBytes(columns.get(i)),
							  CompareFilter.CompareOp.EQUAL,
							  Bytes.toBytes(eqName)
							  );
				//If the column is missing in a row, filter will skip
				filter.setFilterIfMissing(true);
				//Add the filter into filterList
				filters.add(filter);
			}
			//New a filterList and set the operator with MUST_PASS_ONE
			FilterList filterlist = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
			//Get the scan result of HBase table
			ResultScanner rscanner = HbaseReader.getRowsWithFilterList(tableName, filterlist,null,null);
			//Iterator on the ResultSet
			//Result rst=new Result("ddd");
			//rst.
			for(Result rs : rscanner){
				System.out.println("The row of the result is :" + Bytes.toString(rs.getRow()));
				for(i = 0; i < columns.size(); ++i){
					//Find the right column and get the xpath2 column value
					//System.out.println(Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns.get(i)))));
					if(Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns.get(i)))).equals(eqName)){
					//	System.out.println("The xpath column is : " + columns.get(i));
						String parentCol = getParentCol(columns.get(i));
						for(String col : pctable.get(xpath2)){
							if(col.startsWith(parentCol)){
							//	System.out.println("The xpath2 column is : " + col);
								String value = Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(col)));
								System.out.println("The result is : " + value);
								ansList.add(value);
								break;
							}
						}
						//break;
					}
				}
			}
			rscanner.close();
			//Record the end time
	        long timeTestEnd=System.currentTimeMillis();
	        //Print the running time.
	        System.out.println("The Query #1 Running time is: "+(timeTestEnd-timeTestStart) + "ms");
        }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tableName = "C2V-xmark1.0-4";
		String P2Ctable = "P2C-xmark1.0-4";
		Map<String, List<String>> pctable = null;
		Query1 q1 = null;
		try {
			pctable = HbaseRecreateMappingTable.RecP2CMap(P2Ctable, P2Ctable);
			System.out.println("Got the Mapping Table");
			q1 = new Query1(tableName, pctable);
			for(int i = 0; i < 5; i++){
				q1.query();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
