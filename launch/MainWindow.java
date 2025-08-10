package launch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import Floaters.LoadingPCL;
import Floaters.ModifyWindow;
import Floaters.ObjectPCL;
import Interfaces.IFunctions;
import Interfaces.IProgressMonitor;
import Interfaces.IReDraw;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener, IReDraw, IProgressMonitor
{
	private final int WIDTH = 1000;
	private final int HEIGHT = 850;

	private JButton btnAdd;
	private JButton btnModify;
	private JButton btnFindError;
	private JButton btnRemove;
	private JButton btnSearch;
	private JButton btnOpen;
	private JRadioButton matchAll;
	private JRadioButton matchAny;
	private JRadioButton matchAllNot;
	private JRadioButton matchAnyNot;
	private JMenuBar mainMenuBar;

	private JTextField includeInput;
	private JTextField excludeInput;
	private JTable theTable;
	private IFunctions dataController;
	private ModifyWindow mod;
	private FileNameExtensionFilter [] fileExtensions;

	private Finish dataControllerFinish;
	private String modifyFilePath;
	private enum Finish
	{
		notSet,
		add,
		findError,
		search,
		modify,
		modifyLots
	}
	
	
	public MainWindow(IFunctions dataController)
	{
		super("Photograph Database");
		this.dataController = dataController;
		this.dataController.setMonitor(this);
		fileExtensions = dataController.getFileExtensions();
		setBounds(0, 0, WIDTH, HEIGHT);
		createMenuBar();
		makeCentrePanel();
		makeBottomPanel();
		createButtonTips();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - WIDTH / 2, dim.height / 2 - HEIGHT / 2);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btnModify)
			modify();
