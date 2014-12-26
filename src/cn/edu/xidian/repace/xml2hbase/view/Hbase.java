package cn.edu.xidian.repace.xml2hbase.view;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import javax.swing.JOptionPane;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.hbase.HbaseConf;
import cn.edu.xidian.repace.xml2hbase.model.Global;
import cn.edu.xidian.repace.xml2hbase.model.HbaseData;
import cn.edu.xidian.repace.xml2hbase.model.PagePointThread;
import cn.edu.xidian.repace.xml2hbase.model.PrintThread;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;


/**
 * @author Legend
 */
public class Hbase extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;  //serialVersionUID作用是序列化时保持版本的兼容性，即在版本升级时反序列化仍保持对象的唯一性。
	public Hbase() {
		try {
		      UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
		   } catch (Exception e) {}
		initComponents();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
	}
	
	public static void main(String [] arg){
		Hbase hb=new Hbase();
	}

	public void showServer(){
		tabbedPane1.removeAll();
		tabbedPane1.add("服务器信息",new MasterInfo());
	}
	
	public void connect(){
		ConnectDialog cd=new ConnectDialog(this);
		cd.setTitle("连接登陆服务器");
		cd.setSize(600, 300);
		cd.setVisible(true);
		if(cd.isOK){
			//hbaseTree.add(dataTree);
			tree1.setEnabled(true);
			menuConnect.setEnabled(false);
			buttonConnect.setEnabled(false);
			menuDisconnect.setEnabled(true);
			buttonDisconnect.setEnabled(true);
			menuCreate.setEnabled(true);
			buttonCreate.setEnabled(true);
			//buttonRefresh.setEnabled(true);
			button12.setEnabled(true);
			label3.setText("已连接");
			ArrayList<String> list=HbaseData.listTables();
			showTableList(list);
			for(int i=0;i<list.size();i++){
				this.combobox.addItem(list.get(i));
			}
			
			showServer();
			
			if(HbaseData.user.isadmin()){
				showUserList(HbaseData.userList);
			}else{
				treeModel.removeNodeFromParent(userTree);
			}
			
			tree1.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					treeMouseRealse(e);
					
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}
		System.out.println("Press Button 1");
	}
	
	private void button1MouseClicked(MouseEvent e) {
		// TODO add your code here
		connect();
		
	}
	
	private void treeMouseRealse(MouseEvent e){
		
		if(e.isPopupTrigger()){	
			JTree tree = (JTree)e.getComponent();
		     TreePath path = tree.getPathForLocation(e.getX(),e.getY());
		    //选中该节点
		     tree.setSelectionPath(path);
		     if(path.getParentPath().toString().equals("[Hbase, 数据表]")){
		    	 System.out.println(path.toString());
			     p_singleDatabase.show(tree,e.getX(),e.getY());
		     }
		     else if(path.toString().equals("[Hbase, 数据表]")){
		    	 p_databases.show(tree,e.getX(),e.getY());
		     }else if(path.toString().equals("[Hbase, 用户]")&&HbaseData.user.isadmin()){
		    	 p_users.show(tree, e.getX(), e.getY());
		     }else if(path.getParentPath().toString().equals("[Hbase, 用户]")){
		    	 p_singleUser.show(tree, e.getX(), e.getY());
		     }
		     
		}
	}
	
	public void showTableList(ArrayList<String> tableList){
		for(int i=0;i<tableList.size();i++){
			addOneTable(tableList.get(i));
		}
	}
	
	public void showUserList(ArrayList<String> userList){
		for(int i=0;i<userList.size();i++){
			if(userList.get(i).equals(HbaseData.user.username)){
				continue;
			}
			addOneUser(userList.get(i));
		}
	}
	
	public void addOneTable(String tableName){
		DefaultMutableTreeNode table=new DefaultMutableTreeNode(tableName);
		//dataTree.add(table);
		treeModel.insertNodeInto(table,dataTree,dataTree.getChildCount());
		
	}
	public void addOneUser(String userName){
		DefaultMutableTreeNode user=new DefaultMutableTreeNode(userName);
		treeModel.insertNodeInto(user,userTree,userTree.getChildCount());
		//userTree.add(user);
	}
	
	public boolean varify(String tableName){
		if(HbaseData.user.isadmin()){
			return true;
		}
		if(HbaseData.user.getValue(tableName)<=1){
			JOptionPane.showMessageDialog(this, "无权进行此操作，请联系管理员！");
			return false;
		}
		return true;
	}
	
	public void importFile(){
		TreePath path=tree1.getSelectionPath();
		String tableName=path.getLastPathComponent().toString();
		if(!varify(tableName)){
			return;
		}
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setDialogTitle("导入文件");
		
		int intRetVal = fileChooser.showOpenDialog(this); 
	    if( intRetVal == JFileChooser.APPROVE_OPTION){ 
	      //txtfile.setText(fc.getSelectedFile().getPath()); 
	    	System.out.println(tableName+fileChooser.getSelectedFile().getPath());
	    	ImportFile importf=new ImportFile((Frame)null,tableName,fileChooser.getSelectedFile());
	    	importf.setModal(true);
	    } 
	}
	
	public void importFolder(){
		TreePath path=tree1.getSelectionPath();
		String tableName=path.getLastPathComponent().toString();
		if(!varify(tableName)){
			return;
		}
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("导入文件夹");
		
		int intRetVal = fileChooser.showOpenDialog(this); 
	    if( intRetVal == JFileChooser.APPROVE_OPTION){ 
	      //txtfile.setText(fc.getSelectedFile().getPath()); 
	    	System.out.println(tableName+fileChooser.getSelectedFile().getPath());
	    	ImportFolder ifd=new ImportFolder((Frame)null,tableName,fileChooser.getSelectedFile());
	    	ifd.setModal(true);
	    	//ifd.importFolder();
	    } 
	}
	
	public void viewContents(){
		TreePath path=tree1.getSelectionPath();
		String tableName=path.getLastPathComponent().toString();
		Contents cc=new Contents(tableName);
		tabbedPane1.add("内容",cc);
		tabbedPane1.setSelectedComponent(cc);
	}
	
	public void newUser(){
		NewUser nu=new NewUser((JFrame)null);
		nu.setModal(true);
		nu.setVisible(true);
		if(nu.isOK){
			
			System.out.println("New User: Name "+nu.getName()+" Password "+nu.getFamily());
			if(HbaseData.userList.contains(nu.getName())){
				JOptionPane.showMessageDialog(this, "用户 "+nu.getName()+" 已存在。");
				nu.exit();
				return;
			}
			try {
				HbaseData.newUser(nu.getName(), nu.getFamily());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(this, "新建用户失败。");
				e.printStackTrace();
			}
			addOneUser(nu.getName());
			
		}
		nu.exit();
	}
	
	public void newDatabase(){
		NewDatabase ndb=new NewDatabase((JFrame)null);
		ndb.setModal(true);
		ndb.setVisible(true);
		if(ndb.isOK){
			
			System.out.println("New Database: Name "+ndb.getName()+" Family "+ndb.getFamily());
			try {
				if(HbaseData.isTableExists(ndb.getName())){
					JOptionPane.showMessageDialog(this, "表已存在。");
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			addOneTable(ndb.getName());
			HbaseData.newDatabase(ndb.getName(), ndb.getFamily());
			this.combobox.addItem(ndb.getName());
		}
		ndb.exit();
	}
	
	public void deleteFromTree(){
		DefaultMutableTreeNode selectionNode=(DefaultMutableTreeNode)tree1.getSelectionPath().getLastPathComponent();
		treeModel.removeNodeFromParent(selectionNode);
	}
	
	public boolean verifyDelete(String tableName){
		if(HbaseData.user.isadmin()){
			return true;
		}
		boolean ok=false;
		try {
			HTable ht=new HTable(HbaseConf.conf,"tables");
			Get get=new Get(Bytes.toBytes(tableName));
			get.addColumn( Bytes.toBytes("info"),  Bytes.toBytes("creater"));
			Result r=ht.get(get);
			if(r!=null){
				String c=Bytes.toString(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("creater")));
				if(c.equals(HbaseData.user.username)){
					ok=true;
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return ok;
	}
	
	public void deleteDatabase(){
		TreePath path=tree1.getSelectionPath();
		String tableName=path.getLastPathComponent().toString();
		if(!varify(tableName)){
			return;
		}
		
		if(!verifyDelete(tableName)){
			JOptionPane.showMessageDialog(this, "无权进行该操作，请联系管理员！");
			return;
		}
		
		
		
		int res=JOptionPane.showConfirmDialog(null,"确定要删除  "+tableName+" 表吗?","删除确认",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(res==JOptionPane.YES_OPTION){
			boolean success=false;
			try {
				success= HbaseData.deleteDatabase(tableName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(this, "未能删除  "+tableName);
				e.printStackTrace();
			}
			if(success){
				JOptionPane.showMessageDialog(this, "成功删除  "+tableName);
				deleteFromTree();
				this.combobox.removeItem(tableName);
			}else{
				JOptionPane.showMessageDialog(this, "未能删除  "+tableName);
			}
			
			
		}
	}
	
	public void exit(){
		int res=JOptionPane.showConfirmDialog(null,"确定退出吗?","退出确认",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(res==JOptionPane.YES_OPTION){
			System.exit(0);
		}
	}

	private Hbase getSelf(){
		return this;
	}
	
	public void appendResult(String str){
		textAreaResult.append(str+"\n");
		textAreaResult.selectAll();
	}
	
	public void clearResult(){
		textAreaResult.setText("");
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		
		
		this.setTitle("海量小型XML文档存储检索系统");
		
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)  
            {  
				exit();
            } 
		});
		
		menuBar1 = new JMenuBar();
		menu1 = new JMenu();
		menuConnect = new JMenuItem();
		menuDisconnect = new JMenuItem();
		menuCreate = new JMenuItem();
		menuDelete = new JMenuItem();
		menuExit = new JMenuItem();
		menu2 = new JMenu();
		menuItem6 = new JMenuItem();
		menuItem7 = new JMenuItem();
		menu3 = new JMenu();
		menuItem8 = new JMenuItem();
		toolBar1 = new JToolBar();
		buttonConnect = new JButton();
		buttonDisconnect = new JButton();
		buttonRefresh = new JButton();
		buttonCreate = new JButton();
		buttonDelete = new JButton();
		buttonImportFile = new JButton();
		buttonImportFolder = new JButton();
		buttonQuery = new JButton();
		splitPane1 = new JSplitPane();
		splitPane2 = new JSplitPane();
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		tabbedPane1 = new JTabbedPane();
		tabbedPane2 = new JTabbedPane();
		panel1 = new JPanel();
		toolBar2 = new JToolBar();
		button9 = new JButton();
		button10 = new JButton();
		toolBar3 = new JToolBar();
		button11 = new JButton();
		button12 = new JButton();
		tabbedPane3 = new JTabbedPane();
		panel3 = new JPanel();
		panel4 = new JPanel();
		label4 = new JLabel();
		textField1 = new JTextField();
		scrollPane4 = new JScrollPane();
		textArea1 = new JTextArea();
		scrollPane3 = new JScrollPane();
		textPane1 = new JTextPane();
		textAreaResult=new JTextArea();
		label3 = new JLabel();
		label2 = new JLabel();
		label1 = new JLabel();
		combobox=new JComboBox();
		hbaseTree=new DefaultMutableTreeNode("Hbase");
		dataTree=new DefaultMutableTreeNode("数据表");
		userTree=new DefaultMutableTreeNode("用户");
		hbaseTree.add(dataTree);
		hbaseTree.add(userTree);
		tree1.addTreeSelectionListener(new TreeSelectionListener(){

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// TODO Auto-generated method stub
				TreePath path=tree1.getSelectionPath();
				
				if(path!=null&&path.getParentPath()!=null&&path.getParentPath().toString().equals("[Hbase, 数据表]")){
					System.out.println("Select "+path.getLastPathComponent().toString());
					String tableName=path.getLastPathComponent().toString();
					//textField1.setText(tableName);
					combobox.setSelectedItem(tableName);
					tabbedPane1.removeAll();
					//tabbedPane1.add("Properties", new Properties(tableName));
					//tabbedPane1.add("Contents", new Contents(tableName));
					tabbedPane1.add("表信息", new TableInfo(tableName));
					menuDelete.setEnabled(true);
					buttonDelete.setEnabled(true);
					buttonImportFile.setEnabled(true);
					buttonImportFolder.setEnabled(true);
					buttonQuery.setEnabled(true);
					buttonRefresh.setEnabled(true);
					//new PagePointThread(tableName).start();
				}else if(path!=null&&path.getParentPath()!=null&&path.getParentPath().toString().equals("[Hbase, 用户]")){
					tabbedPane1.removeAll();
					String userName=path.getLastPathComponent().toString();
					tabbedPane1.add("用户信息", new UserInfo(userName));
					menuDelete.setEnabled(false);
					buttonDelete.setEnabled(false);
					buttonImportFile.setEnabled(false);
					buttonImportFolder.setEnabled(false);
					buttonQuery.setEnabled(false);
					buttonRefresh.setEnabled(false);
				}else if(path!=null&&path.getParentPath()==null){
					showServer();
					menuDelete.setEnabled(false);
					buttonDelete.setEnabled(false);
					buttonImportFile.setEnabled(false);
					buttonImportFolder.setEnabled(false);
					buttonQuery.setEnabled(false);
					buttonRefresh.setEnabled(false);
				}
				//DefaultMutableTreeNode selectionNode=(DefaultMutableTreeNode)tree1.getSelectionPath().getLastPathComponent();
			}
		});
		
		
		
		m_newDatabase=new JMenuItem("新建表");
		m_newDatabase.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				newDatabase();				
			}
		
		});
		m_deleteDatabase=new JMenuItem("删除表");
		m_deleteDatabase.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				deleteDatabase();
			}
			
		});
		m_refresh=new JMenuItem("刷新");
		m_refresh.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				tabbedPane1.removeAll();
				TreePath path=tree1.getSelectionPath();
				String userName=path.getLastPathComponent().toString();
				tabbedPane1.add("表信息", new TableInfo(userName));
			}
			
		});
		m_addUser=new JMenuItem("添加用户");
		m_addUser.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				newUser();
				
			}
			
		});
		m_deleteUser=new JMenuItem("删除用户");
		m_excuteQuery=new JMenuItem("执行查询");
		m_excuteQuery.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				tabbedPane3.setSelectedIndex(0);
				textArea1.grabFocus();
			}
			
		});
		m_importFile=new JMenuItem("导入文件");
		m_importFile.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				importFile();
			}
			
		});
		m_importFolder=new JMenuItem("导入文件夹");
		m_importFolder.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				importFolder();
			}
			
		});
		m_viewContents=new JMenuItem("查看内容");
		m_viewContents.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				viewContents();
			}
			
		});
		p_singleDatabase=new JPopupMenu();
		p_singleUser=new JPopupMenu();
		p_databases=new JPopupMenu();
		p_users=new JPopupMenu();
		p_databases.add(m_newDatabase);
		p_singleDatabase.add(m_deleteDatabase);
		p_singleDatabase.add(m_excuteQuery);
		p_singleDatabase.add(m_importFile);
		p_singleDatabase.add(m_importFolder);
		p_singleDatabase.add(m_viewContents);
		p_singleDatabase.add(m_refresh);
		p_users.add(m_addUser);
		p_singleUser.add(m_deleteUser);
		
		

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"2*(default, $lcgap), default:grow, 2*($lcgap, default)",
			"2*(default, $lgap), default:grow, 2*($lgap, default)"));

		//======== menuBar1 ========
		{

			//======== menu1 ========
			{
				menu1.setText("数据表");
				menu1.setFont(menu1.getFont().deriveFont(menu1.getFont().getStyle() | Font.BOLD));

				//---- menuItem1 ----
				menuConnect.setText("连接");
				menu1.add(menuConnect);

				//---- menuItem2 ----
				menuDisconnect.setText("断开连接");
				menuDisconnect.setEnabled(false);
				menu1.add(menuDisconnect);

				//---- menuItem3 ----
				menuCreate.setText("新建表");
				menuCreate.setEnabled(false);
				menu1.add(menuCreate);
				menuCreate.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						newDatabase();
					}
					
				});

				//---- menuItem4 ----
				menuDelete.setText("删除表");
				menuDelete.setEnabled(false);
				menuDelete.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						deleteDatabase();
					}
					
				});
				menu1.add(menuDelete);
				

				//---- menuItem5 ----
				menuExit.setText("退出");
				menuExit.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						//System.exit(0);
						exit();
					}
					
				});
				menu1.add(menuExit);
			}
			menuBar1.add(menu1);

			//======== menu2 ========
			{
				menu2.setText("设置");
				menu2.setFont(menu2.getFont().deriveFont(menu2.getFont().getStyle() | Font.BOLD));

				//---- menuItem6 ----
				menuItem6.setText("页面大小");
				menuItem6.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						PageSize ps=new PageSize(getSelf());
					}
					
				});
				menu2.add(menuItem6);

				//---- menuItem7 ----
				menuItem7.setText("修改密码");
				menuItem7.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						ChangePassword cp=new ChangePassword(getSelf());
					}
					
				});
				menu2.add(menuItem7);
			}
			menuBar1.add(menu2);

			//======== menu3 ========
			{
				menu3.setText("帮助");
				menu3.setFont(menu3.getFont().deriveFont(menu3.getFont().getStyle() | Font.BOLD));

				//---- menuItem8 ----
				menuItem8.setText("关于");
				menuItem8.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						//JOptionPane.showMessageDialog(null,"海量小型XML文档存储检索系统","关于",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
						JOptionPane.showMessageDialog(null, "海量小型XML文档存储检索系统", "关于", JOptionPane.OK_OPTION);
					}
					
				});
				menu3.add(menuItem8);
			}
			menuBar1.add(menu3);
		}
		contentPane.add(menuBar1, CC.xywh(1, 1, 9, 1));

		//======== toolBar1 ========
		{

			//---- button1 ----
			buttonConnect.setIcon(new ImageIcon("images/conn.png"));
			buttonConnect.setToolTipText("连接");
			buttonConnect.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					connect();
				}
			
			});
			toolBar1.add(buttonConnect);

			//---- button2 ----
			buttonDisconnect.setIcon(new ImageIcon("images/disco.png"));
			buttonDisconnect.setToolTipText("断开连接");
			buttonDisconnect.setEnabled(false);
			buttonDisconnect.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					buttonConnect.setEnabled(true);
					buttonDisconnect.setEnabled(false);
					menuConnect.setEnabled(true);
					menuDisconnect.setEnabled(false);
					menuCreate.setEnabled(false);
					buttonCreate.setEnabled(false);
					buttonRefresh.setEnabled(false);
					buttonDelete.setEnabled(false);
					buttonImportFile.setEnabled(false);
					buttonImportFolder.setEnabled(false);
					buttonQuery.setEnabled(false);
					button12.setEnabled(false);
					label3.setText("未连接");
					tabbedPane1.removeAll();
					
					hbaseTree=new DefaultMutableTreeNode("Hbase");
					dataTree=new DefaultMutableTreeNode("数据表");
					userTree=new DefaultMutableTreeNode("用户");
					hbaseTree.add(dataTree);
					hbaseTree.add(userTree);
					treeModel=new DefaultTreeModel(hbaseTree);
					//tree1=new JTree();
					tree1.setModel(treeModel);
					tree1.setEnabled(false);
					tree1.addMouseListener(null);
					combobox.removeAllItems();
					HbaseData.discon();
					
				}
			
			});
			toolBar1.add(buttonDisconnect);
			toolBar1.addSeparator();

			//---- button3 ----
			buttonRefresh.setIcon(new ImageIcon("images/refresh.png"));
			buttonRefresh.setToolTipText("刷新");
			buttonRefresh.setEnabled(false);
			buttonRefresh.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					tabbedPane1.removeAll();
					TreePath path=tree1.getSelectionPath();
					String userName=path.getLastPathComponent().toString();
					tabbedPane1.add("表信息", new TableInfo(userName));
				}
				
			});
			toolBar1.add(buttonRefresh);
			toolBar1.addSeparator();

			//---- button4 ----
			buttonCreate.setIcon(new ImageIcon("images/new.png"));
			buttonCreate.setToolTipText("新建表");
			buttonCreate.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					newDatabase();
				}
				
			});
			buttonCreate.setEnabled(false);
			toolBar1.add(buttonCreate);

			//---- button5 ----
			buttonDelete.setIcon(new ImageIcon("images/delete.png"));
			buttonDelete.setToolTipText("删除表");
			buttonDelete.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					deleteDatabase();
				}
				
			});
			buttonDelete.setEnabled(false);
			toolBar1.add(buttonDelete);
			toolBar1.addSeparator();

			//---- button6 ----
			buttonImportFile.setIcon(new ImageIcon("images/file.png"));
			buttonImportFile.setToolTipText("导入文件");
			buttonImportFile.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					importFile();
				}
				
			});
			buttonImportFile.setEnabled(false);
			toolBar1.add(buttonImportFile);

			//---- button7 ----
			buttonImportFolder.setIcon(new ImageIcon("images/flor.png"));
			buttonImportFolder.setToolTipText("导入文件夹");
			buttonImportFolder.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					importFolder();
				}
				
			});
			buttonImportFolder.setEnabled(false);
			toolBar1.add(buttonImportFolder);
			toolBar1.addSeparator();

			//---- button8 ----
			buttonQuery.setIcon(new ImageIcon("images/query.png"));
			buttonQuery.setToolTipText("查询");
			buttonQuery.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					tabbedPane3.setSelectedIndex(0);
					textArea1.grabFocus();
				}
				
			});
			buttonQuery.setEnabled(false);
			toolBar1.add(buttonQuery);
		}
		contentPane.add(toolBar1, CC.xywh(1, 3, 9, 1));

		//======== splitPane1 ========
		{
			splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane1.setDividerLocation(280);

			//======== splitPane2 ========
			{
				splitPane2.setDividerLocation(150);

				//======== scrollPane1 ========
				{
					treeModel=new DefaultTreeModel(hbaseTree);
					tree1.setModel(treeModel);
					scrollPane1.setViewportView(tree1);
				}
				splitPane2.setLeftComponent(scrollPane1);
				splitPane2.setRightComponent(tabbedPane1);
			}
			splitPane1.setTopComponent(splitPane2);

			//======== tabbedPane2 ========
			{

				//======== panel1 ========
				{
					panel1.setLayout(new FormLayout(
						"15dlu, $lcgap, 14dlu, $lcgap, default:grow",
						"default, $lgap, default:grow, $lgap, default"));

					//======== toolBar2 ========
					{
						toolBar2.setOrientation(SwingConstants.VERTICAL);

						//---- button9 ----
						button9.setIcon(new ImageIcon("images/open.png"));
						
						
						
						toolBar2.add(button9);

						//---- button10 ----
						button10.setIcon(new ImageIcon("images/save.png"));
						button10.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								JFileChooser j2 = new JFileChooser();
								int i=j2.showSaveDialog(getSelf());
								if(i== JFileChooser.APPROVE_OPTION){
									String file=j2.getSelectedFile().toString();
									System.out.println(file);
									File f=new File(file);
									BufferedWriter output=null;
									PrintStream p=null;
									if(!f.exists())
										try {
											f.createNewFile();
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									try {
										FileOutputStream fos=new FileOutputStream(f);
										//output = new BufferedWriter(new FileWriter(f));
										p=new PrintStream(fos);
										
									} catch (FileNotFoundException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
									} catch (IOException e3) {
										// TODO Auto-generated catch block
										e3.printStackTrace();
									}
									
									p.print(textAreaResult.getText().replaceAll("\n", "\r\n"));
									p.close();
									//fos.close();
									JOptionPane.showMessageDialog(null, "保存成功。");
									
								}
							}
							
						});
						toolBar2.add(button10);
					}
					panel1.add(toolBar2, CC.xywh(1, 1, 1, 5));

					//======== toolBar3 ========
					{
						toolBar3.setOrientation(SwingConstants.VERTICAL);

						//---- button11 ----
						button11.setIcon(new ImageIcon("images/clear.png"));
						
						button11.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								textArea1.setText("");
							}
							
						});
						
						toolBar3.add(button11);

						//---- button12 ----
						button12.setIcon(new ImageIcon("images/run.png"));
						
						button12.setEnabled(false);
						
						button12.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								tabbedPane3.setSelectedIndex(1);
								try {
									
									//long start=System.currentTimeMillis();
									
									//System.out.println(textField1.getText()+" "+textArea1.getText());
									
									ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
									
									Global.isQuering=true;
									//receive
									PrintThread pt=new PrintThread(queue,getSelf());
									
									//pt.start();
									String name=(String)combobox.getSelectedItem();
									HbaseData.query(name, textArea1.getText(),queue,pt);
									
									
									
									//long stop=System.currentTimeMillis();
									
									//appendResult("\nTotal time : "+(stop-start)+" ms");
									
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
									appendResult(e1.getMessage());
								}
								//scrollPane3.grabFocus();
								
							}
							
						});
						
						toolBar3.add(button12);
					}
					panel1.add(toolBar3, CC.xywh(3, 1, 1, 5));

					//======== tabbedPane3 ========
					{

						//======== panel3 ========
						{
							panel3.setLayout(new FormLayout(
								"default, $lcgap, default:grow",
								"2*(default, $lgap), default:grow"));

							//======== panel4 ========
							{
								panel4.setLayout(new FormLayout(
									"default, 7dlu, 60dlu, 4*(default)",
									"default"));

								//---- label4 ----
								label4.setText("表");
								panel4.add(label4, CC.xy(1, 1));
								textField1.setEnabled(false);
								//panel4.add(textField1, CC.xy(3, 1));
								panel4.add(combobox, CC.xy(3, 1));
							}
							panel3.add(panel4, CC.xywh(1, 1, 3, 1));

							//======== scrollPane4 ========
							{
								scrollPane4.setViewportView(textArea1);
							}
							panel3.add(scrollPane4, CC.xywh(1, 3, 3, 3));
						}
						tabbedPane3.addTab("XQuery", panel3);


						//======== scrollPane3 ========
						{
							scrollPane3.setViewportView(textAreaResult);
						}
						tabbedPane3.addTab("结果", scrollPane3);

					}
					panel1.add(tabbedPane3, CC.xywh(5, 1, 1, 5));
				}
				tabbedPane2.addTab("查询", panel1);

			}
			splitPane1.setBottomComponent(tabbedPane2);
		}
		contentPane.add(splitPane1, CC.xywh(1, 5, 9, 3));

		//---- label3 ----
		label3.setText("未连接");
		contentPane.add(label3, CC.xy(1, 9));

		//---- label2 ----
		label2.setText("V1.0  ");
		contentPane.add(label2, CC.xy(9, 9));
		pack();
		setLocationRelativeTo(getOwner());

		//---- label1 ----
		label1.setText("text");
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JComboBox combobox;
	private JMenuItem m_newDatabase,m_deleteDatabase,m_addUser,m_deleteUser,m_excuteQuery,m_importFile,m_importFolder,m_viewContents,m_refresh;
	private JPopupMenu p_singleDatabase,p_databases,p_users,p_singleUser;
	public DefaultMutableTreeNode hbaseTree;
	public DefaultMutableTreeNode dataTree;
	public DefaultMutableTreeNode userTree;
	public DefaultTreeModel treeModel;
	private JMenuBar menuBar1;
	private JMenu menu1;
	private JMenuItem menuConnect;
	private JMenuItem menuDisconnect;
	private JMenuItem menuCreate;
	private JMenuItem menuDelete;
	private JMenuItem menuExit;
	private JMenu menu2;
	private JMenuItem menuItem6;
	private JMenuItem menuItem7;
	private JMenu menu3;
	private JMenuItem menuItem8;
	private JToolBar toolBar1;
	private JButton buttonConnect;
	private JButton buttonDisconnect;
	private JButton buttonRefresh;
	private JButton buttonCreate;
	private JButton buttonDelete;
	private JButton buttonImportFile;
	private JButton buttonImportFolder;
	private JButton buttonQuery;
	private JSplitPane splitPane1;
	private JSplitPane splitPane2;
	private JScrollPane scrollPane1;
	private JTree tree1;
	private JTabbedPane tabbedPane1;
	private JTabbedPane tabbedPane2;
	private JPanel panel1;
	private JToolBar toolBar2;
	private JButton button9;
	private JButton button10;
	private JToolBar toolBar3;
	private JButton button11;
	private JButton button12;
	private JTabbedPane tabbedPane3;
	private JPanel panel3;
	private JPanel panel4;
	private JLabel label4;
	private JTextField textField1;
	private JScrollPane scrollPane4;
	private JTextArea textArea1;
	private JScrollPane scrollPane3;
	private JTextPane textPane1;
	public JTextArea textAreaResult;
	private JLabel label3;
	private JLabel label2;
	private JLabel label1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
