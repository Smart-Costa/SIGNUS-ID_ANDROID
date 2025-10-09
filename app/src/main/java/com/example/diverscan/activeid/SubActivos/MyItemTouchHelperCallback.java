package com.example.diverscan.activeid.SubActivos;

import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.diverscan.activeid.R;

public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {

    CallBackAssetTouch _callBackAssetTouch;

    public MyItemTouchHelperCallback(CallBackAssetTouch callBackAssetTouch) {
        this._callBackAssetTouch = callBackAssetTouch;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        _callBackAssetTouch.itemTouchOnMode(viewHolder.getAdapterPosition(),
                target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        _callBackAssetTouch.onSwiped(viewHolder, viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
            super.onChildDraw(c, recyclerView, viewHolder, dX
                    , dY, actionState, isCurrentlyActive);
        }else{
            final View foregroundView =
                    ((AdapterSubActivoSwipe.ViewHolder)viewHolder).deleteView;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView,dX
                    , dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState != ItemTouchHelper.ACTION_STATE_DRAG){
            final View foregroundView =
                    ((AdapterSubActivoSwipe.ViewHolder)viewHolder).infoView;
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView,dX,
                    dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((AdapterSubActivoSwipe.ViewHolder)viewHolder).infoView;
        foregroundView.setBackgroundColor(ContextCompat.getColor(((
                AdapterSubActivoSwipe.ViewHolder
                )viewHolder).infoView.getContext(), R.color.blanco));

        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if(viewHolder != null){
            final View foregroundView = ((AdapterSubActivoSwipe.ViewHolder)viewHolder).infoView;
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
                foregroundView.setBackgroundColor(Color.LTGRAY);
            }
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }
}
