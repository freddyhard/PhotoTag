package Floaters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingWorker;

import launch.ThumbnailObject;

public class LoadFiles extends SwingWorker<HashMap<String, ArrayList<String>>, Void>
{

	private HashMap<File, ThumbnailObject> data;
	private ArrayList<File> newEntriesloaded;
	private File[] filePaths;
	private String dataFilePath; 
	private final String filesAlreadyExist;
	private final String filesNotReadable;
	private HashMap<String, ArrayList<String>> loadingProblems;
	
	public LoadFiles(HashMap<File, ThumbnailObject> data, ArrayList<File> newEntries, File[] filePaths, String dataFilePath, String filesAlreadyExist, String filesNotReadable)
	{
		this.data = data;
		newEntriesloaded = newEntries;
		this.filePaths = filePaths;
		this.dataFilePath = dataFilePath;
		this.filesAlreadyExist = filesAlreadyExist;
		this.filesNotReadable = filesNotReadable;
	}
	
	
	/**
	 * This will use an array of File to create PhotoObject's from. Any that already exist or cannot be read will get added
	 * to separate ArrayLists and saved in a HashMap.
	 * @return Each key in the HashMap refer to a list of File's that were not usable. 
	 */
	@Override
	protected HashMap<String, ArrayList<String>> doInBackground() throws Exception
	{
		setProgress(0);
		loadingProblems = new HashMap<String, ArrayList<String>>();
		
		ArrayList<String> notReadable = new ArrayList<>();
		ArrayList<String> duplicates = new ArrayList<>();
		
		int counter = 0;
		for (File file : filePaths)
		{
			counter++;
			try
			{
				if (data.containsKey(file))
					duplicates.add(file.getPath());
				else
				{
					ThumbnailObject testy = new ThumbnailObject(file, dataFilePath);
					data.put(testy.getFile(), testy);
					newEntriesloaded.add(file);
				}
			}
			catch (IOException e)
			{
				notReadable.add(file.getPath());
			}
			
			int progress = counter * 100 / filePaths.length;
			setProgress(progress);
			
			if (isCancelled())
				break;
		}
		
		if (notReadable.size() > 0)
			loadingProblems.put(filesNotReadable, notReadable);
		
		if (duplicates.size() > 0)
			loadingProblems.put(filesAlreadyExist, duplicates);
		
		
		return loadingProblems;
	}
	
	/**
	 * Used to get HashMap if doInBackground is interrupted.
	 * @return strings of loading problems encountered.
	 * 
	 */
	public HashMap<String, ArrayList<String>> getData()
	{
		return loadingProblems;
	}

}
