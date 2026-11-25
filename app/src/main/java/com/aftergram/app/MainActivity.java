package com.aftergram.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 35, 24, 20);
        root.setBackgroundColor(Color.rgb(15, 17, 25));

        TextView title = new TextView(this);
        title.setText("AfterGram");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title);

        EditText search = new EditText(this);
        search.setHint("Поиск");
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.GRAY);
        root.addView(search);

        TextView empty = new TextView(this);
        empty.setText("Добро пожаловать в AfterGram!\n\nЗдесь пока нет чатов.");
        empty.setTextSize(18);
        empty.setTextColor(Color.LTGRAY);
        empty.setGravity(Gravity.CENTER);
        root.addView(empty, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        setContentView(root);
    }
}
