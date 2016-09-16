package com.ifms.tcc.marcusbruno.sisconfealuno.Views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ifms.tcc.marcusbruno.sisconfealuno.Models.Aluno;
import com.ifms.tcc.marcusbruno.sisconfealuno.Models.Disciplina;
import com.ifms.tcc.marcusbruno.sisconfealuno.R;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.GeoCoordinate;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.MacAddress;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.Routes;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityDisciplinas extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ProgressDialog dialog;
    ServiceHandler sh = new ServiceHandler();
    private boolean CONEXAO;
    private Aluno ALUNO = LoginActivity.ALUNO;
    private AlertDialog.Builder builder;
    private JSONObject chamadaAberta;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest loc;
    private ListView disciplinasLV;
    private ArrayList<Disciplina> disciplinas;
    private ArrayList<String> disciplinasAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplinas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        disciplinasLV = (ListView) findViewById(R.id.list_view_lista_disciplinas);
        registerForContextMenu(disciplinasLV);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Carregando...");

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        builder = new AlertDialog.Builder(ActivityDisciplinas.this);

        new getChamadaAberta().execute();
        new getDisciplinasAluno().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disciplinas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        builder = new AlertDialog.Builder(ActivityDisciplinas.this);
        switch (item.getItemId()) {
            case R.id.logout:
                builder.setMessage("Você tem certeza que deseja se desconectar?")
                        .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                                startActivity(i);
                            }
                        }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
                return true;
            case R.id.refresh:
                new getChamadaAberta().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        configuracoes();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { mGoogleApiClient.connect(); }

    @Override
    public void onLocationChanged(Location location) {
        ALUNO.setLatitude(location.getLatitude() + "");
        ALUNO.setLongitude(location.getLongitude() + "");
    }

    public class getDisciplinasAluno extends AsyncTask<String, Integer, Integer> {
        private String status = "";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("ra", ALUNO.getRa()));

            try {
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarDisciplinasAluno(), ServiceHandler.POST, pairs));
                //Tratamento em caso da conexão falhar
                if (jsonObj != null) {
                    CONEXAO = true;
                    //Tratamento em caso do objeto retornar null;
                    if (!jsonObj.equals("")) {
                        CONEXAO = true;
                        disciplinas = new ArrayList<>();
                        disciplinasAdapter = new ArrayList<>();

                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject c = jsonObj.getJSONObject(i);
                            Disciplina d = new Disciplina(c.getString("codigo"), c.getString("nome"), c.getString("descricao"));
                            disciplinas.add(d);
                            disciplinasAdapter.add(d.getCodigo() + " : " + d.getNome());
                        }
                    }
                } else {
                    CONEXAO = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityDisciplinas.this, android.R.layout.simple_expandable_list_item_1, disciplinasAdapter);
            disciplinasLV.setAdapter(adapter);

        }
    }

    public class getChamadaAberta extends AsyncTask<String, Integer, Integer> {
        private String status = "";

        @Override
        protected void onPreExecute() {
            chamadaAberta = null;
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("ra", ALUNO.getRa()));
            try {
                String jsonStr = sh.makeServiceCall(Routes.getUrlBuscarChamadaAberta(), ServiceHandler.POST, pairs);
                //Tratamento em caso da conexão falhar
                if (jsonStr != null) {
                    CONEXAO = true;
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    //Tratamento em caso do objeto retornar null;
                    if (!jsonObj.equals("")) {
                        status = jsonObj.getString("status");
                        if (!status.equalsIgnoreCase("0")) {
                            // Getting data teacher of array in position 0.
                            chamadaAberta = jsonObj.getJSONArray("message").getJSONObject(0);
                        }
                    }
                } else {
                    CONEXAO = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {

            builder = new AlertDialog.Builder(ActivityDisciplinas.this);
            if (CONEXAO && status.equalsIgnoreCase("1")) {
                notificarChamadaAberta();

            } else if (CONEXAO && status.equalsIgnoreCase("0")) {
                builder.setMessage("Não existe chamada aberta até o momento!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            } else if (!CONEXAO) {
                builder.setMessage(R.string.connection_failure)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            }
        }
    }

    private void notificarChamadaAberta() {
        builder = new AlertDialog.Builder(this);
        try {
            builder.setMessage("O professor(a) " + chamadaAberta.getString("nome_professor") + "abriu a chamada da disciplina de " + chamadaAberta.getString("nome_disciplina") + ". Deseja autenticar a presença?")
                    .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new autenticarPresença().execute();

                        }
                    }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create().show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class autenticarPresença extends AsyncTask<String, Integer, Integer> {
        private String status = "";
        String presenca = "0";
        double distancia;

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            while (mGoogleApiClient.isConnecting()) {
                System.out.println("Conectando...");
            }
            try {

                if(!ALUNO.getLatitude().isEmpty() && !ALUNO.getLongitude().isEmpty() && !chamadaAberta.getString("latitude_professor").isEmpty() && !chamadaAberta.getString("longitude_professor").isEmpty()){
                    GeoCoordinate aluno = new GeoCoordinate(Double.parseDouble(ALUNO.getLatitude()),Double.parseDouble(ALUNO.getLongitude()));
                    GeoCoordinate professor = new GeoCoordinate(Double.parseDouble(chamadaAberta.getString("latitude_professor")), Double.parseDouble(chamadaAberta.getString("longitude_professor")));
                    distancia = (aluno.distanceInKm(professor)*1000);

                    if(distancia < 15.0){

                        presenca = "1";
                    }
                }

                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("tb_lista_freq_codigo_ra", ALUNO.getRa()));
                param.add(new BasicNameValuePair("tb_lista_freq_codigo_rp", chamadaAberta.getString("tb_prof_rp")));
                param.add(new BasicNameValuePair("tb_lista_freq_id_diario", chamadaAberta.getString("id_diario")));
                param.add(new BasicNameValuePair("tb_lista_freq_codigo_disciplina", chamadaAberta.getString("codigo_disciplina")));
                param.add(new BasicNameValuePair("tb_lista_freq_latitude_aluno", ALUNO.getLatitude()));
                param.add(new BasicNameValuePair("tb_lista_freq_longitude_aluno", ALUNO.getLongitude()));
                param.add(new BasicNameValuePair("tb_lista_freq_presenca", presenca));
                String jsonStr = sh.makeServiceCall(Routes.getUrlAutenticarPresença(), ServiceHandler.POST, param);

                if (jsonStr != null) {
                    CONEXAO = true;
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    if (!jsonObj.equals("")) {
                        //Get status of response;
                        status = jsonObj.getString("status");
                    }
                } else {
                    CONEXAO = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, ActivityDisciplinas.this);
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            dialog.cancel();
            dialog.dismiss();
            Toast.makeText(ActivityDisciplinas.this, distancia + "", Toast.LENGTH_LONG).show();
            builder = new AlertDialog.Builder(ActivityDisciplinas.this);
            if (CONEXAO && status.equalsIgnoreCase("1")) {

                if (presenca.equalsIgnoreCase("1")) {
                    builder.setMessage("Presença autenticada com sucesso ! ").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).create().show();
                } else {
                    builder.setMessage("Você não está em sala de aula! Sua presença não foi autenticada.")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }).create().show();
                }
                //Encerra Intent
            } else if (CONEXAO && status.equalsIgnoreCase("0")) {
                builder.setMessage(ALUNO.getNome().split(" ")[0] + " você já autenticou sua presença!").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create().show();
            } else if (CONEXAO && status.equalsIgnoreCase("-1")) {
                builder.setMessage(ALUNO.getNome().split(" ")[0] + " esta chamada já está encerrada!").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create().show();
            } else if (!CONEXAO) {
                builder.setMessage(R.string.connection_failure)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            }
        }
    }

    void configuracoes() {

        loc = LocationRequest.create();
        loc.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        loc.setInterval(5 * 1000);
        loc.setFastestInterval(1 * 1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, loc, ActivityDisciplinas.this);

    }
}
