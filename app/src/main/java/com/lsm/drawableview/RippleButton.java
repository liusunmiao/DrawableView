package com.lsm.drawableview;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class RippleButton extends AppCompatButton {
    private RippleDrawable rippleDrawable;

    public RippleButton(Context context) {
        this(context, null);
    }

    public RippleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RippleButton);
        int color = array.getColor(R.styleable.RippleButton_rippleCircleColor, Color.RED);

        rippleDrawable = new RippleDrawable(R.color.colorPrimaryDark);
//        rippleDrawable.setRippleColor(color);
//        setBackgroundDrawable(new RippleDrawable());
        //设置刷新接口  在view中已经实现
        rippleDrawable.setCallback(this);
        array.recycle();
    }

    /**
     * 验证Drawable是否ok
     *
     * @param who
     * @return
     */
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == rippleDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //设置Drawable绘制和刷新可绘制的区域
        rippleDrawable.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rippleDrawable.draw(canvas);
        super.onDraw(canvas);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        rippleDrawable.onTouch(event);
        return true;
    }
}
