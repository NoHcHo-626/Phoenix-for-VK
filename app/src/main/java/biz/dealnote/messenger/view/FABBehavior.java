package biz.dealnote.messenger.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class FABBehavior extends FloatingActionButton.Behavior {

    public FABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public void onNestedScroll(@NonNull androidx.coordinatorlayout.widget.CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        //Logger.d("FABBehavour", String.valueOf(dyConsumed));
        if (child.isShown() && dyConsumed > 0) {
            child.hide(new com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(com.google.android.material.floatingactionbutton.FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        } else if (!child.isShown() && dyConsumed < 0) {
            child.show();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull androidx.coordinatorlayout.widget.CoordinatorLayout coordinatorLayout, @NonNull com.google.android.material.floatingactionbutton.FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}
