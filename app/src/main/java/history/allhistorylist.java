package history;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.CCLEvaluation.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.NetInteractUtils;
import utils.dataManager;
import utils.dialogUtils;

public class allhistorylist extends AppCompatActivity {
    RecyclerView mRecyclerView;
    MyAdapter mMyAdapter;
    List<Buttontext> mNewsList = new ArrayList<>();
    private TextView ChildNumber;
    private String[] numbers;
    private int count = 0;

    private int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);
        mRecyclerView = findViewById(R.id.recyclerview);
        ChildNumber = findViewById(R.id.childnumber);
        String Uid = getIntent().getStringExtra("Uid");
        try {
            NetInteractUtils.UserInfoCallback userInfoCallback = new NetInteractUtils.UserInfoCallback() {
                @Override
                public void onUserInfoResult(String user) throws Exception {
                    //Toast.makeText(allhistorylist.this, user, Toast.LENGTH_SHORT).show();
                    JSONObject jsonObject = new JSONObject(user);
                    String Username = jsonObject.getString("Username");
                    String ID = jsonObject.getString("ID");
                    dataManager.getInstance().saveData( ID + ".json", jsonObject);
                    Log.d("sssss", ID + ".json");
                    count++;
                    if(count == numbers.length){
                        updateUI();
                    }
                }
            };

            NetInteractUtils.UserIDsCallback userIDsCallback = new NetInteractUtils.UserIDsCallback() {
                @Override
                public void onUserIDsResult(String userIDs) {
                    //Toast.makeText(allhistorylist.this, userIDs, Toast.LENGTH_SHORT).show();
                    String strippedInput = userIDs.replace("[", "").replace("]", "").replace(" ", "");
                    // 拆分字符串以获得数字部分
                    numbers = strippedInput.split(",");
                    for(int i=0;i<numbers.length;++i){
                        NetInteractUtils.getInstance(allhistorylist.this).getUserInfo(numbers[i]);
                    }
                }
            };
            NetInteractUtils.getInstance(this).setUserIDsCallback(userIDsCallback);
            NetInteractUtils.getInstance(this).setUserInfoCallback(userInfoCallback);
            NetInteractUtils.getInstance(this).getUserIDs(Uid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(allhistorylist.this);
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
                intent = new Intent(allhistorylist.this, privatehistorylist.class);
                intent.putExtra("Uid", news.childID);
                startActivity(intent);
                finish();
            });
            holder.mDelete.setVisibility(View.GONE);
//            holder.mDelete.setOnClickListener(v -> {
//                int clickedPosition = holder.getAdapterPosition();
//                if (clickedPosition != RecyclerView.NO_POSITION) {
//                    String Uid = getIntent().getStringExtra("Uid");
//                    showLogoutConfirmationDialog(news.childID ,position);
//                }
//            });
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
            mDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
    private void showLogoutConfirmationDialog(String childID,int position) {
        //Toast.makeText(allhistorylist.this,fname+"__childID:"+childID,Toast.LENGTH_SHORT).show();
        dialogUtils.showDialog(allhistorylist.this, "提示信息", "您确定要删除该测评人的全部测评记录吗？",
                "确认", () -> {
                    try {
                        //clearStudentInfoAudio(fname,position,Uid);
                        String Uid = getIntent().getStringExtra("Uid");
                        NetInteractUtils.getInstance(allhistorylist.this).deleteEvaluations(Uid,childID);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "取消", null);
    }
    private void  clearStudentInfoAudio(String fname, int position, String Uid) throws Exception {
        dataManager.getInstance().deleteData(fname);

        // 使用下划线作为分隔符来分割字符串
        String[] parts = fname.split("_");
        // 检查分割后的数组长度，确保至少有两个部分
        if (parts.length >= 2) {
            // 第一部分是我们想要的"23"
            String part1 = parts[0];
            // 第二部分是我们想要的"100008"
            String part2 = parts[1].split("\\.")[0]; // 如果需要确保去除.json后缀
            NetInteractUtils.getInstance(allhistorylist.this).deleteEvaluation(part2,part1);
        }
        Toast.makeText(allhistorylist.this,"儿童信息已删除",Toast.LENGTH_SHORT).show();
    }
    private void del(String Uid,String id) throws Exception {
        dataManager.getInstance().deleteData(id+"_"+Uid+".json");
    }
    // Update UI method
    private void updateUI() throws Exception {
        if (numbers == null || numbers.length == 0) {
            number = 0;
            ChildNumber.setText("共有" + number + "名测评人");
        } else {
            number = numbers.length;
            // Populate the news list with child evaluation data
            for (int i = number - 1; i >= 0; i--) {
                Buttontext news = new Buttontext();
                JSONObject jsonObject = dataManager.getInstance().loadData(numbers[i]+ ".json");
                String Username = jsonObject.getString("Username");
                String ID = jsonObject.getString("ID");
                news.title = "用户名：" + Username + " ID：" +ID;
                news.childID = ID;
                mNewsList.add(news);
            }
            // Set up the RecyclerView
            mMyAdapter = new MyAdapter();
            mRecyclerView.setAdapter(mMyAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(allhistorylist.this);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            ChildNumber.setText("共有" + number + "名测评人");
        }
    }
    private void updateUIWithNoData() {
        number = 0;
        ChildNumber.setText("共有" + number + "名测评人");
    }

}