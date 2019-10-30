package com.example.tomatoclock.report;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

public class MyLineChartView extends View {

    private int originX; // 原点x坐标

    private int originY; // 原点y坐标

    private int firstPointX; //第一个点x坐标

    private int firstMinX; // 移动时第一个点的最小x值

    private int firstMaxX; //移动时第一个点的最大x值

    private int intervalX = 170; // 坐标刻度的间隔

    private int intervalY = 80; // y轴刻度的间隔

    private List<String> xValues;

    private List<Integer> yValues;

    private List<Focus> focusList;

    private int mWidth; // 控件宽度

    private int mHeight; // 控件高度

    private int startX; // 滑动时上一次手指的x坐标值

    private int xyTextSize = 30; //x轴文字大小

    private int yTextSize = 40;

    private int paddingTop = 140;// 默认上下左右的padding

    private int paddingLeft = 160;

    private int paddingRight = 100;

    private int paddingDown = 150;

    private int scaleHeight = 10; // x轴刻度线高度

    private int textToXYAxisGap = 20; // xy轴的文字距xy线的距离

    private int leftRightExtra = intervalX / 3; //x轴左右向外延伸的长度

    private int lableCountY = 6; // Y轴刻度个数

    private int bigCircleR = 7;

    private int smallCircleR = 5;

    private float minValueY; // y轴最小值

    private float maxValueY = 0; // y轴最大值

    private int shortLine = 34; // 比例图线段长度

    private Paint paintWhite, paintBlue, paintRed, paintBack, paintText, paintTextY;

    private int backGroundColor = Color.parseColor("#FAFAFA"); // view的背景颜色（大背景）

    private GestureDetector gestureDetector;

    private String legendTitle = "患者填写";


    public MyLineChartView(Context context) {
        this(context, null);
    }

