package com.aftergram.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    int bg = Color.rgb(15, 19, 29);
    int panel = Color.rgb(27, 34, 49);
    int blue = Color.rgb(65, 145, 255);
    int white = Color.WHITE;
    LinearLayout root, content, navigation;
    String currentTab = "Чаты";

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String verificationId;
    EditText phoneInput, codeInput, firstNameInput, lastNameInput;
    Spinner countrySpinner;

    String[] countryNames = {
        "Украина (+380)", "Польша (+48)", "Германия (+49)",
        "США (+1)", "Великобритания (+44)"
    };
    String[] countryCodes = {"+380", "+48", "+49", "+1", "+44"};

    String[] tabs = {
        "Чаты", "Контакты", "Звонки", "Истории",
        "Подарки", "Звёзды", "Профиль", "Настройки"
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        if (auth.getCurrentUser() == null) {
            showWelcome();
        } else {
            openProfileOrApp();
        }
    }

    void screen(String heading) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        root.setBackgroundColor(bg);

        TextView h = text(heading, 26, white);
        h.setTypeface(null, Typeface.BOLD);
        root.addView(h);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    EditText authInput(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setHintTextColor(Color.GRAY);
        e.setTextColor(white);
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        e.setBackground(shape(panel, 14));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(8), 0, dp(8));
        content.addView(e, p);
        return e;
    }

    void showWelcome() {
        screen("AfterGram");
        title("Добро пожаловать!");
        item("Общайся без границ",
             "Сообщения, звонки, истории и подарки в одном приложении.", null);
        button("НАЧАТЬ ОБЩЕНИЕ", () -> showPhoneScreen());
    }

    void showPhoneScreen() {
        screen("Ваш номер телефона");
        item("Введите номер", "Мы отправим SMS с кодом подтверждения.", null);

        countrySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, countryNames
        );
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );
        countrySpinner.setAdapter(adapter);
        content.addView(countrySpinner);

        phoneInput = authInput("Номер телефона");
        phoneInput.setInputType(3);

        button("ПРОДОЛЖИТЬ", () -> sendPhoneCode());
    }

    void sendPhoneCode() {
        String digits = phoneInput.getText().toString()
            .replaceAll("[^0-9]", "");

        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        if (digits.length() < 7) {
            Toast.makeText(this, "Проверь номер телефона",
                Toast.LENGTH_LONG).show();
            return;
        }

        String phone = countryCodes[countrySpinner.getSelectedItemPosition()]
            + digits;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(MainActivity.this,
                        "Ошибка SMS: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String id,
                    PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = id;
                    showCodeScreen();
                }
            }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        Toast.makeText(this, "Отправляем SMS...", Toast.LENGTH_SHORT).show();
    }

    void showCodeScreen() {
        screen("Подтверждение");
        item("Введите код из SMS",
             "Мы отправили код на ваш номер телефона.", null);

        codeInput = authInput("Код подтверждения");
        codeInput.setInputType(2);

        button("ПОДТВЕРДИТЬ", () -> {
            String code = codeInput.getText().toString().trim();
            if (verificationId == null || code.length() != 6) {
                Toast.makeText(this, "Введите шестизначный код",
                    Toast.LENGTH_SHORT).show();
                return;
            }
            PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        });

        button("Изменить номер", () -> showPhoneScreen());
    }

    void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    openProfileOrApp();
                } else {
                    Toast.makeText(this, "Не удалось войти: " +
                        (task.getException() == null ? "ошибка" :
                        task.getException().getMessage()),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    void openProfileOrApp() {
        if (auth.getCurrentUser() == null) {
            showWelcome();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    showApp();
                } else {
                    showProfileSetup();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Ошибка загрузки профиля: " +
                    e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    void showProfileSetup() {
        screen("Ваш профиль");
        item("Как вас зовут?",
             "Укажите имя, которое увидят другие пользователи.", null);

        firstNameInput = authInput("Имя");
        lastNameInput = authInput("Фамилия");

        button("СОЗДАТЬ ПРОФИЛЬ", () -> saveProfile());
    }

    void saveProfile() {
        String first = firstNameInput.getText().toString().trim();
        String last = lastNameInput.getText().toString().trim();

        if (first.isEmpty()) {
            Toast.makeText(this, "Введите имя",
                Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("phoneNumber", auth.getCurrentUser().getPhoneNumber());
        data.put("firstName", first);
        data.put("lastName", last);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid).set(data)
            .addOnSuccessListener(v -> {
                Toast.makeText(this, "Профиль создан!",
                    Toast.LENGTH_SHORT).show();
                showApp();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Ошибка сохранения: " +
                    e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    int dp(int n) {
        return (int)(n * getResources().getDisplayMetrics().density);
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        return t;
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    void showApp() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        TextView header = text("AfterGram", 25, white);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(dp(18), dp(18), dp(12), dp(12));
        root.addView(header);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        navigation = new LinearLayout(this);
        navigation.setPadding(dp(8), 0, dp(8), dp(8));

        for (String tab : tabs) {
            TextView item = text(tab, 14,
                currentTab.equals(tab) ? blue : Color.LTGRAY);
            item.setBackground(shape(panel, 20));
            LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-2, -2);
            p.setMargins(dp(4), dp(4), dp(4), dp(4));
            navigation.addView(item, p);
            item.setOnClickListener(v -> showTab(tab));
        }

        scroll.addView(navigation);
        root.addView(scroll);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(12));

        ScrollView body = new ScrollView(this);
        body.addView(content);
        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        showTab(currentTab);
    }

    void showTab(String tab) {
        currentTab = tab;
        content.removeAllViews();

        switch (tab) {
            case "Чаты": chats(); break;
            case "Контакты": contacts(); break;
            case "Звонки": calls(); break;
            case "Истории": stories(); break;
            case "Подарки": gifts(); break;
            case "Звёзды": stars(); break;
            case "Профиль": profile(); break;
            case "Настройки": settings(); break;
        }
    }

    void title(String value) {
        TextView t = text(value, 22, white);
        t.setTypeface(null, Typeface.BOLD);
        content.addView(t);
    }

    void item(String name, String description, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(shape(panel, 16));
        card.setPadding(dp(8), dp(5), dp(8), dp(5));

        TextView a = text(name, 17, white);
        a.setTypeface(null, Typeface.BOLD);
        TextView b = text(description, 13, Color.LTGRAY);

        card.addView(a);
        card.addView(b);

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(5), 0, dp(5));
        content.addView(card, p);

        if (action != null) {
            card.setOnClickListener(v -> action.run());
        }
    }

    void button(String label, Runnable action) {
        TextView b = text(label, 15, white);
        b.setGravity(Gravity.CENTER);
        b.setBackground(shape(blue, 14));
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(7), 0, dp(7));
        content.addView(b, p);
        b.setOnClickListener(v -> action.run());
    }

    void chats() {
        title("Сообщения");

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Поиск сообщений и людей");
        search.setHintTextColor(Color.GRAY);
        search.setTextColor(white);
        search.setBackground(shape(panel, 14));
        content.addView(search);

        item("Alex", "Привет! Как дела?", null);
        item("AfterGram News", "Новости приложения", null);
        item("Друзья", "Последние сообщения группы", null);
        item("Избранное", "Сохранённые сообщения", null);
        item("Рабочая группа", "Добро пожаловать!", null);

        button("+ Новый чат", () -> contacts());
        button("+ Создать группу", () ->
            item("Новая группа", "Создание группы будет подключено позже", null));
        button("+ Создать канал", () ->
            item("Новый канал", "Создание канала будет подключено позже", null));
    }

    void contacts() {
        title("Контакты");
        item("Поиск пользователей", "Найти человека по имени или ID", null);
        item("Alex", "В сети", null);
        item("Daniel", "Был недавно", null);
        item("Max", "Был недавно", null);
        button("+ Добавить контакт", () ->
            item("Добавление контакта", "Поиск и добавление будут подключены к серверу", null));
    }

    void calls() {
        title("Звонки");
        button("📞 Новый звонок", () ->
            item("Звонки", "Для настоящих звонков потребуется серверная связь", null));
        item("Alex", "Входящий звонок", null);
        item("Daniel", "Исходящий звонок", null);
        item("Max", "История вызовов", null);
    }

    void stories() {
        title("Истории");
        button("+ Добавить историю", () ->
            item("Новая история", "Публикация фото и видео будет подключена позже", null));
        item("Мои истории", "Здесь будут твои публикации", null);
        item("Истории друзей", "Здесь будут истории контактов", null);
    }

    void gifts() {
        title("Подарки 🎁");
        item("🎁 Подарок", "Виртуальный подарок", null);
        item("🌹 Роза", "Коллекция подарков", null);
        item("⭐ Звёздный подарок", "Подарок за звёзды", null);
        item("💎 Редкий подарок", "Коллекционный предмет", null);
        button("Моя коллекция", () ->
            item("Коллекция", "Здесь будут полученные подарки", null));
        button("Отправить подарок", () ->
            item("Отправка подарка", "Выбор получателя будет подключён позже", null));
    }

    void stars() {
        title("Звёзды ⭐");
        item("⭐ 0", "Демонстрационный баланс AfterGram Stars", null);
        button("Пополнить баланс", () ->
            item("Пополнение", "Реальные платежи пока не подключены", null));
        button("История операций", () ->
            item("История", "Здесь будут операции со звёздами", null));
        button("Отправить звёзды", () ->
            item("Перевод", "Переводы будут доступны после подключения сервера", null));
    }

    void profile() {
        title("Мой профиль");
        item("👤 Пользователь AfterGram", "ID: ещё не назначен", null);
        item("Имя", "Не указано", null);
        item("О себе", "Добавь описание профиля", null);
        button("Изменить профиль", () ->
            item("Редактирование", "Редактирование профиля будет добавлено позже", null));
    }

    void settings() {
        title("Настройки");
        item("Уведомления", "Звуки и сообщения", null);
        item("Приватность", "Кто может видеть твой профиль", null);
        item("Безопасность", "Защита аккаунта", null);
        item("Оформление", "Тема и внешний вид", null);
        item("Язык", "Русский", null);
        item("AfterGram", "Версия 1.0", null);
    }
}
