package Interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingWorker;

public interface IProgressMonitor
{
	void startMonitor(SwingWorker<Object[][], Void> sw);
	void finish(Object[][] o);
	
	void startLoadingMonitor(SwingWorker<HashMap<String, ArrayList<String>>, Void> sw);
	void finishLoading(HashMap<String, ArrayList<String>> hm);
}
