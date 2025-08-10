package Interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.filechooser.FileNameExtensionFilter;

import launch.PhotoManager.Message;

public interface IFunctions
{
	void add(File [] filePaths);// throws FileNotFoundException, IOException;
	void modify(String originalKey, String newKey, String searchWords) throws FileNotFoundException, IOException;
	void modify(String [] keys, String searchWords) throws FileNotFoundException, IOException;
	void findError();
	void remove(Object [] removeObjects) throws FileNotFoundException, IOException;
	void search(String inputInclude, String inputExclude, boolean matchAny, boolean matchAnyNot);
	String [] getHeader();
	String [] getWordWrapCells();
	float [] getColumnRatios();
	int getKeyColumn();
	boolean keyExists(String file);
	String getWords(String filePath);
	void getUpdatedObject(String [] keys);
	File getFile(Object keyColumn);
	FileNameExtensionFilter[] getFileExtensions();
	FileNameExtensionFilter getGraphicFileExtension();
	void getNewEntries();
	Message getTitleMessage();
	void setMonitor(IProgressMonitor pm);
	void finishAdding(HashMap<String, ArrayList<String>> loadingProblems) throws FileNotFoundException, IOException;
}
