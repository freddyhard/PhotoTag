package launch;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
/**
 * I wrote this for me. 
 * @author When in Rome, don't laugh at the Romans.
 *
 */
public class Start
{
	
	public static void main(String[] args)
	{
		try
		{
			MainWindow cWindow = new MainWindow(new PhotoManager());
			cWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			cWindow.setVisible(true);
		} 
		catch (FileNotFoundException e)
		{
			int option = JOptionPane.showConfirmDialog(null, "DataBase Not Found!\nCreate a new one?", "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.OK_OPTION)
			{
				try
				{
					MainWindow cWindow = new MainWindow(new PhotoManager(true));
					cWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					cWindow.setVisible(true);
				}
				catch (FileNotFoundException e2)
				{
					JOptionPane.showMessageDialog(null, "Cannot Access DataBase.\nAccess rights might be denied.", "CRITICAL", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				catch (IOException e2)
				{
					JOptionPane.showMessageDialog(null, "IO Exception was thrown.", "CRITICAL", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Cannot Access DataBase.\nIt might be corrupted or be already in use.", "CRITICAL", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Class Not Found Exception was thrown.", "CRITICAL", JOptionPane.ERROR_MESSAGE);
			//System.exit(-1);
		}
	}
}
