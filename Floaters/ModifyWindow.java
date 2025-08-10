package Floaters;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
//import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Interfaces.IFunctions;
import launch.MainWindow;

@SuppressWarnings("serial")
public class ModifyWindow extends JFrame implements ActionListener, KeyListener
{
	
	private IFunctions master;
	private MainWindow mainWindow;
	private String originalFilePath;
//	private JLabel filePathLabel;
	private JTextField filePathLabel;
	private JTextArea searchWordsArea;
	private JButton okBtn;
	private JButton cancelBtn;
	private JButton findFileBtn;
	private String [] multipleFilePaths;

	public static final String MULTIPLE_EDIT = "*";
	
	/**
	 * Used when editing a single object.
	 * @param filePath The key for the data type
	 * @param master The data controller
	 * @param mW The main window
	 */
	public ModifyWindow(String filePath, IFunctions master, MainWindow mW)
	{
		this.master = master;
		originalFilePath = filePath;
		mainWindow = mW;
		multipleFilePaths = null;
		setUp();
		searchWordsArea.setToolTipText("Use ',' to separate search words.");
	}
	
	
	/**
	 * Used when editing multiple objects.
	 * @param filePaths Array of files being edited
	 * @param master The data controller
	 * @param mW The main window
	 */
	public ModifyWindow(String [] filePaths, IFunctions master, MainWindow mW)
	{
		multipleFilePaths = filePaths;
		this.master = master;
		mainWindow = mW;
		originalFilePath = MULTIPLE_EDIT;
		setUp();
		findFileBtn.setEnabled(false);
		searchWordsArea.setText(MULTIPLE_EDIT + ",");
		searchWordsArea.setToolTipText("Delete " + MULTIPLE_EDIT + ", to remove existing search words.");
	}
	

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == okBtn)
		{
			ok();					
		}
		else if (arg0.getSource() == cancelBtn)
		{
			dispose();
		}
		else if (arg0.getSource() == findFileBtn)
		{
			findFile();
		}
		
	}
	
	/**
	 * This will pass the original file path, the new one and the search text back to the main window or
	 * it will pass the array of files with the new search words being edited back to the main window.
	 * This will then dispose of itself.
	 */
	private void ok()
	{
		if (findFileBtn.isEnabled())
			mainWindow.modifyWindowClose(originalFilePath, filePathLabel.getText(), searchWordsArea.getText());
		else
			mainWindow.modifyLotsWindowClose(multipleFilePaths, searchWordsArea.getText());
		dispose();
	}



	/**
	 * This will use the Main Window to select a file using JFileChooser. Then it will check if the file exists
	 * in the data base. If false it will assign it to the JLabel, otherwise it will display an error to the user. 
	 */
	private void findFile()
	{
		String path = mainWindow.findFile(filePathLabel.getText());
		if (path == null)
			return;
		if (master.keyExists(path))
			JOptionPane.showMessageDialog(null, "That Photograph Is Already In The DataBase", "ERROR", JOptionPane.ERROR_MESSAGE);			
		else
			filePathLabel.setText(path);
	}



	private void setUp()
	{
		JPanel filePanel = new JPanel();
		JPanel textPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel filePathPanel = new JPanel();
		JPanel fileBtnPanel = new JPanel();
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 3, 3));
		okBtn = makeButton("OK");
		cancelBtn = makeButton("Cancel");
		findFileBtn = makeButton("Change...");
//		filePathLabel = new JLabel(originalFilePath);
		filePathLabel = new JTextField(originalFilePath);
		filePathLabel.setEditable(false);
//		filePathLabel.setOpaque(false);
		
		searchWordsArea = new JTextArea("", 8, 50);
		searchWordsArea.setLineWrap(true);
		searchWordsArea.setText(master.getWords(originalFilePath));
		findFileBtn.setToolTipText("Find the file associated with thumb nail.");
		
		filePathPanel.add(filePathLabel);
		fileBtnPanel.add(findFileBtn);
		filePanel.add(filePathPanel, BorderLayout.CENTER);
		filePanel.add(fileBtnPanel, BorderLayout.EAST);
		
		buttonsPanel.add(okBtn);
		buttonsPanel.add(cancelBtn);
		
		textPanel.add(searchWordsArea);
		
		bottomPanel.add(buttonsPanel);
		add(filePanel, BorderLayout.NORTH);
		add(textPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		pack();
		searchWordsArea.requestFocusInWindow();
		searchWordsArea.addKeyListener(this);
	}

	private JButton makeButton(String title)
	{
		JButton button = new JButton(title);
		button.addActionListener(this);
		return button;
	}



	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			ok();
	}



	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
}
