package com.aftergram.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    int bg = Color.rgb(16, 18, 27);
    int panel = Color.rgb(25, 28, 40);
    int blue = Color.rgb(65, 145, 255);
    LinearLayout root, content, nav;
    String currentTab = "Чаты";

    String[] names = {"Alex", "Maria", "AfterGram News", "David"};
    String[] messages = {"Привет! Как дела?", "Отправила фотографию", "Добро пожаловать в AfterGram!", "Увидимся завтра"};
    String[] times = {"18:20", "17:45", "16:30", "15:12"};
    String[] letters = {"A", "M", "N", "D"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().setSoftInputMode(16);
        showHome();
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    void setup(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        TextView header = text(title, 25, Color.WHITE);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(20, 22, 12, 18);
        root.addView(header);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setBackgroundColor(panel);
        nav.setGravity(Gravity.CENTER);
        String[] tabs = {"Чаты", "Контакты", "Звонки", "Настройки"};

        for (String tab : tabs) {
            TextView button = text(tab, 12, tab.equals(currentTab) ? blue : Color.LTGRAY);
            button.setGravity(Gravity.CENTER);
            button.setPadding(2, 17, 2, 17);
            button.setOnClickListener(v -> {
                currentTab = tab;
                showHome();
            });
            nav.addView(button, new LinearLayout.LayoutParams(0, -2, 1));
        }

        root.addView(nav);
        setContentView(root);
    }

    void showHome() {
        setup(currentTab);

        if (currentTab.equals("Чаты")) showChats();
        else if (currentTab.equals("Контакты")) showContacts();
        else if (currentTab.equals("Звонки")) showCalls();
        else showSettings();
    }

    void showChats() {
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Поиск");
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.GRAY);
        search.setPadding(16, 4, 16, 4);
        search.setBackground(shape(panel, 24));

        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 50);
        sp.setMargins(16, 0, 16, 14);
        content.addView(search, sp);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        content.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            addChat(list, names[i], messages[i], times[i], letters[i], () -> openChat(index));
        }

        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                list.removeAllViews();
                for (int i = 0; i < names.length; i++) {
                    if (names[i].toLowerCase().contains(s.toString().toLowerCase())) {
                        final int index = i;
                        addChat(list, names[i], messages[i], times[i], letters[i], () -> openChat(index));
                    }
                }
            }
            public void afterTextChanged(android.text.Editable e) {}
        });
    }

    void addChat(LinearLayout list, String name, String message,
                 String time, String letter, Runnable action) {

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(16, 12, 16, 12);

        TextView avatar = text(letter, 23, Color.WHITE);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(shape(blue, 100));
        row.addView(avatar, new LinearLayout.LayoutParams(52, 52));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(14, 0, 0, 0);

        TextView n = text(name, 17, Color.WHITE);
        n.setTypeface(null, Typeface.BOLD);
        TextView m = text(message, 14, Color.LTGRAY);
        info.addView(n);
        info.addView(m);

        row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text(time, 12, Color.GRAY));

        row.setOnClickListener(v -> action.run());
        list.addView(row);
    }

    void openChat(int index) {
        setup(names[index]);

        TextView back = text("‹  Все чаты", 16, blue);
        back.setPadding(16, 10, 10, 10);
        back.setOnClickListener(v -> showHome());
        content.addView(back);

        ScrollView scroll = new ScrollView(this);
        LinearLayout conversation = new LinearLayout(this);
        conversation.setOrientation(LinearLayout.VERTICAL);
        conversation.setPadding(12, 12, 12, 12);
        scroll.addView(conversation);
        content.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        addMessage(conversation, messages[index], false);

        LinearLayout composer = new LinearLayout(this);
        composer.setPadding(10, 8, 10, 8);
        composer.setGravity(Gravity.CENTER_VERTICAL);

        EditText input = new EditText(this);
        input.setHint("Сообщение...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setBackground(shape(panel, 24));
        composer.addView(input, new LinearLayout.LayoutParams(0, 48, 1));

        TextView send = text("➤", 24, blue);
        send.setGravity(Gravity.CENTER);
        send.setPadding(15, 5, 10, 5);
        composer.addView(send);

        send.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                addMessage(conversation, value, true);
                input.setText("");
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            }
        });

        content.addView(composer);
    }

    void addMessage(LinearLayout list, String value, boolean own) {
        TextView bubble = text(value, 16, Color.WHITE);
        bubble.setPadding(14, 10, 14, 10);
        bubble.setBackground(shape(own ? Color.rgb(43, 100, 180) : panel, 20));

        LinearLayout line = new LinearLayout(this);
        line.setGravity(own ? Gravity.RIGHT : Gravity.LEFT);
        line.setPadding(4, 7, 4, 7);
        line.addView(bubble);
        list.addView(line);
    }

    void showContacts() {
        String[] contacts = {"Alex", "Maria", "David", "AfterGram News"};
        for (String name : contacts) {
            TextView item = text("●   " + name, 18, Color.WHITE);
            item.setPadding(20, 18, 10, 18);
            content.addView(item);
        }
    }

    void showCalls() {
        TextView info = text("Звонки\n\nЗдесь будет история аудио- и видеозвонков.", 18, Color.LTGRAY);
        info.setGravity(Gravity.CENTER);
        content.addView(info, new LinearLayout.LayoutParams(-1, -1));
    }

    void showSettings() {
        String[] options = {"Мой профиль", "Уведомления", "Конфиденциальность", "Оформление", "Язык", "О приложении"};
        for (String option : options) {
            TextView item = text("⚙   " + option, 17, Color.WHITE);
            item.setPadding(20, 18, 10, 18);
            content.addView(item);
        }
    }

    @Override
    public void onBackPressed() {
        showHome();
    }
}
