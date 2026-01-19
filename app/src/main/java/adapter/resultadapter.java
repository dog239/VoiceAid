package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CCLEvaluation.R;

import java.util.ArrayList;

import bean.evaluation;

public class resultadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<evaluation> evaluations;
    private Context context;

    public resultadapter(Context context, ArrayList<evaluation> evaluations) {
        this.context = context;
        this.evaluations = evaluations;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private View[] item = new View[9];
        private int[] Id = new int[]{R.id.i1, R.id.i2, R.id.i3, R.id.i4, R.id.i5, R.id.i6, R.id.i7, R.id.i8, R.id.i9};

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            for (int i = 0; i < item.length; i++) item[i] = itemView.findViewById(Id[i]);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            evaluation evaluation = evaluations.get(position);

            for (View view : itemViewHolder.item) {
                ((TextView) view).setText(null);
                view.setBackgroundResource(R.drawable.btn);
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
            }
        }
    }

    @Override
    public int getItemCount() {
        return evaluations == null ? 0 : evaluations.size();
    }
}
