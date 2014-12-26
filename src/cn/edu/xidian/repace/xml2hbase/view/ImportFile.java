package cn.edu.xidian.repace.xml2hbase.view;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Sun Apr 14 22:10:56 CST 2013
 */



/**
 * @author Legend
 */
public class ImportFile extends JDialog implements ActionListener {
	
	String tableName;
	File file;
	
	public ImportFile(Frame owner,String tableName,File file) {
		super(owner);
		this.tableName=tableName;
		this.file=file;
		initComponents();
		this.okButton.setEnabled(false);
		this.setVisible(true);
		HbaseData.importFile(tableName, file);
		this.label1.setText("上传成功");
		this.okButton.setEnabled(true);
	}

	public ImportFile(Dialog owner,String tableName,File file) {
		super(owner);
		this.tableName=tableName;
		this.file=file;
		initComponents();
		this.okButton.setEnabled(false);
		this.setVisible(true);
		HbaseData.importFile(tableName, file);
		this.label1.setText("上传成功");
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
				label1.setText("正在上传  "+file.getPath());
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
				okButton.setText("确定");
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
		this.dispose();
	}
}
