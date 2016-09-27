package com.ifms.tcc.marcusbruno.sisconfealuno.Views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ifms.tcc.marcusbruno.sisconfealuno.Models.Aluno;
import com.ifms.tcc.marcusbruno.sisconfealuno.R;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.DetectaConexao;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.MacAddress;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.Routes;
import com.ifms.tcc.marcusbruno.sisconfealuno.Utils.ServiceHandler;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn, cadastrarBtn, recuperarSenhaBtn;
    private EditText raET, passET;
    private boolean conexaoServidor;
    private ProgressDialog dialog;
    private String ra, passAluno;
    private AlertDialog.Builder builder;
    protected static Aluno ALUNO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Dexter.initialize(LoginActivity.this);
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        loginBtn = (Button) findViewById(R.id.button_login);
        cadastrarBtn = (Button) findViewById(R.id.button_cadastrar_me);
        recuperarSenhaBtn = (Button) findViewById(R.id.button_recuperar_senha);
        raET = (EditText) findViewById(R.id.edit_text_login_rp);
        passET = (EditText) findViewById(R.id.edit_text_login_senha);
        builder = new AlertDialog.Builder(LoginActivity.this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ra = raET.getText().toString();
                passAluno = passET.getText().toString();
                if (!ra.equalsIgnoreCase("") && !passAluno.equalsIgnoreCase("")) {
                    new AutenticarLogin().execute();
                } else {
                    builder.setMessage(R.string.message_required_inputs_login)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }).create().show();
                }
            }
        });

        cadastrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(LoginActivity.this, CadastrarActivity.class);
                startActivity(i);
            }
        });

        recuperarSenhaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(LoginActivity.this, RecuperarSenhaActivity.class);
                startActivity(i);
            }
        });
    }

    public class AutenticarLogin extends AsyncTask<String, Integer, Integer> {
        String status;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this, "", "Carregando...", true);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("ra", ra));
            pairs.add(new BasicNameValuePair("senha", passAluno));
            pairs.add(new BasicNameValuePair("mac_address", MacAddress.getValueMacAddres()));

            ServiceHandler sh = new ServiceHandler();
            try {
                String jsonStr = sh.makeServiceCall(Routes.getUrlLoginAluno(), ServiceHandler.POST, pairs);
                //Tratamento em caso da conexão falhar
                if (jsonStr != null) {
                    conexaoServidor = true;
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    //Tratamento em caso do objeto retornar null;
                    if (!jsonObj.equals("")) {
                        //Get status of response;
                        status = jsonObj.getString("status");
                        if (!status.equalsIgnoreCase("0")) {
                            // Getting data teacher of array in position 0.
                            JSONObject c = jsonObj.getJSONArray("message").getJSONObject(0);
                            ALUNO = new Aluno(c.getString("tb_alu_ra"), c.getString("tb_alu_nome"), c.getString("tb_alu_telefone"), c.getString("tb_alu_email"), c.getString("tb_alu_mac_address"));
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
            dialog.dismiss();
            if (conexaoServidor && status.equalsIgnoreCase("1") && ALUNO != null) {
                Intent i = new Intent(LoginActivity.this, ActivityDisciplinas.class);
                startActivity(i);
                finish();
            } else if (conexaoServidor && status.equalsIgnoreCase("0")) {
                builder.setMessage("Login e/ou Senha estão errados. Tente novamente!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            } else if(conexaoServidor && status.equalsIgnoreCase("-1")) {
                builder.setMessage("Você não pode acessar com o celular de outro aluno!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            }else if(!conexaoServidor) {
                builder.setMessage("Falha de comunicação com o servidor. Tente novamente!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            }
        }
    }
}
