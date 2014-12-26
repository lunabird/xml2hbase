package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Wed Apr 03 09:57:42 CST 2013
 */



/**
 * @author Legend
 */
public class AddUser extends JDialog implements ActionListener {
	public AddUser(Frame owner) {
		super(owner);
		initComponents();
		//this.setVisible(true);
		//this.setModal(true);
	}

	public AddUser(Dialog owner) {
		super(owner);
		initComponents();
		//this.setVisible(true);
		//this.setModal(true);
	}
	
	public String getName(){
		return textField1.getText();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		buttonBar = new JPanel();
		okButton = new JButton();
		okButton.addActionListener(this);
		cancelButton = new JButton();
		cancelButton.addActionListener(this);

		//======== this ========
		setTitle("添加用户");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.BUTTON_BAR_PAD);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default:grow, 2*($lcgap, default), $lcgap, 23dlu, $lcgap, default:grow",
					"default:grow, $lgap, default, $lgap, default:grow"));

				//---- label1 ----
				label1.setText("用户名");
				contentPanel.add(label1, CC.xy(3, 3));
				contentPanel.add(textField1, CC.xywh(5, 3, 3, 1));
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
				buttonBar.add(okButton, CC.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("取消");
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
	public boolean isOK=false;
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField textField1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource()==okButton){
			isOK=true;
		}
		this.setVisible(false);
	}
	public void exit(){
		this.dispose();
	}
}
