/* Copyright 2020 Yury Borodin. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.yuryborodin.lookanddo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Path;
import android.graphics.Paint;
import java.util.ArrayList;

public class DrawView extends View {
    /**
     * Constant, defines brush size for drawing
     * @since 1.0
     */
    public static int BRUSH_SIZE = 5;

    /**
     * Default color for drawing
     * @since 1.0
     */
    public static final int DEFAULT_COLOR = Color.BLACK;

    /**
     * Default color for background of canvas
     * @since 1.0
     */
    public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    /**
     * Touch tolerance
     * @since 1.0
     */
    private static final float TOUCH_TOLERANCE = 4;

    /**
     * Start x coordinate
     * @since 1.0
     */
    private float mX = 0;

    /**
     * Start y coordinate
     * @since 1.0
     */
    private float mY = 0;

    /**
     * Path object
     * @see Path for further info
     * @since 1.0
     */
    private Path mPath;

    /**
     * Paint object
     * @see Paint for more info
     * @since 1.0
     */
    private Paint mPaint;

    /**
     * Array of paths
     * @since 1.0
     */
    private ArrayList<FingerHandler> paths = new ArrayList<>();

    /**
     * Current color of a brush
     * @since 1.0
     */
    private int mCurrentColor;

    /**
     * Current background color of canvas
     * @since 1.0
     */
    private int mCurrentBackgroundColor = DEFAULT_BACKGROUND_COLOR;

    /**
     * Boolean variable that defines whether the DrawView object is enabled
     * @since 1.0
     */
    private boolean mEnabled = true;

    /**
     * Current stroke width
     * @since 1.0
     */
    private int mCurrentStrokeWidth;

    /**
     * Bitmap object
     * @since 1.0
     */
    public Bitmap mBitmap = null;

    /**
     * Canvas object
     * @since 1.0
     */
    private Canvas mCanvas = null;


    /**
     * Paint object
     * @since 1.0
     */
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    /**
     * Constructor of this class
     * @param context Context object
     * @param attrs AttributeSet object
     * @param defStyle default style
     * @since 1.0
     */
    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Constructor of this class
     * @param context Context object
     * @since 1.0
     */
    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmap = Bitmap.createBitmap(getSuggestedMinimumWidth(), getSuggestedMinimumHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

    }

    /**
     * Constructor of the class
     * @param context Context object
     * @param attrs AttributeSet object
     * @since 1.0
     */
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
        mPath = new Path();
        mBitmap = Bitmap.createBitmap(700, 500, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    /**
     * This method is used to enable/disable painting
     * @param enable boolean (true for enabling painting, false for disabling)
     * @since 1.0
     */
    public void enablePaint(boolean enable){
        if (enable){
            this.mEnabled = true;
        } else {
            this.mEnabled = false;
        }
    }

    /**
     * This method is used to initialize PainView object within an activity
     * @param metrics DisplayMetrics
     * @since 1.0
     */
    public void init(DisplayMetrics metrics) {
        setMeasuredDimension(metrics.widthPixels, metrics.heightPixels);
        mBitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels,
                Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCurrentColor = DEFAULT_COLOR;
        mCurrentStrokeWidth = BRUSH_SIZE;
    }

    /**
     * This method is used to clear the canvas
     * @since 1.0
     */
    public void clear() {
        mCurrentBackgroundColor = DEFAULT_BACKGROUND_COLOR;
        paths.clear();
        mX = 0;
        mY = 0;
        invalidate();
    }

    /**
     * This method is used for extracting bitmap from DrawView object
     * @return bm Bitmap object
     * @since 1.0
     */
    public Bitmap proceed() {
        Bitmap bm = mBitmap;
        return bm;
    }

    /**
     * This is an overridden method used for drawing images
     * See documentations on View class
     * @param canvas Canvas object
     */
    @Override
    public void onDraw(Canvas canvas){
        canvas.save();
        mCanvas.drawColor(mCurrentBackgroundColor);
        super.onDraw(canvas);
        for(FingerHandler fp : paths){
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    /**
     * This method is used for handling paths of the user's finger
     * @param x x coordinate parameter
     * @param y y coordinate parameter
     */
    private void touchStart(float x, float y){
        mPath = new Path();
        FingerHandler fp = new FingerHandler(mCurrentColor, mCurrentStrokeWidth, mPath);
        paths.add(fp);
        mPath.reset();
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
    }

    /**
     * This parameter is used for checking whether the user tried painting something
     * @return boolean true if user touched the canvas, false if not
     * @since 1.0
     */
    public boolean checkIfPainted() {
        // returns false if not painted
        return mX != 0 && mY != 0;
    }

    /**
     * This method is used for handling touch events
     * @param x coordinate x
     * @param y coordinate y
     * @since 1.0
     */
    private void touchMove(float x, float y){
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(mX, mY, (x + mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
    }

    /**
     *This method is used for handling the last position of the finger on screen
     * @since 1.0
     */
    private void touchUp(){
        mPath.lineTo(mX, mY);
    }

    /**
     * This method is used for handling user's finger touches in general
     * @param event MotionEvent object
     * @return boolean parameter, returns true if drawing is enabled, false if not
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!mEnabled) return  false;
        else {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
        }
    }
}
