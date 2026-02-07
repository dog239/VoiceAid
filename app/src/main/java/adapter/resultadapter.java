package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CCLEvaluation.R;

import java.util.ArrayList;

import bean.evaluation;

public class resultadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<evaluation> evaluations;
    private Context context;
    private final ResultUpdateListener updateListener;
    private int editingPosition = RecyclerView.NO_POSITION;
    private int editingCellIndex = -1;

    private static final int ITEM_COUNT = 11;
    private static final int VIEW_TYPE_DISPLAY = 0;
    private static final int VIEW_TYPE_EDIT = 1;
    private static final int EDITABLE_START_INDEX = 3;
    private static final int EDITABLE_END_INDEX = 9;
    private static final String PAYLOAD_EDIT_CELL = "edit_cell";

    public resultadapter(Context context, ArrayList<evaluation> evaluations, ResultUpdateListener updateListener) {
        this.context = context;
        this.evaluations = evaluations;
        this.updateListener = updateListener;
    }

    public resultadapter(Context context, ArrayList<evaluation> evaluations) {
        this(context, evaluations, null);
    }

    public interface ResultUpdateListener {
        void onArticulationDataChanged();
    }

    public void setEditingPosition(int position) {
        setEditingPosition(position, EDITABLE_START_INDEX);
    }

    public void setEditingPosition(int position, int cellIndex) {
        if (position == editingPosition && cellIndex == editingCellIndex) return;
        int old = editingPosition;
        editingPosition = position;
        editingCellIndex = cellIndex;
        if (old != RecyclerView.NO_POSITION && old != editingPosition) {
            notifyItemChanged(old);
        }
        if (editingPosition != RecyclerView.NO_POSITION) {
            if (old == editingPosition) {
                notifyItemChanged(editingPosition, PAYLOAD_EDIT_CELL);
            } else {
                notifyItemChanged(editingPosition);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 只有当前编辑行使用 EditText 布局，其余行用轻量展示布局。
        if (position > 0 && position == editingPosition) {
            return VIEW_TYPE_EDIT;
        }
        return VIEW_TYPE_DISPLAY;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private View[] item = new View[ITEM_COUNT];
        private int[] Id = new int[]{R.id.i1, R.id.i2, R.id.i3, R.id.i4, R.id.i5, R.id.i6, R.id.i7, R.id.i8, R.id.i9, R.id.i10, R.id.i11};

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            for (int i = 0; i < item.length; i++) item[i] = itemView.findViewById(Id[i]);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 展示行使用纯 TextView 布局，降低滚动时的测量开销。
        int layoutId = viewType == VIEW_TYPE_EDIT ? R.layout.item_result : R.layout.item_result_display;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull java.util.List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_EDIT_CELL) && holder instanceof ItemViewHolder) {
            holder.setIsRecyclable(false);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            evaluation evaluation = evaluations.get(position);
            if (evaluation instanceof bean.a) {
                ((bean.a) evaluation).bindEditable(itemViewHolder.item, position, updateListener, true, editingCellIndex);
                configureCellClick(itemViewHolder, position, true);
                requestEditFocusIfNeeded(itemViewHolder);
            }
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            boolean isEditRow = getItemViewType(position) == VIEW_TYPE_EDIT;
            holder.setIsRecyclable(!isEditRow);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            evaluation evaluation = evaluations.get(position);

            // 轻量重置 TextView，避免触发 EditText 的额外开销。
            for (View view : itemViewHolder.item) {
                if (view instanceof TextView && !(view instanceof EditText)) {
                    ((TextView) view).setText(null);
                }
            }

            int pos = evaluation.handle(itemViewHolder.item, position);

            if (position == 0) {
                itemViewHolder.item[0].setBackgroundResource(R.drawable.table_topleft);
                if (pos < itemViewHolder.item.length)
                    itemViewHolder.item[pos].setBackgroundResource(R.drawable.table_topright);
            } else if (position == evaluations.size() - 1) {
                itemViewHolder.item[0].setBackgroundResource(R.drawable.table_bottomleft);
                if (pos < itemViewHolder.item.length)
                    itemViewHolder.item[pos].setBackgroundResource(R.drawable.table_bottomright);
            } else {
                itemViewHolder.item[0].setBackgroundResource(R.drawable.btn);
                if (pos < itemViewHolder.item.length)
                    itemViewHolder.item[pos].setBackgroundResource(R.drawable.btn);
            }

            if (evaluation instanceof bean.a) {
                boolean editable = position > 0 && position == editingPosition;
                int cellIndex = editable ? editingCellIndex : -1;
                ((bean.a) evaluation).bindEditable(itemViewHolder.item, position, updateListener, editable, cellIndex);
                configureCellClick(itemViewHolder, position, editable);
                if (editable) {
                    requestEditFocusIfNeeded(itemViewHolder);
                }
            } else {
                clearRowClick(itemViewHolder);
            }
        }
    }

    private void configureCellClick(ItemViewHolder holder, int position, boolean editable) {
        View itemView = holder.itemView;
        if (!editable && position > 0) {
            if (itemView instanceof ViewGroup) {
                ((ViewGroup) itemView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            }
            itemView.setClickable(true);
            itemView.setOnClickListener(v -> setEditingPosition(position, EDITABLE_START_INDEX));
            for (int i = EDITABLE_START_INDEX; i <= EDITABLE_END_INDEX; i++) {
                View view = holder.item[i];
                if (view == null) continue;
                final int cellIndex = i;
                // 点击单元格进入编辑态（切换为 EditText 行）。
                view.setOnClickListener(v -> setEditingPosition(position, cellIndex));
                view.setOnTouchListener(null);
            }
        } else {
            // 编辑态下由 EditText 自身处理焦点/触摸，但允许切换编辑目标。
            if (itemView instanceof ViewGroup) {
                ((ViewGroup) itemView).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }
            itemView.setOnClickListener(null);
            for (int i = 0; i < holder.item.length; i++) {
                View view = holder.item[i];
                if (view == null) continue;
                if (i >= EDITABLE_START_INDEX && i <= EDITABLE_END_INDEX) {
                    final int cellIndex = i;
                    if (cellIndex == editingCellIndex) {
                        view.setOnClickListener(null);
                        view.setOnTouchListener(null);
                    } else {
                        // 非当前单元格拦截触摸并切换编辑目标。
                        view.setOnClickListener(null);
                        view.setOnTouchListener((v, event) -> {
                            setEditingPosition(position, cellIndex);
                            return true;
                        });
                    }
                } else {
                    view.setOnClickListener(null);
                    view.setOnTouchListener(null);
                }
            }
        }
    }

    private void requestEditFocusIfNeeded(ItemViewHolder holder) {
        if (editingCellIndex < EDITABLE_START_INDEX || editingCellIndex > EDITABLE_END_INDEX || holder == null) return;
        if (editingCellIndex < holder.item.length) {
            View view = holder.item[editingCellIndex];
            if (view instanceof EditText) {
                view.post(view::requestFocus);
            }
        }
    }

    private void clearRowClick(ItemViewHolder holder) {
        View itemView = holder.itemView;
        if (itemView instanceof ViewGroup) {
            ((ViewGroup) itemView).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }
        itemView.setOnClickListener(null);
        for (View view : holder.item) {
            if (view != null) {
                view.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return evaluations == null ? 0 : evaluations.size();
    }
}
