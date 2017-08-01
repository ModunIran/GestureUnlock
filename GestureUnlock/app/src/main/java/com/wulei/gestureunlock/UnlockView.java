package com.wulei.gestureunlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Wulei on 2017/7/27.
 */

public class UnlockView extends View {

    public static final int CIRCLE_NORMAL = 1;//normal state of circle
    public static final int CIRCLE_SELECTED = 2;//selected state of circle
    public static final int CIRCLE_ERROR = 3;//error state of circle
    public static final int CREATE_MODE = 22;//this mode is used for creating gesture
    public static final int CHECK_MODE = 33;//this mode is used for checking gesture
    @UnlockMode
    private int mode;//define the mode
    private int width;//the width of screen,valued in onMeasure
    private int height;//the height of screen,valued in onMeasure
    private int rootX;//root position of the line which can move
    private int rootY;//root position of the line which can move
    private Context ctx;
    private ArrayList<Circle> circleList = new ArrayList<>();//store the circles on screen
    private ArrayList<Circle> pathCircleList = new ArrayList<>();//store the selected circles
    private Bitmap circletBmp;//used for drawing circles
    private Canvas mCanvas;
    private Paint cirNorPaint;//paint of normal state circles
    private Paint cirSelPaint;//paint of selected state circles
    private Paint smallCirSelPaint;//paint of selected state small circles
    private Paint cirErrPaint;//paint of error state circles
    private Paint smallcirErrPaint;//paint of error state small circles
    private Paint pathPaint;//paint of the lines
    private Path mPath;
    private Path tempPath;
    private int pathWidth = 3;//width of the paint of path
    private int normalR = 15;//radius of small circles;
    private int selectR = 30;//radius of big circles;
    private int strokeWidth = 2;//width of big circles;
    private int normalColor = Color.parseColor("#D5DBE8");//defalt color of normal state
    private int selectColor = Color.parseColor("#508CEE");//defalt color of selected state
    private int errorColor = Color.parseColor("#FF3153");//defalt color of error state

    private boolean isUnlocking;
    private boolean isShowError;
    private boolean hasNewCircles;
    private ArrayList<Integer> passList = new ArrayList<>();
    private OnUnlockListener listener;//the listener of unlock
    private CreateGestureListener createListener;//the listener of creating gesture

