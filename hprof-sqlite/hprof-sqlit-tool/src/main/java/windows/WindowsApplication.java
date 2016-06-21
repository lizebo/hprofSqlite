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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
		loadFileFrame = new LoadFileFrame();
		createHprofFrame = new CreateHprofFrame();
		loadDBFrame = new LoadDBFrame();
		resultFrame = new ResultFrame();
		searchFrame = new SearchFrame();
		JFileChooser fileChooser = new JFileChooser();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {150, 150, 150};
		gridBagLayout.rowHeights = new int[]{69, 23, 23, 23, 23, 23, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
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
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		frame.getContentPane().add(btnNewButton, gbc_btnNewButton);
		
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
		
		JButton btnNewButton_2 = new JButton("\u67E5\u627E\u5F15\u7528\u94FE");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				searchFrame.setVisible(true);
			}
		});
		
		JButton btnNewButton_1 = new JButton("\u4E00\u952E\u751F\u6210hprof\u6587\u4EF6");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createHprofFrame.setVisible(true);
			}
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 2;
		frame.getContentPane().add(btnNewButton_1, gbc_btnNewButton_1);
		
		JButton button = new JButton("\u8F7D\u5165\u6570\u636E\u5E93\u6587\u4EF6");
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.NORTH;
		gbc_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 1;
		gbc_button.gridy = 3;
		frame.getContentPane().add(button, gbc_button);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				loadDBFrame.setVisible(true);
			}
		});
		btnNewButton_2.setEnabled(false);
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_2.gridx = 2;
		gbc_btnNewButton_2.gridy = 4;
		frame.getContentPane().add(btnNewButton_2, gbc_btnNewButton_2);
		button_1.setEnabled(false);
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_button_1.gridx = 2;
		gbc_button_1.gridy = 5;
		frame.getContentPane().add(button_1, gbc_button_1);
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
