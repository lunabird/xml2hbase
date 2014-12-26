package cn.edu.xidian.repace.xml2hbase.view;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.apache.hadoop.hbase.HTableDescriptor;

import cn.edu.xidian.repace.xml2hbase.MappingTableManager;
import cn.edu.xidian.repace.xml2hbase.VirtualTable;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseInsert;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Sun Apr 14 22:10:56 CST 2013
 */



/**
 * @author Legend
 */
public class DeleteTable extends JDialog implements ActionListener {
	String tableName;
	Hbase hbase;
	public DeleteTable(Hbase hbase,String tableName) {
		super(hbase,true);
		this.hbase=hbase;
		this.tableName=tableName;
		initComponents();
		this.setVisible(true);
		//this.setModal(true);
		//this.okButton.setEnabled(false);
		//deleteTable();
	}

	
	public DeleteTable(Dialog owner) {
		super(owner);
		initComponents();
	}
	
	public boolean deleteTable(){
		this.label1.setText("正在删除");
		this.button1.setEnabled(false);
		this.okButton.setEnabled(false);
		//this.
		boolean success=false;
		try {
			success= HbaseData.deleteDatabase(tableName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			label1.setText("未能删除 "+tableName);
			button1.setEnabled(true);
			e.printStackTrace();
		}
		label1.setText("删除成功。");
		button1.setEnabled(true);
		return success;
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		buttonBar = new JPanel();
		okButton = new JButton();
		button1=new JButton();
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
				label1.setText("Are you sure to delete "+tableName);
				contentPanel.add(label1, CC.xy(3, 3));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout(
					"default, $glue, $button, $lcgap, $button",
					"pref"));

				//---- button1 ----
				button1.setText("确定");
				button1.addActionListener(this);
				buttonBar.add(button1, CC.xywh(3, 1, 2, 1));

				//---- okButton ----
				okButton.setText("取消");
				okButton.addActionListener(this);
				buttonBar.add(okButton, CC.xy(5, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
			//dialogPane.add(buttonBar, BorderLayout.SOUTH);
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
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==button1){
			if(okButton.isEnabled()){
				if(deleteTable()){
					this.hbase.deleteFromTree();
				}
			}else{
				this.dispose();
			}
		}else{
			this.dispose();
		}
	}
}
