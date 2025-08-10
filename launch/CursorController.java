package launch;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

public class CursorController
{

	public static final Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
    public static final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    public static final int delay = 150;
    
    private CursorController() {}
    
    
    public static ActionListener createListener(final Component component, final ActionListener mainActionListener)
    {
    	ActionListener actionListener = new ActionListener()
		{
			
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				TimerTask timerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						component.setCursor(busyCursor);
					}
				};
				Timer timer = new Timer();
				try
				{
					timer.schedule(timerTask, delay);
					mainActionListener.actionPerformed(e);
				}
				finally
				{
					timer.cancel();
					component.setCursor(defaultCursor);
				}
			}
		};
		
		
		return actionListener;
    }
    
    
    public static KeyListener createKeyListener(final Component component, final KeyListener mainKeyListener)
    {
    	KeyListener keyListener = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				TimerTask timerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						component.setCursor(busyCursor);
					}
				};
				Timer timer = new Timer();
				try
				{
					timer.schedule(timerTask, delay);
					mainKeyListener.keyPressed(e);
				}
				finally
				{
					timer.cancel();
					component.setCursor(defaultCursor);
				}
				
			}
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
		};
		
		
    	return keyListener;
    }
    
}
