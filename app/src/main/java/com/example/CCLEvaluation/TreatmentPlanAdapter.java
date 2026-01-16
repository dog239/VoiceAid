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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TreatmentPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
        if (viewType == PlanUiItem.TYPE_SECTION) {
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
        if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).bind((PlanUiItem.SectionHeader) item);
        } else if (holder instanceof KeyValueViewHolder) {
            ((KeyValueViewHolder) holder).bind((PlanUiItem.KeyValue) item);
        } else if (holder instanceof ListItemViewHolder) {
            ((ListItemViewHolder) holder).bind((PlanUiItem.ListItem) item, listener);
        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).bind((PlanUiItem.AddButton) item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
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
            } else if (item.level == 1) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                title.setPadding(0, 10, 0, 4);
            } else {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                title.setPadding(0, 8, 0, 4);
            }
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
}
