package windows;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextField;
import javax.swing.JButton;

import com.whaley.hprof.sqlitemanager.SqliteManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoadDBFrame extends JFrame {

	private JPanel contentPane;
	private JTextField txtDb;
	private JTextField textField_1;
	private JButton button_1;
	public boolean needRefresh = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoadDBFrame frame = new LoadDBFrame();
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
	public LoadDBFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				textField_1.setText("");
			}
		});
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtDb = new JTextField();
		txtDb.setText("DB\u6587\u4EF6");
		txtDb.setEditable(false);
		txtDb.setBounds(80, 104, 46, 21);
		contentPane.add(txtDb);
		txtDb.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setBounds(136, 104, 162, 21);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter fiter = new FileNameExtensionFilter("数据库文件",
				"db");
		fileChooser.setFileFilter(fiter);
		JButton button = new JButton("\u2026");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(LoadDBFrame.this);
				textField_1.setText(fileChooser.getSelectedFile()
						.getAbsolutePath());
			}
		});
		button.setBounds(311, 103, 34, 23);
		contentPane.add(button);

		button_1 = new JButton("\u786E\u5B9A");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (this) {
					String dbName = fileChooser.getSelectedFile()
							.getAbsolutePath();
					if (dbName != null) {
						SqliteManager.getInstance().connect(dbName);
						needRefresh = true;
						textField_1.setText("");
						setVisible(false);

					}
				}
			}
		});
		button_1.setBounds(168, 164, 93, 23);
		contentPane.add(button_1);
	}

	@Override
	public void setDefaultCloseOperation(int arg0) {
		// TODO Auto-generated method stub
		System.out.print("close");
		super.setDefaultCloseOperation(arg0);
	}

}
