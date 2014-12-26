package cn.edu.xidian.repace.xml2hbase.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

public class HbaseConf {
	
	private static HBaseConfiguration hbaseConfig=null;
    public static Configuration conf = null;//modded the conf to public by kanchuanqi
   
   
static {
  	 conf = HBaseConfiguration.create();
   	 conf.set("hbase.master", "192.168.0.244");
		 conf.set("hbase.zookeeper.property.clientPort","2181");
	     conf.set("hbase.zookeeper.quorum","192.168.0.244,    192.168.0.245,    192.168.0.246," +
	    		 "192.168.0.247,   192.168.0.248,    192.168.0.249,  192.168.0.250,  192.168.0.251,"+
	 		"192.168.0.252, 192.168.0.253,  192.168.0.236, 192.168.0.237,  192.168.0.238, 192.168.0.239," +
	 		"192.168.0.240");// 配置Zookeeper集群的地址列表
 }

public static void create(String master,String port,String quorum){
	 conf = HBaseConfiguration.create();
 	 conf.set("hbase.master", master);
 	 conf.set("hbase.zookeeper.property.clientPort",port);
 	 conf.set("hbase.zookeeper.quorum",quorum);//192.168.1.7,
}
   
public static Configuration getConf()
{
	return conf;
}
public static String getVersion(){
	return conf.get("hbase.defaults.for.version");
}

public static String getRootDir(){
	return conf.get("hbase.rootdir");
}
public static String getHadoopVersion(){
	return conf.get("hadoop.defaults.for.version");
}
public static String getTmpDir(){
	return conf.get("hbase.tmp.dir");
}

public static String getId(){
	return conf.get("hbase.cluster.id");
}
public static String getZook(){
	return conf.get("hbase.zookeeper.quorum");
}
public static String getDis(){
	return conf.get("hbase.cluster.distributed");
}

}
