package windows;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;

import com.whaley.hprof.CmdProceManager;

public class CreateHprofFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtip;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField txthprof;
	private JTextField textField_4;
	
	
	private String ip;
	private String packageName;
	private String sdkPath;
	private String hprofPath;
	
	final String toolsPath = "\\";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CreateHprofFrame frame = new CreateHprofFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CreateHprofFrame() {
		setBounds(100, 100, 450, 300);
		init();

	}
	private void init(){
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtip = new JTextField();
		txtip.setText("\u8BBE\u5907IP\uFF1A");
		txtip.setEditable(false);
		txtip.setBounds(78, 54, 66, 21);
		contentPane.add(txtip);
		txtip.setColumns(10);
		
		textField = new JTextField();
		textField.setBounds(170, 54, 108, 21);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setText("\u5E94\u7528\u5305\u540D\uFF1A");
		textField_1.setEditable(false);
		textField_1.setBounds(78, 102, 66, 21);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setBounds(170, 102, 108, 21);
		contentPane.add(textField_2);
		textField_2.setColumns(10);
		
		txthprof = new JTextField();
		txthprof.setText("\u751F\u6210hprof\u6587\u4EF6\u4F4D\u7F6E\uFF1A");
		txthprof.setEditable(false);
		txthprof.setBounds(22, 155, 122, 21);
		contentPane.add(txthprof);
		txthprof.setColumns(10);
		
		textField_4 = new JTextField();
		textField_4.setBounds(170, 155, 108, 21);
		contentPane.add(textField_4);
		textField_4.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("\u2026");
		btnNewButton_1.setBounds(288, 155, 37, 23);
		contentPane.add(btnNewButton_1);
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		btnNewButton_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				fileChooser.showOpenDialog(CreateHprofFrame.this);
				hprofPath = fileChooser.getSelectedFile().getAbsolutePath();
				textField_4.setText(hprofPath);
				System.out.print(hprofPath);
			}
		});
		
		JButton btnNewButton_2 = new JButton("\u786E\u5B9A");
		btnNewButton_2.setBounds(170, 196, 93, 23);
		contentPane.add(btnNewButton_2);
		btnNewButton_2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				ip = textField.getText();
				packageName = textField_2.getText();
				hprofPath = textField_4.getText();
				if (ip!=null&&packageName!=null&&hprofPath!=null) {
					CmdProceManager.createHprof(ip, packageName, hprofPath);
				}
				setVisible(false);
			}
		});
	}
}
