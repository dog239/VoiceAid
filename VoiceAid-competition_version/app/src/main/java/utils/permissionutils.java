package utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class permissionutils {
    /*
    单例
     */

    public static permissionutils permissionUtils;
    private final int RequestCode = 1;
    public permissionutils(){}
    public static permissionutils getInstance() {
        if (permissionUtils == null) {
            synchronized (permissionutils.class) {
                if (permissionUtils == null) {
                    permissionUtils = new permissionutils();
                }
            }
        }
        return permissionUtils;
    }


    public interface OnPermissionCallBackListener {
        void onGranted();

        void onDenied(List<String> deniedPes);
    }

    private OnPermissionCallBackListener listener;


    public void RequestPermissions(Activity context, String[] permissions, OnPermissionCallBackListener listener) {
        this.listener = listener;

        if (Build.VERSION.SDK_INT >= 23) {
            List<String> mPermissions = new ArrayList<String>();
            for (String pe : permissions) {
                int res = ContextCompat.checkSelfPermission(context, pe);
                if (res != PackageManager.PERMISSION_GRANTED) {
                    mPermissions.add(pe);
                }

            }
            if (mPermissions.size() > 0) {
                String[] array = mPermissions.toArray(new String[mPermissions.size()]);
                ActivityCompat.requestPermissions(context, array, RequestCode);
            } else {
                listener.onGranted();
            }
        }


    }

    public void onRequestPesResult(Activity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCode) {
            List<String> deniedPe = new ArrayList<String>();
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPe.add(permissions[i]);
                    }
                }

            }
            if (deniedPe.size() == 0) {
                listener.onGranted();
            } else {
                listener.onDenied(deniedPe);
            }
        } else {
            listener.onGranted();
        }
    }

    //提示用户手动设置页面权限

    public void showDialogTip(Activity context) {
        dialogUtils.showDialog(context, "提示信息", "已经禁用权限，请手动开启", "取消", new dialogUtils.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                context.finish();
            }
        }, "确定", () -> {
            goToAppSetting(context);
            context.finish();
        });
    }
    /*
    未授权，前往系统页面授权
     */
    public static void  goToAppSetting(Activity context){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }
}
