package com.actions.bluetoothbox.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

public class Preferences {
	private static String TAG = "Preferences";

	public static final String KEY_EQUALIZER_TYPE = "equalizer_type";
	public static final String KEY_EQ_FREQUENCY_80 = "eq_frequency_80";
	public static final String KEY_EQ_FREQUENCY_200 = "eq_frequency_200";
	public static final String KEY_EQ_FREQUENCY_500 = "eq_frequency_500";
	public static final String KEY_EQ_FREQUENCY_1K = "eq_frequency_1K";
	public static final String KEY_EQ_FREQUENCY_4K = "eq_frequency_4K";
	public static final String KEY_EQ_FREQUENCY_8K = "eq_frequency_8K";
	public static final String KEY_EQ_FREQUENCY_16K = "eq_frequency_16K";

	public static final String KEY_LINEIN_EQUALIZER_TYPE = "linein_equalizer_type";
	public static final String KEY_LINEIN_EQ_FREQUENCY_80 = "linein_eq_frequency_80";
	public static final String KEY_LINEIN_EQ_FREQUENCY_200 = "linein_eq_frequency_200";
	public static final String KEY_LINEIN_EQ_FREQUENCY_500 = "linein_eq_frequency_500";
	public static final String KEY_LINEIN_EQ_FREQUENCY_1K = "linein_eq_frequency_1K";
	public static final String KEY_LINEIN_EQ_FREQUENCY_4K = "linein_eq_frequency_4K";
	public static final String KEY_LINEIN_EQ_FREQUENCY_8K = "linein_eq_frequency_8K";
	public static final String KEY_LINEIN_EQ_FREQUENCY_16K = "linein_eq_frequency_16K";

	public static final String KEY_DAE_MODE = "key_dae_mode";
	public static final String KEY_DAE_OPTION = "key_dae_type";
	
	public static final String KEY_RADIO_BAND_SIGN = "radio_band_sign";
	public static final String KEY_RADIO_CHANNEL_NUM = "channelNum_band_";
	public static final String KEY_RADIO_CHANNEL_PREFIX = "channel_";

	public static final String KEY_DEVICE_ADDRESS = "key_device_address";
	public static final String KEY_MUSIC_PLIST = "music_plist";
	public static final String KEY_UHOST_PLIST = "uhost_plist";
	public static final String KEY_CRECORD_PLIST = "crecord_plist";
	public static final String KEY_URECORD_PLIST = "urecord_plist";
	public static final String KEY_RECCARD_PLAYBACK_PLIST = "reccard_playback_plist";
	public static final String KEY_RECUHOST_PLAYBACK_PLIST = "recuhost_playback_plist";

	/** get preference settings */
	public static Object getPreferences(Context context, String key, Object deft) {
		try {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			Map<String, ?> settingMap = settings.getAll();
			Object obj = settingMap.get(key);
			return obj != null ? obj : deft;
		} catch (Exception ex) {
			Log.w(TAG, "null exception!");
			return 0;
		}

	}

	public static Map<String, ?> getPreferences(Context context) {
		try {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			return settings.getAll();
		} catch (Exception ex) {
			Log.w(TAG, "null exception!");
			return null;
		}

	}

	/** store preference settings */
	public static void setPreferences(Context context, String key, Object value) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof String) {
			editor.putString(key, (String) value);
		} else {
			Log.e(TAG, "Unexpected type:" + key + "=" + value);
		}
		editor.commit();
	}

	/** remove preference settings */
	public static void removePreferences(Context context, String key) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
		editor.commit();
	}

	/** store complex data in preference */
	public static void storeComplexDataInPreference(Context context, String key, Object object) {
		try {
			ByteArrayOutputStream toByte = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(toByte);
			oos.writeObject(object);
			String transformedString = new String(Base64.encode(toByte.toByteArray(), Base64.DEFAULT));
			Preferences.setPreferences(context, key, transformedString);
			toByte.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

	}

	/** get complex data in preference */
	public static Object getComplexDataInPreference(Context context, String key) {
		Object object = new Object();
		String transformedString = (String) Preferences.getPreferences(context, key, "");
		if (transformedString.length() != 0) {
			try {
				byte[] mobileBytes = Base64.decode(transformedString.getBytes(), Base64.DEFAULT);
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				object = objectInputStream.readObject();
				objectInputStream.close();
			} catch (Exception ex) {
				System.out.println(ex.toString());
			}
		}
		return object;
	}
}