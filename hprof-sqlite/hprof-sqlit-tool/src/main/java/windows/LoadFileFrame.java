package windows;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
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
	
	private File soureFile;
	
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
				soureFile = null;
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
		FileNameExtensionFilter fiter = new FileNameExtensionFilter("hprof文件", "hprof");
		fileChooser.setFileFilter(fiter);
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
		
		JButton btnNewButton_1 = new JButton("\u89E3\u6790");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				String dbName = textField_3.getText();				
				if(soureFile!=null&&soureFile.exists()&&soureFile.length()!=0){
					JFileChooser saveChooser = new JFileChooser();
					saveChooser.addChoosableFileFilter(new FileFilter() {						
						@Override
						public String getDescription() {
							// TODO Auto-generated method stub
							return "*.db";
						}
						
						@Override
						public boolean accept(File f) {
							// TODO Auto-generated method stub
			                if (f.isDirectory()) {//如果是目录就可以访问
			                    return true;
			                }
			                if (f.getName().endsWith(".db")) {//如果是,txt文件格式的文件,那么就可以显示出来
			                    return true;
			                }
			                return false;
						}
					});
					saveChooser.showSaveDialog(null);
					String dbName = saveChooser.getSelectedFile().getAbsolutePath();
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
						soureFile = null;
			            setVisible(false);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}else{
					JOptionPane.showMessageDialog(contentPane, "没有选择dump文件","错误提示",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton_1.setBounds(165, 136, 93, 23);
		contentPane.add(btnNewButton_1);
		
		JFileChooser fileChooser1 = new JFileChooser();
		fileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
}
