package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Thu Mar 28 16:52:58 CST 2013
 */



/**
 * @author Legend
 */
public class Contents extends JPanel implements ActionListener {
	String tableName;
	ArrayList<String> keyList=null;
	ArrayList<String> lastKeyList=new ArrayList<String>();
	int size=200;
	int totalNum=0;
	String last=null;
	int id=-1;
	String oldStart=null;
	String newStart=null;
	public Contents(String tableName) {
		this.tableName="C2V-"+tableName;
		initComponents();
		/*
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//test();
		start();
	}
	
	private void start(){
		last=null;
		id=-1;
		oldStart=null;
		newStart=null;
		keyList=HbaseReader.pageFilter(tableName, size, null);
		totalNum=keyList.size();
		//id++;
		//lastKeyList.add(keyList.get(totalNum-1));
		if(totalNum>0){
			last=keyList.get(totalNum-1);
		}
		
		showList();
	}
	
	private void showList(){
		listModel1.removeAllElements();
		for(int i=0;i<totalNum;i++){
			listModel1.addElement(keyList.get(i));
		}
	}
	
	public void test(){
		for(int i=0;i<2000;i++){
			listModel1.addElement("test1.0 "+i);
		}
	}
	
	public void nextToList(){
		if(totalNum<size){
			JOptionPane.showMessageDialog(this, "已经到达最后。");
		}else{			
			//last=lastKeyList.get(id);
			keyList=HbaseReader.pageFilter(tableName, size, last);
			lastKeyList.add(last);
			id++;
			totalNum=keyList.size();
			//id++;
			last=keyList.get(totalNum-1);
			showList();
		}
	}
	public void lastToList(){
		if(lastKeyList.size()==0){
			JOptionPane.showMessageDialog(this, "没有上一页了。");
		}else{
			//String last=null;
			//last=lastKeyList.get(id);
			last=lastKeyList.remove(id);
			id--;
			String lastlast=null;
			if(id>=0){
				lastlast=lastKeyList.get(id);
			}
			//id--;
			keyList=HbaseReader.pageFilter(tableName, size, lastlast);
			totalNum=keyList.size();
			//id++;
			//lastKeyList.add(keyList.get(totalNum-1));
			showList();
		}
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		textField1 = new JTextField();
		button3 = new JButton();
		scrollPane1 = new JScrollPane();
		listModel1=new DefaultListModel();
		list1 = new JList(listModel1);
		button1 = new JButton();
		button2 = new JButton();
		
		//======== this ========
		setLayout(new FormLayout(
			"2*(default, $lcgap), default:grow, 3*($lcgap, default)",
			"default, $lgap, default:grow, 2*($lgap, default)"));
		add(textField1, CC.xywh(7, 1, 3, 1));

		//---- button3 ----
		button3.setText("搜索");
		button3.addActionListener(this);
		add(button3, CC.xy(11, 1));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(list1);
		}
		add(scrollPane1, CC.xywh(1, 3, 11, 3));

		//---- button1 ----
		button1.setText("上一页");
		button1.addActionListener(this);
		add(button1, CC.xy(9, 7));

		//---- button2 ----
		button2.setText("下一页");
		button2.addActionListener(this);
		add(button2, CC.xy(11, 7));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTextField textField1;
	private JButton button3;
	private JScrollPane scrollPane1;
	private JList list1;
	private JButton button1;
	private JButton button2;
	private DefaultListModel listModel1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==button1){
			this.lastToList();
		}else if(e.getSource()==button2){
			this.nextToList();
		}else if(e.getSource()==button3){
			if(textField1.getText().length()>0){
				try {
					HTable ht=new HTable(HbaseConf.conf,tableName);
					Get get=new Get(Bytes.toBytes(textField1.getText()));
					//get.addColumn( Bytes.toBytes("info"),  Bytes.toBytes("creater"));
					get.setFilter(new FirstKeyOnlyFilter());
					Result r=ht.get(get);
					if(r!=null){
						String rs=Bytes.toString(r.getRow());
						if(rs!=null){
							listModel1.removeAllElements();
							listModel1.addElement(rs);
						}else{
							JOptionPane.showMessageDialog(this, "不存在");
						}						
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}else{
				start();
			}
		}
	}
}

