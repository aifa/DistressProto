package com.afterhours.distressproto.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.afterhours.distressproto.util.MediaFile.MediaFileUtils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CameraUtils {

	private  static final String TAG = CameraUtils.class.getName();

	/** Check if this device has a camera */
	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e(TAG, e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}

	/** A safe way to get an instance of the Camera object. */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static Camera getCameraInstance(int i) {
		Camera c = null;
		try {
			c = Camera.open(i); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e(TAG, "Camera is not available (in use or does not exist):"+ e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}

	public static class CustomPictureCallback implements PictureCallback {

		private static Camera mCamera;
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			AsyncTask<byte[], String, Boolean> task = new AsyncTask<byte[], String, Boolean>() {

				@Override
				protected Boolean doInBackground(byte[]... data) {
					File pictureFile = MediaFileUtils
							.getOutputMediaFile(MediaFileUtils.MEDIA_TYPE_IMAGE);
					if (pictureFile == null) {
						Log.e(TAG,
								"Error creating media file, check storage permissions...");
						return false;
					}

					try {
						FileOutputStream fos = new FileOutputStream(pictureFile);
						fos.write(data[0]);
						fos.close();
						mCamera.startPreview();
						return true;
					} catch (FileNotFoundException e) {
						Log.e(TAG, "File not found: " + e.getMessage());
					} catch (IOException e) {
						Log.e(TAG, "Error accessing file: " + e.getMessage());
					}
					return false;
				}
			};
			mCamera = camera;
			task.execute(data);
		}

	}
	
	
	/**
	 * Take a picture from an open camera without previewing first
	 * 
	 * @param camera
	 *            : must be already open
	 */
	public static void takePictureNoPreview(Camera camera) {
		if (camera != null) {
			camera.takePicture(null, null, new CustomPictureCallback());

		} else {
			Log.d(TAG, "Camera object is null...");
		}
	}
	
	public static boolean releaseCamera(Camera camera){
		if (camera!=null){
			camera.release();
			camera = null;
			return true;
		}
		return false;
	}
}
