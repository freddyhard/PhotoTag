package launch;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import javax.imageio.ImageIO;
//import javax.media.jai.JAI;
//import javax.media.jai.RenderedOp;
//import javax.media.jai.operator.SubsampleAverageDescriptor;
import javax.swing.ImageIcon;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
/**
 * Holds the data for each Photograph
 * @author root
 *
 */

public class ThumbnailObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ArrayList<String> searchWords;
	private File photoFile;
	private File photoThumbnail;
	private transient BufferedImage thumbNail;
	

	private static final double THUMBNAIL_LONG_SIDE = 200;
	private static final ImageIcon OK = new ImageIcon("src/gphx/ok.gif");
	private static final ImageIcon LOCKED = new ImageIcon("src/gphx/locked.gif");
	
	public static final ImageIcon ERROR = new ImageIcon("src/gphx/error.gif");
	
	public static final String [] HEADER = {"File Path", "Found", "Key Words", "Thumbnail"};
	public static final String [] WORD_WRAP_CELLS = {"Key Words"};
	public static final float [] HEADER_PERCENT_WIDTHS = {31f, 5f, 40f, 24f};
	

	/**
	 * This holds search words for an image. This also creates a thumb nail of the original image.
	 * @param photoFile The original image
	 * @param directory Where the thumb nail is save
	 * @throws IOException Fucked if i know
	 */
	public ThumbnailObject(File photoFile, String directory) throws IOException
	{
		searchWords = new ArrayList<>();
		this.photoFile = photoFile;		
		setThumbNail(directory);
	}
	
	/**
	 * This will delete the thumbnail saved to the HD associated with this object.
	 */
	public void delete()
	{
		if (photoThumbnail != null && photoThumbnail.exists())
			photoThumbnail.delete();
		photoThumbnail = null;
	}
	
	/**
	 * This will return the status of the photograph file that was used to create this object.
	 * @return one of 3 icons showing the file path status
	 */
	public ImageIcon getStatus()
	{
		try
		{
			if (photoFile.exists())
				return OK;
			return ERROR;
		} catch (SecurityException e)
		{
			//TODO: need to look more at this SecurityException
			return LOCKED;
		}
	}

	/**
	 * 
	 * @return an ArrayList of words associated with the photograph.
	 */
	public ArrayList<String> getSearchWords()
	{
		return searchWords;
	}

	/**
	 * This will overwrite or append new key words for searching.
	 * @param newWords The new search words for this photograph
	 * @param append If true then the ArrayList searchWords is added to the existing search words. If false it replaces it.
	 */
	public void setSearchWords(ArrayList<String> newWords, boolean append)
	{
		if (append)
		{
			ArrayList<String> holder = new ArrayList<String>(searchWords);// This really bursted my balls for a day
			holder.addAll(newWords);
			searchWords = holder;
		}
		else
			searchWords = newWords;
	}

	/**
	 * 
	 * @return The File that this object created a thumb nail from
	 */
	public File getFile()
	{
		return photoFile;
	}
	
	/**
	 * 
	 * @return The File of the PhotoThumbnail this object created.
	 */
	public File getPhotoThumbnail()
	{
		return photoThumbnail;
	}
	
	
	
	/**
	 * This will reassign the file path.
	 * @param filePath The new file path for this photograph.
	 */
	public void setFilePath(File filePath)
	{
		this.photoFile = filePath;
	}
	
	/**
	 * This will open the thumbnail file associated with the PhotoObject.
	 * @return Scaled down size of the original photograph
	 */
	public BufferedImage getThumbNail()
	{
		if (thumbNail == null)
			try
			{
				thumbNail = ImageIO.read(photoThumbnail);
			} catch (IOException e)
			{
				return null;
			}

		return thumbNail;
	}

	/**
	 * This will attempt to create a thumbnail of the photograph, with it's longest side set to 200 pixels. This will not scale up a smaller photograph.
	 * @param directory The directory to save the thumbnail to.
	 * @throws IOException Will be thrown if the file is not a readable graphic format or there was an error reading the file.
	 */
	private void setThumbNail(String directory) throws IOException
	{
		File jpgConvert = null;
		BufferedImage photo = ImageIO.read(photoFile);
		
		if (photo == null)
		{
			jpgConvert = convertToJPG(directory);
			
			if (jpgConvert == null)
				throw new IOException("File not readable.");//TODO: this comment isn't used. i should remove it?
			
			photo = ImageIO.read(jpgConvert);

			if (photo == null || photo.getType() == BufferedImage.TYPE_CUSTOM)
			{
				if (jpgConvert != null && jpgConvert.exists())
					jpgConvert.delete();
			
				throw new IOException("File not readable.");//TODO: Yeah this comment is still not used!
			}
		}
		
		double ratio = 0;
		if (photo.getWidth() > photo.getHeight())
			ratio = THUMBNAIL_LONG_SIDE / photo.getWidth();
		else
			ratio = THUMBNAIL_LONG_SIDE / photo.getHeight();
		
		if (ratio > 1)
			ratio = 1;// Do not scale up thumbnails of small photographs
		
		int newWidth = (int)(photo.getWidth() * ratio);
		int newHeight = (int)(photo.getHeight() * ratio);
		try
		{
			//int type = photo.getType();
			//type = (type == 0 ? BufferedImage.TYPE_3BYTE_BGR : type);// Temporary work around for TIF custom that is causing a crash
			thumbNail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);// Just set the thumbnail to be of type TYPE_3BYTE_BGR!

			Graphics2D g2d = thumbNail.createGraphics();
			g2d.drawImage(photo, 0, 0, newWidth, newHeight, null);
			g2d.dispose();
			String name = directory + UUID.randomUUID();
			photoThumbnail = new File(name);
			ImageIO.write(thumbNail, "jpg", photoThumbnail);
		} catch (IllegalArgumentException e)
		{
			// BufferedImage can throw IllegalArgumentException for a number of reasons e.g. width,height has to be > 0
			throw new IOException("File not readable.");//TODO: not using this comment!
		}
		if (jpgConvert != null && jpgConvert.exists())
			jpgConvert.delete();
	}
	
	/**
	 * THis will attempt to convert a TIFF file to a JPG.
	 * @param directory is the default directory to save the thumb nails.
	 * @return The converted file as a JPG or null if colorModel is null or Exception is thrown
	 */
	private File convertToJPG(String directory)
	{
		// Thanks to stackoverflow for this one.
		try
		{
			SeekableStream s = new FileSeekableStream(photoFile);
			TIFFDecodeParam param = null;
			ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
			RenderedImage op = dec.decodeAsRenderedImage();

			if (op.getColorModel() == null)
				return null;//because en.encode(op) gets lost when colormodel is null. i cannot get good docs on JAI to understand what is going wrong.
			
			File tempConvert = new File(directory + UUID.randomUUID());
			
			FileOutputStream fos = new FileOutputStream(tempConvert);
			JPEGEncodeParam jpgparam = new JPEGEncodeParam();
			jpgparam.setQuality(67);
			ImageEncoder en = ImageCodec.createImageEncoder("jpeg", fos,jpgparam);
			en.encode(op);
			fos.flush();
			fos.close();
			return tempConvert;
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
		catch (IOException e)
		{
			return null;
		}
	}
}
