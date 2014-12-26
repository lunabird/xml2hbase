package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Thu Apr 25 20:18:23 CST 2013
 */



/**
 * @author Legend
 */
public class TableInfo extends JPanel {
	String tableName;
	public TableInfo(String tableName) {
		this.tableName=tableName;
		initComponents();
		setInfo();
	}
	
	private void setInfo(){
		try {
			this.name.setText(tableName);
			HTable ht=new HTable(HbaseConf.conf,"tables");
			Get get=new Get(Bytes.toBytes(tableName));
			get.addColumn( Bytes.toBytes("info"),  Bytes.toBytes("creater"));
			get.addColumn( Bytes.toBytes("info"),  Bytes.toBytes("time"));
			get.addColumn( Bytes.toBytes("info"),  Bytes.toBytes("count"));
			Result r=ht.get(get);
			if(r!=null){
				String c=Bytes.toString(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("creater")));
				String t=Bytes.toString(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("time")));
				int count=Bytes.toInt(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("count")));
				this.creater.setText(c);
				this.time.setText(t);
				this.count.setText(count+"");
			}
			this.authority.setText("R/W");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.name.setText(tableName);
		
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		name = new JLabel();
		label2 = new JLabel();
		creater = new JLabel();
		label4 = new JLabel();
		time = new JLabel();
		label5 = new JLabel();
		count = new JLabel();
		label6 = new JLabel();
		authority = new JLabel();

		//======== this ========
		setLayout(new FormLayout(
				"default:grow, $lcgap, 65dlu:grow, $lcgap, default:grow, $lcgap, 79dlu:grow, $lcgap, default:grow",
				"default:grow, 5*($lgap, default), $lgap, default:grow"));

		//---- label1 ----
		label1.setText("表名");
		label1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label1, CC.xy(3, 3));

		//---- name ----
		name.setText("text");
		name.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(name, CC.xy(7, 3));

		//---- label2 ----
		label2.setText("创建者");
		label2.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label2, CC.xy(3, 5));

		//---- creater ----
		creater.setText("text");
		creater.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(creater, CC.xy(7, 5));

		//---- label4 ----
		label4.setText("创建时间");
		label4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label4, CC.xy(3, 7));

		//---- time ----
		time.setText("text");
		time.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(time, CC.xy(7, 7));

		//---- label5 ----
		label5.setText("文件数");
		label5.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label5, CC.xy(3, 9));

		//---- count ----
		count.setText("text");
		count.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(count, CC.xy(7, 9));

		//---- label6 ----
		label6.setText("权限");
		label6.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label6, CC.xy(3, 11));

		//---- authority ----
		authority.setText("text");
		authority.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(authority, CC.xy(7, 11));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JLabel name;
	private JLabel label2;
	private JLabel creater;
	private JLabel label4;
	private JLabel time;
	private JLabel label5;
	private JLabel count;
	private JLabel label6;
	private JLabel authority;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

