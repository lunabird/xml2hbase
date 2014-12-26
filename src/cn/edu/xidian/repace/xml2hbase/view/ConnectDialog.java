package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;
import cn.edu.xidian.repace.xml2hbase.model.User;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Tue Mar 26 11:23:37 CST 2013
 */



/**
 * @author Legend
 */
public class ConnectDialog extends JDialog implements ActionListener {
	
	public ConnectDialog(Frame owner) {
		super(owner,true);
		initComponents();
		
		getConf();
	}

	public ConnectDialog(Dialog owner) {
		super(owner,true);
		initComponents();
		this.setSize(600, 400);
		getConf();
	}
	
	public void getConf(){
		Properties props = new Properties();
		String fileName = "server.conf";
	    FileInputStream in;
	    String serverIPs;
		try {
			in = new FileInputStream(fileName);
			props.load(in);
			textField4.setText(props.getProperty("Master"));
			textField5.setText(props.getProperty("ClientPort"));
			serverIPs=props.getProperty("ServerIPs");
			String [] IPs=serverIPs.split(",");
			for (int i=0;i<IPs.length;i++){
				this.comboBox1.addItem(IPs[i]);
			}
			//textField1.setText(props.getProperty("ServerIPs"));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	public boolean logIn(String username,String password){
		boolean ok=false;
		try {
			HTable htuser=new HTable(HbaseConf.conf,Bytes.toBytes("users"));
			Get get=new Get(Bytes.toBytes(username));
			//get.addColumn(Bytes.toBytes("user"), Bytes.toBytes("password"));
			get.addFamily(Bytes.toBytes("user"));
			Result r=htuser.get(get);
			if(r.getValue(Bytes.toBytes("user"), Bytes.toBytes("password"))!=null){
				if(Bytes.toString(r.getValue(Bytes.toBytes("user"), Bytes.toBytes("password"))).equals(password)){
					ok=true;
					boolean isadmin=false;
					if(Bytes.toString(r.getValue(Bytes.toBytes("user"), Bytes.toBytes("isAdmin"))).equals("yes")){
						isadmin=true;
					}
					User u=new User(username,password,isadmin);
					
					Get get2=new Get(Bytes.toBytes(username));
					get2.addFamily(Bytes.toBytes("tables"));
					//get.
					r=htuser.get(get2);
					//r.
					//int i =Bytes.toInt(r.getValue(Bytes.toBytes("tables"), Bytes.toBytes("xmark-10.0-1")));
					//System.out.println(i);
					
					for(KeyValue kv:r.list()){
						String table=Bytes.toString(kv.getQualifier());
						int aoth=0;
						if(kv.getValue()!=null){
							aoth=Bytes.toInt(kv.getValue());
							
						}
						System.out.println(table+" "+aoth);
						u.put(table, aoth);
					}
					HbaseData.user=u;
					
					if(u.isadmin()){
						ArrayList<String> userlist=new ArrayList<String>();
						Scan scan=new Scan();
						scan.setFilter(new FirstKeyOnlyFilter());
						ResultScanner rs=htuser.getScanner(scan);
						for(Result r1:rs){
							userlist.add(Bytes.toString(r1.getRow()));
						}
						HbaseData.userList=userlist;
					}
					
				}else{
					JOptionPane.showMessageDialog(this, "密码错误");
				}
			}else{
				JOptionPane.showMessageDialog(this, "用户不存在");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ok;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label4 = new JLabel();
		textField4 = new JTextField();
		label5 = new JLabel();
		textField5 = new JTextField();
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		button1 = new JButton();
		button4 = new JButton();
		label2 = new JLabel();
		textField2 = new JTextField();
		label3 = new JLabel();
		textField3 = new JTextField();
		button3 = new JButton();
		password=new JPasswordField();
		
		button3.addActionListener(this);
		
		button2 = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"default:grow, 3*($lcgap, default), $lcgap, default:grow, $lcgap, default, $lcgap, default:grow, 2*($lcgap, default), $lcgap, default:grow",
			"default:grow, 8*($lgap, default), $lgap, default:grow"));

		//---- label4 ----
		label4.setText("Master地址");
		contentPane.add(label4, CC.xy(5, 3));
		contentPane.add(textField4, CC.xywh(9, 3, 5, 1));

		//---- label5 ----
		label5.setText("服务端口");
		contentPane.add(label5, CC.xy(5, 5));
		contentPane.add(textField5, CC.xywh(9, 5, 5, 1));

		//---- label1 ----
		label1.setText("\u670d\u52a1\u5668\u5730\u5740");
		label1.setHorizontalAlignment(SwingConstants.LEFT);
		contentPane.add(label1, CC.xy(5, 7));
		contentPane.add(comboBox1, CC.xywh(9, 7, 5, 1));

		//---- button1 ----
		button1.setIcon(new ImageIcon("images\\deleteip.png"));
		button1.addActionListener(this);
		contentPane.add(button1, CC.xy(15, 7));

		//---- button4 ----
		button4.setIcon(new ImageIcon("images\\addip.png"));
		button4.addActionListener(this);
		contentPane.add(button4, CC.xy(17, 7));

		//---- label2 ----
		label2.setText("\u7528\u6237\u540d");
		contentPane.add(label2, CC.xy(5, 9));
		contentPane.add(textField2, CC.xywh(9, 9, 5, 1));

		//---- label3 ----
		label3.setText("\u5bc6\u7801");
		contentPane.add(label3, CC.xy(5, 11));
		contentPane.add(password, CC.xywh(9, 11, 5, 1));

		//---- button3 ----
		button3.setText("\u767b\u9646");
		contentPane.add(button3, CC.xy(7, 15));

		//---- button2 ----
		button2.setText("\u53d6\u6d88");
		contentPane.add(button2, CC.xy(11, 15));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPasswordField password;
	private JLabel label4;
	private JTextField textField4;
	private JLabel label5;
	private JTextField textField5;
	private JLabel label1;
	private JComboBox comboBox1;
	private JButton button1;
	private JButton button4;
	private JLabel label2;
	private JTextField textField2;
	private JLabel label3;
	private JTextField textField3;
	private JButton button3;
	private JButton button2;
	public boolean isOK=false;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==button3){
			//		
			System.out.println(this.textField2.getText()+this.password.getPassword().toString().trim());
			isOK=this.logIn(this.textField2.getText(), new String(this.password.getPassword()));
			if(isOK){
				this.setVisible(false);
			}
			
		}else if(e.getSource()==button2){
			this.setVisible(false);
			this.dispose();
		}else if(e.getSource()==button4){
			//add ip
			String addip=JOptionPane.showInputDialog(this, "Add server ip(s) ", "Add IP", JOptionPane.QUESTION_MESSAGE);
			if(addip!=null){
				//this.serverIPs+=","+addip;
				String [] add=addip.split(",");
				for(int i=0;i<add.length;i++){
					this.comboBox1.addItem(add[i]);
				}
				this.comboBox1.setSelectedItem(add[0]);
			}
		}else if(e.getSource()==button1){
			//deleteip
			int select=this.comboBox1.getSelectedIndex();
			//this.comboBox1.remove(select);
			this.comboBox1.removeItemAt(select);
		}
		
	}
}
