package com.example.user.sensormonitoring_v20;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.R.layout.simple_spinner_item;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button temp;
    Button bat;
    Spinner spinner;

    // IP (raspberry pi host)
    String IP = "http://tfgalexhost.hopto.org";
    // Web Services
    String GET_NODES = IP + "/get_nodes.php";
    String ACTIVATE = IP + "/activate.php";

    GetNodes connectionthread;
    ActivateNode hilodeconexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Link to visual element on XML
        temp = (Button)findViewById(R.id.temp);
        bat = (Button)findViewById(R.id.bat);
        spinner = (Spinner)findViewById(R.id.spinner);

        //Button listener
        temp.setOnClickListener(this);
        bat.setOnClickListener(this);

        //execute getNodes --> set data on the list (spinner)
        connectionthread = new GetNodes();
        connectionthread.execute(GET_NODES); //param for doInBackground (URL)

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class GetNodes extends AsyncTask<String,Void,String> {

        //Array para guardar los nodos
        ArrayList<String> nodesArray = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String cadena = params[0];
            URL url = null;
            String nodes = "";

            try {
                url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Open connection

                int respuesta = connection.getResponseCode();
                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK){

                    InputStream in = new BufferedInputStream(connection.getInputStream());  // preparo la cadena de entrada

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  // la introduzco en un BufferedReader

                    // El siguiente proceso lo hago porque el JSONOBject necesita un String y tengo
                    // que tranformar el BufferedReader a String. Esto lo hago a traves de un
                    // StringBuilder.

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);        // Paso toda la entrada al StringBuilder
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena

                    //Accedemos al vector de resultados
                    String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo(tag) en el JSON

                    if (resultJSON.equals("1")){      // hay datos a mostrar
                        JSONArray medidasJSON = respuestaJSON.getJSONArray("nodes");   // nodes es el nombre del campo en el JSON
                        for(int i=0;i<medidasJSON.length();i++){
                            nodesArray.add(medidasJSON.getJSONObject(i).getString("node_id"));
                        }
                    }

                    else if (resultJSON.equals("2")){
                        nodesArray.add("No hay nodos disponibles en la red");
                    }

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // Application of the Array to the Spinner
            ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), simple_spinner_item, nodesArray);
            spinner.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view

        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

    }

    @Override
    public void onClick(View v) {

        String active = "Y";
        String activate_link = ACTIVATE;

        switch (v.getId()) {
            case R.id.temp:

                hilodeconexion = new ActivateNode();
                //String link = ACTIVATE; //+ "?node_id=" + spinner.getSelectedItem().toString();
                //String active = "Y";
                hilodeconexion.execute(activate_link, spinner.getSelectedItem().toString(), active);

                Intent myIntent = new Intent(MainActivity.this, ChartActivity.class);
                myIntent.putExtra("spinner_value", spinner.getSelectedItem().toString()); //Optional parameters
                myIntent.putExtra("measure_type", "1");
                MainActivity.this.startActivity(myIntent);

                break;

            case R.id.bat:

                hilodeconexion = new ActivateNode();
                //String link = ACTIVATE; //+ "?node_id=" + spinner.getSelectedItem().toString();
                //String active = "Y";
                hilodeconexion.execute(activate_link, spinner.getSelectedItem().toString(), active);

                Intent miIntent = new Intent(MainActivity.this, ChartActivity.class);
                miIntent.putExtra("spinner_value", spinner.getSelectedItem().toString()); //Optional parameters
                miIntent.putExtra("measure_type", "2");
                MainActivity.this.startActivity(miIntent);

                break;

            default:

                break;

        }
    }

    public class ActivateNode extends AsyncTask <String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String chain = params[0];
            URL url = null;
            String devuelve ="";

            try {
                HttpURLConnection urlConn;

                DataOutputStream printout;
                DataInputStream input;
                url = new URL(chain);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.connect();
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("node_id",params[1]);
                jsonParam.put("is_active", params[2]);
                // Envio los parámetros post.
                OutputStream os = urlConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                int respuesta = urlConn.getResponseCode();


                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        result.append(line);
                        //response+=line;
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON

                    if (resultJSON == "1") {      // hay un alumno que mostrar
                        devuelve = "Nodo activado correctamente";

                    } else if (resultJSON == "2") {
                        devuelve = "Nodo no activado";
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return devuelve;
            //return null;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
