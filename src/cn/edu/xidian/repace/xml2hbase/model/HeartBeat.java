package cn.edu.xidian.repace.xml2hbase.model;

import java.util.concurrent.ThreadPoolExecutor;

public class HeartBeat extends Thread{
	PrintThread pt;
	ThreadPoolExecutor threadPool;
	public HeartBeat(ThreadPoolExecutor threadPool,PrintThread pt){
		this.threadPool=threadPool;
		this.pt=pt;
	}
	
	public void run(){
		while(!threadPool.isTerminated()&&!Global.hasException){
			try {
				Thread.sleep(100);
				//System.out.println("Running");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(Global.hasException){
			threadPool.shutdownNow();
		}
		pt.stopCurrentThread();
	}
}
