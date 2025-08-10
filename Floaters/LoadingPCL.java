package Floaters;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import Interfaces.IProgressMonitor;

public class LoadingPCL implements PropertyChangeListener
{

	private ProgressMonitor monitor;
	private SwingWorker<HashMap<String, ArrayList<String>>, Void> swingWorker;
	private IProgressMonitor mainWindow;
	
	
	public LoadingPCL(ProgressMonitor pm, SwingWorker<HashMap<String, ArrayList<String>>, Void> sw, IProgressMonitor ipm)
	{
		monitor = pm;
		swingWorker = sw;
		mainWindow = ipm;
	}
	
	
	/**
	 * This will attempt to retrieve a HashMap of errors from LoadFile's Class once it is finished or this is cancelled.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		
		if (arg0.getPropertyName().equals("progress"))
		{
			int value = swingWorker.getProgress();
						
			monitor.setProgress(value);
			if (monitor.isCanceled() || swingWorker.isDone())
			{
				if (monitor.isCanceled())
					swingWorker.cancel(true);
				try
				{
					HashMap<String, ArrayList<String>> data = swingWorker.get();
					mainWindow.finishLoading(data);
				}
				catch (Exception e)
				{
					HashMap<String, ArrayList<String>> data = ((LoadFiles) swingWorker).getData();
					mainWindow.finishLoading(data);
				}
			}
		}
	}
}
