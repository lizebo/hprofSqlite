package com.whaley.windows;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.JScrollPane;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import com.sun.media.sound.ModelAbstractChannelMixer;
import com.whaley.hprof.sqlitemanager.SqliteManager;
import com.whaley.hprof.sqlitemanager.Utils;
import com.whaley.hprof.sqlitemanager.model.InstanceTraceItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.ListSelectionModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SearchFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private ArrayList<String> classList;
	private JTextField textField_1;
	private Hashtable<Integer, InstanceTraceItem> data;

	// private JFileChooser mChooser;

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
		data = new Hashtable<Integer, InstanceTraceItem>();

		setBounds(100, 100, 504, 427);
		// mChooser = new JFileChooser();
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 165, 103, 34, 93 };
		gbl_contentPane.rowHeights = new int[] { 23, 26, 288 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		contentPane.setLayout(gbl_contentPane);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 1.0;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridwidth = 3;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		contentPane.add(textField, gbc_textField);
		textField.setColumns(10);

		JButton btnNewButton = new JButton("\u67E5\u627E");

		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 0;
		contentPane.add(btnNewButton, gbc_btnNewButton);

		textField_1 = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.weightx = 1.0;
		gbc_textField_1.anchor = GridBagConstraints.NORTH;
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.gridwidth = 2;
		gbc_textField_1.gridx = 0;
		gbc_textField_1.gridy = 1;
		contentPane.add(textField_1, gbc_textField_1);
		textField_1.setColumns(10);

		JButton btnNewButton_1 = new JButton("\u2026");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser mChooser = new JFileChooser();
				mChooser.showOpenDialog(contentPane);
				textField_1.setText(mChooser.getSelectedFile()
						.getAbsolutePath());
			}
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.SOUTH;
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 1;
		contentPane.add(btnNewButton_1, gbc_btnNewButton_1);

		JButton button = new JButton("\u5206\u6790");

		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.SOUTH;
		gbc_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_button.insets = new Insets(0, 0, 5, 0);
		gbc_button.gridx = 3;
		gbc_button.gridy = 1;
		contentPane.add(button, gbc_button);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		contentPane.add(scrollPane, gbc_scrollPane);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.weighty = 1.0;
		gbc_scrollPane_1.weightx = 1.0;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 2;
		contentPane.add(scrollPane_1, gbc_scrollPane_1);
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
							int length;
							try {
								length = SqliteManager.getInstance()
										.findLengthById(id,
												new ArrayList<Integer>());
								InstanceTraceItem item = SqliteManager
										.getInstance().getInstanceTraceItem(id);
								JTree tree = new JTree(item);
								scrollPane_1.setViewportView(tree);
								scrollPane_1.invalidate();
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

						}
					}
				});
			}
		});
		button.addActionListener(new ActionListener() {
			private JTree tree;

			public void actionPerformed(ActionEvent arg0) {
				String path = textField_1.getText().trim();
				File file = new File(path);
				data.clear();
				try {
					InputStream in = new BufferedInputStream(
							new FileInputStream(file));
					classList = Utils.convertStreamToString(in);
					if (classList != null && classList.size() > 0) {
						DefaultMutableTreeNode root = new DefaultMutableTreeNode(
								"classs", true);
						Iterator<String> iterator = classList.iterator();
						while (iterator.hasNext()) {
							String className = iterator.next();
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(
									className, true);
							ArrayList<Integer> instances = (ArrayList<Integer>) SqliteManager
									.getInstance().getInstanceForClass(
											className);
							Iterator<Integer> iterator2 = instances.iterator();
							while (iterator2.hasNext()) {
								int id = iterator2.next();
								// int length;
								// try {
								// length = SqliteManager.getInstance()
								// .findLengthById(id,
								// new ArrayList<Integer>());
								// InstanceTraceItem item = SqliteManager
								// .getInstance()
								// .getInstanceTraceItem(id,
								// new ArrayList<Integer>(),
								// length);
								// if (item != null) {
								DefaultMutableTreeNode idNode = new DefaultMutableTreeNode(
										id);
								node.add(idNode);
								// data.put(id, item);
								// System.out.print(id + "\n");
								// }
								//
								// } catch (SQLException e1) {
								// // TODO Auto-generated catch block
								// e1.printStackTrace();
								// }

							}
							root.add(node);
						}
						tree = new JTree(root);
						scrollPane.setViewportView(tree);
						tree.addTreeSelectionListener(new TreeSelectionListener() {

							private JTree rootTree;

							@Override
							public void valueChanged(TreeSelectionEvent e) {
								synchronized (this) {
									// TODO Auto-generated method stub
									DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
											.getLastSelectedPathComponent();

									if (node == null)
										return;
									Object object = node.getUserObject();
									if (node.isLeaf()) {
										// System.out.print("valuechange");
										if (object instanceof Integer) {
											System.out.print("Integer" + "\n");
											int id = (int) object;
											int length;
											try {
												length = SqliteManager
														.getInstance()
														.findLengthById(
																id,
																new ArrayList<Integer>());
//												InstanceTraceItem item = SqliteManager
//														.getInstance()
//														.getInstanceTraceItem(
//																id,
//																new ArrayList<Integer>(),
//																length);
												InstanceTraceItem item = SqliteManager
														.getInstance()
														.getInstanceTraceItem(
																id);
												// InstanceTraceItem item = data
												// .get(id);
												if (item != null) {
													rootTree = new JTree(item);
													scrollPane_1
															.setViewportView(rootTree);
													scrollPane_1.invalidate();
													scrollPane_1.setVisible(true);
												}else {
													scrollPane_1.setVisible(false);
												}

											} catch (SQLException e1) {
												// TODO Auto-generated catch
												// block
												e1.printStackTrace();
											}
										} else {
											System.out.print(object);
										}
									}
								}
							}
						});
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
