package launch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;

import Floaters.BuildArray;
import Floaters.LoadFiles;
import Floaters.ModifyWindow;
import Interfaces.IFunctions;
import Interfaces.IProgressMonitor;

public class PhotoManager extends Thread implements IFunctions
{
	private HashMap<File, ThumbnailObject> data;
	private final String ERROR_FILE_PATH = "src/errors/";
	private final String DATA_FILE_PATH = "src/data/";
	private final String DATA_FILE = "data.dat";
	private FileChannel dataChannel;
	private FileLock dataLock;
	private Message message;
	private IProgressMonitor monitor;
	
	private static final String FILES_NOT_READABLE = "FILES NOT READABLE";
	private static final String FILES_ALREADY_EXIST	 = "FILES ALREADY EXIST";
	
	private static final ImageIcon BROKEN = new ImageIcon("src/gphx/broken.gif");
	
	public static final FileNameExtensionFilter TEXT_FILES = new FileNameExtensionFilter("Text Files", "txt");
	public static final FileNameExtensionFilter GRAPHIC_FILES = new FileNameExtensionFilter("Graphic Files: JPG, BMP, PNG, TIF, DNG", "jpg", "bmp", "png", "tif", "dng");
	private ArrayList<File> newEntriesloaded = new ArrayList<>();
	
	{
		message = new Message();		
	}
	
