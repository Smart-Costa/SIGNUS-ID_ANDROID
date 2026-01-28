package com.example.diverscan.activeid.GeneralTag;

import android.content.Context;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

public interface ResponseHandlerInterface {
    void handleTagdata(ReaderTag[] tagData);
    void handleTriggerPress(boolean pressed);
    Context GetContext();
    void  SetMessage(String Text);
}
