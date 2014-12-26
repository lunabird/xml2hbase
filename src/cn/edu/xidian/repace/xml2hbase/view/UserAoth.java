package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.*;
import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Fri Apr 26 12:59:14 CST 2013
 */



/**
 * @author Legend
 */
public class UserAoth extends JPanel {
	String tableName;
	int auth;
	public UserAoth(String tableName,int auth) {
		this.tableName=tableName;
		this.auth=auth;
		initComponents();
	}
	
	public int getValue(){
		return comboBox1.getSelectedIndex();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		comboBox1.addItem("-");
		comboBox1.addItem("R");
		comboBox1.addItem("RW");
		comboBox1.setSelectedIndex(auth);

		//======== this ========
		setLayout(new FormLayout(
				"default:grow, $lcgap, 70dlu:grow, $lcgap, 20dlu, $lcgap, 40dlu, $lcgap, default:grow",
				"$lcgap, default, $lcgap"));

		//---- label1 ----
		label1.setText(tableName);
		label1.setHorizontalAlignment(SwingConstants.LEFT);
		label1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 18));
		add(label1, CC.xy(3, 2));
		add(comboBox1, CC.xy(7, 2));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	 JLabel label1;
	 JComboBox comboBox1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

