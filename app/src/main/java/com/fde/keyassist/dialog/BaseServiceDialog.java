package com.fde.keyassist.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.fde.keyassist.R;


/**
 * 在Service中可使用的dialog
 */
public abstract class BaseServiceDialog extends Dialog {

    public BaseServiceDialog(@NonNull Context context) {
        super(context, R.style.NoTitleDialog);
        setContentView(getLayoutId());
        onInited();
    }

    // 显示
    @Override
    public void show() {
        // 获取窗口
        Window window = getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                //该类型的窗口可以显示在其他应用之上
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            WindowManager.LayoutParams wmParams = window.getAttributes();
            wmParams.width = getWidth();
            wmParams.height = getHeight();
            window.setAttributes(wmParams);
        }
        super.show();
    }

    protected abstract int getLayoutId();

    protected abstract int getWidth();

    protected abstract int getHeight();

    protected abstract void onInited();
}
