package carlotta.digital.gestionproyectoscarlotta;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import webservices.LoginWS;

public class MainActivity extends Activity {

    ActionBar actionBar;
    ProgressBar circular;
    EditText loginText, passText;
    Button loginBtn;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);
        //Inicializar ActionBar
        initActionBar();
        //Ocultar Spinner al arranque
        circular = (ProgressBar)findViewById(R.id.loginProgress);
        circular.setVisibility(ProgressBar.INVISIBLE);
        //Inicializar resto de items
        loginText = (EditText)findViewById(R.id.loginMail);
        passText = (EditText)findViewById(R.id.loginPass);
        loginBtn = (Button)findViewById(R.id.loginBtn);
        //Inicializar las preferencias básicas
        prefs = getSharedPreferences("prefs",getApplicationContext().MODE_PRIVATE);
        if(prefs.getInt("id",0)!=0){
            Intent i = new Intent(MainActivity.this, Projects.class);
            startActivity(i);
            finish();
        }
        //Establecer el listener del boton
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void initActionBar(){
        actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FE0000")));
    }
    /**
     * Ejecutar la secuencia de login
     *
     */
    public void doLogin(){
        /*Handlers*/
        final Handler loginIncorrect = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.loginIncorrect), Toast.LENGTH_LONG).show();
                circular.setVisibility(ProgressBar.INVISIBLE);
            }
        };
        final Handler loginCorrect = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Intent i = new Intent(MainActivity.this, Projects.class);
                startActivity(i);
                finish();
            }
        };
        /*Handlers*/
        //Comprobar que los campos están llenos
        if((loginText.getText().toString() != "")&&(passText.getText().toString()!="")){
            circular.setVisibility(ProgressBar.VISIBLE);
            //Arranque del thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Instanciar el objeto DAO
                    LoginWS loginDAO = new LoginWS(getResources().getString(R.string.server));
                    //Obtenemos el resultado
                    int result = loginDAO.doLogin(loginText.getText().toString(), passText.getText().toString());
                    //Logica de confirmación o denegación de login
                    if(result!=0){
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("id", result);
                        editor.commit();
                        loginCorrect.sendEmptyMessage(0);
                    }else{
                        loginIncorrect.sendEmptyMessage(0);
                    }
                }
            }).start();
        }else{
            Toast.makeText(getApplicationContext(), "Introduzca Email y contraseña para acceder", Toast.LENGTH_SHORT).show();
        }
    }
}
