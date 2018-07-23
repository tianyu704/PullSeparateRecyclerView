package com.example.fir.myapplication;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by hugo on 2018/7/23
 */
public class DetailActivity extends AppCompatActivity {
    CardView cardView;
    TextView nameTextView;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        cardView = findViewById(R.id.card);
        nameTextView = findViewById(R.id.text_name);
        linearLayout = findViewById(R.id.linear);
        int color = getIntent().getIntExtra("color", 0);
        String name = getIntent().getStringExtra("name");
        cardView.setCardBackgroundColor(color);
        nameTextView.setText(name);
        linearLayout.post(new Runnable() {
            @Override
            public void run() {
                animation();
            }
        });

    }

    public void animation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_bottom);
        linearLayout.startAnimation(animation);
    }

    public static void show(AppCompatActivity activity, View view, int color, String name) {
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra("color", color);
        intent.putExtra("name", name);
        activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity, view, "card").toBundle());
    }

}
