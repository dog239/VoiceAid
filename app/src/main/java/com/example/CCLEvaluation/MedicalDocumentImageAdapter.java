package com.example.CCLEvaluation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.MedicalDiagnosisImageHelper;

public class MedicalDocumentImageAdapter extends RecyclerView.Adapter<MedicalDocumentImageAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(int position);
    }

    private final List<JSONObject> items = new ArrayList<>();
    private final OnDeleteClickListener onDeleteClickListener;
    private boolean editable;

    public MedicalDocumentImageAdapter(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setItems(List<JSONObject> newItems, boolean editable) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.editable = editable;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_document_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject item = items.get(position);
        String localPath = MedicalDiagnosisImageHelper.getLocalPath(item);
        Bitmap bitmap = decodePreviewBitmap(localPath);
        if (bitmap != null) {
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        String fileName = MedicalDiagnosisImageHelper.getFileName(item);
        if (TextUtils.isEmpty(fileName)) {
            fileName = TextUtils.isEmpty(localPath) ? "未命名资料图片" : new File(localPath).getName();
        }
        holder.nameView.setText(fileName);
        holder.deleteButton.setVisibility(editable ? View.VISIBLE : View.GONE);
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDelete(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private Bitmap decodePreviewBitmap(String localPath) {
        if (TextUtils.isEmpty(localPath)) {
            return null;
        }
        File file = new File(localPath);
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localPath, bounds);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, 480, 480);
        return BitmapFactory.decodeFile(localPath, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
        return Math.max(inSampleSize, 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_medical_document_image);
            nameView = itemView.findViewById(R.id.tv_medical_document_name);
            deleteButton = itemView.findViewById(R.id.btn_delete_medical_document);
        }
    }
}
