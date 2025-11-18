package com.example.diverscan.activeid.Utilities;

import android.content.Context;
import android.widget.Toast;

public class DialogUtils {
    public static void toast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void error(Context ctx, String msg) {
        Toast.makeText(ctx, "❌ " + msg, Toast.LENGTH_LONG).show();
    }

    public static void warning(Context ctx, String msg) {
        Toast.makeText(ctx, "⚠️ " + msg, Toast.LENGTH_LONG).show();
    }
}
