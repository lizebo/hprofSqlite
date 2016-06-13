package windows;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.JScrollPane;

import com.sun.media.sound.ModelAbstractChannelMixer;
import com.whaley.hprof.sqlitemanager.InstanceTraceItem;
import com.whaley.hprof.sqlitemanager.SqliteManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ListSelectionModel;

public class SearchFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchFrame frame = new SearchFrame();
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
	public SearchFrame() {
		setBounds(100, 100, 504, 427);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(42, 10, 316, 21);
		contentPane.add(textField);
		textField.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(42, 41, 165, 317);
		contentPane.add(scrollPane);
		

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(217, 41, 261, 317);
		contentPane.add(scrollPane_1);



		JButton btnNewButton = new JButton("\u67E5\u627E");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String className = textField.getText();
				ArrayList<Integer> instances = (ArrayList<Integer>) SqliteManager
						.getInstance().getInstanceForClass(className);
				// list.set
				DefaultListModel<Integer> model = new DefaultListModel<Integer>();
				for (int i = 0; i < instances.size(); i++) {
					model.addElement(instances.get(i));
				}
				JList list = new JList(model);
				scrollPane.setViewportView(list);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				list.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						// TODO Auto-generated method stub
						synchronized (this) {
							System.out.print("valuechange");
							int id = (int) list.getSelectedValue();
							int length = SqliteManager.getInstance()
									.findLengthById(id, new ArrayList<Integer>());
							InstanceTraceItem item = SqliteManager.getInstance()
									.getInstanceTraceItem(id,
											new ArrayList<Integer>(), length);
							JTree tree = new JTree(item);
							scrollPane_1.setViewportView(tree);
							scrollPane_1.invalidate();
						}
					}
				});
			}
		});
		btnNewButton.setBounds(385, 9, 93, 23);
		contentPane.add(btnNewButton);
	}
}
