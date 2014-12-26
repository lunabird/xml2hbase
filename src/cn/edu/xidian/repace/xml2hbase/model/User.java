package cn.edu.xidian.repace.xml2hbase.model;

import java.util.HashMap;

public class User {
	public String username;
	public String password;
	boolean isAdmin;
	HashMap<String ,Integer> aoth;
	public User(String username,String password,boolean isAdmin){
		this.username=username;
		this.password=password;
		this.isAdmin=isAdmin;
		aoth=new HashMap<String,Integer>();
	}
	public void put(String table,int i){
		this.aoth.put(table, i);
	}
	public boolean isadmin(){
		return isAdmin;
	}
	public int getValue(String name){
		if(!aoth.containsKey(name))
			return 0;
		return aoth.get(name);
	}
}
