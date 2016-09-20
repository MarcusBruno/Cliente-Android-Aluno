package com.ifms.tcc.marcusbruno.sisconfealuno.Views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ifms.tcc.marcusbruno.sisconfealuno.Models.Aluno;
import com.ifms.tcc.marcusbruno.sisconfealuno.Models.FaltasEPresencas;
import com.ifms.tcc.marcusbruno.sisconfealuno.R;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.Routes;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ListaFrequenciaActivity extends AppCompatActivity {
    ServiceHandler sh = new ServiceHandler();
    private boolean CONEXAO;
    private Aluno ALUNO = LoginActivity.ALUNO;
    private ArrayList<FaltasEPresencas> listaFrequencia;
    private String codigoDisciplina;
    private String nomeDisciplina;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_frequencia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        codigoDisciplina = (String) i.getSerializableExtra("codigoDisciplina");
        nomeDisciplina = (String) i.getSerializableExtra("nomeDisciplina");

        new buscarListaFrequenciaAluno().execute();
    }

    public class buscarListaFrequenciaAluno extends AsyncTask<String, Integer, Integer>{

        @Override
        protected Integer doInBackground(String... params) {

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("ra", ALUNO.getRa()));
            pairs.add(new BasicNameValuePair("disciplina", codigoDisciplina));
            try {
                JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlBuscarListaFrequenciaAluno(), ServiceHandler.POST, pairs));
                //Tratamento em caso da conexão falhar
                if (jsonObj != null) {
                    CONEXAO = true;
                    //Tratamento em caso do objeto retornar null;
                    if (jsonObj.getString("status").equalsIgnoreCase("1")) {
                        CONEXAO = true;

                        listaFrequencia = new ArrayList<>();
                        JSONArray jsonArr = jsonObj.getJSONArray("message");
                        for (int i = 0; i < jsonArr.length(); i++) {

                            JSONObject objJson = jsonObj.getJSONArray("message").getJSONObject(i);
                            FaltasEPresencas situacao = new FaltasEPresencas();

                            situacao.setDataTime(objJson.getString("tb_lista_freq_data_hora"));
                            situacao.setPresenca(objJson.getString("tb_lista_freq_presenca"));
                            listaFrequencia.add(situacao);
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
            contrutorTabelaLF();
        }
    }
    void contrutorTabelaLF(){
        String formattedTime=null;
        boolean flag = false;

        for(int i=0;i<listaFrequencia.size();i++){

            // get a reference for the TableLayout
            TableLayout table = (TableLayout)findViewById(R.id.tabelaListaFrequencia);
            TextView labelTitleDisciplina = (TextView) findViewById(R.id.labelTitleDisciplina);
            labelTitleDisciplina.setText(nomeDisciplina);
            // create a new TableRow
            TableRow row = new TableRow(this);

            //Data
            TextView date = new TextView(this);
            date.setPadding(20,20,20,20);
            date.setGravity(10);
            date.setTextColor(Color.BLACK);

            try {
                //Formatter Date
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));//Set timezone
                Date d = formatter.parse(listaFrequencia.get(i).getDataTime());
                formattedTime = output.format(d);
                date.setText(formattedTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Presença
            TextView presenca = new TextView(this);
            presenca.setPadding(20,20,20,20);
            presenca.setGravity(10);
            presenca.setTextColor(Color.BLACK);
            String situacao;
            if(listaFrequencia.get(i).getPresenca().equalsIgnoreCase("1")){
                situacao = "Presente";
            }else {
                situacao = "Ausente";
            }
            presenca.setText(situacao);


            if(flag){
                date.setBackgroundColor(0xFFcac9c9);
                presenca.setBackgroundColor(0xFFD3D3D3);
                flag = false;
            }else {
                date.setBackgroundColor(0xFF898989);
                presenca.setBackgroundColor(0xFF737373);
                flag = true;
            }
            row.addView(date,0);
            row.addView(presenca,1);
            // add the TableRow to the TableLayout
            table.addView(row, new TableLayout.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent i = new Intent(ListaFrequenciaActivity.this, ActivityDisciplinas.class);
        startActivity(i);
    }
}
