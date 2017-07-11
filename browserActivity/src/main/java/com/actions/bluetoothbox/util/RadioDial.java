package com.actions.bluetoothbox.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.actions.bluetoothbox.R;

public class RadioDial extends View {
	private static final String TAG = "RadioDial";

	public static final int RADIO_SYSTEM_CN_US = 0;
	public static final int RADIO_SYSTEM_JAPAN = 1;
	public static final int RADIO_SYSTEM_EUROPE = 2;
	private Context mContext;
	private static final int SCALE_JAPAN_START = 76000;
	private static final int SCALE_JAPAN_END = 90000;
	private static final int SCALE_OTHER_START = 87500;
	private static final int SCALE_OTHER_END = 108000;
	private static final int SCALE_UNIT = 250;

	private static final int PADDING_PIXEL_MIN = 10;
	private static final float LOCATION_INDICATOR = 0.3f;
	private static final float LOCATION_NUMBER = 0.5f;
	private static final float LOCATION_SCALE = 0.6f;

	private Bitmap mDialBg = null;
	private Paint mFrePaint = null;
	private Paint mLinePaint = null;
	private Paint mPointerPaint = null;
	private Paint mIndicatorPaint = null;

	private float mPointerPosition = 0f;
	private int mFrequence = 0;
	private boolean mInitialized = false;
	private int mViewWidth = 0;
	private int mViewHeight = 0;
	private int mFreqDots = 0;
	private float mDotStep = 0f;
	private float mPaddingLeft = 0f;
	private float mPaddingRight = 0f;
	private int mStartFreq = SCALE_OTHER_START;
	private int mEndFreq = SCALE_OTHER_END;
	private float[] mLineRegion;
	private float[] mTextCoordinate;
	private int mRadioSystem = RADIO_SYSTEM_EUROPE;
	private final static DecimalFormat mDecimalFormat[] = { new DecimalFormat("###.0"), new DecimalFormat("###.00") };
	private final static int mPaddingDistance[] = { 50, 65 };
	private int mPD = mPaddingDistance[0];

	private OnChannelChangeListener mChannelListener = null;

	private int VIEW_WIDTH_MIN = 240;
	private int VIEW_HEIGHT_MIN = 90;

	public RadioDial(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);

		mIndicatorPaint = new Paint();
		mIndicatorPaint.setColor(0xFFFFFFFF);
		mIndicatorPaint.setTextSize(25 * Utils.screenDensity(mContext));

