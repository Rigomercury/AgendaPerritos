package com.example.agendaperritos.inicio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.agendaperritos.R;

public class Logeo_usuario extends AppCompatActivity {
    EditText etPass;
    Button btnIngresar;

    SharedPreferences sharedPreferences;

    private static final String SHARED_PREF = "mi_pref";
    private static final String KEY_PASSWORD = "6279";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_logeo_usuario);


        InicializarVaiables();

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String S_password = etPass.getText().toString().trim();
                String password_SP = sharedPreferences.getString(KEY_PASSWORD, null);

                if(S_password.equals("")){
                    Toast.makeText(Logeo_usuario.this, "Campo es obligatorio", Toast.LENGTH_SHORT).show();
                } else if (!S_password.equals(KEY_PASSWORD)) {
                    Toast.makeText(Logeo_usuario.this, "La contraseña no es la correcta", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(Logeo_usuario.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });
    }

    private void InicializarVaiables() {
        etPass = findViewById(R.id.etPass);
        btnIngresar = findViewById(R.id.btnIngresar);
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
    }
}