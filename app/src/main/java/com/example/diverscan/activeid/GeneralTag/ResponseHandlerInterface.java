package com.example.diverscan.activeid.GeneralTag;

import android.content.Context;

import com.zebra.rfid.api3.TagData;

public interface ResponseHandlerInterface {
    void handleTagdata(TagData[] tagData);
    void handleTriggerPress(boolean pressed);
    Context GetContext();
    void  SetMessage(String Text);
}
