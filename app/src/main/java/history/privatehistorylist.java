package history;

import android.content.Intent;
import android.graphics.Typeface;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import utils.dataManager;
import utils.dialogUtils;
import utils.NetInteractUtils;

public class privatehistorylist extends AppCompatActivity {
    RecyclerView mRecyclerView;
    MyAdapter mMyAdapter;
    List<Buttontext> mNewsList = new ArrayList<>();
    private TextView ChildNumber;

    private TextView pw;
    private int[] intArray;

    private int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);
        mRecyclerView = findViewById(R.id.recyclerview);
        ChildNumber = findViewById(R.id.childnumber);
//        pw  = findViewById(R.id.pleasewait);
//
//        Typeface typeface = Typeface.createFromAsset(getAssets(),"font/iconfont.ttf");
//        pw.setTypeface(typeface);
//
//        Netinteractutils.getInstance(this).setListener(new Netinteractutils.UiRefreshListener() {
//            /**
//             * @param isPlaying 是否需要打开等待动画中
//             */
//            @Override
//            public void refreshUI(Boolean isPlaying) {
//
//                if (isPlaying == true){//加载动画
//                    pw.setVisibility(View.VISIBLE);
//                }
//                else {//关闭动画
//                    pw.setVisibility(View.GONE);
//                }
//
//            }
//        });


        String Uid = getIntent().getStringExtra("Uid");

        try {
            if (Uid == null || Uid.isEmpty()) {
                updateUIWithNoData();
                return;
            }

            NetInteractUtils.getInstance(this).setEvaluationsCallback(new NetInteractUtils.EvaluationsCallback() {
                @Override
                public void onEvaluationsResult(String evaluations) {
                    try {
                        JSONArray jsonArray = new JSONArray(evaluations);
                        if (jsonArray.length() == 0) {
                            updateUIWithNoData();
                            return;
                        }
                        intArray = new int[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("ID");
                            intArray[i] = Integer.parseInt(id);
                            dataManager.getInstance().saveData(id + "_" + Uid + ".json", jsonObject);
                            Log.d("sssss", id + "_" + Uid + ".json");
                        }
                        updateUI(Uid);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            NetInteractUtils.getInstance(this).getEvaluations(Uid);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {



        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(privatehistorylist.this);
            View view = inflater.inflate(R.layout.item_list,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Buttontext news = mNewsList.get(position);
            holder.mTitleTv.setText(news.title);
            holder.mTitleTv.setOnClickListener(v -> {
                Intent intent;
                intent = new Intent(privatehistorylist.this, showprivatechildinformation.class);
                intent.putExtra("position", position);
                Log.d("wwwwq", String.valueOf(position));
                intent.putExtra("fName", news.fname);
                String Uid = getIntent().getStringExtra("Uid");
                intent.putExtra("Uid", Uid);
                intent.putExtra("childID", news.childID);
                startActivity(intent);
                finish();
            });
            holder.mDelete.setOnClickListener(v -> {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    String Uid = getIntent().getStringExtra("Uid");
                    showLogoutConfirmationDialog(news.fname, news.childID, position, Uid);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mNewsList.size();
        }
    }

    public static int[] stringToIntArray(String str) {
        if (str == null) {
            return new int[0];
        }
        String trimmed = str.trim();
        if (trimmed.length() <= 2 || "[]".equals(trimmed)) {
            return new int[0];
        }

        // 去除首尾方括号
        trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        if (trimmed.isEmpty()) {
            return new int[0];
        }

        String[] strArr = trimmed.split(",");
        int[] intArray = new int[strArr.length];

        for (int i = 0; i < strArr.length; i++) {
            String part = strArr[i].trim();
            if (part.isEmpty()) {
                return new int[0];
            }
            intArray[i] = Integer.parseInt(part);
        }

        return intArray;
    }

    private void showLogoutConfirmationDialog(String fname,String childID,int position, String Uid) {
        Toast.makeText(privatehistorylist.this,fname+"__childID:"+childID,Toast.LENGTH_SHORT).show();
        dialogUtils.showDialog(privatehistorylist.this, "提示信息", "您确定要删除该学生的全部信息和答题记录吗？",
                "确认", () -> {
                    try {
                        clearStudentInfoAudio(fname,position,Uid);
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
            NetInteractUtils.getInstance(privatehistorylist.this).deleteEvaluation(part2,part1);
        }
        Toast.makeText(privatehistorylist.this,"儿童信息已删除",Toast.LENGTH_SHORT).show();
    }
    private void del(String Uid,String id) throws Exception {
        dataManager.getInstance().deleteData(id+"_"+Uid+".json");
    }
    // Update UI method
    private void updateUI(String Uid) throws Exception {
        if (intArray == null || intArray.length == 0) {
            number = 0;
            ChildNumber.setText("共测评" + number + "名儿童");
        } else {
            number = intArray.length;
            // Populate the news list with child evaluation data
            for (int i = number - 1; i >= 0; i--) {
                Buttontext news = new Buttontext();
                JSONObject child = dataManager.getInstance().loadData(String.valueOf(intArray[i]) + "_" + Uid + ".json");
                Log.d("dashabi", child.toString());
                JSONObject info = child.getJSONObject("info");
                String currentDate = info.optString("testDate", "未提供");
                String name = info.optString("name", "未提供");
                String tester = info.optString("examiner", "未提供");
                news.title = "时间：" + currentDate + " 儿童：" + name + " 测试员：" + tester;
                news.fname = String.valueOf(intArray[i]) + "_" + Uid + ".json";
                news.childID = child.getString("ID");
                mNewsList.add(news);
            }
            // Set up the RecyclerView
            mMyAdapter = new MyAdapter();
            mRecyclerView.setAdapter(mMyAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(privatehistorylist.this);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            ChildNumber.setText("共测评" + number + "名儿童");
        }
    }
    private void updateUIWithNoData() {
        number = 0;
        ChildNumber.setText("共测评" + number + "名儿童");
    }

}
