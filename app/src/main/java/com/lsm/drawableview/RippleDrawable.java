package com.lsm.drawableview;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class RippleDrawable extends Drawable {
    //透明度 0~255
    private int mAlpha = 200;
    //颜色
    private int mRippleColor = 0;
    //画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //圆心 和半径
    private float mRipplePointX, mRipplePointY, mRippleRadius = 0;
    //按下时的点击点
    private float mDonePointX, mDonePointY;
    //按下的中心区域
    private float mCenterPointX, mCenterPointY;
    //开始和结束的半径
    private float mStartRadius, mEndRadius;
    //背景透明度
    private int mBgAlpha;
    private int mCircleAlpha;
    //标识用户手是否抬起
    private boolean mTouchRelease = false;
    private boolean mEnterDone = false;
    //进度值
    private float mProgress = 0;
    //插值器  用于实现从快到慢的效果
    private Interpolator mEnterInterpolator = new DecelerateInterpolator(2);
    //每次递增的值
    private float mEnterIncrement = 16f / 2400;
    //退出动画的进度值
    private float mExitProgress = 0;
    //每次递增的进度值
    private float mExitIncrement = 16f / 36;
    //退出动画插值器  实现由慢到快的过程
    private Interpolator mExitInterpolator = new AccelerateInterpolator(2);

    //    /**
//     * 设置颜色
//     *
//     * @param color
//     */
    public void setRippleColor(int color) {
        mRippleColor = color;
        onColorOrAlphaChange();
    }

    public RippleDrawable(int mRippleColor) {
        this.mRippleColor = mRippleColor;
        //抗锯齿
        mPaint.setAntiAlias(true);
        //防抖动
        mPaint.setDither(true);
        setRippleColor(mRippleColor);
//        setColorFilter(new LightingColorFilter(0xFFFF0000, 0X00330000));
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mCenterPointX = bounds.centerX();
        mCenterPointY = bounds.centerY();
        //得到圆的最大半径
        float maxRadius = Math.max(mCenterPointX, mCenterPointY);
        mStartRadius = maxRadius * 0.0f;
        mEndRadius = maxRadius * 1.2f;
    }

    /**
     * 计算获取圆圈的透明度
     *
     * @param preAlpha
     * @param bgAlpha
     * @return
     */
    private int getCircleAlpha(int preAlpha, int bgAlpha) {
        int dAlpha = preAlpha - bgAlpha;
        return (int) ((dAlpha * 255f) / (255f - bgAlpha));
    }

    @Override
    public void draw(Canvas canvas) {
        int preAlpha = mPaint.getAlpha();
        int bgAlpha = (int) (preAlpha * (mBgAlpha / 255f));
        int maxCircleAlpha = getCircleAlpha(preAlpha, bgAlpha);
        int circleAlpha = (int) (maxCircleAlpha * (mCircleAlpha / 255f));
        //绘制背景区域颜色透明度
        mPaint.setAlpha(bgAlpha);
        canvas.drawColor(mPaint.getColor());
        mPaint.setAlpha(circleAlpha);
        //绘制一个圆
        canvas.drawCircle(mRipplePointX, mRipplePointY, mRippleRadius, mPaint);
        mPaint.setAlpha(preAlpha);
    }

    /**
     * 更改颜色透明度
     */
    private int changeColorAlpha(int color, int alpha) {
        int a = (color >> 24) & 0xFF;
        a = (int) (a * (alpha / 255f));
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }


    public void onTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            //判断点击操作类型
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                onTouchCancle(event.getX(), event.getY());
                break;
        }
    }

    private void onTouchDown(float x, float y) {
        mTouchRelease = false;
        mDonePointX = x;
        mDonePointY = y;
        mRippleRadius = 0;
        startEnterRunnable();
    }

    private void onTouchMove(float x, float y) {

    }

    private void onTouchUp(float x, float y) {
        //标识手抬起
        mTouchRelease = true;
        //启动退出动画
        if (mEnterDone) {
            //当进入动画完成时开始退出动画
            startExitRunnable();
        }
    }

    private void onTouchCancle(float x, float y) {
        //标识手抬起
        mTouchRelease = true;
        //启动退出动画
        if (mEnterDone) {
            startExitRunnable();
        }
    }

    @Override
    public void setAlpha(int alpha) {
        //设置透明度
        mAlpha = alpha;
        onColorOrAlphaChange();
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //设置颜色过滤器  颜色滤镜
        if (mPaint.getColorFilter() != colorFilter) {
            mPaint.setColorFilter(colorFilter);
            //刷新当前drawable
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        int alpha = mPaint.getAlpha();
        if (alpha == 255) {
            //不透明
            return PixelFormat.OPAQUE;
        } else if (alpha == 0) {
            //全透明
            return PixelFormat.TRANSPARENT;
        } else {
            //半透明
            return PixelFormat.TRANSLUCENT;
        }
    }

    private void onColorOrAlphaChange() {
        mPaint.setColor(mRippleColor);
        if (mAlpha != 255) {
            //获取画笔的透明度
            int pAlpha = mPaint.getAlpha();
            //获取颜色的透明度
            int realAlpha = (int) (pAlpha * (mAlpha / 255f));
            //重新设置画笔透明度
            mPaint.setAlpha(realAlpha);
            Log.e("TAG", "原始颜色:" + mRippleColor + "现在的颜色:" + mPaint.getColor());
        }
        //刷新当前drawable
        invalidateSelf();
    }

    /**
     * 进入动画完成的时候调用的方法
     */
    private void onEnterDone() {
        mEnterDone = true;
        //当用户手势放开时开始退出动画
        if (mTouchRelease) {
            startExitRunnable();
        }
    }

    /**
     * 开始动画
     */
    private void startEnterRunnable() {
        mCircleAlpha = 255;
        mEnterDone = false;
        mProgress = 0;
        unscheduleSelf(mExitRunnable);
        //先取消
        unscheduleSelf(mEnterRunnable);
        //再进行注册
        scheduleSelf(mEnterRunnable, SystemClock.uptimeMillis());
    }


    //动画的回调
    private Runnable mEnterRunnable = new Runnable() {
        @Override
        public void run() {
            mProgress = mProgress + mEnterIncrement;
            if (mProgress > 1) {
                //进入动画完成的时候调用的方法
                onEnterProgress(1);
                onEnterDone();
                return;
            }
            float realProgress = mEnterInterpolator.getInterpolation(mProgress);

            onEnterProgress(realProgress);
            //基于当前时间推迟16毫秒 保证界面刷新频率接近60fps
            scheduleSelf(this, SystemClock.uptimeMillis() + 16);
        }
    };

    private void onEnterProgress(float progress) {
        mRippleRadius = getProgressValue(mStartRadius, mEndRadius, progress);
        mRipplePointX = getProgressValue(mDonePointX, mCenterPointX, progress);
        mRipplePointY = getProgressValue(mDonePointY, mCenterPointY, progress);

        mBgAlpha = (int) getProgressValue(0, 182, progress);
        invalidateSelf();
    }

    private float getProgressValue(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    //动画回调
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mEnterDone) {
                return;
            }
            mExitProgress = mExitProgress + mExitIncrement;
            if (mExitProgress > 1) {
                onExitProgress(1);
                onExitDone();
                return;
            }
            float realProgress = mExitInterpolator.getInterpolation(mExitProgress);
            onExitProgress(realProgress);
            scheduleSelf(this, SystemClock.uptimeMillis() + 16);
        }
    };

    /**
     * 退出动画刷新方法
     *
     * @param progress 进度值
     */
    private void onExitProgress(float progress) {
        mBgAlpha = (int) getProgressValue(182, 0, progress);
        mCircleAlpha = (int) getProgressValue(255, 0, progress);
        invalidateSelf();
    }

    /**
     * 启动退出动画
     */
    private void startExitRunnable() {

        mExitProgress = 0;
        //取消进入动画
        unscheduleSelf(mEnterRunnable);
        //取消退出动画
        unscheduleSelf(mExitRunnable);

        scheduleSelf(mExitRunnable, SystemClock.uptimeMillis());
    }

    /**
     * 当退出动画完成时触发
     */
    private void onExitDone() {

    }
}
