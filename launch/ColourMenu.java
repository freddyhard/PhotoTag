package launch;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Interfaces.IReDraw;

@SuppressWarnings("serial")
public class ColourMenu extends JMenu implements ActionListener
{
	private IReDraw notifyParent;
	private JFrame jFrame;
	private JRadioButtonMenuItem [] menuButtons;

	private HashMap<String, String> colourSchemes;
	
	
	
	public ColourMenu(JFrame jFrame, IReDraw parent, JMenuBar mainMenu)
	{
		colourSchemes = new HashMap<>();
		colourSchemes.put("Acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
		colourSchemes.put("Aero", "com.jtattoo.plaf.aero.AeroLookAndFeel");
		colourSchemes.put("Aluminium", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		colourSchemes.put("Berstein", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
		colourSchemes.put("Fast", "com.jtattoo.plaf.fast.FastLookAndFeel");
		colourSchemes.put("Graphite", "com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
		colourSchemes.put("HiFi", "com.jtattoo.plaf.hifi.HiFiLookAndFeel");
		colourSchemes.put("Luna", "com.jtattoo.plaf.luna.LunaLookAndFeel");
		colourSchemes.put("McWin", "com.jtattoo.plaf.mcwin.McWinLookAndFeel");
		colourSchemes.put("Mint", "com.jtattoo.plaf.mint.MintLookAndFeel");
		colourSchemes.put("Noire", "com.jtattoo.plaf.noire.NoireLookAndFeel");
		colourSchemes.put("Smart", "com.jtattoo.plaf.smart.SmartLookAndFeel");
		colourSchemes.put("Texture", "com.jtattoo.plaf.texture.TextureLookAndFeel");
		
		menuButtons = new JRadioButtonMenuItem[colourSchemes.size()];
		notifyParent = parent;
		this.jFrame = jFrame;
		createMenu(mainMenu);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		String colourScheme = "";
		for (int f = 0; f < menuButtons.length; f++)
		{
			if (arg0.getSource() == menuButtons[f])
			{
				setScheme(colourSchemes.get(menuButtons[f].getText()));
				break;
			}
		}
		
		if (colourScheme.equals(""))
			return;
		
		setScheme(colourScheme);
	}
	
	/**
	 * Creates the colour menu and applies action to it.
	 * @param parentMenu JMenuBar to attach to.
	 */
	private void createMenu(JMenuBar parentMenu)
	{
		setText("Colour Mode");
		
		setMnemonic(KeyEvent.VK_C);
		parentMenu.add(this);

		ButtonGroup group = new ButtonGroup();
		
		int counter = 0;
		for (String name: colourSchemes.keySet())
		{
			menuButtons[counter] = new JRadioButtonMenuItem(name);
			menuButtons[counter].addActionListener(this);
			
			// Just for me
			if (menuButtons[counter].getText().equals("HiFi"))
				menuButtons[counter].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
			
			group.add(menuButtons[counter]);
			add(menuButtons[counter++]);
		}
	}

	
	
	/**
	 * This will attempt to apply a selected JTattoo scheme.
	 * @param scheme String value of the JTattoo library.
	 */
	private void setScheme(String scheme)
	{
		try
		{
			UIManager.setLookAndFeel(scheme);
			SwingUtilities.updateComponentTreeUI(jFrame);
			notifyParent.redrawTable();
		} 
		catch (ClassNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Not Coloured In.", "OOPS", JOptionPane.ERROR_MESSAGE);
		}
		catch (InstantiationException e)
		{
			JOptionPane.showMessageDialog(null, "Not Coloured In.", "OOPS", JOptionPane.ERROR_MESSAGE);
		}
		catch (IllegalAccessException e)
		{
			JOptionPane.showMessageDialog(null, "Not Coloured In.", "OOPS", JOptionPane.ERROR_MESSAGE);
		}
		catch (UnsupportedLookAndFeelException e)
		{
			JOptionPane.showMessageDialog(null, "Not Coloured In.", "OOPS", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
}
