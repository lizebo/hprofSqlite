package windows;

import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;

import javax.swing.JTextField;
import javax.swing.JEditorPane;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;

import com.badoo.hprof.library.HprofReader;
import com.whaley.hprof.SQLDataProcessor;
import com.whaley.hprof.sqlitemanager.SqliteManager;

import javax.swing.JPanel;
import javax.swing.Box;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowsApplication {

	private JFrame frame;
	private File origin;
	private LoadFileFrame loadFileFrame;
	private LoadDBFrame loadDBFrame;
	private ResultFrame resultFrame;

	private SearchFrame searchFrame;
	private CreateHprofFrame createHprofFrame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WindowsApplication window = new WindowsApplication();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WindowsApplication() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();

		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		loadFileFrame = new LoadFileFrame();
		createHprofFrame = new CreateHprofFrame();
		loadDBFrame = new LoadDBFrame();
		resultFrame = new ResultFrame();
		searchFrame = new SearchFrame();
		JFileChooser fileChooser = new JFileChooser();
		
		JButton btnNewButton = new JButton("\u8F7D\u5165\u6587\u4EF6");
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
//			    JFrame frame = new JFrame("title2");
//			    fileFrame.setVisible(true);
				loadFileFrame.setVisible(true);
			}
		});
		
//			{
//			@Override
//			public void mouseClicked(MouseEvent arg0) {
//				Frame fileFrame = new Frame();
//				fileFrame.setTitle("������");
//
//				fileFrame.setLocation(300,200);
//
//				fileFrame.setVisible(true);
//			}
//		});
		btnNewButton.setBounds(150, 69, 135, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton button = new JButton("\u8F7D\u5165\u6570\u636E\u5E93\u6587\u4EF6");
		button.setBounds(150, 135, 135, 23);
		frame.getContentPane().add(button);
		
		JButton btnNewButton_1 = new JButton("\u4E00\u952E\u751F\u6210hprof\u6587\u4EF6");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createHprofFrame.setVisible(true);
			}
		});
		btnNewButton_1.setBounds(150, 102, 135, 23);
		frame.getContentPane().add(btnNewButton_1);
		
		JButton button_1 = new JButton("\u6700\u5927\u5185\u5B58\u5360\u7528");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				resultFrame
				if (loadDBFrame.needRefresh||loadFileFrame.needRefresh) {
					resultFrame.clearData();
				}
				resultFrame.setVisible(true);
				loadDBFrame.needRefresh = false;
				loadFileFrame.needRefresh = false;
			}
		});
		button_1.setEnabled(false);
		button_1.setBounds(293, 208, 105, 23);
		frame.getContentPane().add(button_1);
		
		JButton btnNewButton_2 = new JButton("\u67E5\u627E\u5F15\u7528\u94FE");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				searchFrame.setVisible(true);
			}
		});
		btnNewButton_2.setEnabled(false);
		btnNewButton_2.setBounds(305, 179, 93, 23);
		frame.getContentPane().add(btnNewButton_2);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				loadDBFrame.setVisible(true);
			}
		});
		frame.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				if (SqliteManager.getInstance().hasConnect()) {
					button_1.setEnabled(true);
				}else {
					button_1.setEnabled(false);
				}
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent arg0) {
				if (SqliteManager.getInstance().hasConnect()) {
					button_1.setEnabled(true);
					btnNewButton_2.setEnabled(true);
				}else {
					button_1.setEnabled(false);
					btnNewButton_2.setEnabled(false);
				}
			}
		});
	}
	
}
