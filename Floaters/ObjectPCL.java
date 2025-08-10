package Floaters;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import Interfaces.IProgressMonitor;

public class ObjectPCL implements PropertyChangeListener
{

	ProgressMonitor monitor;
	SwingWorker<Object[][], Void> swingWorker;
	IProgressMonitor mainWindow;
	
	
	public ObjectPCL(ProgressMonitor pm, SwingWorker<Object[][], Void> sw, IProgressMonitor ipm)
	{
		monitor = pm;
		swingWorker = sw;
		mainWindow = ipm;
	}
	
	/**
	 * This will attempt to retrieve an Object[][] from BuildArray Class once it is finished or this is cancelled. 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("progress"))
		{
			int value = swingWorker.getProgress();
			
			monitor.setProgress(value);
			if (monitor.isCanceled() || swingWorker.isDone())
			{
				if (monitor.isCanceled())
					swingWorker.cancel(true);
				try
				{
					Object[][] data = swingWorker.get();
					mainWindow.finish(data);
				} 
				catch (Exception e)
				{
					Object[][] data = ((BuildArray) swingWorker).getTrimmedArray();
					mainWindow.finish(data);
				}
			}
		}
	}
}
