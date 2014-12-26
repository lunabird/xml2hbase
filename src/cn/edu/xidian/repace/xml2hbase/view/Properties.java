package cn.edu.xidian.repace.xml2hbase.view;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.hadoop.hbase.HTableDescriptor;

import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Thu Mar 28 11:29:46 CST 2013
 */



/**
 * @author Legend
 */
public class Properties extends JPanel {
	public Properties(String tableName) {
		initComponents(tableName);
		//table1.s
	}

	private void initComponents(String tableName) {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		separator1 = compFactory.createSeparator("P2C");
		scrollPane2 = new JScrollPane();
		table2 = new JTable();
		separator2 = compFactory.createSeparator("C2V");
		scrollPane1 = new JScrollPane();
		table1 = new JTable();

		//======== this ========
		setLayout(new FormLayout(
				"default:grow, $lcgap, default:grow",
				"4*(default, $lgap), default:grow, $lgap, default"));
		add(separator1, CC.xywh(1, 3, 3, 1));

		
		HTableDescriptor p2c=HbaseData.getP2CByName(tableName);
		//======== scrollPane2 ========
		{
			table2.setModel(new DefaultTableModel(
					new Object[][] {
						{"Table Name", p2c.getNameAsString()},
						{"ReadOnly", p2c.isReadOnly()},
						{"Families", p2c.getColumnFamilies()[0].getNameAsString()},
						{"Memery flush size", p2c.getMemStoreFlushSize()},
						{"Max File Size", p2c.getMaxFileSize()},
					},
					new String[] {
						"Property Name", "Value"
					}
				));
			scrollPane2.setViewportView(table2);
		}
		add(scrollPane2, CC.xywh(1, 5, 3, 1));
		add(separator2, CC.xywh(1, 7, 3, 1));

		
		HTableDescriptor c2v=HbaseData.getC2VByName(tableName);
		//======== scrollPane1 ========
		{

			//---- table1 ----
			table1.setModel(new DefaultTableModel(
				new Object[][] {
					{"Table Name", c2v.getNameAsString()},
					{"ReadOnly", c2v.isReadOnly()},
					{"Families", c2v.getColumnFamilies()[0].getNameAsString()},
					{"Memery flush size", c2v.getMemStoreFlushSize()},
					{"Max File Size", c2v.getMaxFileSize()},
				},
				new String[] {
					"Property Name", "Value"
				}
			));
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, CC.xywh(1, 9, 3, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JComponent separator1;
	private JScrollPane scrollPane2;
	private JTable table2;
	private JComponent separator2;
	private JScrollPane scrollPane1;
	private JTable table1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

