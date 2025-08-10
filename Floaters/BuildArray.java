package Floaters;


import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import launch.ThumbnailObject;


public class BuildArray extends SwingWorker<Object[][], Void>
{
	private ArrayList<ThumbnailObject> files;
	private Object[][] array;
	private ImageIcon broken;
	
	private int row;
	private int column;
	
	public BuildArray(ArrayList<ThumbnailObject> files, ImageIcon broken)
	{
		this.files = files;
		//this.files = new ArrayList<>(files);
		
		this.broken = broken;
		column = ThumbnailObject.HEADER.length;
		array = new Object[files.size()][column];
	}
	
	
	/**
	 * This will attempt to load each PhotoObject into a Object[][] array.
	 * @return 2D array of Object to load into a JTable
	 */
	@Override
	protected Object[][] doInBackground() throws Exception
	{
		row = 0;
		setProgress(0);
		
		for (ThumbnailObject pO : files)
		{
			array[row][0] = pO.getFile();
			array[row][1] = pO.getStatus();
			array[row][2] = pO.getSearchWords();
			if (pO.getThumbNail() == null)
				array[row][3] = broken;
			else
				array[row][3] = new ImageIcon(pO.getThumbNail());
			row++;
			
			int current = (row * 100) / files.size();
			setProgress(current);
			
			if (isCancelled())
				break;
		}
		
		return array;
	}



	
	
	/**
	 * This will trim the array so that there are no null objects in it.
	 */
	public Object[][] getTrimmedArray()
	{
		Object[][] trimmedArray = new Object[findFirstNull()][column];
		
		for (int f = 0; f < trimmedArray.length; f++)
			for (int col = 0; col < column; col++)
				trimmedArray[f][col] = array[f][col];

		return trimmedArray;
	}
	
	/**
	 * Finds the first null element of the Object[][] array. Cannot depend on row++ operation.
	 * @return The index of the first null element
	 */
	private int findFirstNull()
	{
		for (int f = 0; f < array.length; f++)
		{
			if (array[f][0] == null)
				return f;
		}
		return array.length;
	}
	
	
}