 	public PhotoManager() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		loadData();
	}
	
	
	public PhotoManager(boolean startNewDataBase) throws FileNotFoundException, IOException
	{
		createNewDataBase();
	}
	



	/**
	 * This will create the dat file where the database will be saved to. This should only be called on first time running
	 * or if the database file was deleted.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void createNewDataBase() throws FileNotFoundException, IOException
	{
		data = new HashMap<File, ThumbnailObject>();
		File newFile = new File(DATA_FILE_PATH);
		newFile.mkdir();
		
		FileOutputStream fileOut = new FileOutputStream(DATA_FILE_PATH + DATA_FILE);
		ObjectOutputStream dataOut = new ObjectOutputStream(fileOut);
		dataOut.writeObject(data);
		dataOut.close();
		lockDataFile();
	}


	
	/**
	 * This will load existing data into memory from a file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void loadData() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		//IOException is thrown here if the file is locked by another instance of this program is running.
		FileInputStream fileIn = new FileInputStream(DATA_FILE_PATH + DATA_FILE);
		ObjectInputStream dataIn = new ObjectInputStream(fileIn);
		
		Object o = dataIn.readObject();
//		testData = (HashMap<File, ThumbnailObject>) dataIn.readObject();
		data = (HashMap<File, ThumbnailObject>)o;
		dataIn.close();
		lockDataFile();
	}
	
	@SuppressWarnings("resource")
	private void lockDataFile()
	{
		try
		{
			// This channel is closed with ShutDownHook
			dataChannel = new RandomAccessFile(new File(DATA_FILE_PATH + DATA_FILE), "rw").getChannel();
			dataLock = dataChannel.tryLock();			
			Runtime.getRuntime().addShutdownHook(this);
		}
		catch(IOException e)
		{
			e.printStackTrace();// This will only happen if this program tries to lock the data file before unlocking it.
		}
	}



	/**
	 * This is only called when the program terminates to release the lock on the data file.
	 */
	public void run()
	{
		try
		{
			if (dataLock != null)
			{
				dataLock.release();
				dataChannel.close();
			}
		}
		catch (IOException e)
		{
			// At this point the program is closing. Let's pretend we didn't see it.
		}
	}
	


	/**
	 * This will attempt to add the filePaths passed into the database. Files that cannot be read or files that already exist will not be added.
	 * These in turn will be added to loadingProblems and returned to the window for the user to see. 
	 */
	@Override
	public void add(File [] filePaths)
	{
		newEntriesloaded = new ArrayList<>();

		LoadFiles loader = new LoadFiles(data, newEntriesloaded, filePaths, DATA_FILE_PATH, FILES_ALREADY_EXIST, FILES_NOT_READABLE);
		monitor.startLoadingMonitor(loader);
		loader.execute();
	}

	/**
	 * This will complete the add() method by writing any errors out to txt files and saving the data to HD.
	 * @param errorsReturned Each key in the HashMap refer to an ArrayList of errors for that key.
	 */
	@Override
	public void finishAdding(HashMap<String, ArrayList<String>> errorsReturned) throws FileNotFoundException, IOException
	{
		for (String key: errorsReturned.keySet())
		{
			if (key == FILES_NOT_READABLE)
				writeErrorsToFile(FILES_NOT_READABLE, errorsReturned.get(FILES_NOT_READABLE));
			else if (key == FILES_ALREADY_EXIST)
				writeErrorsToFile(FILES_ALREADY_EXIST, errorsReturned.get(FILES_ALREADY_EXIST));
		}
		
		if (newEntriesloaded.size() > 0)
		{
			message.setMessage("Files Loaded: ", newEntriesloaded.size());
			saveData();
		}
	}


	/**
	 * This will append/overwrite the errors for each type to a separate file on the HD, so the user can read later.
	 * @param fileName The file name to append/overwrite errors to.
	 * @param errors The list of errors recorded for any one error type
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeErrorsToFile(String fileName, ArrayList<String> errors) throws FileNotFoundException, IOException
	{
		Scanner wordSorter = new Scanner(fileName.replace(" ", "_"));
		String newFileName = new String();
		while (wordSorter.hasNext())
			newFileName += wordSorter.next();
		
		File tempFile = new File(ERROR_FILE_PATH + newFileName + ".txt");
		wordSorter.close();
		
		boolean append = true;
		
		if (tempFile.exists())
			append = (tempFile.length() < 1024 * 1024);// limit the size of the file to 1MB
		else
		{
			File newFile = new File(ERROR_FILE_PATH);
			newFile.mkdir();
		}
		
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date d = new Date();
		
		PrintWriter fileOut = new PrintWriter(new FileWriter(tempFile, append));
		
		for (String s: errors)
			fileOut.printf("%-30s%s\n", sdf.format(d), s);
		
		fileOut.close();
	}


	/**
	 * This will attempt to save the database to file. 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void saveData() throws FileNotFoundException, IOException
	{
		if (dataLock != null)
			dataLock.release();
		
		FileOutputStream fileOut = new FileOutputStream(DATA_FILE_PATH + DATA_FILE);
		ObjectOutputStream dataOut = new ObjectOutputStream(fileOut);
		dataOut.writeObject(data);
		dataOut.close();
		dataLock = dataChannel.tryLock();
	}

	/**
	 * The key is a file path, but the main window does not know this.
	 */
	public File getFile(Object keyColumn)
	{
		return (File)keyColumn;
	}

	
	@Override
	public FileNameExtensionFilter getGraphicFileExtension()
	{
		return GRAPHIC_FILES;
	}
	
	/**
	 * This will return the file extensions allowed.
	 */
	@Override
	public FileNameExtensionFilter [] getFileExtensions()
	{
		FileNameExtensionFilter [] extensions = {TEXT_FILES, GRAPHIC_FILES};
		return extensions;
	}

	/**
	 * This returns the search words for a PhotoObject that matches the filePath. Each word will be separated by a comma and put into a String, if any.
	 */
	@Override
	public String getWords(String filePath)
	{
		if (filePath == ModifyWindow.MULTIPLE_EDIT)
			return "";
		
		ArrayList<String> searchWords = new ArrayList<>();
		for (File f: data.keySet())
			if (filePath == f.getAbsolutePath())
			{
				searchWords = data.get(f).getSearchWords();
				break;
			}
		
		String words = new String();
		for (String word : searchWords)
			words += word + ",";
		
		return words;
	}
	
	
	/**
	 * This will copy the first search word to the clipboard or do nothing if there are no search words
	 */
	/*
	@Override
	public void copyToClipboard(String key)
	{
		List<String> searchWords = data.get(new File(key)).getSearchWords();
		if (searchWords.size() == 0)
			return;
		
		java.awt.datatransfer.StringSelection stringSelection = new StringSelection(searchWords.get(0));
		java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	*/
	
	
	
	/**
	 * This will add or over write search words for each PhotoObject being edited.
	 */
	@Override
	public void modify(String [] keys, String searchWords) throws FileNotFoundException, IOException
	{
		ArrayList<String> newWords = extractWords(searchWords);
		boolean append = false;
		if (newWords.size() > 0 && newWords.get(0).equals(ModifyWindow.MULTIPLE_EDIT))
		{
			append = true;
			newWords.remove(0);
		}
		for (String key: keys)
		{
			ThumbnailObject photo = data.get(new File(key));
			if (photo == null)// if one of the chosen ThumbnailObjects have been removed from HashMap after modify was pressed then photo will return null when modify attempts to work on this
				continue;
			photo.setSearchWords(newWords, append);
		}
		saveData();
	}

	/**
	 * This will update a PhotoObject with a new Key and search words. If a new Key was passed then the old key is removed from the HashMap
	 * and the PhotoObject is remapped with the new key. Either way the original PhotoObject will have it's search words replaced. 
	 */
	@Override
	public void modify(String originalKey, String newKey, String searchWords) throws FileNotFoundException, IOException
	{
		ArrayList<String> newWords = extractWords(searchWords);

		if (originalKey.equalsIgnoreCase(newKey))
		{
			ThumbnailObject photo = data.get(new File(originalKey));
			if (photo != null)// if the ThumbnailObject was removed from HashMap after modify was pressed then photo will return null when modify attempts to work on this
			{
				photo.setSearchWords(newWords, false);
				saveData();
			}
		}
		else
		{		
			ThumbnailObject photo = data.remove(new File(originalKey));
			
			if (photo != null)// if the ThumbnailObject was removed from HashMap after modify was pressed then photo will return null when modify attempts to work on this
			{
				File newFile = new File(newKey);
				photo.setSearchWords(newWords, false);
				photo.setFilePath(newFile);
				data.put(newFile, photo);
				saveData();
			}
		}
	}

	
	/**
	 * This is used by the MainWindow to get an Object[][] of the modified entries for the JTable
	 */
	@Override 
	public void getUpdatedObject(String [] keys)
	{
		ArrayList<ThumbnailObject> photos = new ArrayList<>();
		for (String key : keys)
			for (ThumbnailObject po : data.values())
				if (po.getFile().getAbsolutePath().equalsIgnoreCase(key))
				{
					photos.add(po);
					break;
				}
		build2DArray(photos);
	}
	
	
	/**
	 * This will cycle through the saved photographs and return any that have no searchable words or
	 * the File is not found at it's recorded location.
	 */
	public void findError()
	{
		ArrayList<ThumbnailObject> errors = new ArrayList<>();
		for (ThumbnailObject pO: data.values())
		{
			if (pO.getStatus() == ThumbnailObject.ERROR)
			{
				errors.add(pO);
				continue;
			}
			if (pO.getSearchWords().size() == 0)
				errors.add(pO);
		}
		if (errors.size() > 0)
			message.setMessage("Errors Found: ", errors.size());
		
		build2DArray(errors);
	}


	/**
	 * This will remove selected PhotoObjects from the HashMap and also delete the associated thumbnail with the PhotoObject
	 */
	@Override
	public void remove(Object [] removeObjects) throws FileNotFoundException, IOException
	{
		for (Object ob: removeObjects)
		{
			ThumbnailObject p = data.remove((File)ob);// ob can be safely cast to File, because PhotoManger knows the Key is a File class.
			p.delete();
		}
		saveData();
		//message.reduceTotalMessage(removeObjects.length);
	}


	/**
	 * This will search through each PhotoObject's search words list to try and find a match with one
	 * of the user input words. Any PhotoObject match found will be added to the 2D array.
	 * @param inputInclude a string of words to select a PhotoObject if matched. Each word should be separated with a comma.
	 * @param inputExclude a string of words to exclude a PhotoObject from inputInclude match. Each word should be separated with a comma.
	 * @param matchAny If true this will add a photoObject that has 1 word that matches, otherwise all words will need to match for inclusion.
	 * @param matchAnyNot If true this will remove a photoObject that has 1 word that matches, otherwise all words will need to match for exclusion.
	 */
	@Override
	public void search(String inputInclude, String inputExclude, boolean matchAny, boolean matchAnyNot)
	{
		
		ArrayList<String> searchWords = extractWords(inputInclude);
		ArrayList<String> excludeWords = extractWords(inputExclude);
		ArrayList<ThumbnailObject> matched = new ArrayList<>();
		
		// if no include words are present, then skip the search and return here.
		if (searchWords.size() == 0)
		{
			build2DArray(new ArrayList<ThumbnailObject>());
			return;
		}
				
		boolean selectAll = searchWords.get(0).equals("*");
		if (excludeWords.size() > 0 && excludeWords.get(0).equalsIgnoreCase("*"))
			excludeWords.remove(0);		
		
		for (ThumbnailObject photo: data.values())
		{
			if (selectAll)
			{
				matched.add(photo);
				continue;
			}
			
			if (matchAny)
			{
				if (matchAny(searchWords, photo))
				{
					matched.add(photo);
				}
			}
			else
				if (matchAll(searchWords, photo))
					matched.add(photo);
		}
		ArrayList<ThumbnailObject> tempMatched = new ArrayList<>(matched);
		
		if (excludeWords.size() > 0)
			for (ThumbnailObject po : tempMatched)
			{
				if (matchAnyNot)
				{
					if (matchAny(excludeWords, po))
					{
						matched.remove(po);
					}
				} else if (matchAll(excludeWords, po))
					matched.remove(po);
			}
		
		message.setMessage("Matches Found: ", matched.size());
		build2DArray(matched);
	}
	

	@Override
	public String[] getHeader()
	{
		return ThumbnailObject.HEADER;
	}


	@Override
	public float[] getColumnRatios()
	{
		return ThumbnailObject.HEADER_PERCENT_WIDTHS;
	}


	@Override
	public String[] getWordWrapCells()
	{
		return ThumbnailObject.WORD_WRAP_CELLS;
	}	
	

	@Override
	public int getKeyColumn()
	{
		return 0;// PhotoManager builds the data for the JTable and column 0 is the key column, which is a File class.
	}

	/**
	 * Tests to see if the Key is already in used with the HashMap.
	 */
	@Override
	public boolean keyExists(String file)
	{
		for (File f: data.keySet())
			if (f.getAbsolutePath().equalsIgnoreCase(file))
				return true;
		
		return false;
	}
	


	/**
	 * Used when loading new Photographs.
	 */
	@Override
	public void getNewEntries()
	{
		ArrayList<ThumbnailObject> newPhotos = new ArrayList<>();
		
		for (File f: newEntriesloaded)
			newPhotos.add(data.get(f));
		
		 build2DArray(newPhotos);
	}
	

	/**
	 * This will return a Message object with text update for the MainWindow
	 */
	@Override
	public Message getTitleMessage()
	{
		return message;
	}


	@Override
	public void setMonitor(IProgressMonitor pm)
	{
		monitor = pm;
	}

	
	
	/**
	 * This creates a List of words separated with a comma 
	 * @param searchWords The user input as a String
	 * @return The user input returned as an Arraylist
	 */
	private ArrayList<String> extractWords(String searchWords)
	{
		ArrayList<String> wordList = new ArrayList<>();
		Scanner wordSorter = new Scanner(searchWords.replace(" ", ""));
		wordSorter.useDelimiter(",");
		while (wordSorter.hasNext())
			wordList.add(wordSorter.next());
		
		wordSorter.close();
		return wordList;
	}
	
	
	/**
	 * This will check that all searchWords are found in a PhotoObject before returning true. 
	 * @param searchWords User input words
	 * @param photo The PhotoObject to find matching words.
	 * @return True once all user input words were found in PhotoObject search words.
	 */
	private boolean matchAll(ArrayList<String> searchWords, ThumbnailObject photo)
	{		
		ArrayList<String> photoWords = photo.getSearchWords();
		
		for (String word: searchWords)
		{
			boolean wordFound = false; 
			for (String photoWord: photoWords)
				if (word.equalsIgnoreCase(photoWord))
				{
					wordFound = true;
					break;
				}
			
			if (!wordFound)
				return false;
		}
		return true;
	}
	
	/**
	 * This will check that at least one word in searchWords matches a word in the PhotoObject
	 * @param searchWords User input words.
	 * @param photo The PhotoObject to find matching words.
	 * @return True once one user input word is found in the PhotoObject search words.
	 */
	private boolean matchAny(ArrayList<String> searchWords, ThumbnailObject photo)
	{
		ArrayList<String> photoWords = photo.getSearchWords();
		for (String word: searchWords)
			for (String photoWord: photoWords)
				if (word.equalsIgnoreCase(photoWord))
					return true;
		
		return false;
	}
	
	
