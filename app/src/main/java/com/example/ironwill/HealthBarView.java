package com.example.ironwill;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class HealthBarView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private float progress; // Progress value between 0 and 1

    public HealthBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFCCCCCC); // Background color
        progressPaint = new Paint();
        progressPaint.setColor(0xFFFF0000); // Progress color
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress)); // Ensure progress is between 0 and 1
        invalidate(); // Request a redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Draw progress based on the current progress value
        float progressWidth = getWidth() * progress;
        canvas.drawRect(0, 0, progressWidth, getHeight(), progressPaint);
    }
}
