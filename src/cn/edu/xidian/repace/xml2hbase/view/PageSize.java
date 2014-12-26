package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import cn.edu.xidian.repace.xml2hbase.model.Global;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Thu May 09 15:29:59 CST 2013
 */



/**
 * @author Legend
 */
public class PageSize extends JDialog implements ActionListener {
	public PageSize(Frame owner) {
		super(owner,true);
		initComponents();
		this.setVisible(true);
	}

	public PageSize(Dialog owner) {
		super(owner,true);
		initComponents();
		this.setVisible(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		lpage = new JLabel();
		pageSize = new JTextField();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

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
					"default:grow, $lcgap, 53dlu, $lcgap, 63dlu, $lcgap, default:grow",
					"default:grow, $lgap, default, $lgap, default:grow"));

				//---- lpage ----
				lpage.setText("页面大小");
				pageSize.setText(Global.step+"");
				contentPanel.add(lpage, CC.xy(3, 3));
				contentPanel.add(pageSize, CC.xy(5, 3));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout(
					"$glue, $button, $rgap, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("确定");
				okButton.addActionListener(this);
				buttonBar.add(okButton, CC.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("取消");
				cancelButton.addActionListener(this);
				buttonBar.add(cancelButton, CC.xy(4, 1));
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
	private JLabel lpage;
	private JTextField pageSize;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==okButton){
			String num=pageSize.getText();			
			try {
				int n=Integer.parseInt(num);
				Global.step=n;
			}catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "输入非法！");
			}
		}
		
		this.dispose();
	}
}

