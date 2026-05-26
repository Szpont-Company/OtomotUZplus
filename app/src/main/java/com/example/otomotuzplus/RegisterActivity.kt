/**
 * @file RegisterActivity.kt
 * @brief Ekran rejestracji nowego konta użytkownika przez Firebase Auth.
 */
package com.example.otomotuzplus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.otomotuzplus.data.PreferenceManager
import com.example.otomotuzplus.ui.models.EnglishStrings
import com.example.otomotuzplus.ui.models.PolishStrings
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity obsługująca tworzenie nowych kont przez Firebase Auth.
 *
 * Sprawdza czy:
 * - Wszystkie trzy pola (e-mail, hasło, powtórz hasło) są niepuste.
 * - Oba pola hasła są zgodne.
 * - Hasło ma co najmniej 6 znaków.
 *
 * Po sukcesie nawiguje bezpośrednio do [MainActivity], czyszcząc stos back.
 * Kliknięcie linku "przejdź do logowania" wywołuje `finish()` aby wrócić do
 * [LoginActivity].
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#FBD70E")
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val lang = PreferenceManager(this).getLanguage()
        val s = if (lang == "Polski") PolishStrings else EnglishStrings

        val emailET = findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordET = findViewById<TextInputEditText>(R.id.passwordEditText)
        val repeatPasswordET = findViewById<TextInputEditText>(R.id.repeatPasswordEditText)
        val registerBtn = findViewById<Button>(R.id.registerButton)
        val loginTV = findViewById<TextView>(R.id.goToLoginText)

        findViewById<TextView>(R.id.titleText).text = s.registerTitle
        findViewById<TextInputLayout>(R.id.emailInputLayout).hint = s.emailHint
        findViewById<TextInputLayout>(R.id.passwordInputLayout).hint = s.passwordHintMinLength
        findViewById<TextInputLayout>(R.id.repeatPasswordInputLayout).hint = s.repeatPasswordHint
        registerBtn.text = s.registerButton
        loginTV.text = s.hasAccountLogin

        registerBtn.setOnClickListener {
            val email = emailET.text.toString().trim()
            val pass = passwordET.text.toString().trim()
            val repeatPass = repeatPasswordET.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(this, s.fillAllFields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != repeatPass) {
                Toast.makeText(this, s.passwordMismatch, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, s.passwordTooShort, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, s.registerSuccess, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, s.registerError.format(task.exception?.message), Toast.LENGTH_LONG).show()
                    }
                }
        }

        loginTV.setOnClickListener {
            finish()
        }
    }
}