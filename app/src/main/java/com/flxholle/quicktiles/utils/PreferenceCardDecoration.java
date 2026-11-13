package com.flxholle.quicktiles.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.flxholle.quicktiles.R;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * Draws a rounded card behind every group of preference rows, so the settings screen reads as
 * sections instead of one endless list. The category header stays outside of its card, which is what
 * makes the grouping visible at a glance.
 * <p>
 * It works on the item views only - a row is a row if it has the widget frame that every
 * {@link androidx.preference.Preference} layout brings along - so it does not depend on the internal
 * adapter of the preference library.
 */
public class PreferenceCardDecoration extends RecyclerView.ItemDecoration {

    private final MaterialShapeDrawable card = new MaterialShapeDrawable();
    private final int inset;

    public PreferenceCardDecoration(@NonNull Context context) {
        card.setShapeAppearanceModel(ShapeAppearanceModel.builder()
                .setAllCornerSizes(context.getResources().getDimension(R.dimen.settings_card_corner))
                .build());
        card.setFillColor(ColorStateList.valueOf(cardColor(context)));
        inset = context.getResources().getDimensionPixelSize(R.dimen.settings_card_inset);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        if (right <= left) {
            return;
        }

        int cardTop = -1;
        int cardBottom = -1;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (!isRow(child)) {
                // A category header or a preference outside of any category: the current card ends
                drawCard(canvas, left, right, cardTop, cardBottom);
                cardTop = -1;
                cardBottom = -1;
                continue;
            }
            int top = child.getTop() - inset;
            int bottom = child.getBottom() + inset;
            if (cardTop < 0) {
                cardTop = top;
                cardBottom = bottom;
            } else {
                cardTop = Math.min(cardTop, top);
                cardBottom = Math.max(cardBottom, bottom);
            }
        }
        drawCard(canvas, left, right, cardTop, cardBottom);
    }

    private void drawCard(Canvas canvas, int left, int right, int top, int bottom) {
        if (top < 0 || bottom <= top) {
            return;
        }
        card.setBounds(left, top, right, bottom);
        card.draw(canvas);
    }

    /**
     * @return true for a preference row, false for a category header or the search field.
     */
    private static boolean isRow(View child) {
        return child.findViewById(android.R.id.widget_frame) != null;
    }

    /**
     * @return the container colour of the current theme, falling back to the surface variant on
     * themes that do not know the Material 3 container roles.
     */
    private static int cardColor(Context context) {
        // Referenced directly so the resource shrinker keeps the attribute
        int color = resolve(context, com.google.android.material.R.attr.colorSurfaceContainerLow);
        if (color != 0) {
            return color;
        }
        color = resolve(context, com.google.android.material.R.attr.colorSurfaceVariant);
        return color != 0 ? color : Color.TRANSPARENT;
    }

    private static int resolve(Context context, int themeAttribute) {
        if (themeAttribute == 0) {
            return 0;
        }
        TypedValue value = new TypedValue();
        if (!context.getTheme().resolveAttribute(themeAttribute, value, true)) {
            return 0;
        }
        if (value.resourceId != 0) {
            return ContextCompat.getColor(context, value.resourceId);
        }
        return value.data;
    }
}
