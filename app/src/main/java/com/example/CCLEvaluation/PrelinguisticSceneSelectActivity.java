package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PrelinguisticSceneSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSceneA, btnSceneB;
    private String uid;
    private String childUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prelinguistic_scene_select);

        btnSceneA = findViewById(R.id.btn_scene_a);
        btnSceneB = findViewById(R.id.btn_scene_b);

        btnSceneA.setOnClickListener(this);
        btnSceneB.setOnClickListener(this);

        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
    }

    @Override
    public void onClick(View v) {
        String scene = "A";
        if (v.getId() == R.id.btn_scene_b) {
            scene = "B";
        }

        Intent intent = new Intent(this, testactivity.class);
        intent.putExtra("format", "11");
        intent.putExtra("moduleKey", "PL");
        intent.putExtra("scene", scene);
        intent.putExtra("fName", getIntent().getStringExtra("fName"));
        intent.putExtra("Uid", uid);
        intent.putExtra("childID", childUser);
        startActivity(intent);
        finish();
    }
}
