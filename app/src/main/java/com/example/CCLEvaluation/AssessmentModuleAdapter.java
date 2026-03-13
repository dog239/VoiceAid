package com.example.CCLEvaluation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import bean.AssessmentModule;

public class AssessmentModuleAdapter extends RecyclerView.Adapter<AssessmentModuleAdapter.ViewHolder> {

    private List<AssessmentModule> modules;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AssessmentModule module);
    }

    public AssessmentModuleAdapter(List<AssessmentModule> modules, OnItemClickListener listener) {
        this.modules = modules;
        this.listener = listener;
    }

    public void updateData(List<AssessmentModule> newModules) {
        this.modules = newModules;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assessment_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssessmentModule module = modules.get(position);
        holder.bind(module, listener);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView moduleIcon;
        TextView moduleTitle;
        TextView moduleDesc;
        TextView moduleTime;
        TextView moduleStatus;
        MaterialButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleIcon = itemView.findViewById(R.id.module_icon);
            moduleTitle = itemView.findViewById(R.id.module_title);
            moduleDesc = itemView.findViewById(R.id.module_desc);
            moduleTime = itemView.findViewById(R.id.module_time);
            moduleStatus = itemView.findViewById(R.id.module_status);
            btnAction = itemView.findViewById(R.id.btn_action);
        }

        public void bind(final AssessmentModule module, final OnItemClickListener listener) {
            moduleIcon.setImageResource(module.getIconResId());
            moduleTitle.setText(module.getTitle());
            moduleDesc.setText(module.getDescription());
            moduleTime.setText(module.getTimeEstimate());

            if (module.isCompleted()) {
                moduleStatus.setText("已完成");
                moduleStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.teal_700)); // #008A7C
                moduleStatus.setBackgroundResource(R.drawable.bg_chip_primary_light);

                btnAction.setText("已完成");
                btnAction.setEnabled(false);
            } else {
                moduleStatus.setText("未完成");
                moduleStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.warm_gray)); // #64748B
                moduleStatus.setBackgroundResource(R.drawable.bg_chip_primary_light); // Using same bg for now, can change if needed

                if ("A".equals(module.getId())) {
                    int status = module.getProgressStatus();
                    if (status == AssessmentModule.STATUS_IN_PROGRESS) {
                        btnAction.setText("继续测试");
                    } else {
                        btnAction.setText("开始测试");
                    }
                } else {
                    btnAction.setText("开始测评");
                }
                btnAction.setEnabled(true);
            }

            // Button click triggers the same action as item click
            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onItemClick(module);
                }
            };

            btnAction.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }
    }
}
