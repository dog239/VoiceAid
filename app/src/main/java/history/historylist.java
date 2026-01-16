package history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CCLEvaluation.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.dataManager;
import utils.dialogUtils;
import utils.dirpath;

public class historylist extends AppCompatActivity {
    RecyclerView mRecyclerView;
    MyAdapter mMyAdapter;
    List<Buttontext> mNewsList = new ArrayList<>();
    private TextView ChildNumber;

    private int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);
        mRecyclerView = findViewById(R.id.recyclerview);
        ChildNumber = findViewById(R.id.childnumber);
        //得到有多少儿童被测了
        try {
            File file = new File(dirpath.PATH_FETCH_DIR_INFO, "Index.json");
            if(!file.exists()){
                number = 0;
                ChildNumber.setText("共测评"+(number)+"名儿童");
            }else{
                JSONObject data = dataManager.getInstance().loadData("Index.json");
                if(data.length() == 0){
                    number = 0;
                    ChildNumber.setText("共测评"+(number)+"名儿童");
                }else{
                    if(data.has("number")){
                        number = data.getInt("number");
                    }else {
                        number = 0;
                    }
                    // 构造一些数据
                    for (int i = number; i > 0; i--) {
                        Buttontext news = new Buttontext();
                        String fname = data.getString(String.valueOf(i));
                        JSONObject child = dataManager.getInstance().loadData(fname);
                        JSONObject info = child.getJSONObject("info");
                        String currentDate = info.getString("testDate");
                        String name = info.getString("name");
                        String tester = info.getString("examiner");
                        news.title = "时间：" + currentDate + "儿童：" + name + " 测试员：" + tester;
                        news.fname = fname;
                        mNewsList.add(news);
                    }
                    mMyAdapter = new MyAdapter();
                    mRecyclerView.setAdapter(mMyAdapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(historylist.this);
                    layoutManager.setOrientation(RecyclerView.VERTICAL);
                    mRecyclerView.setLayoutManager(layoutManager);
                    ChildNumber.setText("共测评"+(number)+"名儿童");
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(historylist.this);
            View view = inflater.inflate(R.layout.item_list,parent,false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Buttontext news = mNewsList.get(position);
            holder.mTitleTv.setText(news.title);
            holder.mTitleTv.setOnClickListener(v -> {
                Intent intent;
                intent = new Intent(historylist.this, showchildinformation.class);
                intent.putExtra("position",position);
                intent.putExtra("fName",news.fname);
                Boolean old1 = getIntent().getBooleanExtra("old1",false);
                String Uid = getIntent().getStringExtra("Uid");
                intent.putExtra("old1",old1);
                intent.putExtra("Uid", Uid);
                startActivity(intent);
                finish();
            });
            holder.mDelete.setOnClickListener(v -> {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    showLogoutConfirmationDialog(news.fname,position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mNewsList.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleTv;
        ImageView mDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTv = itemView.findViewById(R.id.textView);
            //mTitleTv.setBackgroundResource(R.drawable.btnhistory);
            mDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
    private void  clearStudentInfoAudio(String fname, int position) throws Exception {
        JSONObject data = dataManager.getInstance().loadData(fname);
        String[] tasks = {"A","E","NWR","PN","PST","RE","RG","S"};
        for (String task : tasks) {
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray(task);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.has("audioPath") && !object.isNull("audioPath")) {
                    dataManager.getInstance().deleteAudioFile(object.getString("audioPath"));
                }
            }
        }
        dataManager.getInstance().deleteData(fname);
        // 保存数据到SharedPreferences
        JSONObject index = dataManager.getInstance().loadData("Index.json");
        int number = index.getInt("number");
        for(int i=number-position;i<number;++i){
            index.put(String.valueOf(i),index.getString(String.valueOf(i+1)));
        }
        index.remove(String.valueOf(number));
        index.put("number",number-1);
        dataManager.getInstance().saveData("Index.json",index);
        Toast.makeText(this,"儿童信息已删除",Toast.LENGTH_SHORT).show();
    }
    private void showLogoutConfirmationDialog(String fname,int position) {
        dialogUtils.showDialog(this, "提示信息", "您确定要删除该学生的全部信息和答题记录吗？",
                "确认", () -> {
                    try {
                        clearStudentInfoAudio(fname,position);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Buttontext itemToRemove = mNewsList.get(position);
                    mNewsList.remove(itemToRemove);
                    mMyAdapter.notifyItemRemoved(position);
                    ChildNumber.setText("共测评"+(number-1)+"名儿童");
                    number = number-1;
                }, "取消", null);
    }
}