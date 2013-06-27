package carlotta.digital.gestionproyectoscarlotta;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import adapters.DrawerProyectosAdapter;
import fragments.ListProyectos;
import models.Proyecto;
import models.Usuario;
import models.UsuariosProyecto;
import sqlite.DBManager;
import webservices.ProyectosWS;
import webservices.UsuariosProjWS;
import webservices.UsuariosWS;

public class Projects extends Activity {

    ActionBar actionBar;
    ArrayList <String> proyectos;
    ListView drawerList;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    ArrayList<Proyecto> prj = new ArrayList<Proyecto>();
    ArrayList<Usuario> users = new ArrayList<Usuario>();
    ArrayList<UsuariosProyecto> userProj = new ArrayList<UsuariosProyecto>();
    DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_projects);

        setProgressBarIndeterminateVisibility(true);
        //Inicializar el ActionBar
        initActionBar();
        //Inicializar el drawerLayout
        dbManager = new DBManager(getApplicationContext(),"database", null, 1);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList = (ListView)findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer, R.string.projects, R.string.projects){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(getResources().getString(R.string.projects));
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionBar.setTitle(getResources().getString(R.string.projects));
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
        actionBar.setTitle(getResources().getString(R.string.projects));
    }
    /*
    * Este Método adquiere los proyectos y los carga los datos en el navigation drawer
    * */
    public void getProjects(){
        //Declarar el array list como final para poder ser accedido desde una inner class

        final Handler finishLoadProgress = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setProgressBarIndeterminateVisibility(false);
            }
        };

        final Handler printProjects = new Handler(){
            @Override
            public void handleMessage(Message msg){
                //Obtener los datos, crear un array de string y printar los proyectos en el drawer
                ArrayList<String> prjNames = new ArrayList<String>();
                prjNames.add(getResources().getString(R.string.fav));
                prjNames.add(getResources().getString(R.string.done));
                //Abrir el acceso a la base de datos y cargar los datos desde la db
                SQLiteDatabase dbRead = dbManager.getReadableDatabase();
                //Abrir el acceso a la base de datos y cargar los datos desde la db END//
                Cursor datos = dbRead.rawQuery("SELECT * FROM PROYECTOS", null);
                if(datos.moveToFirst()){
                    prjNames.add(datos.getString(1));
                    while(datos.moveToNext()){
                        prjNames.add(datos.getString(1));
                    }
                }
                //Establecer el adaptador
                drawerList.setAdapter(new DrawerProyectosAdapter(getApplicationContext(),prjNames));
            }
        };
        /*
        * Printar los datos antes de que se actualicen los webservices (EN CASO DE NO EXISTA CONEXION
        * */
        printProjects.sendEmptyMessage(0);
        /*
        * Zona de descarga de los datos
        * */
        final ProyectosWS prjDAO = new ProyectosWS(getResources().getString(R.string.server));
        final UsuariosWS usersDAO = new UsuariosWS(getResources().getString(R.string.server));
        final UsuariosProjWS userProjDAO = new UsuariosProjWS(getResources().getString(R.string.server));
        new Thread(new Runnable() {
            @Override
            public void run() {
                prj = prjDAO.getAllProyectos();
                SQLiteDatabase db = dbManager.getWritableDatabase();
                if(prj!=null){
                //Grabar los proyectos a la db de PROYECTOS
                db.execSQL("DELETE FROM PROYECTOS WHERE 1");
                    for(int a=0;a<prj.size();a++){
                        //Grabar cada item a la db
                        try{
                            db.execSQL("INSERT INTO PROYECTOS (id, nombre, descripcion, owner) VALUES ("+prj.get(a).getId()+", '"+prj.get(a).getNombre()+"','"+prj.get(a).getDescripcion()+"',"+prj.get(a).getOwner().getId()+")");
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                //Grabar los proyectos a la db DE PROYECTOS END//
                //Grabar los USUARIOS a la db
                users = usersDAO.getAllUsuarios();
                if(users !=null){
                db.execSQL("DELETE FROM USUARIOS WHERE 1");
                for(int a=0;a<users.size();a++){
                    db.execSQL("INSERT INTO USUARIOS (id, nombre, apellidos, telefono, mail) VALUES ("+users.get(a).getId()+",'"+users.get(a).getNombre()+"','"+users.get(a).getApellidos()+"',"+users.get(a).getTelefono()+",'"+users.get(a).getMail()+"')");

                }
                }
                //Grabar los USUARIOS a la db END//
                //Grabar los USUARIOS ASIGNADOS A PROYECTOS en la db/
                userProj = userProjDAO.getAllUsuarios();
                if(userProj != null){
                db.execSQL("DELETE FROM USER_PROJ WHERE 1");
                for(int a=0;a<userProj.size();a++){
                    db.execSQL("INSERT INTO USER_PROJ (id, id_usuario, id_proyecto) VALUES ("+userProj.get(a).getId()+","+userProj.get(a).getUsuario()+","+userProj.get(a).getProyecto()+")");
                }
                }
                //Grabar los USUARIOS ASIGNADOS A PROYECTOS en la db END //
                db.close();
                printProjects.sendEmptyMessage(0);
                finishLoadProgress.sendEmptyMessage(0);
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
                        Fragment fragment = new ListProyectos();
                        Bundle args = new Bundle();
                        fragment.setArguments(args);
                        //Cambiar el fragment actual por el nuevo
                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction().replace(R.id.content, fragment, "vistaProyectos").commit();
                        //Cerrar el drawer
                        drawer.closeDrawer(drawerList);
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
