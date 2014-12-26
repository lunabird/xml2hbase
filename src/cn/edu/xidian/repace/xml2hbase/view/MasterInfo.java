package cn.edu.xidian.repace.xml2hbase.view;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.hadoop.hbase.ServerName;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Tue Apr 23 10:10:37 CST 2013
 */



/**
 * @author Legend
 */
public class MasterInfo extends JPanel {
	ServerName server=HbaseData.cs.getMaster();
	public MasterInfo() {
		initComponents();
	}
	
	private String getDetail(){
		String result=""+server.getHostname()+":2181 ";
		//ServerName sn=HbaseData.cs.getMaster();
		for(ServerName sn:HbaseData.cs.getServers())
		result=result+sn.getHostname()+":2181 ";
		return result;
	}
	
	private String getTime(){
		long t=HbaseData.cs.getMaster().getStartcode();
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dt = new Date(t);  
		String sDateTime = sdf.format(dt); 
		return sDateTime;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();

		//======== this ========
		setLayout(new FormLayout(
			"2*(default, $lcgap), 2*(default:grow, $lcgap), default",
			"23dlu, $lgap, default:grow, $lgap, default"));

		//---- label1 ----
		label1.setText("属性");
		label1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
		add(label1, CC.xy(3, 1));

		//======== scrollPane1 ========
		{
			table1.setModel(new DefaultTableModel(
					new Object[][] {
						{"isRunning",HbaseData.isRunning },
						{"HBase Version", HbaseData.cs.getHBaseVersion()},
						{"HBase Master", HbaseData.cs.getMaster().getHostAndPort()},
						{"HBase Cluster ID", HbaseData.cs.getClusterId()},
						{"Servers size",HbaseData.cs.getServersSize()+1},
						{"Zookeeper Quorum",getDetail()},
						{"Load average",HbaseData.cs.getAverageLoad()},
						{"Root Dir",HbaseConf.getRootDir()},
						//{"Coprocessors",HbaseData.cs.getMaster().},
						{"HMaster Start Time",getTime() },
					},
					new String[] {
						"属性名", "值"
					}
				));
			
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, CC.xywh(3, 3, 5, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JScrollPane scrollPane1;
	private JTable table1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