    public MyLineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        gestureDetector = new GestureDetector(context, new MyOnGestureListener());
    }

    private void initPaint() {
        paintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhite.setColor(Color.parseColor("#000000"));
        paintWhite.setStyle(Paint.Style.STROKE);

        paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);//线条颜色
        paintBlue.setColor(Color.parseColor("#FFA500"));
        paintBlue.setStrokeWidth(40);
        paintBlue.setStrokeCap(Paint.Cap.ROUND);
        paintBlue.setStyle(Paint.Style.STROKE);

        paintBack = new Paint(Paint.ANTI_ALIAS_FLAG);//
        paintBack.setColor(Color.parseColor("#FAFAFA"));//侧栏颜色
        paintBack.setStyle(Paint.Style.FILL);

        paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRed.setColor(Color.RED);
        paintRed.setStrokeWidth(3f);
        paintRed.setStyle(Paint.Style.STROKE);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.parseColor("#000000"));//坐标轴上的字体颜色
        paintText.setTextSize(xyTextSize);
        paintText.setStrokeWidth(2f);

        paintTextY = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextY.setColor(Color.parseColor("#000000"));//坐标轴上的字体颜色
        paintTextY.setTextSize(yTextSize);
        paintTextY.setStrokeWidth(2f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();

            originX = paddingLeft - leftRightExtra;
            originY = mHeight - paddingDown;

            firstPointX = paddingLeft;
            firstMinX = mWidth - originX - (xValues.size() - 1) * intervalX - leftRightExtra;
            // 滑动时，第一个点x值最大为paddingLeft，在大于这个值就不能滑动了
            firstMaxX = firstPointX;
            setBackgroundColor(backGroundColor);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawX(canvas);
        drawBrokenLine(canvas);
        drawY(canvas);
        drawLegend(canvas);

    }

    /**
     * 画x轴
     *
     * @param canvas
     */
    private void drawX(Canvas canvas) {
        Path path = new Path();
        path.moveTo(originX, originY);
        for (int i = 0; i < 25; i++) {
            // x轴线
            path.lineTo(mWidth - paddingRight, originY);  // 写死不变
            // x轴箭头
            canvas.drawLine(mWidth - paddingRight, originY, mWidth - paddingRight - 15, originY + 10, paintWhite);
            canvas.drawLine(mWidth - paddingRight, originY, mWidth - paddingRight - 15, originY - 10, paintWhite);

            // x轴线上的刻度线
            canvas.drawLine(firstPointX + i * intervalX, originY, firstPointX + i * intervalX, originY - scaleHeight, paintWhite);
            // x轴上的文字
            String textOnX = i+":00";

            canvas.drawText(textOnX, firstPointX + i * intervalX - getTextWidth(paintText, "17.01") / 2,
                    originY + textToXYAxisGap + getTextHeight(paintText, "17.01"), paintText);
        }
        canvas.drawPath(path, paintWhite);

        //画虚刻度线
        for(int i = 0;i < 25;i++) {
            Paint pp = new Paint();
            pp.setStyle(Paint.Style.STROKE);
            pp.setColor(Color.BLACK);
            PathEffect pathEffect = new DashPathEffect(new float[]{8, 10, 8, 10}, 0);
            pp.setPathEffect(pathEffect);
            Path pathDash = new Path();
            pathDash.moveTo(firstPointX + i*intervalX, originY);
            pathDash.lineTo(firstPointX + i*intervalX, originY - mHeight + 400);
            canvas.drawPath(pathDash, pp);
        }


        // x轴虚线
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);

        Path path1 = new Path();
        DashPathEffect dash = new DashPathEffect(new float[]{8, 10, 8, 10}, 0);
        p.setPathEffect(dash);
        for (int i = 0; i < lableCountY; i++) {
            path1.moveTo(originX, mHeight - paddingDown - leftRightExtra - i * intervalY);
            path1.lineTo(mWidth - paddingRight, mHeight - paddingDown - leftRightExtra - i * intervalY);
        }
        canvas.drawPath(path1, p);
    }

    /**
     * 画折线
     *
     * @param canvas
     */
    private void drawBrokenLine(Canvas canvas) {
        canvas.save();
        // y轴文字
        minValueY = yValues.get(0);
        for (int i = 0; i < yValues.size(); i++) {
            // 找出y轴的最大最小值
            if (yValues.get(i) > maxValueY) {
                maxValueY = yValues.get(i);
            }
            if (yValues.get(i) < minValueY) {
                minValueY = yValues.get(i);
            }
        }
        // 画折线
        //float aver = (lableCountY - 1) * intervalY / (maxValueY - minValueY);
        //aver = 1.0;
        //System.out.println("aver="+aver);
//        Path path = new Path();
//        path.moveTo(firstPointX,(float)(mHeight/2.0));
//        for (int i = 0; i < yValues.size(); i++) {
////            if (i == 0) {
////                path.moveTo(firstPointX, mHeight - paddingDown - leftRightExtra - yValues.get(i) * aver + minValueY * aver);
////            } else {
////                path.lineTo(firstPointX + i * intervalX, mHeight - paddingDown - leftRightExtra - yValues.get(i) * aver + minValueY * aver);
////            }
//            if(i == 0)
//                continue;
//            else
//                path.lineTo(firstPointX + i * intervalX,(float)(mHeight/2.0));
//        }
//
//
//        canvas.drawPath(path, paintBlue);

        for(int i = 0;i < focusList.size();i++)
        {
            System.out.println(i);
            Path path = new Path();
            double start = focusList.get(i).startHour + focusList.get(i).startMinute/60.0;
            double dura = focusList.get(i).dura / 60.0;
            path.moveTo((float)(firstPointX+ start * intervalX),(float)(mHeight/2.0));

            path.lineTo((float)(firstPointX + (start+dura) * intervalX),(float)(mHeight/2.0));
            canvas.drawPath(path, paintBlue);
        }
        //画第二条线
        //Path path2 = new Path();
        //path2.moveTo(firstPointX,(float)(firstPointX+100.0));
        //path2.lineTo(firstPointX,firstPointX);
        //canvas.drawPath(path2,paintBlue);
        // 折线中的圆点
        for (int i = 0; i < yValues.size(); i++) {
//            canvas.drawCircle(firstPointX + i * intervalX,
//                    mHeight - paddingDown - leftRightExtra - yValues.get(i) * aver + minValueY * aver, bigCircleR, paintBlue);
//            canvas.drawCircle(firstPointX + i * intervalX,
//                    mHeight - paddingDown - leftRightExtra - yValues.get(i) * aver + minValueY * aver, smallCircleR, paintBack);
            //canvas.drawCircle(firstPointX + i * intervalX,(float)(mHeight/2.0),bigCircleR,paintBlue);
            //canvas.drawCircle(firstPointX + i * intervalX,(float)(mHeight/2.0),bigCircleR,paintBack);
        }
        //将折线超出x轴坐标的部分截取掉（左边）
        paintBack.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        RectF rectF = new RectF(0, 0, originX, mHeight);
        canvas.drawRect(rectF, paintBack);
        canvas.restore();
    }

    /**
     * 画y轴
     *
     * @param canvas
     */
    private void drawY(Canvas canvas) {
        canvas.save();
        Path path = new Path();
        path.moveTo(originX, originY);

        for (int i = 0; i < lableCountY; i++) {
            // y轴线
            if (i == 0) {
                path.lineTo(originX, mHeight - paddingDown - leftRightExtra);
            } else {
                path.lineTo(originX, mHeight - paddingDown - leftRightExtra - i * intervalY);
            }

            int lastPointY = mHeight - paddingDown - leftRightExtra - i * intervalY;
            if (i == lableCountY - 1) {
                int lastY = lastPointY - leftRightExtra - leftRightExtra / 2;
                // y轴最后一个点后，需要额外加上一小段，就是一个半leftRightExtra的长度
                canvas.drawLine(originX, lastPointY, originX, lastY, paintWhite);
                // y轴箭头
                canvas.drawLine(originX, lastY, originX - 10, lastY + 15, paintWhite);
                canvas.drawLine(originX, lastY, originX + 10, lastY + 15, paintWhite);
            }
        }
        canvas.drawPath(path, paintWhite);




        // y轴文字
//        float space = (maxValueY - minValueY) / (lableCountY - 1);
//        DecimalFormat decimalFormat = new DecimalFormat("0.00");
//        List<String> yTitles = new ArrayList<>();
//        for (int i = 0; i < lableCountY; i++) {
//            yTitles.add(decimalFormat.format(minValueY + i * space));
//        }
//        for (int i = 0; i < yTitles.size(); i++) {
//            canvas.drawText(yTitles.get(i), originX - textToXYAxisGap - getTextWidth(paintText, "00.00"),
//                    mHeight - paddingDown - leftRightExtra - i * intervalY + getTextHeight(paintText, "00.00") / 2, paintText);
//        }
        canvas.drawText("专注",originX - textToXYAxisGap - getTextWidth(paintTextY, "0000"),(float)(mHeight/2.0),paintTextY);
        // 截取折线超出部分（右边）
        paintBack.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        RectF rectF = new RectF(mWidth - paddingRight, 0, mWidth, mHeight);
        canvas.drawRect(rectF, paintBack);
        canvas.restore();
    }

    /**
     * 画图例
     */
    private void drawLegend(Canvas canvas) {
        // 开始点的坐标
        int x = 350;
        int y = mHeight - (paddingDown - textToXYAxisGap - getTextHeight(paintText, "06.00")) / 2;
//        canvas.save();
//        canvas.drawLine(x, y, x + 2 * shortLine, y, paintBlue);
//        canvas.drawCircle(x + shortLine, y, bigCircleR, paintBlue);
//        canvas.drawCircle(x + shortLine, y, smallCircleR, paintBack);
//        canvas.drawText(legendTitle, x + 2 * shortLine + 10, y + getTextHeight(paintText, legendTitle) / 2 - 2, paintText);
//
//        canvas.drawLine(x + 2 * shortLine + getTextWidth(paintText, legendTitle) + 20,
//                y, x + 2 * shortLine + getTextWidth(paintText, legendTitle) + 20 + 2 * shortLine, y, paintRed);
//        canvas.drawCircle(x + 2 * shortLine + getTextWidth(paintText, legendTitle) + 20 + shortLine, y, bigCircleR, paintRed);
//        canvas.drawCircle(x + 2 * shortLine + getTextWidth(paintText, legendTitle) + 20 + shortLine, y, smallCircleR, paintBack);
//        canvas.drawText("护士填写", x + 2 * shortLine + getTextWidth(paintText, legendTitle) + 30 + 2 * shortLine,
//                y + getTextHeight(paintText, legendTitle) / 2 - 2, paintText);
        //canvas.restore();
    }


    /**
     * 手势事件
     */
    class MyOnGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) { // 按下事件
            return false;
        }

        // 按下停留时间超过瞬时，并且按下时没有松开或拖动，就会执行此方法
        @Override
        public void onShowPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) { // 单击抬起
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getX() > originX && e1.getX() < mWidth - paddingRight &&
                    e1.getY() > paddingTop && e1.getY() < mHeight - paddingDown) {
                //注意：这里的distanceX是e1.getX()-e2.getX()
//                distanceX = -distanceX;//按上面的注释，这里就要取负
//                if (firstPointX + distanceX > firstMaxX) {
//                    firstPointX = firstMaxX;
//                } else if (firstPointX + distanceX < firstMinX) {
//                    firstPointX = firstMinX;
//                } else {
//                    firstPointX = (int) (firstPointX + distanceX) + 1;
//                }
                distanceX = -distanceX;
                System.out.println(distanceX);
                System.out.println(firstPointX);
                if (firstPointX + distanceX > firstMaxX)
                    firstPointX = firstMaxX;
                else if(firstPointX + distanceX < -intervalX*18)
                    firstPointX = -intervalX*18;
                else
                    firstPointX = (int)(firstPointX + distanceX);
                invalidate();//猜测：invalidate 后重算firstPointX相关值？
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
        } // 长按事件

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //if (yValues.size() < 7) {
        //    return false;
        //}
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setXValues(List<String> values) {
        this.xValues = values;
    }

    public void setYValues(List<Integer> values) {
        this.yValues = values;
    }

    public void setFocusList(List<Focus> focusList)
    {
        this.focusList = focusList;
    }

    /**
     * 获取文字的宽度
     *
     * @param paint
     * @param text
     * @return
     */
    private int getTextWidth(Paint paint, String text) {
        return (int) paint.measureText(text);
    }

    /**
     * 获取文字的高度
     *
     * @param paint
     * @param text
     * @return
     */
    private int getTextHeight(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }
}