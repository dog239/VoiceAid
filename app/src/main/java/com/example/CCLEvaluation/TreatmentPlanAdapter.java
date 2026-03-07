package com.example.CCLEvaluation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TreatmentPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String PLACEHOLDER_TEXT = "（根据评估情况待定）";

    public interface ListActionListener {
    }

    private final List<PlanUiItem> items;

    public TreatmentPlanAdapter(List<PlanUiItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case PlanUiItem.TYPE_SECTION_DIVIDER:
                return new SectionDividerViewHolder(inflater.inflate(R.layout.item_plan_section_divider, parent, false));
            case PlanUiItem.TYPE_MODULE_CARD:
                return new ModuleCardViewHolder(inflater.inflate(R.layout.item_plan_card_module, parent, false), this);
            default:
                return new EmptyViewHolder(new View(parent.getContext()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlanUiItem item = items.get(position);
        if (holder instanceof SectionDividerViewHolder) {
            ((SectionDividerViewHolder) holder).bind((PlanUiItem.SectionDivider) item);
        } else if (holder instanceof ModuleCardViewHolder) {
            ((ModuleCardViewHolder) holder).bind((PlanUiItem.ModuleCard) item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SectionDividerViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        SectionDividerViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tv_section_divider_title);
        }
        void bind(PlanUiItem.SectionDivider item) {
            title.setText(item.title);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View v) { super(v); }
    }

    class ModuleCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView toggle;
        private final ImageView editBtn;
        private final LinearLayout contentLayout;
        private final LinearLayout headerLayout;
        private final TreatmentPlanAdapter adapter;
        private final LayoutInflater inflater;

        ModuleCardViewHolder(@NonNull View itemView, TreatmentPlanAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            this.inflater = LayoutInflater.from(itemView.getContext());
            
            title = itemView.findViewById(R.id.tv_module_title);
            toggle = itemView.findViewById(R.id.btn_expand_toggle);
            editBtn = itemView.findViewById(R.id.btn_edit_module);
            contentLayout = itemView.findViewById(R.id.layout_card_content);
            headerLayout = itemView.findViewById(R.id.layout_card_header);
        }

        void bind(PlanUiItem.ModuleCard item) {
            title.setText(item.title);
            
            toggle.setRotation(item.expanded ? 0f : -90f);
            contentLayout.setVisibility(item.expanded ? View.VISIBLE : View.GONE);
            
            View.OnClickListener toggleAction = v -> {
                item.expanded = !item.expanded;
                toggle.animate().rotation(item.expanded ? 0f : -90f).setDuration(200).start();
                contentLayout.setVisibility(item.expanded ? View.VISIBLE : View.GONE);
            };
            headerLayout.setOnClickListener(toggleAction);
            toggle.setOnClickListener(toggleAction);

            updateEditButtonState(item);
            editBtn.setOnClickListener(v -> {
                item.isEditing = !item.isEditing;
                updateEditButtonState(item);
                renderContent(item);
            });

            if (item.expanded) {
                renderContent(item);
            }
        }

        private void updateEditButtonState(PlanUiItem.ModuleCard item) {
            if (item.isEditing) {
                editBtn.setColorFilter(0xFF1976D2);
            } else {
                editBtn.setColorFilter(0xFF666666);
            }
        }

        private void renderContent(PlanUiItem.ModuleCard item) {
            contentLayout.removeAllViews();
            renderListItems(item.children, item);
        }

        private void renderListItems(List<PlanUiItem> items, PlanUiItem.ModuleCard rootCard) {
            for (int i = 0; i < items.size(); i++) {
                PlanUiItem child = items.get(i);
                View childView = createChildView(child, items, i, rootCard);
                if (childView != null) {
                    contentLayout.addView(childView);
                }
            }
        }

        private View createChildView(PlanUiItem child, List<PlanUiItem> currentList, int index, PlanUiItem.ModuleCard rootCard) {
            if (child instanceof PlanUiItem.InfoBox) {
                // 渲染灰色信息框
                View view = inflater.inflate(R.layout.item_plan_info_box, contentLayout, false);
                PlanUiItem.InfoBox infoBox = (PlanUiItem.InfoBox) child;
                TextView boxTitle = view.findViewById(R.id.tv_info_box_title);
                LinearLayout boxItems = view.findViewById(R.id.ll_info_box_items);
                
                boxTitle.setText(infoBox.title);
                boxItems.removeAllViews();
                
                // 递归渲染 InfoBox 内部的子项
                for (int j = 0; j < infoBox.children.size(); j++) {
                    PlanUiItem subItem = infoBox.children.get(j);
                    View subView = createChildView(subItem, infoBox.children, j, rootCard);
                    if (subView != null) {
                        boxItems.addView(subView);
                    }
                }
                return view;
            } else if (child instanceof PlanUiItem.SectionHeader) {
                View view = inflater.inflate(R.layout.item_plan_section_header, contentLayout, false);
                PlanUiItem.SectionHeader header = (PlanUiItem.SectionHeader) child;
                TextView tv = view.findViewById(R.id.tv_section_title);
                View divider = view.findViewById(R.id.divider);
                
                tv.setText(header.title);
                if (header.level == 1) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    tv.setTextColor(0xFF222222);
                    divider.setVisibility(View.VISIBLE);
                } else {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    tv.setTextColor(0xFF444444);
                    divider.setVisibility(View.GONE);
                }
                return view;
                
            } else if (child instanceof PlanUiItem.KeyValue) {
                View view = inflater.inflate(R.layout.item_plan_key_value, contentLayout, false);
                PlanUiItem.KeyValue kv = (PlanUiItem.KeyValue) child;
                TextView label = view.findViewById(R.id.tv_label);
                EditText value = view.findViewById(R.id.et_value);
                
                label.setText(kv.label);
                value.setText(kv.value);
                value.setInputType(kv.inputType);
                
                value.setEnabled(rootCard.isEditing);
                value.setBackgroundResource(rootCard.isEditing ? R.drawable.bg_edit_focus : 0);
                if (!rootCard.isEditing) {
                    value.setBackground(null);
                }

                value.addTextChangedListener(new SimpleTextWatcher(text -> kv.value = text));
                return view;
                
            } else if (child instanceof PlanUiItem.ListItem) {
                View view = inflater.inflate(R.layout.item_plan_list_row, contentLayout, false);
                PlanUiItem.ListItem li = (PlanUiItem.ListItem) child;
                EditText value = view.findViewById(R.id.et_item_value);
                ImageButton deleteBtn = view.findViewById(R.id.btn_delete_row);
                
                value.setText(li.value);
                
                value.setEnabled(rootCard.isEditing);
                boolean canDelete = rootCard.isEditing;
                deleteBtn.setVisibility(canDelete ? View.VISIBLE : View.GONE);
                
                value.setBackgroundResource(rootCard.isEditing ? R.drawable.bg_edit_focus : 0);
                if (!rootCard.isEditing) {
                    value.setBackground(null);
                }

                value.addTextChangedListener(new SimpleTextWatcher(text -> li.value = text));
                
                if (canDelete) {
                    deleteBtn.setOnClickListener(v -> {
                        currentList.remove(index);
                        renderContent(rootCard);
                    });
                }
                return view;
                
            } else if (child instanceof PlanUiItem.AddButton) {
                if (!rootCard.isEditing) {
                    return null;
                }
                View view = inflater.inflate(R.layout.item_plan_add_row, contentLayout, false);
                PlanUiItem.AddButton addBtn = (PlanUiItem.AddButton) child;
                Button btn = view.findViewById(R.id.btn_add_row);
                btn.setText(addBtn.label);
                
                btn.setOnClickListener(v -> {
                    PlanUiItem.ListItem newItem = new PlanUiItem.ListItem(addBtn.listPath, 0, "");
                    // 插入到当前 AddButton 所在位置（index），AddButton 顺延到下一位
                    currentList.add(index, newItem);
                    renderContent(rootCard);
                });
                return view;
            } else if (child instanceof PlanUiItem.StageHeader) {

                View view = inflater.inflate(R.layout.item_plan_stage_header, contentLayout, false);
                TextView tv = view.findViewById(R.id.tv_stage_title);
                tv.setText(((PlanUiItem.StageHeader)child).title);
                return view;
            } else if (child instanceof PlanUiItem.ListMirror) {
                 View view = inflater.inflate(R.layout.item_plan_list_mirror, contentLayout, false);
                 LinearLayout container = view.findViewById(R.id.ll_mirror_items);
                 container.removeAllViews();
                 
                 PlanUiItem.ListMirror mirror = (PlanUiItem.ListMirror) child;
                 List<String> values = mirror.fallbackValues != null ? mirror.fallbackValues : new ArrayList<>();
                 if (values.isEmpty()) values.add(mirror.emptyHint);
                 
                 for (String val : values) {
                     View row = inflater.inflate(R.layout.item_plan_list_mirror_row, container, false);
                     ((TextView)row.findViewById(R.id.tv_mirror_text)).setText(val);
                     container.addView(row);
                 }
                 return view;
            }
            
            return null;
        }
    }

    interface TextChangeCallback {
        void onChange(String text);
    }

    static class SimpleTextWatcher implements TextWatcher {
        private final TextChangeCallback callback;
        SimpleTextWatcher(TextChangeCallback callback) { this.callback = callback; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.onChange(s == null ? "" : s.toString());
        }
        @Override public void afterTextChanged(Editable s) {}
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}

