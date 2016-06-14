package windows;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;

import com.badoo.hprof.library.HprofReader;
import com.whaley.hprof.SQLDataProcessor;
import com.whaley.hprof.sqlitemanager.SqliteManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoadFileFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	
	private File soureFile;
	private JButton btnNewButton_2;
	
	public boolean needRefresh = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoadFileFrame frame = new LoadFileFrame();
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
	public LoadFileFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				textField_1.setText("");
				textField_3.setText("");
			}
		});
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setText("\u6587\u4EF6\u5730\u5740");
		textField.setEditable(false);
		textField.setBounds(74, 83, 66, 21);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(167, 83, 132, 21);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		JFileChooser fileChooser = new JFileChooser();
		JButton btnNewButton = new JButton("\u2026");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser.showOpenDialog(LoadFileFrame.this);
				soureFile = fileChooser.getSelectedFile();
				textField_1.setText(soureFile.getAbsolutePath());
			}
		});
		btnNewButton.setBounds(320, 82, 32, 23);
		contentPane.add(btnNewButton);
		
		
		textField_2 = new JTextField();
		textField_2.setEditable(false);
		textField_2.setText("\u6570\u636E\u5E93\u4F4D\u7F6E");
		textField_2.setBounds(74, 137, 66, 21);
		contentPane.add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setBounds(167, 137, 132, 21);
		contentPane.add(textField_3);
		textField_3.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("\u89E3\u6790");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dbName = textField_3.getText();

				if(soureFile!=null&&dbName!=null){
					SqliteManager.getInstance().createTables(dbName);
		            InputStream in;
					try {
						in = new BufferedInputStream(new FileInputStream(soureFile));
			            SQLDataProcessor processor = new SQLDataProcessor();
			            HprofReader reader = new HprofReader(in, processor);
			            while (reader.hasNext()) {
			                reader.next();
			            }
			            SqliteManager.getInstance().initClassLength();
			            SqliteManager.getInstance().getTotalSize();
			            SqliteManager.getInstance().commit();
			            needRefresh = true;
						textField_1.setText("");
						textField_3.setText("");
			            setVisible(false);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

			}
		});
		btnNewButton_1.setBounds(161, 191, 93, 23);
		contentPane.add(btnNewButton_1);
		
		JFileChooser fileChooser1 = new JFileChooser();
		fileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		btnNewButton_2 = new JButton("\u2026");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser1.showOpenDialog(LoadFileFrame.this);
				textField_3.setText(fileChooser1.getSelectedFile().getAbsolutePath()+"\\hprof");
			}
		});
		btnNewButton_2.setBounds(320, 136, 32, 23);
		contentPane.add(btnNewButton_2);
	}
	
}
