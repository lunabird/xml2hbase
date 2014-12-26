package cn.edu.xidian.repace.xml2hbase.view;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Tue Apr 02 10:38:20 CST 2013
 */



/**
 * @author Legend
 */
public class NewUser extends JDialog implements ActionListener {
	public NewUser(Frame owner) {
		super(owner);
		initComponents();
	}

	public NewUser(Dialog owner) {
		super(owner);
		initComponents();
	}

	public String getName(){
		return databaseName.getText();
	}
	public String getFamily(){
		return family.getText();
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		databaseName = new JTextField();
		label2 = new JLabel();
		family = new JTextField();
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
					"default:grow, 3*($lcgap, default), $lcgap, 20dlu, $lcgap, default:grow",
					"default:grow, 2*($lgap, default), $lgap, default:grow"));

				//---- label1 ----
				label1.setText("用户名");
				contentPanel.add(label1, CC.xy(3, 3));
				contentPanel.add(databaseName, CC.xywh(7, 3, 3, 1));

				//---- label2 ----
				label2.setText("密码");
				contentPanel.add(label2, CC.xy(3, 5));
				contentPanel.add(family, CC.xywh(7, 5, 3, 1));
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
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField databaseName;
	private JLabel label2;
	private JTextField family;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	public boolean isOK=false;
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