		mFrePaint = new Paint();
		mFrePaint.setColor(0xFFFFFFFF);
		mFrePaint.setTextSize(10 * Utils.screenDensity(mContext));

		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.FILL);
		mLinePaint.setColor(0xFFFFFFFF);

		mPointerPaint = new Paint();
		mPointerPaint.setStyle(Style.FILL);
		mPointerPaint.setColor(0xFFCC0000);
	}

	public void setRadioSystem(int system) {
		Log.v(TAG, "setRadioSystem " + system);
		if (mRadioSystem != system) {
			mInitialized = false;
			mRadioSystem = system;
		}
		if (system != RADIO_SYSTEM_EUROPE) {
			mPD = mPaddingDistance[0];
		} else {
			mPD = mPaddingDistance[1];
		}
		invalidate();
	}

	private void initEnvironment() {
		if (mInitialized)
			return;

		mViewWidth = getWidth();
		mViewHeight = getHeight();

		mDialBg = createBackgroundBitmap();

		if (mRadioSystem == RADIO_SYSTEM_JAPAN) {
			mStartFreq = SCALE_JAPAN_START;
			mEndFreq = SCALE_JAPAN_END;
			mTextCoordinate = new float[6];
		} else {
			mStartFreq = SCALE_OTHER_START;
			mEndFreq = SCALE_OTHER_END;
			mTextCoordinate = new float[8];
		}

		mFreqDots = (mEndFreq - mStartFreq + 100) / SCALE_UNIT;
		mDotStep = (mViewWidth - PADDING_PIXEL_MIN) / mFreqDots - 1;
		if (mRadioSystem == RADIO_SYSTEM_JAPAN) {
			mPaddingLeft = (mViewWidth - (mFreqDots * mDotStep + mFreqDots + 1)) / Utils.screenDensity(mContext) / 2;
		} else {
			mPaddingLeft = (mViewWidth - (mFreqDots * mDotStep + mFreqDots + 1)) / 2;
		}

		mPaddingRight = mViewWidth - mPaddingLeft;
		mPointerPosition = mPaddingLeft + 1;

		mLineRegion = new float[4 * (mFreqDots + 2)];
		int index = 0;
		for (int i = 0; i <= mFreqDots; i++) {
			int frequence = mStartFreq + SCALE_UNIT * i;
			float position = mPaddingLeft + (mDotStep + 1) * i + 1;

			mLineRegion[i * 4] = position;
			mLineRegion[i * 4 + 2] = position;

			if (frequence % 5000 == 0) {
				mLineRegion[i * 4 + 1] = mViewHeight * LOCATION_SCALE;
				mLineRegion[i * 4 + 3] = mViewHeight * (LOCATION_SCALE + 0.3f);
				mTextCoordinate[index++] = position;
				mTextCoordinate[index++] = mViewHeight * LOCATION_NUMBER;
			} else if (frequence % 1000 == 0) {
				mLineRegion[i * 4 + 1] = mViewHeight * (LOCATION_SCALE + 0.05f);
				mLineRegion[i * 4 + 3] = mViewHeight * (LOCATION_SCALE + 0.25f);
			} else {
				mLineRegion[i * 4 + 1] = mViewHeight * (LOCATION_SCALE + 0.1f);
				mLineRegion[i * 4 + 3] = mViewHeight * (LOCATION_SCALE + 0.2f);
			}
		}
		mLineRegion[4 * (mFreqDots + 1)] = 0;
		mLineRegion[4 * (mFreqDots + 1) + 1] = mViewHeight * (LOCATION_SCALE + 0.15f);
		mLineRegion[4 * (mFreqDots + 1) + 2] = mViewWidth + 1;
		mLineRegion[4 * (mFreqDots + 1) + 3] = mViewHeight * (LOCATION_SCALE + 0.15f);

		if (mFrequence != 0) {
			setFrequence(mFrequence);
		} else if (mRadioSystem == RADIO_SYSTEM_JAPAN) {
			setFrequence(76000);
		} else {
			setFrequence(87500);
		}
		mInitialized = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		initEnvironment();
		canvas.drawBitmap(mDialBg, 0, 0, null);
		drawScale(canvas);
		drawPointer(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int dw = VIEW_WIDTH_MIN;
		int dh = VIEW_HEIGHT_MIN;
		setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
		// setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
		// resolveSizeAndState(dh, heightMeasureSpec, 0));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		if (event.getX() <= mPaddingLeft) {
			mPointerPosition = mPaddingLeft + 1;
		} else if (event.getX() >= mPaddingRight) {
			mPointerPosition = mPaddingRight - 1;
		} else {
			mPointerPosition = event.getX();
		}

		computeFrequence();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mChannelListener != null) {
				mChannelListener.onStartTrackingTouch(this);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mChannelListener != null) {
				mChannelListener.onChannelChanged(this, getFrequence());
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mChannelListener != null) {
				mChannelListener.onStopTrackingTouch(this);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mChannelListener != null) {
				mChannelListener.onStopTrackingTouch(this);
			}
			break;
		default:
			break;
		}

		invalidate();
		return true;
	}

	private void drawPointer(Canvas canvas) {
		canvas.drawText(frequenceToString(getFrequence()) + " MHz", mViewWidth / 2 - mPD * Utils.screenDensity(mContext), mViewHeight * LOCATION_INDICATOR,
				mIndicatorPaint);
		canvas.drawRect(mPointerPosition - 1, mViewHeight * 0.1f, mPointerPosition + 1, mViewHeight * 0.9f, mPointerPaint);
	}

	private void drawScale(Canvas canvas) {
		if (mRadioSystem == RADIO_SYSTEM_JAPAN) {
			canvas.drawText("80", mTextCoordinate[0] - 7 * Utils.screenDensity(mContext), mTextCoordinate[1], mFrePaint);
			canvas.drawText("85", mTextCoordinate[2] - 7 * Utils.screenDensity(mContext), mTextCoordinate[3], mFrePaint);
			canvas.drawText("90", mTextCoordinate[4] - 7 * Utils.screenDensity(mContext), mTextCoordinate[5], mFrePaint);
		} else {
			canvas.drawText("90", mTextCoordinate[0] - 8, mTextCoordinate[1], mFrePaint);
			canvas.drawText("95", mTextCoordinate[2] - 8, mTextCoordinate[3], mFrePaint);
			canvas.drawText("100", mTextCoordinate[4] - 12, mTextCoordinate[5], mFrePaint);
			canvas.drawText("105", mTextCoordinate[6] - 12, mTextCoordinate[7], mFrePaint);
		}

		canvas.drawLines(mLineRegion, mLinePaint);
	}

	private Bitmap createBackgroundBitmap() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dial_bg);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix matrix = new Matrix();
		float scaleWidth = ((float) mViewWidth / w);
		float scaleHeight = ((float) mViewHeight / h);
		matrix.postScale(scaleWidth, scaleHeight);

		Bitmap background = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		bitmap.recycle();

		return background;
	}

	public void setOnChannelListener(OnChannelChangeListener listener) {
		mChannelListener = listener;
	}

	public static interface OnChannelChangeListener {

		public void onStartTrackingTouch(RadioDial dial);

		public void onStopTrackingTouch(RadioDial dial);

		public void onChannelChanged(RadioDial dial, int frequence);

	}

	private void computeFrequence() {
		int frequence = 0;
		float distance = mPointerPosition - mPaddingLeft - 1;
		int dots = (int) (distance / (mDotStep + 1));
		float rsd = distance % (mDotStep + 1);
		if (rsd < mDotStep / 4) {
			frequence = dots * SCALE_UNIT;
		} else if (rsd > mDotStep * 3 / 4) {
			frequence = (dots + 1) * SCALE_UNIT;
		} else {
			frequence = dots * SCALE_UNIT + SCALE_UNIT / 2;
		}

		mFrequence = getMinFrequence() + frequence;
		if (mFrequence < getMinFrequence()) {
			mFrequence = getMinFrequence();
		} else if (mFrequence > getMaxFrequence()) {
			mFrequence = getMaxFrequence();
		}
	}

	private int getMinFrequence() {
		return mRadioSystem == RADIO_SYSTEM_JAPAN ? SCALE_JAPAN_START : SCALE_OTHER_START;
	}

	private int getMaxFrequence() {
		return mRadioSystem == RADIO_SYSTEM_JAPAN ? SCALE_JAPAN_END : SCALE_OTHER_END;
	}

	public int getFrequence() {
		return mFrequence;
	}

	public void setFrequence(int frequence) {
		mFrequence = frequence;
		if (frequence < getMinFrequence()) {
			frequence = getMinFrequence();
		} else if (frequence > getMaxFrequence()) {
			frequence = getMaxFrequence();
		}

		int dots = (frequence - getMinFrequence()) / SCALE_UNIT;
		int rsd = (frequence - getMinFrequence()) % SCALE_UNIT;
		float distance = dots * (mDotStep + 1);
		if (rsd != 0) {
			distance = distance + mDotStep / 2;
		}

		mPointerPosition = mPaddingLeft + distance + 1;
		invalidate();
	}

	private String frequenceToString(int frequence) {
		if (mRadioSystem != RADIO_SYSTEM_EUROPE) {
			return mDecimalFormat[0].format(frequence * 0.001);
		} else {
			return mDecimalFormat[1].format(frequence * 0.001);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (mDialBg != null) {
			mDialBg.recycle();
		}
	}
}
