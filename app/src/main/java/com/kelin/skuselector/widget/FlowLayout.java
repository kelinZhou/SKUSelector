package com.kelin.skuselector.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kelin.skuselector.R;
import com.kelin.skuselector.model.TagModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private static final int KEY_TAG_MODEL = 0x0C00_0000;
    private static final int KEY_CUSTOM_TAG = 0x1100_0000;
    private Context mContext;
    private int usefulWidth; // the space of a line we can use(line's width minus the sum of left and right padding
    private int lineSpacing = 0; // the spacing between lines in flowlayout
    List<View> childList = new ArrayList<>();
    List<Integer> lineNumList = new ArrayList<>();
    private int tagLayoutId;
    private int tagTextViewId;
    private int lineCount;
    private boolean clickRepeatable = false;
    private View lastClick;
    private String tagsString;
    private OnTagClickListener tagClickListener;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        lineSpacing = ta.getDimensionPixelSize(
                R.styleable.FlowLayout_lineSpacing, 0);
        tagLayoutId = ta.getResourceId(R.styleable.FlowLayout_tagLayout, View.NO_ID);
        tagTextViewId = ta.getResourceId(R.styleable.FlowLayout_tagTextViewId, View.NO_ID);
        lineCount = ta.getInteger(R.styleable.FlowLayout_lineCount, 0);
        tagsString = ta.getString(R.styleable.FlowLayout_tags);
        ta.recycle();
    }

    public void setLineCount(int count) {
        lineCount = count;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineSpacing(int spacing) {
        lineSpacing = spacing;
    }

    public int getLineSpacing() {
        return lineSpacing;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (tagsString != null && !TextUtils.isEmpty(tagsString = tagsString.trim())) {
            setTags(Arrays.asList(tagsString.split(",")));
        }
    }

    public void resetTags(List tags) {
        removeAllViews();
        setTags(tags);
    }

    public void setTags(List tags) {
        setTags(tags, null);
    }

    public void setTags(List tags, List selectedList) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            Object tag = getChildAt(i).getTag(KEY_CUSTOM_TAG);
            if (tag instanceof Integer && (int) tag == tagLayoutId) {
                removeViewAt(i);
            }
        }
        if (tags != null && tags.size() > 0) {
            TextView tvTag;
            View rootView;
            for (Object tag : tags) {
                rootView = LayoutInflater.from(getContext()).inflate(tagLayoutId, this, false);
                rootView.setTag(KEY_CUSTOM_TAG, tagLayoutId);
                if (tag instanceof TagModel) {
                    rootView.setSelected(((TagModel) tag).isEnable() && ((TagModel) tag).isSelected());
                    rootView.setEnabled(((TagModel) tag).isEnable());
                }
                tvTag = (TextView) (tagTextViewId == NO_ID ? rootView : rootView.findViewById(tagTextViewId));
                tvTag.setText(tag instanceof String ? (String) tag : tag instanceof TagModel ? ((TagModel) tag).getShowText() : tag.toString());
                if (selectedList != null && !selectedList.isEmpty() && selectedList.contains(tag)) {
                    rootView.setSelected(true);
                    if (rootView instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) rootView;
                        checkBox.setChecked(true);
                    }
                }
                rootView.setTag(KEY_TAG_MODEL, tag);
                rootView.setOnClickListener(v -> {
                    if (clickRepeatable || lastClick != v) {
                        lastClick = v;
                        if (tagClickListener != null) {
                            tagClickListener.onTagClick(FlowLayout.this, v, v.getTag(KEY_TAG_MODEL), getChildIndex(v));
                        }
                    }
                });
                addView(rootView);
            }
        }
    }

    public void refresh() {
        TagModel tag;
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            tag = (TagModel) child.getTag(KEY_TAG_MODEL);
            child.setSelected(tag.isEnable() && tag.isSelected());
            child.setEnabled(tag.isEnable());
        }
    }

    public <T> List<T> getSelectedTags() {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.isSelected()) {
                list.add((T) child.getTag(KEY_TAG_MODEL));
            }
        }
        return list;
    }

    int getChildIndex(View v) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == v) {
                return i;
            }
        }
        throw new RuntimeException("Child not found!");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mPaddingLeft = getPaddingLeft();
        int mPaddingRight = getPaddingRight();
        int mPaddingTop = getPaddingTop();
        int mPaddingBottom = getPaddingBottom();

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int lineUsed = mPaddingLeft + mPaddingRight;
        int lineY = mPaddingTop;
        int lineHeight = 0;
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            int spaceWidth = mlp.leftMargin + mlp.rightMargin;
            int spaceHeight = mlp.topMargin + mlp.bottomMargin;
            if (lineCount > 0) {
                mlp.width = (widthSize - mPaddingLeft - mPaddingRight - spaceWidth * lineCount) / lineCount;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, lineY);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            spaceWidth += childWidth;
            spaceHeight += childHeight;

            if (lineUsed + spaceWidth > widthSize) {
                //approach the limit of width and move to next line
                lineY += lineHeight + lineSpacing;
                lineUsed = mPaddingLeft + mPaddingRight;
                lineHeight = 0;
            }
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth;
        }
        setMeasuredDimension(
                widthSize,
                heightMode == MeasureSpec.EXACTLY ? heightSize : lineY + lineHeight + mPaddingBottom
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int mPaddingLeft = getPaddingLeft();
        int mPaddingRight = getPaddingRight();
        int mPaddingTop = getPaddingTop();

        int lineX = mPaddingLeft;
        int lineY = mPaddingTop;
        int lineWidth = r - l;
        usefulWidth = lineWidth - mPaddingLeft - mPaddingRight;
        int lineUsed = mPaddingLeft + mPaddingRight;
        int lineHeight = 0;
        int lineNum = 0;

        lineNumList.clear();
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int spaceWidth = 0;
            int spaceHeight = 0;
            int left;
            int top;
            int right;
            int bottom;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            spaceWidth = i == getChildCount() - 1 ? mlp.leftMargin : mlp.leftMargin + mlp.rightMargin;
            spaceHeight = mlp.topMargin + mlp.bottomMargin;
            left = lineX + mlp.leftMargin;
            top = lineY + mlp.topMargin;
            right = lineX + mlp.leftMargin + childWidth;
            bottom = lineY + mlp.topMargin + childHeight;
            spaceWidth += childWidth;
            spaceHeight += childHeight;

            if (lineUsed + spaceWidth > lineWidth) {
                //approach the limit of width and move to next line
                lineNumList.add(lineNum);
                lineY += lineHeight + lineSpacing;
                lineUsed = mPaddingLeft + mPaddingRight;
                lineX = mPaddingLeft;
                lineHeight = 0;
                lineNum = 0;
                left = lineX + mlp.leftMargin;
                top = lineY + mlp.topMargin;
                right = lineX + mlp.leftMargin + childWidth;
                bottom = lineY + mlp.topMargin + childHeight;
            }
            child.layout(left, top, right, bottom);
            lineNum++;
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth;
            lineX += spaceWidth;
        }
        // add the num of last line
        lineNumList.add(lineNum);
    }

    public <T> void setOnTagClickListener(OnTagClickListener<T> listener) {
        setOnTagClickListener(false, listener);
    }

    /**
     * 设置点击事件。
     *
     * @param clickRepeatable 点击事件是否可以重复触发。
     * @param listener        点击监听。
     */
    public <T> void setOnTagClickListener(boolean clickRepeatable, OnTagClickListener<T> listener) {
        tagClickListener = listener;
        this.clickRepeatable = clickRepeatable;
    }

    /**
     * resort child elements to use lines as few as possible
     */
    public void relayoutToCompress() {
        post(new Runnable() {
            @Override
            public void run() {
                compress();
            }
        });
    }

    private void compress() {
        int childCount = this.getChildCount();
        if (0 == childCount) {
            //no need to sort if flowlayout has no child view
            return;
        }
        int count = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            count++;
        }
        View[] childs = new View[count];
        int[] spaces = new int[count];
        int n = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            childs[n] = v;
            LayoutParams childLp = v.getLayoutParams();
            int childWidth = v.getMeasuredWidth();
            if (childLp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                spaces[n] = mlp.leftMargin + childWidth + mlp.rightMargin;
            } else {
                spaces[n] = childWidth;
            }
            n++;
        }
        int[] compressSpaces = new int[count];
        for (int i = 0; i < count; i++) {
            compressSpaces[i] = spaces[i] > usefulWidth ? usefulWidth : spaces[i];
        }
        sortToCompress(childs, compressSpaces);
        this.removeAllViews();
        for (View v : childList) {
            this.addView(v);
        }
        childList.clear();
    }

    private void sortToCompress(View[] childs, int[] spaces) {
        int childCount = childs.length;
        int[][] table = new int[childCount + 1][usefulWidth + 1];
        for (int i = 0; i < childCount + 1; i++) {
            for (int j = 0; j < usefulWidth; j++) {
                table[i][j] = 0;
            }
        }
        boolean[] flag = new boolean[childCount];
        for (int i = 0; i < childCount; i++) {
            flag[i] = false;
        }
        for (int i = 1; i <= childCount; i++) {
            for (int j = spaces[i - 1]; j <= usefulWidth; j++) {
                table[i][j] = (table[i - 1][j] > table[i - 1][j - spaces[i - 1]] + spaces[i - 1]) ? table[i - 1][j] : table[i - 1][j - spaces[i - 1]] + spaces[i - 1];
            }
        }
        int v = usefulWidth;
        for (int i = childCount; i > 0 && v >= spaces[i - 1]; i--) {
            if (table[i][v] == table[i - 1][v - spaces[i - 1]] + spaces[i - 1]) {
                flag[i - 1] = true;
                v = v - spaces[i - 1];
            }
        }
        int rest = childCount;
        View[] restArray;
        int[] restSpaces;
        for (int i = 0; i < flag.length; i++) {
            if (flag[i]) {
                childList.add(childs[i]);
                rest--;
            }
        }

        if (0 == rest) {
            return;
        }
        restArray = new View[rest];
        restSpaces = new int[rest];
        int index = 0;
        for (int i = 0; i < flag.length; i++) {
            if (!flag[i]) {
                restArray[index] = childs[i];
                restSpaces[index] = spaces[i];
                index++;
            }
        }
        sortToCompress(restArray, restSpaces);
    }

    /**
     * add some blank view to make child elements look in alignment
     */
    public void relayoutToAlign() {
        post(this::align);
    }

    private void align() {
        int childCount = this.getChildCount();
        if (0 == childCount) {
            //no need to sort if flowlayout has no child view
            return;
        }
        int count = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            count++;
        }
        View[] childs = new View[count];
        int[] spaces = new int[count];
        int n = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            childs[n] = v;
            LayoutParams childLp = v.getLayoutParams();
            int childWidth = v.getMeasuredWidth();
            if (childLp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                spaces[n] = mlp.leftMargin + childWidth + mlp.rightMargin;
            } else {
                spaces[n] = childWidth;
            }
            n++;
        }
        int lineTotal = 0;
        int start = 0;
        this.removeAllViews();
        for (int i = 0; i < count; i++) {
            if (lineTotal + spaces[i] > usefulWidth) {
                int blankWidth = usefulWidth - lineTotal;
                int end = i - 1;
                int blankCount = end - start;
                if (blankCount >= 0) {
                    if (blankCount > 0) {
                        int eachBlankWidth = blankWidth / blankCount;
                        MarginLayoutParams lp = new MarginLayoutParams(eachBlankWidth, 0);
                        for (int j = start; j < end; j++) {
                            this.addView(childs[j]);
                            BlankView blank = new BlankView(mContext);
                            this.addView(blank, lp);
                        }
                    }
                    this.addView(childs[end]);
                    start = i;
                    i--;
                    lineTotal = 0;
                } else {
                    this.addView(childs[i]);
                    start = i + 1;
                    lineTotal = 0;
                }
            } else {
                lineTotal += spaces[i];
            }
        }
        for (int i = start; i < count; i++) {
            this.addView(childs[i]);
        }
    }

    /**
     * use both of relayout methods together
     */
    public void relayoutToCompressAndAlign() {
        post(() -> {
            compress();
            align();
        });
    }

    /**
     * cut the flowlayout to the specified num of lines
     */
    public void specifyLines(final int line_num_now) {
        post(() -> {
            int line_num = line_num_now;
            int childNum = 0;
            if (line_num > lineNumList.size()) {
                line_num = lineNumList.size();
            }
            for (int i = 0; i < line_num; i++) {
                childNum += lineNumList.get(i);
            }
            List<View> viewList = new ArrayList<>();
            for (int i = 0; i < childNum; i++) {
                viewList.add(getChildAt(i));
            }
            removeAllViews();
            for (View v : viewList) {
                addView(v);
            }
        });
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(super.generateDefaultLayoutParams());
    }

    public void handleSingleSelect(View tagView) {
        boolean isCancel = tagView.isSelected();
        if (!isCancel) {
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                if (child.isSelected()) {
                    child.setSelected(false);
                }
            }
            tagView.setSelected(true);
        }
    }

    static class BlankView extends View {

        public BlankView(Context context) {
            super(context);
        }
    }

    public interface OnTagClickListener<T> {
        void onTagClick(FlowLayout parent, View tagView, T tag, int index);
    }
}
