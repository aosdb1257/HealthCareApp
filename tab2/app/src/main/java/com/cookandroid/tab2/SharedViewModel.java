package com.cookandroid.tab2;

import android.widget.ImageView;

import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private ImageView stepGifImageView;

    public ImageView getStepGifImageView() {
        return stepGifImageView;
    }

    public void setStepGifImageView(ImageView stepGifImageView) {
        this.stepGifImageView = stepGifImageView;
    }
}