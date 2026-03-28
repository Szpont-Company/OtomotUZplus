package com.example.otomotuzplus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {

    // 1. Definiujemy instancję Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 2. Inicjalizujemy Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 3. Znajdujemy elementy widoku
        val emailET = findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordET = findViewById<TextInputEditText>(R.id.passwordEditText)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val registerTV = findViewById<TextView>(R.id.goToRegisterText)

        // 4. Obsługa kliknięcia "Zaloguj"
        loginBtn.setOnClickListener {
            val email = emailET.text.toString().trim()
            val pass = passwordET.text.toString().trim()

            // Prosta walidacja
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- MAGIA FIREBASE: LOGOWANIE ---
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sukces!
                        Toast.makeText(this, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show()
                        otworzGlowneWnetrzeAppki()
                    } else {
                        // Błąd (np. złe hasło, brak usera)
                        Toast.makeText(this, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 5. Obsługa przejścia do rejestracji (na razie pusty Toast)
        registerTV.setOnClickListener {
            // Zamień Toast na to:
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // 6. Kluczowa funkcja: sprawdza czy user jest już zalogowany
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User jest zalogowany -> pomijamy ekran logowania
            otworzGlowneWnetrzeAppki()
        }
    }

    // Pomocnicza funkcja do przenoszenia do MainActivity
    private fun otworzGlowneWnetrzeAppki() {
        val intent = Intent(this, MainActivity::class.java)
        // Flagi, żeby user po kliknięciu "Wstecz" nie wrócił do logowania
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Zamyka LoginActivity
    }
}