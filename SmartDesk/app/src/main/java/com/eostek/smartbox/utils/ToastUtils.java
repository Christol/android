package com.eostek.smartbox.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static Toast mToast;

    public static void init(Context context) {
        mToast = Toast.makeText(context, null, Toast.LENGTH_LONG);
    }

    public static void show(int resId) {
        mToast.setText(resId);
        mToast.show();
    }

    public static void show(CharSequence charSequence) {
        mToast.setText(charSequence);
        mToast.show();
    }
}
