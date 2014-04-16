package com.assignment2.takeandsavephoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class TakeAndSavePicture extends Activity {

	private SurfaceView surfacePreviewPhoto = null;
	private ImageView imagePreviewPhoto = null;
	private SurfaceHolder surfacePreviewHolder = null;
	private Camera photoCamera = null;
	private byte[] photoData = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_take_and_save_picture);
		surfacePreviewPhoto = (SurfaceView) findViewById(R.id.surfacePreviewPhoto);
		imagePreviewPhoto = (ImageView) findViewById(R.id.imagePreviewPhoto);
		surfacePreviewHolder = surfacePreviewPhoto.getHolder();
		surfacePreviewHolder.addCallback(surfaceCallback);
		surfacePreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (photoCamera == null) {
			photoCamera = Camera.open();
		}
		photoCamera.startPreview();
	}

	@Override
	public void onPause() {
		super.onPause();
		photoCamera.stopPreview();
		photoCamera.release();
		photoCamera = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.take_and_save_picture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_take_photo) {
			photoCamera.takePicture(null, null, photoCallback);
		}

		if (item.getItemId() == R.id.item_save_photo) {
			if (photoData == null) {
				Toast.makeText(TakeAndSavePicture.this,
						"Press 'Take Photo' \n then you can save the file",
						Toast.LENGTH_LONG).show();
				return false;
			}
			AlertDialog.Builder inputDialogFileName = new AlertDialog.Builder(
					this);

			inputDialogFileName.setTitle("Enter the file name");
			inputDialogFileName.setMessage("The file name:");
			final EditText inputEditText = new EditText(this);
			inputDialogFileName.setView(inputEditText);
			inputDialogFileName.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// onCanceled
						}
					});
			inputDialogFileName.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String fileName = inputEditText.getText()
									.toString();
							if (fileName.isEmpty()) {
								Toast.makeText(TakeAndSavePicture.this,
										"Enter characters", Toast.LENGTH_LONG)
										.show();
								return;
							}
							File filePhoto = new File(Environment
									.getExternalStoragePublicDirectory(
											Environment.DIRECTORY_PICTURES)
									.getPath()
									+ "/" + fileName + ".jpg");
							FileOutputStream outputPhotoFileStream;
							try {
								outputPhotoFileStream = new FileOutputStream(
										filePhoto, filePhoto.createNewFile());
								outputPhotoFileStream.write(photoData);
								outputPhotoFileStream.flush();
								outputPhotoFileStream.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			inputDialogFileName.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// wait until surfaceChanged()
		}

	/*	private Camera.Size getBestPreviewSize(int width, int height,
				Camera.Parameters parameters) {
			Camera.Size result = null;
			for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
				if (size.width <= width && size.height <= height) {
					if (result == null) {
						result = size;
					} else {
						int resultArea = result.width * result.height;
						int newArea = size.width * size.height;
						if (newArea > resultArea) {
							result = size;
						}
					}
				}
			}
			return (result);
		}

		private void initPreview(int width, int height) {
			if (photoCamera != null
					&& surfacePreviewHolder.getSurface() != null) {
				try {
					photoCamera.setPreviewDisplay(surfacePreviewHolder);
				} catch (Throwable t) {
					Toast.makeText(TakeAndSavePicture.this, t.getMessage(),
							Toast.LENGTH_LONG).show();
				}
				Camera.Parameters parameters = photoCamera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);
				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					parameters.setPictureFormat(ImageFormat.JPEG);
					photoCamera.setParameters(parameters);
				}
			}

		}*/

		void setCameraDisplayOrientation() {
			// get degree of Rotation
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			int degrees = 0;
			switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			}
			int result = 0;
			// get camera info cameraId
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(0, info); // 0 is back camera
			result = ((360 - degrees) + info.orientation);
			result %= 360;
			photoCamera.setDisplayOrientation(result);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			setCameraDisplayOrientation();
			try {
				photoCamera.setPreviewDisplay(holder);
				Toast.makeText(TakeAndSavePicture.this,
						"Surface size is " + width + " w, " + height + " h",
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(TakeAndSavePicture.this, e.toString(),
						Toast.LENGTH_LONG).show();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePhotoTask().execute(data);
		}
	};

	class SavePhotoTask extends AsyncTask<byte[], String, String> {
		@Override
		protected String doInBackground(byte[]... data) {
			photoData = null;
			photoData = Arrays.copyOf(data[0], data[0].length);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			imagePreviewPhoto.setImageBitmap(BitmapFactory.decodeByteArray(
					photoData, 0, photoData.length));
		}
	}

}