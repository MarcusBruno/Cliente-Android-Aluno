package com.ifms.tcc.marcusbruno.sisconfealuno.Views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.DetectaConexao;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.GeoCoordinate;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.Routes;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityDisciplinas extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ProgressDialog dialog;
    ServiceHandler sh = new ServiceHandler();
    private boolean conexaoServidor;
    private Aluno ALUNO = LoginActivity.ALUNO;
    private AlertDialog.Builder builder;
    private JSONObject chamadaAberta;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ListView disciplinasLV;
    protected static ArrayList<Disciplina> DISCIPLINAS;
    private ArrayList<String> disciplinasAdapter;
    private int itemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplinas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        disciplinasLV = (ListView) findViewById(R.id.list_view_lista_disciplinas);
        registerForContextMenu(disciplinasLV);

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.carregando));
        builder = new AlertDialog.Builder(this);
        disciplinasAdapter = new ArrayList<>();

        if (new DetectaConexao(this).existeConexao() && new DetectaConexao(this).localizacaoAtiva()) {
            googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            new getChamadaAberta().execute();
            new getDisciplinasAluno().execute();
        } else {
            builder.setMessage(R.string.ativar_internet_localizacao)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                            startActivity(i);
                        }
                    }).create().show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        itemSelected = acmi.position;

        menu.setHeaderTitle("Opções ");
        //GroupID - ItemId - OrderForId
        menu.add(0, 1, 0, "Visualizar Faltas/Presenças");
        //menu.add(0, 2, 1, "Enviar Notificação aos Alunos");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {

            Intent i = new Intent(ActivityDisciplinas.this, ListaFrequenciaActivity.class);
            i.putExtra("codigoDisciplina", DISCIPLINAS.get(itemSelected).getCodigo().toString());
            i.putExtra("nomeDisciplina", DISCIPLINAS.get(itemSelected).getNome());
            startActivity(i);
            finish();
        } else if (item.getItemId() == 2) {
            Toast.makeText(getApplicationContext(), "Opc 2", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == 3) {
            Toast.makeText(getApplicationContext(), "Opc 3", Toast.LENGTH_LONG).show();
        } else {
            return false;
        }
        return true;
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
                builder.setMessage(R.string.desconectar)
                        .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                                startActivity(i);
                            }
                        }).setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
                return true;
            case R.id.refresh:
                if (new DetectaConexao(this).existeConexao() && new DetectaConexao(this).localizacaoAtiva()) {
                    new getChamadaAberta().execute();
                    new getDisciplinasAluno().execute();
                } else {
                    builder.setMessage(R.string.ativar_internet_localizacao)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                                    startActivity(i);
                                }
                            }).create().show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    protected void onResume() {
        super.onResume();
        if (googleApiClient != null) {
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            }
        }
    }

    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        configuracaoLocalizacao();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        ALUNO.setLatitude(location.getLatitude() + "");
        ALUNO.setLongitude(location.getLongitude() + "");
    }

    public class getDisciplinasAluno extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            if (!new DetectaConexao(ActivityDisciplinas.this).existeConexao() || !new DetectaConexao(ActivityDisciplinas.this).localizacaoAtiva()) {
                builder.setMessage(R.string.ativar_internet_localizacao)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                                startActivity(i);
                            }
                        }).create().show();
            }else{
                disciplinasAdapter.clear();
            }

        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("ra", ALUNO.getRa()));

            try {
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarDisciplinasAluno(), ServiceHandler.POST, pairs));
                //Tratamento em caso da conexão falhar
                if (jsonObj != null) {
                    conexaoServidor = true;
                    //Tratamento em caso do objeto retornar null;
                    if (!jsonObj.equals("")) {
                        conexaoServidor = true;
                        DISCIPLINAS = new ArrayList<>();


                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject c = jsonObj.getJSONObject(i);
                            Disciplina d = new Disciplina(c.getString("codigo"), c.getString("nome"), c.getString("descricao"));
                            DISCIPLINAS.add(d);
                            disciplinasAdapter.add(d.getCodigo() + " : " + d.getNome());
                        }
                    }
                } else {
                    conexaoServidor = false;
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
            if (!new DetectaConexao(ActivityDisciplinas.this).existeConexao() || !new DetectaConexao(ActivityDisciplinas.this).localizacaoAtiva()) {
                builder.setMessage(R.string.ativar_internet_localizacao)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                                startActivity(i);
                            }
                        }).create().show();
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("ra", ALUNO.getRa()));
            try {
                String jsonStr = sh.makeServiceCall(Routes.getUrlBuscarChamadaAberta(), ServiceHandler.POST, pairs);
                //Tratamento em caso da conexão falhar
                if (jsonStr != null) {
                    conexaoServidor = true;
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
                    conexaoServidor = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {

            builder = new AlertDialog.Builder(ActivityDisciplinas.this);
            if (conexaoServidor && status.equalsIgnoreCase("1")) {
                notificarChamadaAberta();

            } else if (conexaoServidor && status.equalsIgnoreCase("0")) {
                builder.setMessage(R.string.nenhuma_chamada_aberta)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            } else if (!conexaoServidor) {
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
            builder.setMessage("O professor(a) " + chamadaAberta.getString("nome_professor") + " abriu a chamada da disciplina de " + chamadaAberta.getString("nome_disciplina") + ". Deseja autenticar a presença?")
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
            if(!new DetectaConexao(ActivityDisciplinas.this).existeConexao() || !new DetectaConexao(ActivityDisciplinas.this).localizacaoAtiva()){
                builder.setMessage(R.string.ativar_internet_localizacao)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(ActivityDisciplinas.this, LoginActivity.class);
                                startActivity(i);
                            }
                        }).create().show();
            }
            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            while (googleApiClient.isConnecting()) {
                System.out.println(R.string.carregando);
            }
            try {

                if (!ALUNO.getLatitude().isEmpty() && !ALUNO.getLongitude().isEmpty() && !chamadaAberta.getString("latitude_professor").isEmpty() && !chamadaAberta.getString("longitude_professor").isEmpty()) {
                    GeoCoordinate aluno = new GeoCoordinate(Double.parseDouble(ALUNO.getLatitude()), Double.parseDouble(ALUNO.getLongitude()));
                    GeoCoordinate professor = new GeoCoordinate(Double.parseDouble(chamadaAberta.getString("latitude_professor")), Double.parseDouble(chamadaAberta.getString("longitude_professor")));
                    distancia = (aluno.distanceInKm(professor) * 1000);

                    if (distancia < 30.0) {
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
                    conexaoServidor = true;
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    if (!jsonObj.equals("")) {
                        //Get status of response;
                        status = jsonObj.getString("status");
                    }
                } else {
                    conexaoServidor = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            dialog.cancel();
            dialog.dismiss();
            Toast.makeText(ActivityDisciplinas.this, distancia + "", Toast.LENGTH_LONG).show();
            builder = new AlertDialog.Builder(ActivityDisciplinas.this);
            if (conexaoServidor && status.equalsIgnoreCase("1")) {

                if (presenca.equalsIgnoreCase("1")) {
                    builder.setMessage(R.string.presenca_sucesso).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).create().show();
                } else {
                    builder.setMessage(R.string.aluno_fora_da_sala)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }).create().show();
                }
                //Encerra Intent
            } else if (conexaoServidor && status.equalsIgnoreCase("0")) {
                builder.setMessage(ALUNO.getNome().split(" ")[0] + " você já autenticou sua presença!").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create().show();
            } else if (conexaoServidor && status.equalsIgnoreCase("-1")) {
                builder.setMessage(ALUNO.getNome().split(" ")[0] + " esta chamada já está encerrada!").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create().show();
            } else if (!conexaoServidor) {
                builder.setMessage(R.string.connection_failure)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            }
        }
    }

    private void configuracaoLocalizacao() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1 * 1000);
        locationRequest.setFastestInterval(1 * 1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, ActivityDisciplinas.this);
    }

}
