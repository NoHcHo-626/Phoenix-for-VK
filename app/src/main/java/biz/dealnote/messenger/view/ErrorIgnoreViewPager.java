package biz.dealnote.messenger.view;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ErrorIgnoreViewPager extends androidx.viewpager.widget.ViewPager {

    public ErrorIgnoreViewPager(Context context) {
        super(context);
    }

    public ErrorIgnoreViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
        }
        return false;
    }
}