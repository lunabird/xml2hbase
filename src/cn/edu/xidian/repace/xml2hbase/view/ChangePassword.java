package cn.edu.xidian.repace.xml2hbase.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

import cn.edu.xidian.repace.xml2hbase.model.HbaseData;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Thu May 09 13:53:17 CST 2013
 */



/**
 * @author Legend
 */
public class ChangePassword extends JDialog implements ActionListener {
	public ChangePassword(Frame owner) {
		super(owner,true);
		initComponents();
		this.setVisible(true);
	}

	public ChangePassword(Dialog owner) {
		super(owner,true);
		initComponents();
		this.setVisible(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		lold = new JLabel();
		passwordOld = new JPasswordField();
		lnew1 = new JLabel();
		passwordNew1 = new JPasswordField();
		lnew2 = new JLabel();
		passwordNew2 = new JPasswordField();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("修改密码");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.BUTTON_BAR_PAD);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default:grow, $lcgap, 60dlu, $lcgap, 65dlu, $lcgap, default:grow",
					"default:grow, 3*($lgap, default), $lgap, default:grow"));

				//---- old ----
				lold.setText("原密码");
				contentPanel.add(lold, CC.xy(3, 3));
				contentPanel.add(passwordOld, CC.xy(5, 3));

				//---- new1 ----
				lnew1.setText("新密码");
				contentPanel.add(lnew1, CC.xy(3, 5));
				contentPanel.add(passwordNew1, CC.xy(5, 5));

				//---- new2 ----
				lnew2.setText("确认密码");
				contentPanel.add(lnew2, CC.xy(3, 7));
				contentPanel.add(passwordNew2, CC.xy(5, 7));
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
	private JLabel lold;
	private JPasswordField passwordOld;
	private JLabel lnew1;
	private JPasswordField passwordNew1;
	private JLabel lnew2;
	private JPasswordField passwordNew2;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==okButton){
			String old=new String(this.passwordOld.getPassword());
			String new1=new String(this.passwordNew1.getPassword());
			String new2=new String(this.passwordNew2.getPassword());
			if(old.equals(HbaseData.user.password)){
				if(new1.equals(new2)){
					if(!new1.equals(old)){
						try {
							HbaseData.newUser(HbaseData.user.username, new1);
							HbaseData.user.password=new1;
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}
					
					JOptionPane.showMessageDialog(this, "Change password successfully");
				}else{
					JOptionPane.showMessageDialog(this, "The two passwords isn't same");
				}
			}else{
				JOptionPane.showMessageDialog(this, "Old Password is wrong");
			}
		}
		this.dispose();
	}
}
