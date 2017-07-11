package com.actions.bluetoothbox.util;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {
    @SuppressWarnings("unused")
    private static final String TAG = "VerticalSeekBar";

    public VerticalSeekBar(Context context) {
        super(context);

    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(height, width);
        setMeasuredDimension(getMeasuredWidth(), height);
    }

    @Override
	protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        super.onTouchEvent(event);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_UP:
            setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            break;

        case MotionEvent.ACTION_CANCEL:
            break;
        }
        
        return true;
    }

    public synchronized void setProgressAndThumb(int progress) {
        setProgress(getMax() - (getMax() - progress));
        onSizeChanged(getWidth(), getHeight(), 0, 0);
        invalidate();
    }

    public synchronized void setMaximum(int maximum) {
        setMax(maximum);
    }

    public synchronized int getMaximum() {
        return getMax();
    }
}