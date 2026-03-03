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

import java.util.List;

public class TreatmentPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String PLACEHOLDER_TEXT = "（根据评估情况待定）";
    private static final int GROUP_NONE = 0;
    private static final int GROUP_CARD = 1;
    private static final int GROUP_STAGE = 2;
    private static final int POS_SINGLE = 0;
    private static final int POS_TOP = 1;
    private static final int POS_MIDDLE = 2;
    private static final int POS_BOTTOM = 3;

    public interface ListActionListener {
        void onAddRow(String listPath, int position);
        void onRemoveRow(PlanUiItem.ListItem item, int position);
    }

    private final List<PlanUiItem> items;
    private final ListActionListener listener;

    public TreatmentPlanAdapter(List<PlanUiItem> items, ListActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == PlanUiItem.TYPE_CARD_HEADER) {
            View view = inflater.inflate(R.layout.item_plan_card_header, parent, false);
            return new CardHeaderViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_CARD_END) {
            View view = inflater.inflate(R.layout.item_plan_card_end, parent, false);
            return new CardEndViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_LIST_MIRROR) {
            View view = inflater.inflate(R.layout.item_plan_list_mirror, parent, false);
            return new ListMirrorViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_STAGE_HEADER) {
            View view = inflater.inflate(R.layout.item_plan_stage_header, parent, false);
            return new StageHeaderViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_STAGE_END) {
            View view = inflater.inflate(R.layout.item_plan_stage_end, parent, false);
            return new StageEndViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_SECTION) {
            View view = inflater.inflate(R.layout.item_plan_section_header, parent, false);
            return new SectionViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_KEY_VALUE) {
            View view = inflater.inflate(R.layout.item_plan_key_value, parent, false);
            return new KeyValueViewHolder(view);
        } else if (viewType == PlanUiItem.TYPE_LIST_ITEM) {
            View view = inflater.inflate(R.layout.item_plan_list_row, parent, false);
            return new ListItemViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_plan_add_row, parent, false);
            return new AddButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlanUiItem item = items.get(position);
        if (holder instanceof CardHeaderViewHolder) {
            ((CardHeaderViewHolder) holder).bind((PlanUiItem.CardHeader) item, this);
        } else if (holder instanceof CardEndViewHolder) {
            ((CardEndViewHolder) holder).bind();
        } else if (holder instanceof ListMirrorViewHolder) {
            ((ListMirrorViewHolder) holder).bind((PlanUiItem.ListMirror) item, items);
        } else if (holder instanceof StageHeaderViewHolder) {
            ((StageHeaderViewHolder) holder).bind((PlanUiItem.StageHeader) item);
        } else if (holder instanceof StageEndViewHolder) {
            ((StageEndViewHolder) holder).bind();
        } else if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).bind((PlanUiItem.SectionHeader) item);
        } else if (holder instanceof KeyValueViewHolder) {
            ((KeyValueViewHolder) holder).bind((PlanUiItem.KeyValue) item);
        } else if (holder instanceof ListItemViewHolder) {
            ((ListItemViewHolder) holder).bind((PlanUiItem.ListItem) item, listener);
        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).bind((PlanUiItem.AddButton) item, listener);
        }
        applyItemVisibility(holder.itemView, position);
        applyCardBackground(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void applyItemVisibility(View itemView, int position) {
        boolean hidden = isHiddenByCollapse(position);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (hidden) {
            itemView.setVisibility(View.GONE);
            params.height = 0;
        } else {
            itemView.setVisibility(View.VISIBLE);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        itemView.setLayoutParams(params);
    }

    private boolean isHiddenByCollapse(int position) {
        for (int i = position; i >= 0; i--) {
            PlanUiItem item = items.get(i);
            if (item instanceof PlanUiItem.CardEnd) {
                return false;
            }
            if (item instanceof PlanUiItem.CardHeader) {
                PlanUiItem.CardHeader header = (PlanUiItem.CardHeader) item;
                if (!header.expanded && !(items.get(position) instanceof PlanUiItem.CardHeader)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private void applyCardBackground(View itemView, int position) {
        View container = itemView.findViewById(R.id.card_container);
        if (container == null) {
            return;
        }
        int groupType = resolveGroupType(position);
        if (groupType == GROUP_NONE) {
            container.setBackground(null);
            return;
        }
        int groupPosition = resolveGroupPosition(position, groupType);
        int backgroundRes = resolveBackgroundRes(groupType, groupPosition);
        container.setBackgroundResource(backgroundRes);
    }

    private int resolveGroupType(int position) {
        if (isInsideStageGroup(position)) {
            return GROUP_STAGE;
        }
        if (isInsideCardGroup(position)) {
            return GROUP_CARD;
        }
        return GROUP_NONE;
    }

    private boolean isInsideCardGroup(int position) {
        for (int i = position; i >= 0; i--) {
            PlanUiItem item = items.get(i);
            if (item instanceof PlanUiItem.CardEnd) {
                return false;
            }
            if (item instanceof PlanUiItem.CardHeader) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideStageGroup(int position) {
        for (int i = position; i >= 0; i--) {
            PlanUiItem item = items.get(i);
            if (item instanceof PlanUiItem.StageEnd) {
                return false;
            }
            if (item instanceof PlanUiItem.StageHeader) {
                return true;
            }
        }
        return false;
    }

    private int resolveGroupPosition(int position, int groupType) {
        if (groupType == GROUP_CARD) {
            if (items.get(position) instanceof PlanUiItem.CardHeader) {
                PlanUiItem.CardHeader header = (PlanUiItem.CardHeader) items.get(position);
                if (!header.expanded || isNextCardEnd(position)) {
                    return POS_SINGLE;
                }
                return POS_TOP;
            }
            if (items.get(position) instanceof PlanUiItem.CardEnd) {
                return POS_BOTTOM;
            }
            return isNextCardEnd(position) ? POS_BOTTOM : POS_MIDDLE;
        }
        if (groupType == GROUP_STAGE) {
            if (items.get(position) instanceof PlanUiItem.StageHeader) {
                return isNextStageEnd(position) ? POS_SINGLE : POS_TOP;
            }
            if (items.get(position) instanceof PlanUiItem.StageEnd) {
                return POS_MIDDLE;
            }
            return isNextStageEnd(position) ? POS_BOTTOM : POS_MIDDLE;
        }
        return POS_MIDDLE;
    }

    private boolean isNextCardEnd(int position) {
        for (int i = position + 1; i < items.size(); i++) {
            PlanUiItem item = items.get(i);
            if (item instanceof PlanUiItem.CardEnd) {
                return true;
            }
            if (item instanceof PlanUiItem.CardHeader) {
                return true;
            }
        }
        return true;
    }

    private boolean isNextStageEnd(int position) {
        for (int i = position + 1; i < items.size(); i++) {
            PlanUiItem item = items.get(i);
            if (item instanceof PlanUiItem.StageEnd) {
                return true;
            }
            if (item instanceof PlanUiItem.StageHeader) {
                return true;
            }
        }
        return true;
    }

    private int resolveBackgroundRes(int groupType, int groupPosition) {
        if (groupType == GROUP_STAGE) {
            if (groupPosition == POS_SINGLE) {
                return R.drawable.bg_plan_stage_card_single;
            } else if (groupPosition == POS_TOP) {
                return R.drawable.bg_plan_stage_card_top;
            } else if (groupPosition == POS_BOTTOM) {
                return R.drawable.bg_plan_stage_card_bottom;
            }
            return R.drawable.bg_plan_stage_card_middle;
        }
        if (groupPosition == POS_SINGLE) {
            return R.drawable.bg_plan_card_single;
        } else if (groupPosition == POS_TOP) {
            return R.drawable.bg_plan_card_top;
        } else if (groupPosition == POS_BOTTOM) {
            return R.drawable.bg_plan_card_bottom;
        }
        return R.drawable.bg_plan_card_middle;
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_section_title);
        }

        void bind(PlanUiItem.SectionHeader item) {
            title.setText(item.title);
            if (item.level == 0) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                title.setPadding(0, 12, 0, 6);
                title.setTextColor(0xFF000000);
            } else if (item.level == 1) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                title.setPadding(0, 10, 0, 4);
                title.setTextColor(0xFF222222);
            } else {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                title.setPadding(0, 8, 0, 4);
                title.setTextColor(0xFF444444);
            }
        }
    }

    static class CardHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView toggle;

        CardHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_card_title);
            toggle = itemView.findViewById(R.id.btn_card_toggle);
        }

        void bind(PlanUiItem.CardHeader item, TreatmentPlanAdapter adapter) {
            title.setText(item.title);
            toggle.setVisibility(item.collapsible ? View.VISIBLE : View.GONE);
            toggle.setRotation(item.expanded ? 0f : -90f);
            itemView.setOnClickListener(v -> {
                if (!item.collapsible) {
                    return;
                }
                item.expanded = !item.expanded;
                adapter.notifyDataSetChanged();
            });
        }
    }

    static class CardEndViewHolder extends RecyclerView.ViewHolder {
        CardEndViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind() {
        }
    }

    static class StageHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        StageHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_stage_title);
        }

        void bind(PlanUiItem.StageHeader item) {
            title.setText(item.title);
        }
    }

    static class StageEndViewHolder extends RecyclerView.ViewHolder {
        StageEndViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind() {
        }
    }

    static class KeyValueViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final EditText value;
        private TextWatcher watcher;

        KeyValueViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tv_label);
            value = itemView.findViewById(R.id.et_value);
        }

        void bind(PlanUiItem.KeyValue item) {
            label.setText(item.label);
            value.setInputType(item.inputType);
            if (watcher != null) {
                value.removeTextChangedListener(watcher);
            }
            value.setText(item.value);
            watcher = new SimpleTextWatcher(text -> item.value = text);
            value.addTextChangedListener(watcher);
        }
    }

    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        private final EditText value;
        private final ImageButton deleteButton;
        private TextWatcher watcher;

        ListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            value = itemView.findViewById(R.id.et_item_value);
            deleteButton = itemView.findViewById(R.id.btn_delete_row);
        }

        void bind(PlanUiItem.ListItem item, ListActionListener listener) {
            if (watcher != null) {
                value.removeTextChangedListener(watcher);
            }
            value.setText(item.value);
            watcher = new SimpleTextWatcher(text -> item.value = text);
            value.addTextChangedListener(watcher);
            deleteButton.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onRemoveRow(item, pos);
                }
            });
        }
    }

    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        private final Button button;

        AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btn_add_row);
        }

        void bind(PlanUiItem.AddButton item, ListActionListener listener) {
            button.setText(item.label);
            button.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onAddRow(item.listPath, pos);
                }
            });
        }
    }

    static class ListMirrorViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout container;
        private final LayoutInflater inflater;

        ListMirrorViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ll_mirror_items);
            inflater = LayoutInflater.from(itemView.getContext());
        }

        void bind(PlanUiItem.ListMirror item, List<PlanUiItem> allItems) {
            container.removeAllViews();
            List<String> values = resolveValues(item, allItems);
            if (values.isEmpty()) {
                values.add(item.emptyHint == null ? PLACEHOLDER_TEXT : item.emptyHint);
            }
            for (String value : values) {
                View row = inflater.inflate(R.layout.item_plan_list_mirror_row, container, false);
                TextView textView = row.findViewById(R.id.tv_mirror_text);
                textView.setText(safeText(value).isEmpty() ? PLACEHOLDER_TEXT : safeText(value));
                container.addView(row);
            }
        }

        private List<String> resolveValues(PlanUiItem.ListMirror item, List<PlanUiItem> allItems) {
            List<String> values = new java.util.ArrayList<>();
            if (item.fallbackValues != null && !item.fallbackValues.isEmpty()) {
                values.addAll(item.fallbackValues);
                return values;
            }
            if (item.listPath == null) {
                return values;
            }
            for (PlanUiItem planUiItem : allItems) {
                if (planUiItem instanceof PlanUiItem.ListItem) {
                    PlanUiItem.ListItem listItem = (PlanUiItem.ListItem) planUiItem;
                    if (item.listPath.equals(listItem.listPath)) {
                        values.add(listItem.value);
                    }
                }
            }
            return values;
        }
    }

    interface TextChangeCallback {
        void onChange(String text);
    }

    static class SimpleTextWatcher implements TextWatcher {
        private final TextChangeCallback callback;

        SimpleTextWatcher(TextChangeCallback callback) {
            this.callback = callback;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.onChange(s == null ? "" : s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
