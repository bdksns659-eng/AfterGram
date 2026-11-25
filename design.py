from pathlib import Path

p = Path("app/src/main/java/com/aftergram/app/MainActivity.java")
s = p.read_text()

start = s.index("    void showWelcome()")
end = s.index("    void sendPhoneCode()", start)

replacement = '''
    void showWelcome() {
        screen("AfterGram");
        title("✈");
        title("AfterGram");
        item("Добро пожаловать!",
             "Общайся с друзьями и близкими где угодно.", null);
        button("НАЧАТЬ ОБЩЕНИЕ", () -> showPhoneScreen());
    }

    void showPhoneScreen() {
        screen("Ваш номер телефона");
        item("Выберите страну и введите номер телефона.", "", null);
        countrySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_dropdown_item, countryNames);
        countrySpinner.setAdapter(adapter);
        content.addView(countrySpinner);
        phoneInput = authInput("Номер телефона");
        button("ПРОДОЛЖИТЬ", () -> sendPhoneCode());
    }

    void showCodeScreen() {
        screen("Код подтверждения");
        item("Введите код из SMS.", "", null);
        codeInput = authInput("Код из SMS");
        button("ПРОДОЛЖИТЬ", () -> {
            String code = codeInput.getText().toString().trim();
            if (verificationId == null || code.length() != 6) {
                Toast.makeText(this, "Введите код из 6 цифр", Toast.LENGTH_SHORT).show();
                return;
            }
            PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        });
        button("ИЗМЕНИТЬ НОМЕР", () -> showPhoneScreen());
    }

    void showProfileSetup() {
        screen("Ваше имя");
        item("Как вас зовут?", "Укажите имя для профиля.", null);
        firstNameInput = authInput("Имя");
        lastNameInput = authInput("Фамилия (необязательно)");
        button("ПРОДОЛЖИТЬ", () -> saveProfile());
    }

'''

p.write_text(s[:start] + replacement + s[end:])
print("Экраны регистрации обновлены.")
