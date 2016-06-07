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

import javax.swing.JTree;

public class ResultFrame extends JFrame {

	private JPanel contentPane;
	ArrayList<InstanceTraceItem> datas;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (int i = 0; i < datas.size(); i++) {
			JTree tree = new JTree(datas.get(i));
			panel.add(tree);
		}
		contentPane.add(panel, BorderLayout.CENTER);
		
	}

}
