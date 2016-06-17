package windows;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
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

	private String ip;
	private String packageName;
	private String sdkPath;

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

	private void init() {
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtip = new JTextField();
		txtip.setText("\u8BBE\u5907IP\uFF1A");
		txtip.setEditable(false);
		txtip.setBounds(106, 54, 66, 21);
		contentPane.add(txtip);
		txtip.setColumns(10);

		textField = new JTextField();
		textField.setBounds(214, 54, 108, 21);
		contentPane.add(textField);
		textField.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setText("\u5E94\u7528\u5305\u540D\uFF1A");
		textField_1.setEditable(false);
		textField_1.setBounds(106, 102, 66, 21);
		contentPane.add(textField_1);
		textField_1.setColumns(10);

		textField_2 = new JTextField();
		textField_2.setBounds(214, 102, 108, 21);
		contentPane.add(textField_2);
		textField_2.setColumns(10);

		JButton btnNewButton_2 = new JButton("\u751F\u6210dump\u6587\u4EF6");
		btnNewButton_2.setBounds(151, 156, 108, 23);
		contentPane.add(btnNewButton_2);
		btnNewButton_2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				ip = textField.getText();
				packageName = textField_2.getText();
				JFileChooser saveChooser = new JFileChooser();
				saveChooser.showSaveDialog(null);
				String hprofPath = saveChooser.getSelectedFile().getAbsolutePath();
				if (ip != null && packageName != null && hprofPath != null) {
					CmdProceManager.createHprof(ip, packageName, hprofPath);
					File file = new File(hprofPath);
					if (!file.exists()||(file.exists() && file.length() == 0)) {
						file.delete();
						JOptionPane.showMessageDialog(contentPane, "生成文件失败，请重新试试","错误提示",JOptionPane.ERROR_MESSAGE);
						return;
					}
					setVisible(false);
				}else {
					JOptionPane.showMessageDialog(contentPane, "参数不足","错误提示",JOptionPane.ERROR_MESSAGE);
				}

			}
		});
	}
}
