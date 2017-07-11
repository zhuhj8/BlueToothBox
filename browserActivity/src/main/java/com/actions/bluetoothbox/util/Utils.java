package com.actions.bluetoothbox.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

public class Utils {
	static private Context sContext = null;
	static private Toast sToast = null;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static Point screenSize(Context ctx) {
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			display.getSize(size);
		} else {
			size.x = display.getWidth();
			size.y = display.getHeight();
		}
		return size;
	}

	public static float screenDensity(Context ctx) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getMetrics(dm);
		return dm.density;
	}

	public static String showTime(int time) {
		// make ms change to s
		time /= 1000;
		int hour;
		int minute;
		int second;
		if (time >= 3600) {
			hour = time / 3600;
			minute = (time % 3600) / 60;
			second = (time % 3600) % 60;// time-hour*3600-minute*60
			return String.format("%02d:%02d:%02d", hour, minute, second);
		} else {
			minute = time / 60;
			second = time % 60;
			return String.format("%02d:%02d", minute, second);
		}

	}

	public static int generateRandom(int num, int index) {
		Random ran = new Random();
		int ranNum = ran.nextInt(num);
		if (num > 0) {
			if (ranNum == index) {
				ranNum = generateRandom(num, index);
			}
		}
		return ranNum;
	}

	public static void setAlphaForView(View v, float alpha) {
		AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
		animation.setDuration(0);
		animation.setFillAfter(true);
		v.startAnimation(animation);
	}

	public static void displayToast(int resId) {
		if (sToast == null) {
			sToast = Toast.makeText(sContext, resId, Toast.LENGTH_SHORT);
		} else {
			sToast.setText(resId);
		}
		sToast.show();
	}

	public static boolean[] checkExternalStorageAvailable() {
		boolean mExternalStorageReadable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageReadable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageReadable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageReadable = mExternalStorageWriteable = false;
		}
		boolean[] rwstate = { mExternalStorageReadable, mExternalStorageWriteable };
		return rwstate;
	}

	public static boolean hasExternalStoragePrivateFile(String filename) {
		if (checkExternalStorageAvailable()[0]) {
			File file = new File(sContext.getExternalFilesDir(null), filename);
			return file.exists();
		} else {
			Log.w("ExternalStorage", "Error reading");
			return false;
		}
	}

	// initialize
	public static void setContext(Context context) {
		sContext = context;
	}

	public static void createExternalStoragePrivateFile(String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(sContext.getExternalFilesDir(null), filename);
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				if (buffer != null) {
					os.write(buffer);
				}
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	public static void appendExternalStoragePrivateFile(String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(sContext.getExternalFilesDir(null), filename);
			OutputStream os = null;
			try {
				os = new FileOutputStream(file, true);
				os.write(buffer);
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] b = new byte[1024];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			outBuff.flush();
		} finally {
			// 关闭
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}
}
