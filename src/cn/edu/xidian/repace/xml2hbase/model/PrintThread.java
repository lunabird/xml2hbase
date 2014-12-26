package cn.edu.xidian.repace.xml2hbase.model;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JTextArea;

import cn.edu.xidian.repace.xml2hbase.view.Hbase;

public class PrintThread extends Thread{
	volatile boolean stopFlag=false;
	ConcurrentLinkedQueue<String> queue;
	Hbase hbase;
	
	public synchronized void stopCurrentThread() {
	    stopFlag = true;
	}
	
	public PrintThread(ConcurrentLinkedQueue<String> queue,Hbase hbase){
		this.queue=queue;
		this.hbase=hbase;
		this.hbase.clearResult();
	}
	
	public void run(){
		long start=System.currentTimeMillis();
		while(!stopFlag||!queue.isEmpty()){
			if(!queue.isEmpty()){
				hbase.appendResult(queue.poll());
			}
			
		}
		if(Global.hasException){
			//Global.e.printStackTrace(new PrintStream(textArea1));
			hbase.appendResult("Exception:\n"+Global.e.getMessage());
			Global.hasException=false;
		}
		long stop =System.currentTimeMillis();
		hbase.appendResult("Total time:"+(stop-start)+"ms");
		System.out.println("Print Exit total time:"+(stop-start)+"ms");
	}
}
