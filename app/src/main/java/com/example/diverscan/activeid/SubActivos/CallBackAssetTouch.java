package com.example.diverscan.activeid.SubActivos;

import android.support.v7.widget.RecyclerView;

public interface CallBackAssetTouch {
    void itemTouchOnMode(int oldPosition, int newPosition);
    void onSwiped(RecyclerView.ViewHolder viewHolder, int position);
}
