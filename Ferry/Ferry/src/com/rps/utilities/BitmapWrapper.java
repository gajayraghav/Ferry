package com.rps.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

@SuppressWarnings("serial")
public class BitmapWrapper implements Serializable {

	int rowSize, height, width, ordinal;
	byte[] imageBytes;

	public BitmapWrapper(String fileName) {

		Bitmap image = getDownScaledBitmap(fileName,1024);

		rowSize = image.getRowBytes();
		height = image.getHeight();
		width = image.getWidth();
		ordinal = image.getConfig().ordinal();
		final int bmSize = rowSize * height;
		System.out.println("width " + width + "row size " + rowSize + "height "
				+ height + " allocating : " + bmSize);

		ByteBuffer dst = ByteBuffer.allocate(bmSize);
		dst.rewind();
		image.copyPixelsToBuffer(dst);
		dst.flip();
		imageBytes = dst.array();
	}

	public static Bitmap getDownScaledBitmap(String fileName,final int IMAGE_MAX_SIZE) {
		File sdImageFile = new File(fileName);
		Bitmap b = null;		
		try {
			// Decode image size
			BitmapFactory.Options oldOptions = new BitmapFactory.Options();
			oldOptions.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(sdImageFile);
			BitmapFactory.decodeStream(fis, null, oldOptions);
			fis.close();

			int scale = 1;
			if (oldOptions.outHeight > IMAGE_MAX_SIZE || oldOptions.outWidth > IMAGE_MAX_SIZE) {
				scale = (int) Math.pow(
						2,
						(int) Math.round(Math.log(IMAGE_MAX_SIZE
								/ (double) Math.max(oldOptions.outHeight, oldOptions.outWidth))
								/ Math.log(0.5)));
			}

			// Decode with inSampleSize
			BitmapFactory.Options newOptions = new BitmapFactory.Options();
			newOptions.inSampleSize = scale;
			fis = new FileInputStream(sdImageFile);
			b = BitmapFactory.decodeStream(fis, null, newOptions);
			fis.close();
		} catch (IOException e) {
		}
		return b;
	}

	private Bitmap getEquivalentBitmap() {
		Bitmap.Config config = Bitmap.Config.values()[ordinal];
		final int bmSize = rowSize * height;
		ByteBuffer dst = ByteBuffer.allocate(bmSize);
		dst.rewind();
		dst.put(imageBytes);
		dst.flip();
		Bitmap icon = Bitmap.createBitmap(width, height, config);
		icon.copyPixelsFromBuffer(dst);
		return icon;
	}

	public String saveImageToSDcard(ContentResolver contentResolve) {
		Bitmap image = getEquivalentBitmap();
		long recievedTime = System.currentTimeMillis();
		String photoPath = Environment.getExternalStorageDirectory()
				+ File.separator + "trialDir" + File.separator;

		File sdImageFile = new File(photoPath);
		sdImageFile.mkdirs();

		photoPath += "recieve_" + recievedTime + ".jpeg";
		sdImageFile = new File(photoPath);

		Uri outputFileUri = Uri.fromFile(sdImageFile);
		System.out.println("Cam : " + outputFileUri);
		OutputStream imageFileOS;
		try {
			imageFileOS = contentResolve.openOutputStream(outputFileUri);
			image.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);
			imageFileOS.flush();
			imageFileOS.close();
			System.out.println("Image saved");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return photoPath;
	}
}
