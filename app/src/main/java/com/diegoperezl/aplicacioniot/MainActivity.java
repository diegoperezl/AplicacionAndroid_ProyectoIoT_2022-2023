package com.diegoperezl.aplicacioniot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se solicitan las credenciales y se envían a la pantalla de conexión
        Button sendButton = (Button) findViewById(R.id.entrar);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TableActivity.class);
                i.putExtra("usuario", ((TextView)findViewById(R.id.Usuario)).getText().toString());
                i.putExtra("pass", ((TextView)findViewById(R.id.Pass)).getText().toString());
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}