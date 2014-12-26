package cn.edu.xidian.repace.xml2hbase.view;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.MappingTableManager;
import cn.edu.xidian.repace.xml2hbase.VirtualTable;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseInsert;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;
import cn.edu.xidian.repace.xml2hbase.model.PagePointThread;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Sun Apr 14 22:10:56 CST 2013
 */



/**
 * @author Legend
 */
public class ImportFolder extends JDialog implements ActionListener {
	String tableName;
	File file;
	int num;
	public ImportFolder(Frame owner,String tableName,File file) {
		super(owner,true);
		this.tableName=tableName;
		this.file=file;
		initComponents();
		this.setVisible(true);
		//this.okButton.setEnabled(false);
		//importFolder();
		//this.okButton.setEnabled(true);
	}

	
	public ImportFolder(Dialog owner) {
		super(owner);
		initComponents();
	}
	
	public void importTest(){
		this.okButton.setText("OK");
		this.okButton.setEnabled(false);
		for(int i=0;i<1000;i++){
			this.label1.setText("uploading "+i+" test");
			this.update(getGraphics());
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.label1.setText("Upload success");
		this.okButton.setEnabled(true);
	}
	
	public void importFolder(){
		this.okButton.setText("确定");
		this.okButton.setEnabled(false);
		MappingTableManager MTmanager = new MappingTableManager();		
		Map<String, String> code2Value= null;
		Map<String, List<String>> path2Code = new HashMap<String, List<String>>();
		HTableDescriptor p2c=HbaseData.getP2CByName(tableName);
		String family=p2c.getColumnFamilies()[0].getNameAsString();
		String P2CtableName ="P2C-"+tableName;
		String C2VtableName = "C2V-"+tableName;
		String C2ProwName = "";
		VirtualTable vt = new VirtualTable(P2CtableName);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			num=files.length;
	        for (int i = 0; i < num; i++) {
	        	String path = files[i].getPath();//article path 
	        	this.label1.setText("正在上传   "+path);
	        	this.update(getGraphics());
	        	C2ProwName =  files[i].getName();//article name as the C2VrowName
	        	code2Value= new HashMap<String, String>();
	        	MTmanager.createMappingTable(path,C2ProwName,vt,path2Code,code2Value);	        	
	        	vt.flushCurrentNumber();
	        	//insert data
				HbaseInsert.addRecord(C2VtableName, C2ProwName, family, code2Value);
				code2Value = null;	
			

	       }
	         HbaseInsert.addRecord(P2CtableName,path2Code, family);
	        
		}
		
		
		Get get2 = new Get(Bytes.toBytes(tableName));
		get2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("count"));
		HTable ht;
		try {
			ht = new HTable(HbaseConf.conf,"tables");
			Result r=ht.get(get2);
			int s=Bytes.toInt(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("count")));
			s+=num;
			Put put = new Put(Bytes.toBytes(tableName));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(s));
			ht.put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(num>5000){
			new PagePointThread(tableName).start();
		}
		
		
		this.label1.setText("上传成功！");
		this.okButton.setEnabled(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		buttonBar = new JPanel();
		okButton = new JButton();
		
		okButton.addActionListener(this);

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.BUTTON_BAR_PAD);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default, $lcgap, default:grow, $lcgap, default",
					"default:grow, $lgap, default, $lgap, default:grow"));

				//---- label1 ----
				label1.setText(this.file.getPath());
				contentPanel.add(label1, CC.xy(3, 3));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout(
					"$glue, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("上传");
				buttonBar.add(okButton, CC.xy(2, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//this.dispose();
		if(((JButton)e.getSource()).getText().equals("上传")){
			this.importFolder();
		}else{
			this.dispose();
		}
		
	}
}
