package cn.edu.xidian.repace.xml2hbase.model;

public class Global {
	public static int step=2000;
	public static volatile boolean isQuering=false;
	public static volatile boolean hasException=false;
	public static String exception="";
	public static Exception e=null;
	public static synchronized void setE(Exception ee) {
		hasException = true;
		e=ee;
	}
}
