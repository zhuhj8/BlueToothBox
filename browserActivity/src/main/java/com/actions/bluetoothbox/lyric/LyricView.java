package com.actions.bluetoothbox.lyric;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.util.Utils;

public class LyricView extends TextView {
	private String Tag = "LyricView";
	private Paint mPaint;
	private Paint mFocusPaint;

	private int mX;
	private int mY;
	private float middleX;
	private float middleY;

	public int mOldY;
	public int mNewY;
	public boolean mDirection = true;
	public int mDistance;
	private int mDY = 20;

	private int mLyricDistanceY = 65;
	public static Lyric mLyric;

	public int index = 0;
	private List<Sentence> list = null;

	public LyricView(Context context, AttributeSet attr) {
		super(context, attr);
		setFocusable(true);
		TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.lyricView);
		int focusColor = typedArray.getColor(R.styleable.lyricView_textColor, Color.GREEN);
		float textSize = typedArray.getDimension(R.styleable.lyricView_textSize, 15 * Utils.screenDensity(context));
		mDY=(int) (mDY* Utils.screenDensity(context));
		mLyricDistanceY=(int) (mLyricDistanceY* Utils.screenDensity(context));
				
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);//25
		mPaint.setColor(Color.WHITE);
		mPaint.setTypeface(Typeface.SERIF);
		mPaint.setTextAlign(Paint.Align.CENTER);

		mFocusPaint = new Paint();
		mFocusPaint.setAntiAlias(true);
		mFocusPaint.setColor(focusColor);
		mFocusPaint.setTextSize(textSize);
		mFocusPaint.setTypeface(Typeface.SERIF);
		mFocusPaint.setTextAlign(Paint.Align.CENTER);

		typedArray.recycle();
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (list == null)
			return;
		if (index == -1)
			return;
		try {
			float temp = middleY;
			String[] currentLine = spiltLyric(list.get(index).getContent(), mPaint, mX - 50);
			if (currentLine.length > 1) {
				for (int i = 0; i < currentLine.length; i++) {
					canvas.drawText(currentLine[i], middleX, temp, mFocusPaint);
					temp += mDY;
				}
			} else {
				canvas.drawText(currentLine[0], middleX, middleY, mFocusPaint);
			}

			float tempY = middleY;
			for (int i = index - 1; i >= 0; i--) {
				tempY = tempY - mLyricDistanceY;
				if (tempY < 0) {
					break;
				}
				String[] preLine = spiltLyric(list.get(i).getContent(), mPaint, mX - 50);
				if (preLine.length == 1) {
					canvas.drawText(preLine[0], middleX, tempY, mPaint);
				} else {
					for (int j = 0; j < preLine.length; j++) {
						canvas.drawText(preLine[j], middleX, tempY + (mDY * j), mPaint);
					}
				}
			}

			tempY = middleY;
			for (int i = index + 1; i < list.size(); i++) {
				tempY = tempY + mLyricDistanceY;
				if (tempY > mY) {
					break;
				}
				String[] nextLine = spiltLyric(list.get(i).getContent(), mPaint, mX - 50);
				if (nextLine.length == 1) {
					canvas.drawText(nextLine[0], middleX, tempY, mPaint);
				} else {
					for (int j = 0; j < nextLine.length; j++) {
						canvas.drawText(nextLine[j], middleX, tempY + (mDY * j), mPaint);
					}
				}
			}
		} catch (Exception e) {
			// Trance care...
			return;
		}

	}

	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		mX = w;
		mY = h;
		// remember the center of the screen
		middleX = w * 0.5f;
		middleY = h * 0.5f;
	}

	public long updateIndex(long time) {
		if (list == null)
			return 0;
		index = mLyric.getNowSentenceIndex(time);
		updateView();
		if (index == -1)
			return 0;
		Sentence sen = list.get(index);
		long currentDunringTime = sen.getToTime() - time;
		return currentDunringTime;
	}

	public void setLyric(String filePath, String fileName) {
		mLyric = new Lyric(new File(filePath), fileName);
		list = mLyric.list;
		updateView();
	}

	private String[] spiltLyric(String content, Paint p, float width) {
		int length = content.length();
		float textWidth = p.measureText(content);
		if (textWidth <= width) {
			return new String[] { content };
		}

		int start = 0, end = 1, i = 0;
		int lines = (int) Math.ceil(textWidth / width);
		String[] lineTexts = new String[lines];
		while (start < length) {
			if (p.measureText(content, start, end) > width) {
				lineTexts[i++] = (String) content.subSequence(start, end);
				start = end;
			}
			if (end == length) {
				lineTexts[i] = (String) content.subSequence(start, end);
				break;
			}
			end += 1;
		}
		return lineTexts;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mOldY = (int) event.getY();// float DownY
			break;
		case MotionEvent.ACTION_MOVE:
			mNewY = (int) event.getY();
			mDirection = mNewY > mOldY ? true : false;
			mDistance = Math.abs(mNewY - mOldY);
			if (mDistance > (mDY / 2)) {
				if (mDirection) {
					if (index > 1) {
						index = index - 1;
					}
				} else {
					if (index < mLyric.getSongSize() - 1) {
						index = index + 1;
					}
				}
				updateView();
			}
			mOldY = mNewY;
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return true;
	}

	public void setInterval(int interval) {
		mLyricDistanceY = interval;
	}

	private void updateView() {
		invalidate();
	}
}