    /**
     * used for refresh the canvas after MotionEvent.ACTION_UP
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            resetAll();
            invalidate();
            return true;
        }
    });

    public UnlockView(Context context) {
        super(context);
        this.ctx = context;
        strokeWidth = dip2px(ctx, strokeWidth);
        normalR = dip2px(ctx, normalR);
        selectR = dip2px(ctx, selectR);
        pathWidth = dip2px(ctx, pathWidth);
    }

    public UnlockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        strokeWidth = dip2px(ctx, strokeWidth);
        normalR = dip2px(ctx, normalR);
        selectR = dip2px(ctx, selectR);
        pathWidth = dip2px(ctx, pathWidth);
    }

    public UnlockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.ctx = context;
        strokeWidth = dip2px(ctx, strokeWidth);
        normalR = dip2px(ctx, normalR);
        selectR = dip2px(ctx, selectR);
        pathWidth = dip2px(ctx, pathWidth);

    }

    /**
     * reset all states
     */
    private void resetAll() {
        isShowError = false;
        isUnlocking = false;
        mPath.reset();
        tempPath.reset();
        pathCircleList.clear();
        passList.clear();
        for (Circle circle : circleList) {
            circle.setState(CIRCLE_NORMAL);
        }
        pathPaint.setColor(selectColor);
        cirSelPaint.setColor(selectColor);
        smallCirSelPaint.setColor(selectColor);
        clearCanvas();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(circletBmp, 0, 0, null);
        for (int i = 0; i < circleList.size(); i++) {
            drawCircles(circleList.get(i));
        }
        canvas.drawPath(mPath, pathPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isShowError)
            return true;
        int curX = (int) event.getX();
        int curY = (int) event.getY();
        Circle circle = getOuterCircle(curX, curY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.resetAll();
                if (circle != null) {
                    rootX = circle.getX();
                    rootY = circle.getY();
                    circle.setState(CIRCLE_SELECTED);
                    pathCircleList.add(circle);
                    tempPath.moveTo(rootX, rootY);
                    addItem(circle.getPosition() + 1);
                    isUnlocking = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isUnlocking) {
                    mPath.reset();
                    mPath.addPath(tempPath);
                    mPath.moveTo(rootX, rootY);
                    mPath.lineTo(curX, curY);
                    handleMove(circle);
                }
                break;
            case MotionEvent.ACTION_UP:
                isUnlocking = false;
                if (pathCircleList.size() > 0) {
                    mPath.reset();
                    mPath.addPath(tempPath);
                    StringBuilder sb = new StringBuilder();
                    for (Integer num : passList) {
                        sb.append(num);
                    }

                    if (this.mode == CREATE_MODE) {
                        if(createListener!=null){
                            createListener.onGestureCreated(sb.toString());
                        }else{
                            Log.e("UnLockView","Please set CreateGestureListener first!");
                        }
                    } else if(this.mode == CHECK_MODE){
                        if(listener!=null){
                            if (listener.isUnlockSuccess(sb.toString())) {
                                listener.onSuccess();
                            } else {
                                listener.onFailure();
                                for (Circle circle1 : pathCircleList) {
                                    circle1.setState(CIRCLE_ERROR);
                                }
                                pathPaint.setColor(errorColor);
                            }
                        }else{
                            Log.e("UnLockView","Please set OnUnlockListener first!");
                        }

                    }else{
                        try {
                            throw new Exception("Please set mode first!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isShowError = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0);
                        }
                    }, 1000);

                }
                break;
        }
        invalidate();
        return true;
    }

