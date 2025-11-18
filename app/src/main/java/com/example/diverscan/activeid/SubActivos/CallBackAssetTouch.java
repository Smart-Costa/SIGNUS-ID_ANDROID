package com.example.diverscan.activeid.SubActivos;

import androidx.recyclerview.widget.RecyclerView;

public interface CallBackAssetTouch {
    void itemTouchOnMode(int oldPosition, int newPosition);
    void onSwiped(RecyclerView.ViewHolder viewHolder, int position);
}