//	/**
//	 * eliminates white space and create words using a comma as the separator.
//	 * @param input string to separate
//	 * @return 
//	 */
//	private ArrayList<String> sortInput(String input)
//	{
//		ArrayList<String> words = new ArrayList<>();
//		Scanner wordSorter = new Scanner(input.replace(" ", ""));
//		wordSorter.useDelimiter(",");
//		
//		while(wordSorter.hasNext())
//			words.add(wordSorter.next());
//		wordSorter.close();
//		
//		return words;
//	}
	
	
	
	
	/**
	 * Builds table data for JTable.
	 * @param files List of Photograph Objects to build table from.
	 */
	private void build2DArray(ArrayList<ThumbnailObject> files)
	{
		if (files.size() == 0)
			monitor.startMonitor(null);
		
		
		BuildArray buildArray = new BuildArray(files, BROKEN);
		
		monitor.startMonitor(buildArray);
		buildArray.execute();
	}


	
	/**
	 * This class is here so PhotoManager has access to the private methods and fields.
	 * @author root
	 *
	 */
	public class Message
	{
		private String text;
		private int total;
		
		public Message()
		{
			total = 0;
			text = new String();
		}
		

		
		
		/**
		 * Used to set Title on MainWindow
		 * @param totalIn Number of elements found
		 */
		private void setMessage(String text, int totalIn)
		{
			total = totalIn;
			this.text = text;
		}

		/**
		 * Used to get the message contained stored in this class.
		 * @return String of the message.
		 */
		public String getMessage()
		{
			return text + total;
		}
	}




}





