package windows;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;

import com.sun.org.apache.xml.internal.security.Init;
import com.whaley.hprof.sqlitemanager.InstanceTraceItem;
import com.whaley.hprof.sqlitemanager.SqliteManager;

import javax.swing.JTree;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ResultFrame extends JFrame {

	private JPanel contentPane;
	ArrayList<InstanceTraceItem> datas;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;
	private JTextField textField_8;
	private JTextField textField_9;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ResultFrame frame = new ResultFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	public ResultFrame(ArrayList<InstanceTraceItem> data) throws HeadlessException {
		super();
		this.datas = data;
		init();
	}


	/**
	 * Create the frame.
	 */
	public ResultFrame() {

		init();
	}
	
	private void init(){
		setBounds(100, 100, 670, 475);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setText("\u5360\u7528\u603B\u5185\u5B58");
		textField.setEditable(false);
		textField.setBounds(43, 10, 66, 21);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setEditable(false);
		textField_1.setBounds(126, 10, 66, 21);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setText("\u5360\u7528\u5185\u5B58\u5927\u5C0F");
		textField_2.setEditable(false);
		textField_2.setBounds(53, 168, 80, 21);
		contentPane.add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setEditable(false);
		textField_3.setBounds(143, 168, 66, 21);
		contentPane.add(textField_3);
		textField_3.setColumns(10);
		
		textField_4 = new JTextField();
		textField_4.setText("\u5360\u7528\u5185\u5B58\u5927\u5C0F");
		textField_4.setEditable(false);
		textField_4.setBounds(53, 358, 80, 21);
		contentPane.add(textField_4);
		textField_4.setColumns(10);
		
		textField_5 = new JTextField();
		textField_5.setEditable(false);
		textField_5.setBounds(143, 358, 66, 21);
		contentPane.add(textField_5);
		textField_5.setColumns(10);
		
		textField_6 = new JTextField();
		textField_6.setText("\u767E\u5206\u6BD4");
		textField_6.setEditable(false);
		textField_6.setBounds(255, 168, 66, 21);
		contentPane.add(textField_6);
		textField_6.setColumns(10);
		
		textField_7 = new JTextField();
		textField_7.setEditable(false);
		textField_7.setBounds(341, 168, 66, 21);
		contentPane.add(textField_7);
		textField_7.setColumns(10);
		
		textField_8 = new JTextField();
		textField_8.setText("\u767E\u5206\u6BD4");
		textField_8.setEditable(false);
		textField_8.setBounds(255, 358, 66, 21);
		contentPane.add(textField_8);
		textField_8.setColumns(10);
		
		textField_9 = new JTextField();
		textField_9.setBounds(341, 358, 66, 21);
		contentPane.add(textField_9);
		textField_9.setColumns(10);
		
		JButton btnNewButton = new JButton("\u67E5\u627E\u7C7B\u4FE1\u606F");
		btnNewButton.setBounds(278, 404, 93, 23);
		contentPane.add(btnNewButton);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {

				double totalSize = SqliteManager.getInstance().getTotalSize();

				textField_1.setText(totalSize+"byte");
				ArrayList<InstanceTraceItem> items = SqliteManager.getInstance().findHeapMax();
				textField_3.setText(items.get(0).getLength()+"byte");
				textField_7.setText(items.get(0).getLength()/totalSize*100 +"%");
				textField_5.setText(items.get(1).getLength()+"byte");
				textField_9.setText(items.get(1).getLength()/totalSize*100 +"%");
				JTree tree = new JTree(items.get(0));
				tree.setBounds(43, 37, 572, 121);
				contentPane.add(tree);
				
				JTree tree_1 = new JTree(items.get(1));
				tree_1.setBounds(43, 216, 572, 121);
				contentPane.add(tree_1);
			}
		});

//		tree.set
	}
}
