package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Fri Apr 26 13:10:48 CST 2013
 */



/**
 * @author Legend
 */
public class UserInfo extends JPanel implements ActionListener {
	String username;
	ArrayList<UserAoth> list;
	public UserInfo(String username) {
		this.username=username;
		list=new ArrayList<UserAoth>();
		initComponents();
	}
	
	public void getValues() throws IOException{
		HTable ht=new HTable(HbaseConf.conf,"users");
		Get get=new Get(Bytes.toBytes(username));
		get.addFamily(Bytes.toBytes("tables"));
		Result r=ht.get(get);
		/*
		for(KeyValue kv:r.list()){
			String table=Bytes.toString(kv.getQualifier());
			int aoth=0;
			if(kv.getValue()!=null){
				aoth=Bytes.toInt(kv.getValue());
				
			}else{
				aoth=0;
			}
			System.out.println(table+" "+aoth);
			UserAoth ua=new UserAoth(table,aoth);
			list.add(ua);
			jp.add(ua);
		}
		*/
		for(int i=0;i<HbaseData.tableList.size();i++){
			int aoth=0;
			if(r.getValue(Bytes.toBytes("tables"), Bytes.toBytes(HbaseData.tableList.get(i)))!=null){
				aoth=Bytes.toInt(r.getValue(Bytes.toBytes("tables"), Bytes.toBytes(HbaseData.tableList.get(i))));
			}
			UserAoth ua=new UserAoth(HbaseData.tableList.get(i),aoth);
			list.add(ua);
			int temp=2*(i+1)+1;
			jp.add(ua,CC.xy(3, temp));
			//jl.add(ua);
		}
		
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		name = new JLabel();
		//scrollPane1 = new JScrollPane();
		name.setText(username);
		button1 = new JButton();
		jl=new JList();
		
		int n=HbaseData.tableList.size();
		
		jp=new JPanel(new FormLayout(
				"default, $lcgap, default:grow, $lcgap, default",
				"default:grow, "+n+"*($lgap, default), $lgap, default:grow"));
		//jp.setLayout(mgr)
		
		try {
			getValues();
			scrollPane1 = new JScrollPane(jp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//======== this ========
		setLayout(new FormLayout(
				"default, $lcgap, 29dlu, $lcgap, 45dlu, $lcgap, default:grow, 2*($lcgap, default)",
				"2*(default, $lgap), default:grow, 3*($lgap, default)"));

		//---- label1 ----
		label1.setText("用户:");
		label1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label1, CC.xy(3, 3));

		//---- name ----
		name.setText(username);
		name.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(name, CC.xy(5, 3));
		add(scrollPane1, CC.xywh(3, 5, 7, 3));

		//---- button1 ----
		button1.setText("确定");
		button1.addActionListener(this);
		add(button1, CC.xy(9, 9));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	JPanel jp;
	JList jl;
	private JLabel label1;
	private JLabel name;
	private JScrollPane scrollPane1;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		try {
			HTable ht=new HTable(HbaseConf.conf,"users");
			Put put=new Put(Bytes.toBytes(username));
			for(int i=0;i<list.size();i++){
				put.add(Bytes.toBytes("tables"), Bytes.toBytes(list.get(i).tableName), Bytes.toBytes(list.get(i).getValue()));
			}
			ht.put(put);
			JOptionPane.showMessageDialog(this, "修改成功");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
