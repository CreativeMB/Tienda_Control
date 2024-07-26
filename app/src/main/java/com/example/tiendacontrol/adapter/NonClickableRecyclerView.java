package com.example.tiendacontrol.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class NonClickableRecyclerView extends RecyclerView {
    public NonClickableRecyclerView(Context context) {
        super(context);
    }

    public NonClickableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonClickableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Permitir el desplazamiento (ACTION_MOVE) pero bloquear los clics (ACTION_DOWN)
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            return false; // Permitir el desplazamiento
        }
        return true; // Bloquear clics
    }
}