    private synchronized void handleMove(Circle c) {
        if (c != null&&!(c.getState()==CIRCLE_SELECTED)) {
            c.setState(CIRCLE_SELECTED);
            pathCircleList.add(c);
            rootX = c.getX();
            rootY = c.getY();
            tempPath.lineTo(rootX, rootY);
            addItem(c.getPosition() + 1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mode != CHECK_MODE || mode != CREATE_MODE) {
            Log.e("UnlockView", "Please set mode first!");
        }
        //init all path/paint
        mPath = new Path();
        tempPath = new Path();
        pathPaint = new Paint();
        pathPaint.setColor(selectColor);
        pathPaint.setDither(true);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeWidth(pathWidth);
        //普通状态小圆画笔
        circletBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(circletBmp);
        cirNorPaint = new Paint();
        cirNorPaint.setAntiAlias(true);
        cirNorPaint.setDither(true);
        cirNorPaint.setColor(normalColor);
        //选中状态大圆画笔
        cirSelPaint = new Paint();
        cirSelPaint.setAntiAlias(true);
        cirSelPaint.setDither(true);
        cirSelPaint.setStyle(Paint.Style.STROKE);
        cirSelPaint.setStrokeWidth(strokeWidth);
        cirSelPaint.setColor(selectColor);
        //选中状态小圆画笔
        smallCirSelPaint = new Paint();
        smallCirSelPaint.setAntiAlias(true);
        smallCirSelPaint.setDither(true);
        smallCirSelPaint.setColor(selectColor);
        //出错状态大圆画笔
        cirErrPaint = new Paint();
        cirErrPaint.setAntiAlias(true);
        cirErrPaint.setDither(true);
        cirErrPaint.setStyle(Paint.Style.STROKE);
        cirErrPaint.setStrokeWidth(strokeWidth);
        cirErrPaint.setColor(errorColor);
        //出错状态小圆画笔
        smallcirErrPaint = new Paint();
        smallcirErrPaint.setAntiAlias(true);
        smallcirErrPaint.setDither(true);
        smallcirErrPaint.setColor(errorColor);

        //init all circles
        int hor = width / 6;
        int ver = height / 6;
        if(!hasNewCircles){
            for (int i = 0; i < 9; i++) {
                int tempX = (i % 3 + 1) * 2 * hor - hor;
                int tempY = (i / 3 + 1) * 2 * ver - ver;
                Circle circle = new Circle(i, tempX, tempY, CIRCLE_NORMAL);
                circleList.add(circle);
            }
        }
        hasNewCircles=true;

    }

    /**
     * called in onDraw for drawing all circles
     *
     * @param circle
     */
    private void drawCircles(Circle circle) {
        switch (circle.getState()) {
            case CIRCLE_NORMAL:
                mCanvas.drawCircle(circle.getX(), circle.getY(), normalR, cirNorPaint);
                break;
            case CIRCLE_SELECTED:
                mCanvas.drawCircle(circle.getX(), circle.getY(), selectR, cirSelPaint);
                mCanvas.drawCircle(circle.getX(), circle.getY(), normalR, smallCirSelPaint);
                break;
            case CIRCLE_ERROR:
                mCanvas.drawCircle(circle.getX(), circle.getY(), selectR, cirErrPaint);
                mCanvas.drawCircle(circle.getX(), circle.getY(), normalR, smallcirErrPaint);
                break;
        }
    }

    /**
     * clear canvas
     */
    private void clearCanvas() {
        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        mPath.reset();
        tempPath.reset();
    }

    /**
     * J U S T  A  T O O L !
     *
     * @param context  Context
     * @param dipValue value of dp
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * check whether the point is in a circle
     *
     * @param x
     * @param y
     * @return
     */
    @Nullable
    private Circle getOuterCircle(int x, int y) {
        for (int i = 0; i < circleList.size(); i++) {
            Circle circle = circleList.get(i);
            if ((x - circle.getX()) * (x - circle.getX()) + (y - circle.getY()) * (y - circle.getY()) <= normalR * normalR) {
                if (circle.getState() != CIRCLE_SELECTED) {
                    return circle;
                }
            }
        }
        return null;
    }

    /**
     * check whether the password list contains the number
     *
     * @param num
     * @return
     */
    private boolean arrContainsInt(int num) {
        for (Integer value : passList) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * put the num into password list
     *
     * @param num
     */
    private void addItem(Integer num) {
        if (!arrContainsInt(num)) {
            passList.add(num);
        }
    }


    /**
     * Create Mode Listener
     */
    interface CreateGestureListener {
        void onGestureCreated(String result);
    }

    public void setGestureListener(CreateGestureListener listener) {
        this.createListener = listener;
    }

    /**
     * Check Mode Listener
     */
    interface OnUnlockListener {
        boolean isUnlockSuccess(String result);

        void onSuccess();

        void onFailure();
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        this.listener = listener;
    }

    public void setPathWidth(int pathWidth) {
        this.pathWidth = pathWidth;
    }

    public void setNormalR(int normalR) {
        this.normalR = normalR;
    }

    public void setSelectR(int selectR) {
        this.selectR = selectR;
    }

    public void setNormalColor(int normalColor) {
        this.normalColor = normalColor;
    }


    public void setSelectColor(int selectColor) {
        this.selectColor = selectColor;
    }


    public void setErrorColor(int errorColor) {
        this.errorColor = errorColor;
    }

    public void setMode(@UnlockMode int mode) {
        this.mode = mode;
    }


    class Circle {
        /**
         * position of the circle
         */
        private int position;
        private int x;
        private int y;
        /**
         * the state of circle
         */
        private int state;

        public Circle() {
        }

        public Circle(int position, int x, int y, int state) {
            this.position = position;
            this.x = x;
            this.y = y;
            this.state = state;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }

    /**
     * It's an annotation
     */
    @IntDef({CREATE_MODE, CHECK_MODE})
    @interface UnlockMode {
    }
}
