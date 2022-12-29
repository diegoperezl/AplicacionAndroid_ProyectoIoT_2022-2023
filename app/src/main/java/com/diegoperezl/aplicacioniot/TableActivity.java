package com.diegoperezl.aplicacioniot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class TableActivity extends AppCompatActivity {

    String token = null;
    Thread update;

    //IDs de los vagones
    final String[] vagones = new String[]{"c9a50c70-6984-11ed-8d2b-073de4e14907",
                                          "10a5c4a0-6c0c-11ed-8d2b-073de4e14907",
                                          "a7b640e0-6c0c-11ed-8d2b-073de4e14907"};

    TextView[] personasVagonText;

    TextView[] maxPersonasText;

    TableRow[] filas;

    //Al crear el activity se crea la conexión con la plataforma Thingsboard para obtener el token de sesión
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        Intent intent = getIntent();
        String usuario = intent.getStringExtra("usuario");
        String pass = intent.getStringExtra("pass");

        Thread hilo = new Thread(new Runnable() {
            public void run() {
                try {
                    URL uri = new URL("https://srv-iot.diatel.upm.es/api/auth/login");
                    URLConnection conn = uri.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");

                    String str = "{\"username\":\"" + usuario + "\",\"password\":\"" + pass + "\"}";
                    byte[] outputInBytes = str.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();

                    os.write(outputInBytes);
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    String result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

                    JSONObject jObject = new JSONObject(result);


                    token = jObject.getString("token");

                    personasVagonText = new TextView[]{findViewById(R.id.personasVagon1),
                                                        findViewById(R.id.personasVagon2),
                                                        findViewById(R.id.personasVagon3)};

                    maxPersonasText = new TextView[]{findViewById(R.id.maxVagon1),
                                                        findViewById(R.id.maxVagon2),
                                                        findViewById(R.id.maxVagon3)};

                    filas = new TableRow[]{findViewById(R.id.fila1),
                                            findViewById(R.id.fila2),
                                            findViewById(R.id.fila3)};

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        hilo.start();
    }

    //Al iniciar la aplicación se inicia el hilo que actualizará la información de los vagones
    @Override
    protected void onStart() {
        super.onStart();
        updateData();
    }

    //Hilo que actualiza la información del aforo de los vagones cada 2 segundos
    private void updateData(){
        update = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    for (int i = 0; i<3; i++) {
                        try {
                            //Se hace la solicitud de información mediante la API
                            URL uri = new URL("https://srv-iot.diatel.upm.es/api/plugins/telemetry/ASSET/" + vagones[i] + "/values/attributes?keys=maxPersonas,personasVagon");
                            URLConnection conn = uri.openConnection();
                            conn.addRequestProperty("X-Authorization", "Bearer " + token);
                            conn.setRequestProperty("Content-Type", "application/json");


                            InputStream is = new BufferedInputStream(conn.getInputStream());
                            String result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

                            JSONArray jObject = new JSONArray(result);

                            //Se obtienen las personas y el máximo de personas del vagón
                            String maxPersonas = ((JSONObject) jObject.get(0)).getString("value");
                            String personasVagon = ((JSONObject) jObject.get(1)).getString("value");
                            System.out.println("Personas: " + personasVagon + " - Max Personas: " + maxPersonas);

                            personasVagonText[i].setText(personasVagon);
                            maxPersonasText[i].setText(maxPersonas);

                            //Se muestra el color del registro en función de si se ha excedido, o no, el límite
                            if(Integer.parseInt(personasVagon)>Integer.parseInt(maxPersonas)){
                                int finalI = i;
                                TableActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        filas[finalI].setBackgroundResource(R.color.red);
                                    }
                                });
                            }else{
                                int finalI = i;
                                TableActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        filas[finalI].setBackgroundResource(R.color.green);
                                    }
                                });
                            }

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        update.start();
    }
}