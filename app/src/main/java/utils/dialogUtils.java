package utils;

import android.app.AlertDialog;
import android.content.Context;

public class dialogUtils {

    public interface OnLeftClickListener {
        public void onLeftClick() throws Exception;
    }

    public interface OnRightClickListener {
        public void onRightClick();
    }

    public static void showDialog(Context context, String title, String msg, String LB, OnLeftClickListener LListener, String RB, OnRightClickListener RListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(msg);
        builder.setNegativeButton(LB, (dialogInterface, i) -> {
            if (LListener != null) {
                try {
                    LListener.onLeftClick();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(RB, (dialogInterface, i) -> {
            if (RListener != null) {
                RListener.onRightClick();
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }
}
