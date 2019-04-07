package com.hamami.musictrywithmitch.adapters;


public interface ItemTouchHelperAdapter {


    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
