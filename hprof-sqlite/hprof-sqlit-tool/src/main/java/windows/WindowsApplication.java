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

public class WindowsApplication {

	private JFrame frame;
	private File origin;

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
		
		JFileChooser fileChooser = new JFileChooser();
		
		JButton btnNewButton = new JButton("\u8F7D\u5165\u6587\u4EF6");
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
//			    JFrame frame = new JFrame("title2");
//			    fileFrame.setVisible(true);
				fileChooser.showOpenDialog(frame);
				origin = fileChooser.getSelectedFile();
			}
		});
		
//			{
//			@Override
//			public void mouseClicked(MouseEvent arg0) {
//				Frame fileFrame = new Frame();
//				fileFrame.setTitle("Ö÷´°Ìå");
//
//				fileFrame.setLocation(300,200);
//
//				fileFrame.setVisible(true);
//			}
//		});
		btnNewButton.setBounds(172, 84, 93, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton button = new JButton("\u89E3\u6790");
		button.setBounds(172, 146, 93, 23);
		frame.getContentPane().add(button);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				synchronized (this) {
					if (origin!=null) {
			            InputStream in;
						try {
							in = new BufferedInputStream(new FileInputStream(origin));
				            SQLDataProcessor processor = new SQLDataProcessor();
				            HprofReader reader = new HprofReader(in, processor);
				            while (reader.hasNext()) {
				                reader.next();
				            }
				            SqliteManager.getInstance().initClassLength();
				            SqliteManager.getInstance().getTotalSize();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}
				}

			}
		});
	}
}
