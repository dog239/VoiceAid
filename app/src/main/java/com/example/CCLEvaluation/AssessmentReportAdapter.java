package com.example.CCLEvaluation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bean.AssessmentModule;

public class AssessmentReportAdapter extends RecyclerView.Adapter<AssessmentReportAdapter.ViewHolder> {

    private List<AssessmentModule> modules;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AssessmentModule module);
    }

    public AssessmentReportAdapter(List<AssessmentModule> modules, OnItemClickListener listener) {
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
                .inflate(R.layout.item_assessment_report, parent, false);
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
        ImageView icon;
        TextView title;
        TextView status;
        TextView desc;
        TextView date;
        TextView reportStatus;
        ImageView arrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            status = itemView.findViewById(R.id.status);
            desc = itemView.findViewById(R.id.desc);
            date = itemView.findViewById(R.id.date);
            reportStatus = itemView.findViewById(R.id.report_status);
            arrow = itemView.findViewById(R.id.arrow);
        }

        public void bind(final AssessmentModule module, final OnItemClickListener listener) {
            icon.setImageResource(module.getIconResId());
            title.setText(module.getTitle() + "结果"); // e.g., "构音评估结果"
            desc.setText(module.getDescription());
            
            // Set completion status
            if (module.isCompleted()) {
                status.setText("已完成");
                status.setTextColor(itemView.getContext().getResources().getColor(R.color.teal_700));
                status.setBackgroundResource(R.drawable.bg_chip_primary_light);
                
            } else {
                status.setText("待完成");
                status.setTextColor(itemView.getContext().getResources().getColor(R.color.warm_gray));
                status.setBackgroundResource(R.drawable.bg_chip_primary_light); // Using same bg for now
                
            }
            date.setVisibility(View.GONE);

            // Set report generation status
            if (module.isReportGenerated()) {
                reportStatus.setText("已生成干预报告");
                reportStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.purple_500));
                reportStatus.setBackgroundResource(R.drawable.bg_chip_purple_light);
            } else {
                reportStatus.setText("未生成干预报告");
                reportStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.warm_gray));
                reportStatus.setBackgroundResource(R.drawable.bg_chip_purple_light); // Using same bg for now
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(module);
                }
            });
        }
    }
}
