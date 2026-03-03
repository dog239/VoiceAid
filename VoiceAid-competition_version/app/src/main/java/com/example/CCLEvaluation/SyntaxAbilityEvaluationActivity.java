package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SyntaxAbilityEvaluationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSyntaxComprehension;
    private Button btnSyntaxExpression;
    private String fName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syntax_ability_evaluation);

        btnSyntaxComprehension = findViewById(R.id.btn_syntax_comprehension);
        btnSyntaxExpression = findViewById(R.id.btn_syntax_expression);

        btnSyntaxComprehension.setOnClickListener(this);
        btnSyntaxExpression.setOnClickListener(this);

        fName = getIntent().getStringExtra("fName");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_syntax_comprehension) {
            // 启动句法理解组选择
            Intent intent = new Intent(this, SyntaxComprehensionGroupSelectActivity.class);
            intent.putExtra("fName", fName);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_syntax_expression) {
            // 启动句法表达组选择
            Intent intent = new Intent(this, SyntaxExpressionGroupSelectActivity.class);
            intent.putExtra("fName", fName);
            startActivity(intent);

        }
    }
}
