package com.example.otomotuzplus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#FBD70E")
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailET = findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordET = findViewById<TextInputEditText>(R.id.passwordEditText)
        val repeatPasswordET = findViewById<TextInputEditText>(R.id.repeatPasswordEditText)
        val registerBtn = findViewById<Button>(R.id.registerButton)
        val loginTV = findViewById<TextView>(R.id.goToLoginText)

        registerBtn.setOnClickListener {
            val email = emailET.text.toString().trim()
            val pass = passwordET.text.toString().trim()
            val repeatPass = repeatPasswordET.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != repeatPass) {
                Toast.makeText(this, "Hasła nie są identyczne!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Hasło musi mieć min. 6 znaków", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Konto utworzone pomyślnie!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Błąd rejestracji: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loginTV.setOnClickListener {
            finish()
        }
    }
}