//		else if (e.getSource() == btnFindError)
//			findError();
		else if (e.getSource() == btnRemove)
			remove();
		else if (e.getSource() == btnOpen)
			open();
		else if (e.getSource() == btnSearch)
			search();
		else if (e.getSource() == btnAdd)
			add();
	}

	

	/**
	 * This will open a JFileChooser at the directory of filePath. This will allow 1 graphic file to be selected. 
	 * This is used by the ModifyWindow to select the file path of a missing/renamed Photograph.
	 * @param filePath a String value of the existing filepath
	 * @return a String value of the chosen file's filepath
	 */
	public String findFile(String filePath)
	{
		JFileChooser choose = new JFileChooser();
		choose.setFileFilter(dataController.getGraphicFileExtension());
		choose.setAcceptAllFileFilterUsed(false);
		choose.setMultiSelectionEnabled(false);
		
		choose.setCurrentDirectory(new File(filePath.substring(0, filePath.lastIndexOf("\\"))));
		
		if (choose.showOpenDialog(mod) != JFileChooser.APPROVE_OPTION)
			return null;
		File selected = choose.getSelectedFile();
		
		return selected.getAbsolutePath();
	}
	
	
	/**
	 * This will get a selection of graphic files OR a selection of text files. The graphic files will be passed onto the data controller to add to the database.
	 * The text files will be analysed and each line will be assumed to be a file path leading to a photograph. Each of these lines in turn will be passed onto 
	 * the data controller to add to the database.
	 */
	private void add()
	{
		JFileChooser choose = new JFileChooser();
		for (FileNameExtensionFilter ext: fileExtensions)
			choose.setFileFilter(ext);
		
		choose.setAcceptAllFileFilterUsed(false);
		choose.setMultiSelectionEnabled(true);
		choose.setCurrentDirectory(new File(".."));

		if (choose.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File [] selected = choose.getSelectedFiles();
		
		if (choose.getFileFilter() != dataController.getGraphicFileExtension())
			selected = extractFiles(selected);

		dataController.add(selected);

	}
	
	
	/**
	 * This will display any errors encountered with the add() method. This will request the new entries loaded from the DataContorller. 
	 */
	@Override
	public void finishLoading(HashMap<String, ArrayList<String>> errorsReturned)
	{
		try
		{
			dataController.finishAdding(errorsReturned);
			for (String s : errorsReturned.keySet())
			{
				int sizy = errorsReturned.get(s).size();// forgot to get the value associated with the KEY and measure that size() for the String[] correct size.
				String[] fileList = errorsReturned.get(s).toArray(new String[sizy]);// Fuck it, i'm leaving these 2 lines here.
				displayErrors(s, fileList);
			}

			dataControllerFinish = Finish.add;
			dataController.getNewEntries();
		} 
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(this, "Error Writing Database.", "CRITICAL", JOptionPane.ERROR_MESSAGE);
		} 
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "IO Exception Thrown.", "CRITICAL", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * This will complete the finishLoading() method. This will display the data in the JTable if any is returned. This will notify the user about the files loaded. 
	 * @param table Object[][] containing info of the files loaded. 
	 */
	private void addFinish(Object[][] table)
	{
		if (table != null)
		{
			setTitle(dataController.getTitleMessage().getMessage());
			updateTable(table);
			displayErrors("Loaded", new String[]{String.format("New entries - %d", table.length)});
		}
		else
			displayErrors("Oops.", new String[]{"No New Photographs Added."});
		
	}



	/**
	 * This will attempt to open the file with the default external program.
	 */
	private void open()
	{
		int [] row = theTable.getSelectedRows();
		if (row.length != 1)
			displayErrors("Error", new String[]{"Select 1 To Open."});
		else
		{
			//This seems a bit over killed? Or maybe not. The manager will return the file for this row. Although the key is the file path, but maybe with the next manager it won't.
			File fileToOpen = dataController.getFile(theTable.getValueAt(row[0], dataController.getKeyColumn()));
			if (!fileToOpen.exists())
			{
				JOptionPane.showMessageDialog(this, "Cannot Open That File.", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try
			{
				Desktop.getDesktop().open(fileToOpen);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(this, "Cannot Open That File.", "ERROR", JOptionPane.ERROR_MESSAGE);
			}

		}
		
	}
	
	
	
	
	/**
	 * This will open the ModifyWindow, disposing one that might be already be open, loading it with the data from the selected rows.
	 */
	private void modify()
	{
		int [] rowNumbers = theTable.getSelectedRows();
		if (rowNumbers.length == 0)
		{
			displayErrors("Error", new String[]{"Select Rows To Edit."});
			return;
		}
		
		if (mod != null)
			mod.dispose();
		
		if (rowNumbers.length > 1)
		{
			String [] selectedRows = new String[rowNumbers.length];
			for (int f = 0; f < rowNumbers.length; f++)
				selectedRows[f] = theTable.getValueAt(rowNumbers[f], dataController.getKeyColumn()).toString();
			
			mod = new ModifyWindow(selectedRows, dataController, this);
		}
		else
		{
			String selectedRow = theTable.getValueAt(rowNumbers[0], dataController.getKeyColumn()).toString();
			mod = new ModifyWindow(selectedRow, dataController, this);
		}
		
		Point p = getLocationOnScreen();
		mod.setLocation(p.x + WIDTH / 2 - mod.getSize().width / 2, p.y + HEIGHT / 2 - mod.getSize().height / 2);
		mod.setTitle("EDITOR");
		mod.setVisible(true);
	}
	
	
	/**
	 * This will pass the data back to the controller for updating. 
	 * @param filePaths The selected files to update.
	 * @param words The search words entered by the user. 
	 */
	public void modifyLotsWindowClose(String [] filePaths, String words)
	{
		try
		{
			dataController.modify(filePaths, words);
			
			dataController.getUpdatedObject(filePaths);
			dataControllerFinish = Finish.modifyLots;
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(this, "Error Writing Database.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "IO Exception Thrown.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * This will complete the modifyLotsWindowClose() by updating the modified rows to show the user the changes.
	 * @param table Object[][] of modified data.
	 */ 
	private void modifyLotsFinish(Object [][] table)
	{
		int keyColumn = dataController.getKeyColumn();

		for (int tableRow = 0; tableRow < theTable.getRowCount(); tableRow++)
		{
			for (int objectRow = 0; objectRow < table.length; objectRow++)
			{
				if (theTable.getValueAt(tableRow, keyColumn).toString().equalsIgnoreCase(table[objectRow][keyColumn].toString()))
				{
					for (int column = 0; column < theTable
							.getColumnCount(); column++)
						theTable.setValueAt(table[objectRow][column],tableRow, column);
					break;
				}
			}
		}
	}
	
	
	
	
	
	/**
	 * This will pass data back to data controller for updating.
	 * @param originalFilePath The file path that was passed to the ModifyWindow
	 * @param newFilePath The file path returned from the ModifyWindow.
	 * @param words String value of the search words for this object.
	 */
	public void modifyWindowClose(String originalFilePath, String newFilePath, String words)
	{
		try
		{
			dataController.modify(originalFilePath, newFilePath, words);
			String [] filePaths = new String[1];
			filePaths[0] = newFilePath;
			dataController.getUpdatedObject(filePaths);
			dataControllerFinish = Finish.modify;
			modifyFilePath = originalFilePath;
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(this, "Error Writing Database.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "IO Exception Thrown.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	/**
	 * This will complete modifyWindowClose() by updating the row that was being edited to show the user the changes.
	 * @param table Object[][] of modified data.
	 */
	private void modifyFinish(Object[][] table)
	{
		int keyColumn = dataController.getKeyColumn();
		
		for (int row = 0; row < theTable.getRowCount(); row++)
			if (theTable.getValueAt(row, keyColumn).toString().equalsIgnoreCase(modifyFilePath))
			{
				for (int column = 0; column < theTable.getColumnCount(); column++)
					theTable.setValueAt(table[0][column], row, column);
				break;
			}
		
	}
	


	/**
	 * This is called by the ColourMenu class when it applies a new JTattoo scheme
	 */
	@Override
	public void redrawTable()
	{
		theTable.setRowHeight(204);
	}
	
	

	/**
	 * Called by the dataController to initiate ProgressMonitor to watch a SwingWorker that builds a Object[][] array.
	 * @param sw The SwingWorker object grinding the task at hand.
	 */
	@Override
	public void startMonitor(SwingWorker<Object[][], Void> sw)
	{
		if (sw == null)
			finish(null);
		else
			sw.addPropertyChangeListener(new ObjectPCL(new ProgressMonitor(this, null, "Processing...", 0, 100), sw, this));
	}
	
	/**
	 * Called by the dataController to initiate ProgressMonitor to watch a SwingWorker that loads files from the HD and records any errors encountered.
	 * @param sw The SwingWorker object grinding the task at hand.
	 */
	@Override
	public void startLoadingMonitor(SwingWorker<HashMap<String, ArrayList<String>>, Void> sw)
	{
		sw.addPropertyChangeListener(new LoadingPCL(new ProgressMonitor(this, null, "Processing...", 0, 100), sw, this));
	}
	
	

	
	/**
	 * This will complete the SwingWorker operation of building Object[][] for JTable
	 * @param table used to display results in a JTable
	 */
	@Override
	public void finish(Object[][] table)
	{
		if (dataControllerFinish == Finish.add)
			addFinish(table);
		else if (dataControllerFinish == Finish.findError)
			findErrorFinish(table);
		else if (dataControllerFinish == Finish.modify)
			modifyFinish(table);
		else if (dataControllerFinish == Finish.modifyLots)
			modifyLotsFinish(table);
		else if (dataControllerFinish == Finish.search)
			searchFinish(table);
		
		dataControllerFinish = Finish.notSet;
		setTitle(dataController.getTitleMessage().getMessage());
	}
	
	
	
	
	/**
	 * This will ask the data controller to find errors in the data base. That is any photograph file that cannot be found or
	 * entries with no search words.
	 */
	private void findError()
	{
		dataControllerFinish = Finish.findError;
		dataController.findError();
	}
	
	/**
	 * This will complete findError() method by updating the JTable with the data found.
	 * If nothing is found a message is displayed to the user with no change to the JTable, otherwise a hit count is displayed with the JTable updated.
	 * @param table Object[][] of data found. Might be null.
	 */
	private void findErrorFinish(Object [][] table)
	{
		if (table == null)
			displayErrors("Results", new String[]{"No Errors Found."});
		else
		{
			setTitle(dataController.getTitleMessage().getMessage());
			updateTable(table);
			displayErrors("Results", new String[]{String.format("%d found.", table.length)});
		}
	}
	

	
	/**
	 * This will get selected rows and pass them to the data controller to delete after confirming with the user. This will then
	 * remove those selected rows from the JTable.
	 */
	private void remove()
	{
		int [] selection = theTable.getSelectedRows();
		if (selection.length == 0)
			displayErrors("Nothing Selected.", new String[]{"Please Select At Least One Row."});
		else
			if (JOptionPane.showConfirmDialog(this, "Are You Sure?", "REMOVE", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION)
				return;
		
		int keyColumn = dataController.getKeyColumn();
		Object [] objects = new Object[selection.length];
		for (int f = 0; f < selection.length; f++)
			objects[f] = theTable.getValueAt(selection[f], keyColumn);
		
		try
		{
			dataController.remove(objects);
			DefaultTableModel t = (DefaultTableModel)theTable.getModel();
			for (int i = selection.length - 1; i >= 0; i--)
				t.removeRow(selection[i]);
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(this, "Error Writing Database.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "IO Exception Thrown.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

	
	/**
	 * This will pass the inputed search words onto the data controller to find a match. 
	 */
	private void search()
	{
		dataControllerFinish = Finish.search;
		dataController.search(includeInput.getText(), excludeInput.getText(), matchAny.isSelected(), matchAnyNot.isSelected());
	}
	
	/**
	 * This will complete search() method by updating the JTable with the data found. 
	 * If nothing is found a message is displayed to the user with no change to the JTable, otherwise a hit count is displayed with the JTable updated.
	 * @param table Object[][] of the data found. Might be null.
	 */
	private void searchFinish(Object[][] table)
	{
		if (table == null)
			displayErrors("Results", new String[]{"No photographs matched the search."});
		else
		{
			setTitle(dataController.getTitleMessage().getMessage());
			updateTable(table);
			displayErrors("Results", new String[]{String.format("%d loaded.", table.length)});
		}
	}


	
	/**
	 * Loads the JTable with data
	 * @param table 2D array of data for the JTable
	 */
	private void updateTable(Object[][] table)
	{
		DefaultTableModel model = new DefaultTableModel(table, dataController.getHeader())
		{
			public Class<?> getColumnClass(int column)
			{
				return getValueAt(0, column).getClass();
			}
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		
		theTable.setModel(model);
		for (String columnName : dataController.getWordWrapCells())
			theTable.getColumn(columnName).setCellRenderer(new WordyWrapy());
		
		setTableColumnWidths();
	}
	
	
		
	
	/**
	 * This will create a File from each line in each txt file that was passed to it. This will display a message with 
	 * all the files that were not found. 
	 * @param selected array of files that in turn have lists of file paths
	 * @return array of found Files.
	 */
	private File[] extractFiles(File[] selected)
	{
		ArrayList<File> filePaths = new ArrayList<File>();
		ArrayList <String> filesNotFound = new ArrayList<String>();
		
		for (File f: selected)
		{
			try
			{
				Scanner reader = new Scanner(f);
				while (reader.hasNextLine())
				{
					File graphicFile = new File(reader.nextLine());
					if (graphicFile.exists())
						filePaths.add(graphicFile);
					else
						filesNotFound.add(graphicFile.getPath());
				}
				reader.close();
			} 
			catch (FileNotFoundException e)
			{
				displayErrors(new String("FILE NOT FOUND!"), new String[]{f + " has moved!"});
			}
		}
		if (filesNotFound.size() > 0)
			displayErrors("Files Not Found!", filesNotFound.toArray(new String[filesNotFound.size()]));
		
		return filePaths.toArray(new File[filePaths.size()]);
	}
	
	
	/**
	 * This displays a JOptionPane with the details passed. 
	 * @param title title for window
	 * @param messages messages to display to the user
	 */
	private void displayErrors(String title, String [] messages)
	{
		String message = messages[0];
		for (int f = 1; f < messages.length; f++)
			message += "\n" + messages[f];
		
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
	}

	
	/**
	 * This class overwrites the default cell rendered JLabel with a JTextArea on the JTable.
	 * @author Copied off the internet.
	 *
	 */
	private class WordyWrapy extends JTextArea implements TableCellRenderer
	{
		public WordyWrapy()
		{
			setLineWrap(true);
			setWrapStyleWord(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(isSelected)
				setBackground(getSelectionColor());
			else
				setBackground(null);
			setText(arg1.toString());
			setSize(arg0.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
			// Don't need resizing height according to text volume because the thumbnail takes precedence
//			if (arg0.getRowHeight(row) != getPreferredSize().height)
//				arg0.setRowHeight(row, getPreferredSize().height);
			
			return this;
		}
	}

	//	======================================================================
	//								WINDOW SETUP
	//	======================================================================

	private void createMenuBar()
	{
		mainMenuBar = new JMenuBar();
		@SuppressWarnings("unused")
		ColourMenu colourMenu = new ColourMenu(this, this, mainMenuBar);
		setJMenuBar(mainMenuBar);
	}
	
	private void makeCentrePanel()
	{
		JPanel centre = new JPanel();
		
		centre.setLayout(new BorderLayout());// NEW
		
		theTable = new JTable();
		theTable.setRowHeight(204);
		theTable.setPreferredScrollableViewportSize(new Dimension(WIDTH - 50, HEIGHT - 150));
		theTable.addMouseListener(
				new MouseAdapter()
				{
					public void mousePressed(MouseEvent mouseEvent)
					{
						if (mouseEvent.getClickCount() == 2)
							open();
					}
				});

		JScrollPane scrollBars = new JScrollPane(theTable);
		scrollBars.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//centre.add(scrollBars);
		centre.add(BorderLayout.CENTER, scrollBars);// NEW
		
		add(centre, BorderLayout.CENTER);
	}

	private void makeBottomPanel()
	{
		createSpecialListeners();
		
		JPanel bottom = new JPanel();
		btnModify = makeButton("Modify...");
		//btnFindError = makeButton("Find Errors");
		btnRemove = makeButton("Remove");		
		btnOpen = makeButton("Open");
		matchAll = new JRadioButton("All");
		matchAny = new JRadioButton("Any");
		matchAllNot = new JRadioButton("All");
		matchAnyNot = new JRadioButton("Any");
		ButtonGroup includeRadios = new ButtonGroup();
		includeRadios.add(matchAll);
		includeRadios.add(matchAny);
		includeRadios.setSelected(matchAll.getModel(), true);
		ButtonGroup excludeRadios = new ButtonGroup();
		excludeRadios.add(matchAllNot);
		excludeRadios.add(matchAnyNot);
		excludeRadios.setSelected(matchAnyNot.getModel(), true);

		JPanel buttonsPnl = new JPanel(new GridLayout(1, 6, 5, 5));
		JPanel searchPnl = new JPanel();
		JPanel searchLabelPnl = new JPanel(new GridLayout(2, 1, 0, 5));
		JPanel searchInputsPnl = new JPanel(new GridLayout(2, 1, 0, 5));
		JPanel radioButtonsPnl = new JPanel(new GridLayout(2, 2, 0, 0));
		buttonsPnl.add(btnAdd);
		buttonsPnl.add(btnOpen);
		buttonsPnl.add(btnModify);
		buttonsPnl.add(btnFindError);
		buttonsPnl.add(btnRemove);
		buttonsPnl.add(btnSearch);

		searchLabelPnl.add(new JLabel("+", JLabel.LEFT));
		searchLabelPnl.add(new JLabel("-", JLabel.LEFT));

		searchInputsPnl.add(includeInput);
		searchInputsPnl.add(excludeInput);
		
		searchPnl.add(searchLabelPnl);
		searchPnl.add(searchInputsPnl);
		
		radioButtonsPnl.add(matchAll);
		radioButtonsPnl.add(matchAny);
		radioButtonsPnl.add(matchAllNot);
		radioButtonsPnl.add(matchAnyNot);
		
		bottom.add(buttonsPnl, BorderLayout.WEST);
		bottom.add(searchPnl, BorderLayout.CENTER);
		bottom.add(radioButtonsPnl, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
	}
	
	
	/**
	 * This creates the special listeners that change the cursor to busy for the user to see.
	 */
	private void createSpecialListeners()
	{
		btnAdd = new JButton("Add...");
		ActionListener addListener = new ActionListener()// Special thanks to the internet for this one. Adds a busy timer while loading in files.
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				add();
			}
		};
		ActionListener cursorAdd = CursorController.createListener(this, addListener);
		btnAdd.addActionListener(cursorAdd);
		
		btnSearch = new JButton("Search");
		ActionListener searchInclude = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				search();
			}
		};
		ActionListener searchCursor = CursorController.createListener(this, searchInclude);
		btnSearch.addActionListener(searchCursor);
		
		includeInput = new JTextField("", 20);
		KeyListener includeListener = new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent e){}
			
			@Override
			public void keyReleased(KeyEvent e){}
			
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					search();
			}
		};
		KeyListener includeCursor = CursorController.createKeyListener(this, includeListener);
		includeInput.addKeyListener(includeCursor);
		
		btnFindError = new JButton("Find Errors");
		ActionListener errorListener = new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				findError();
			}
		};
		ActionListener errorCursor = CursorController.createListener(this, errorListener);
		btnFindError.addActionListener(errorCursor);
		
		excludeInput = new JTextField("", 20);
		excludeInput.addKeyListener(includeCursor);
	}

	
	private void createButtonTips()
	{
		btnAdd.setToolTipText("Add graphic images to the database.");
		btnModify.setToolTipText("Add search words or change a file path.");
		btnFindError.setToolTipText("Shows images with no search words or missing files.");
		btnRemove.setToolTipText("Remove entries from the database.");
		btnSearch.setToolTipText("Spaces and Letter Case is ignored.");
		btnOpen.setToolTipText("Open the original image with the default editor.");
		matchAll.setToolTipText("All words matched is selected.");
		matchAny.setToolTipText("Any word matched is selected.");
		matchAllNot.setToolTipText("All words matched is deselected.");
		matchAnyNot.setToolTipText("Any word matched is deselected.");
		includeInput.setToolTipText("Inclusion words or * for all.");
		excludeInput.setToolTipText("Exclusion words.");
	}
	
	
	private JButton makeButton(String title)
	{
		JButton button = new JButton(title);
		button.addActionListener(this);
		return button;
	}

	/**
	 * This will adjust the column widths in the JTable to match the ratios received from the data controller.
	 */
	private void setTableColumnWidths()
	{
		float[] widthPercents = dataController.getColumnRatios();
		int tableWidth = theTable.getWidth();
		for (int f = 0; f < widthPercents.length; f++)
		{
			int width = Math.round(widthPercents[f] * tableWidth);
			theTable.getColumnModel().getColumn(f).setPreferredWidth(width);
		}
	}


}
