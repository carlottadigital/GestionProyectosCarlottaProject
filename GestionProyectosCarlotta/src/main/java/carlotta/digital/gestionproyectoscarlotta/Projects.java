package carlotta.digital.gestionproyectoscarlotta;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import adapters.DrawerProyectosAdapter;
import models.Proyecto;
import webservices.ProyectosWS;

public class Projects extends Activity {

    ActionBar actionBar;
    ArrayList <String> proyectos;
    ListView drawerList;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    ArrayList<Proyecto> prj = new ArrayList<Proyecto>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        //Inicializar el ActionBar
        initActionBar();
        //Inicializar el drawerLayout
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList = (ListView)findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer, R.string.projects, R.string.projects){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //Obtener los datos del Webservice
        getProjects();
        //Establecer el listener de la lista de proyectos
        addDrawerListener();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.projects, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*
    * Inicializar el ActionBar
    * */
    public void initActionBar(){
        actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FE0000")));
    }
    /*
    * Este Método adquiere los proyectos y los carga los datos en el navigation drawer
    * */
    public void getProjects(){
        //Declarar el array list como final para poder ser accedido desde una inner class

        final Handler printProjects = new Handler(){
            @Override
            public void handleMessage(Message msg){
                //Obtener los datos, crear un array de string y printar los proyectos en el drawer
                ArrayList<String> prjNames = new ArrayList<String>();
                prjNames.add(getResources().getString(R.string.fav));
                prjNames.add(getResources().getString(R.string.done));
                for(int a=0;a<prj.size();a++){
                    prjNames.add(prj.get(a).getNombre());
                }
                //Establecer el adaptador
                drawerList.setAdapter(new DrawerProyectosAdapter(getApplicationContext(),prjNames));
            }
        };
        final ProyectosWS prjDAO = new ProyectosWS(getResources().getString(R.string.server));
        new Thread(new Runnable() {
            @Override
            public void run() {
                prj = prjDAO.getAllProyectos();
                printProjects.sendEmptyMessage(0);
            }
        }).start();
    }
    /*
    * Este médoto implementa el click listener en el listview
    * */
    public void addDrawerListener(){
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i){
                    case 0:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.fav),Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.done),Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        //Ejecutar el código normal para cada una de las selecciones de la lista

                        break;
                }
            }
        });
        drawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch(i){
                    case 0:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.fav)+" Long",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.done)+" Long",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        //Ejecutar el código normal para cada una de las selecciones de la lista

                        break;
                }
                return false;
            }
        });
    }
}
