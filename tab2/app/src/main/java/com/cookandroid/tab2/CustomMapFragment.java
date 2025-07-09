package com.cookandroid.tab2;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.SupportMapFragment;

public class CustomMapFragment extends SupportMapFragment {

    public static class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(@NonNull Context context) {
            super(context);
        }

        public TouchableWrapper(@NonNull Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // 뷰페이저2의 터치 이벤트를 가로채지 않도록 터치 이벤트를 처리합니다.
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TouchableWrapper frameLayout = new TouchableWrapper(getActivity());
        frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) getView()).addView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}