package com.teinvdlugt.android.piano;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TagLayout extends ViewGroup {
    private int deviceWidth;
    private OnTagClickListener onTagClickListener;

    interface OnTagClickListener {
        void onClickTag(String tag);
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childLeft = this.getPaddingLeft();
        final int childTop = this.getPaddingTop();
        final int childRight = this.getMeasuredWidth() - this.getPaddingRight();
        final int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
        final int childWidth = childRight - childLeft;
        final int childHeight = childBottom - childTop;

        int maxHeight = 0;
        int curLeft = childLeft;
        int curTop = childTop;
        int curWidth, curHeight;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
            curWidth = child.getMeasuredWidth();
            curHeight = child.getMeasuredHeight();

            maxHeight = Math.max(maxHeight, curHeight);
            if (curLeft + curWidth >= childRight) {
                curLeft = childLeft;
                curTop += maxHeight;
                maxHeight = 0;
            }

            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight);
            curLeft += curWidth;
        }
    }

    private void init(Context context) {
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point deviceDisplay = new Point();
        display.getSize(deviceDisplay);
        deviceWidth = deviceDisplay.x;
    }

    public void setTags(String tagsString) {
        removeAllViews();

        int margins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getContext().getResources().getDisplayMetrics());
        int paddings = margins;

        int tagColor = SongActivity.getColor(getContext(), R.color.colorAccent);
        int textColor = SongActivity.getColor(getContext(), R.color.textColorPrimary);

        String[] tags = tagsString.split(",");
        for (String tagWithHair : tags) {
            final String tag = tagWithHair.trim();
            if (tag.isEmpty()) continue;
            TextView textView = new TextView(getContext());
            textView.setBackgroundColor(tagColor);
            textView.setText(tag);
            textView.setPadding(2 * paddings, paddings, 2 * paddings, paddings);
            textView.setTextColor(textColor);
            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.setPadding(margins, margins, margins, margins);
            frameLayout.addView(textView);
            addView(frameLayout);

            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTagClickListener != null)
                        onTagClickListener.onClickTag(tag);
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        int leftWidth = 0;
        int rowCount = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            maxWidth += Math.max(maxWidth, childWidth);
            leftWidth += childWidth;

            if ((leftWidth / deviceWidth) > rowCount) {
                maxHeight += childHeight;
                rowCount++;
            } else {
                maxHeight = Math.max(maxHeight, childHeight);
            }
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth + getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    public TagLayout(Context context) {
        super(context);
        init(context);
    }

    public TagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
}
