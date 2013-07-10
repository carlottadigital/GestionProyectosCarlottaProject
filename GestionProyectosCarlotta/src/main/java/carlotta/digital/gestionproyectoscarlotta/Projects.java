package carlotta.digital.gestionproyectoscarlotta;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

import adapters.DrawerProyectosAdapter;
import adapters.UserListAdapter;
import fragments.ListProyectos;
import models.Proyecto;
import models.Tareas;
import models.Usuario;
import models.UsuariosProyecto;
import sqlite.DBManager;
import webservices.ProyectosWS;
import webservices.TareasWS;
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
    ArrayList<Tareas> tareas = new ArrayList<Tareas>();
    DBManager dbManager;
    Menu menu;
    int selectedItem, userID;
    boolean editando = false;
    SharedPreferences prefs;
    //Booleano que indica si se está sincronizando
    boolean sincronizando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_projects);
        //Inicializar el ActionBar
        initActionBar();
        prefs = getSharedPreferences("prefs",getApplicationContext().MODE_PRIVATE);
        userID = prefs.getInt("id",0);
        //Inicializar el drawerLayout
        dbManager = new DBManager(getApplicationContext(),"database", null, 1);
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
        //Cargar la vista de inicio
        showMainScreen();
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
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()){
            case R.id.sincro:
                getProjects();
                break;
            case R.id.add_prj:
                    addProject();
                break;
            case R.id.usr_prj:
                    gestCompanyUsers();
                break;
            case R.id.logOff:
                //Hacer LogOff y borrar las bases de datos
                final ProgressDialog pd = ProgressDialog.show(this, "Salida", "Borrando datos", true, false);
                pd.show();
                dbManager.purge(dbManager.getWritableDatabase(), prefs);
                Intent goToLogOff = new Intent(Projects.this, MainActivity.class);
                startActivity(goToLogOff);
                Toast.makeText(getApplicationContext(), "Borrado completado, vuelva a iniciar sesion", Toast.LENGTH_LONG).show();
                pd.dismiss();
                finish();
                break;
            case R.id.about:

                break;
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
        getOverflowMenu();
    }
    /*
    * Este Método adquiere los proyectos y los carga los datos en el navigation drawer
    * */
    public void getProjects(){
        if(!sincronizando){
            //Desactiva la sincronización para que no pueda ser iniciada de nuevo a mitad de progreso (excepción de punteros ya abiertos)
            sincronizando = true;
        //Declarar el array list como final para poder ser accedido desde una inner class
        final Handler startLoadProgress = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setProgressBarIndeterminateVisibility(true);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.sincro), Toast.LENGTH_SHORT).show();
            }
        };
        startLoadProgress.sendEmptyMessage(0);
        final Handler finishLoadProgress = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setProgressBarIndeterminateVisibility(false);
                //Recargar la vista tras la sincronización
                ListProyectos detalle = (ListProyectos) getFragmentManager().findFragmentByTag("vistaProyectos");
                if(detalle !=null){
                    detalle.getProjects();
                    detalle.updateProjectStatus();
                }
            }
        };
        final Handler errorAllowed = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "Sincronización cancelada debido a que no se pudo subir todos los cambios al servidor", Toast.LENGTH_LONG).show();
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
                //Reestablecer el arrayList de proyectos
                prj = new ArrayList<Proyecto>();
                prj.clear();
                //Abrir el acceso a la base de datos y cargar los datos desde la db END//
                //Precargar en que proyectos se pertenece
                Cursor idParticipante = dbRead.rawQuery("SELECT id_proyecto FROM USER_PROJ WHERE id_usuario="+userID, null);
                //Obtener cada uno de los IDs de  los proyectos en los que se participa
                if(idParticipante.moveToFirst()){
                    //Obtener el proyecto a través de cada uno de los IDs obtenidos
                    Cursor project = dbRead.rawQuery("SELECT * FROM PROYECTOS WHERE id="+idParticipante.getInt(0), null);
                    if(project.moveToFirst()){
                        prjNames.add(project.getString(1));
                        Proyecto tempPrj = new Proyecto();
                        tempPrj.setId(project.getInt(0));
                        tempPrj.setNombre(project.getString(1));
                        tempPrj.setOwner(project.getInt(3));
                        prj.add(tempPrj);
                    }
                    while(idParticipante.moveToNext()){
                        project = dbRead.rawQuery("SELECT * FROM PROYECTOS WHERE id="+idParticipante.getInt(0), null);
                        if(project.moveToFirst()){
                            prjNames.add(project.getString(1));
                            Proyecto tempPrj = new Proyecto();
                            tempPrj.setId(project.getInt(0));
                            tempPrj.setNombre(project.getString(1));
                            tempPrj.setOwner(project.getInt(3));
                            prj.add(tempPrj);
                        }
                    }
                }
                //Precargar en que proyectos se pertenece END//
                Cursor datos = dbRead.rawQuery("SELECT * FROM PROYECTOS WHERE owner="+userID, null);
                if(datos.moveToFirst()){
                    prjNames.add(datos.getString(1));
                    Proyecto tempPrj = new Proyecto();
                    tempPrj.setId(datos.getInt(0));
                    tempPrj.setNombre(datos.getString(1));
                    tempPrj.setOwner(datos.getInt(3));
                    prj.add(tempPrj);
                    while(datos.moveToNext()){
                        prjNames.add(datos.getString(1));
                        tempPrj = new Proyecto();
                        tempPrj.setId(datos.getInt(0));
                        tempPrj.setNombre(datos.getString(1));
                        tempPrj.setOwner(datos.getInt(3));
                        prj.add(tempPrj);
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
        final TareasWS tareasDAO = new TareasWS(getResources().getString(R.string.server));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Variable que decide si se continua con la sincronización
                boolean allowedSyncro=false;
                /*Enviar todos los cambios al servidor antes de sincronizar*/
                 SQLiteDatabase dbRead = dbManager.getWritableDatabase();
                Cursor sync = dbRead.rawQuery("SELECT * FROM SYNCRO", null);
                if(sync.moveToFirst()){
                    //Llamada al metodo (por organización) que se encarga de controlar y enviar los cambios
                    allowedSyncro = uploadChanges(sync.getInt(1),sync.getInt(0),sync.getInt(2));
                    while(sync.moveToNext()){
                        //Llamada al metodo (por organización) que se encarga de controlar y enviar los cambios
                        allowedSyncro =  uploadChanges(sync.getInt(1),sync.getInt(0),sync.getInt(2));
                    }
                }
                dbRead.execSQL("DELETE FROM SYNCRO WHERE 1");
                /*Enviar todos los cambios al servidor antes de sincronizar END*/
                prj = prjDAO.getAllProyectos();
                SQLiteDatabase db = dbManager.getWritableDatabase();

                if(prj!=null){
                //Grabar los proyectos a la db de PROYECTOS
                db.execSQL("DELETE FROM PROYECTOS WHERE 1");
                    for(int a=0;a<prj.size();a++){
                        //Grabar cada item a la db
                        try{
                            db.execSQL("INSERT INTO PROYECTOS (id, nombre, descripcion, owner) VALUES ("+prj.get(a).getId()+", '"+prj.get(a).getNombre()+"','"+prj.get(a).getDescripcion()+"',"+prj.get(a).getOwner()+")");
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
                //Grabar las tareas de los proyectos a la DB
                tareas = tareasDAO.getAllTareas();
                if(tareas != null){
                    db.execSQL("DELETE FROM TASK_PROJ WHERE 1");
                    for(int a=0;a<tareas.size();a++){
                        db.execSQL("INSERT INTO TASK_PROJ (id, id_proyecto, nombre , descripcion , coste, costeFinal, valor , id_usuario , completado ) VALUES ("+tareas.get(a).getId()+", "+tareas.get(a).getProyecto()+", '"+tareas.get(a).getNombre()+"', '"+tareas.get(a).getDescripcion()+"', "+tareas.get(a).getCoste()+", "+tareas.get(a).getCosteFinal()+" ,"+tareas.get(a).getValor()+", "+tareas.get(a).getUsuario()+", "+tareas.get(a).getCompletado()+")");
                    }
                }
                //Grabar las tareas de los proyectos a la DB END//
                printProjects.sendEmptyMessage(0);
                db.close();
                finishLoadProgress.sendEmptyMessage(0);
                //Permitir de nuevo la sincronización
                sincronizando = false;
                //Re-cargar los datos de la vista
                //Actualizar la vista del fragment

            }
        }).start();
    }
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
                        showMainScreen();
                        actionBar.setTitle(getResources().getString(R.string.toDo));
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.done),Toast.LENGTH_SHORT).show();
                        break;
                    default:
                            //Ejecutar el código normal para cada una de las selecciones de la lista
                            Fragment fragment = new ListProyectos();
                            Bundle args = new Bundle();
                            args.putInt("prjID", prj.get(i-2).getId());
                            args.putString("nombrePrj", prj.get(i - 2).getNombre());
                            args.putBoolean("isHome", false);
                            fragment.setArguments(args);
                            //Cambiar el fragment actual por el nuevo
                            FragmentManager fm = getFragmentManager();
                            fm.beginTransaction().replace(R.id.content, fragment, "vistaProyectos").commit();
                            //Cerrar el drawer
                            drawer.closeDrawer(drawerList);
                            actionBar.setTitle(prj.get(i - 2).getNombre());
                        break;
                }
            }
        });
        drawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch(i){
                    case 0:

                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.done)+" Long",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        //Ejecutar el código normal para cada una de las selecciones de la lista
                        selectedItem = i;
                        editando =true;
                        startActionMode(editActionBar);
                        break;
                }
                return false;
            }
        });
    }
    public boolean uploadChanges(int tipo, int id, int idCambio){
        boolean result=false;
        switch (tipo){
            case 6:
                //Obtener instancia del webservice de Tareas
                SQLiteDatabase db6 = dbManager.getWritableDatabase();
                TareasWS tareasDAO = new TareasWS(getResources().getString(R.string.server));
                Cursor tasks = db6.rawQuery("SELECT * FROM TASK_PROJ WHERE id="+idCambio,null);
                if(tasks.moveToFirst()){
                    tareasDAO.addTask(tasks.getString(2),tasks.getString(3),tasks.getInt(5),tasks.getInt(4),tasks.getInt(5),tasks.getInt(6), tasks.getInt(1));
                    while (tasks.moveToNext()){
                        tareasDAO.addTask(tasks.getString(2),tasks.getString(3),tasks.getInt(5),tasks.getInt(4),tasks.getInt(5),tasks.getInt(6), tasks.getInt(1));
                    }
                }
                break;
            case 1:
                //Crear el webservice
                tareasDAO = new TareasWS(getResources().getString(R.string.server));
                //Obtener los datos de la db y solo borrarlos en caso de que la sincrnización se realice correctamente
                SQLiteDatabase db = dbManager.getWritableDatabase();
                Cursor c = db.rawQuery("SELECT completado FROM TASK_PROJ WHERE id="+idCambio, null);
                if(c.moveToFirst()){
                    int completado = c.getInt(0);
                    if(c.getInt(0)==1){
                        //Si está completado
                        if(tareasDAO.setTaskDone(idCambio)){
                            db.execSQL("DELETE FROM SYNCRO WHERE id="+id);
                            result= true;
                        }else{
                            result= false;
                        }
                    }else{
                        //Si no está completado
                        if(tareasDAO.setTaskUnDone(idCambio)){
                            db.execSQL("DELETE FROM SYNCRO WHERE id="+id);
                            result= true;
                        }else{
                            result= false;
                        }
                    }
                }
                break;
            case 4:
                //Obtener el WS de proyectos
                ProyectosWS proyectosDAO = new ProyectosWS(getResources().getString(R.string.server));
                //Obtener los cambios de la db
                SQLiteDatabase db2 = dbManager.getWritableDatabase();
                proyectosDAO.deleteProject(idCambio);
                break;

            case 7:
                UsuariosProjWS userDAO = new UsuariosProjWS(getResources().getString(R.string.server));
                SQLiteDatabase dbRenole = dbManager.getReadableDatabase();
                Cursor enroleData = dbRenole.rawQuery("SELECT id_proyecto, id_usuario FROM USER_PROJ WHERE id="+idCambio, null);
                if(enroleData.moveToFirst()){
                    userDAO.enrolUser(enroleData.getInt(0), enroleData.getInt(1));
                }
                break;
            case 8:
                tareasDAO = new TareasWS(getResources().getString(R.string.server));
                SQLiteDatabase db8 = dbManager.getReadableDatabase();
                Cursor c8 = db8.rawQuery("SELECT costeFinal FROM TASK_PROJ WHERE id="+idCambio,null);
                if(c8.moveToFirst()){
                    tareasDAO.addOverCost(c8.getInt(0), idCambio);
                }
                break;
            case 3:
                tareasDAO = new TareasWS(getResources().getString(R.string.server));
                tareasDAO.deleteTask(idCambio);
                break;
        }
        return result;
    }
    /**
     * Interfaz para convertir el actionBar en modo editar proyecto
     */

    private ActionMode.Callback editActionBar = new ActionMode.Callback(){
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.edit_projects, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.borrar:
                    dialogBorrar().show();
                    onDestroyActionMode(actionMode);
                    //Volver a la vista de tareas pendientes
                    showMainScreen();
                    break;
                case R.id.addTask:
                    addTask();
                    break;
                case R.id.usr_in_prj:
                    gestUsersInProjects(selectedItem-2);
                    break;
                default:
                    break;
            }
            editando = false;
            drawer.closeDrawer(drawerList);
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            actionMode = null;
            editando = false;
        }
    };
    /**
     * Metodo para borrar un proyecto
     */
    public void deleteProject(){
        try{
            Toast.makeText(getApplicationContext(), "Borrando...", Toast.LENGTH_SHORT).show();
            SQLiteDatabase db = dbManager.getWritableDatabase();
            db.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (4, "+prj.get(selectedItem-2).getId()+")");
            db.execSQL("DELETE FROM PROYECTOS WHERE id=" + prj.get(selectedItem - 2).getId());
            getProjects();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error Nº: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /*
    * Dialogo de creación de proyecto
    * */
    public void addProject(){//Preparar el dialogo
        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(R.layout.dialog_add_project, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(deleteDialogView);
        //Cargar los datos en los textviews
        final EditText nombrePrj = (EditText) deleteDialogView.findViewById(R.id.prjName);
        final EditText descripcionPrj = (EditText) deleteDialogView.findViewById(R.id.prjDesc);
        Button btnPrj = (Button) deleteDialogView.findViewById(R.id.createPrjBtn);
        //Handlers//

        /*
        * En caso de que los datos esté incompleto
        * */
        final Handler errorEmpty = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "Introduzca un nombre para continuar", Toast.LENGTH_SHORT).show();
            }
        };
        /*
        * En caso de que ocurra un error de red
        * */
        final Handler errorVarius = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "Error desconocido...", Toast.LENGTH_SHORT).show();
                deleteDialog.dismiss();
            }
        };
        /*
        * En caso de que el resultado sea ok
        * */
        final Handler resultOK = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "Sincronizando...", Toast.LENGTH_SHORT).show();
                setProgressBarIndeterminateVisibility(false);
                getProjects();
                deleteDialog.dismiss();
            }
        };
        //Handlers END//
        //Accion del boton
        btnPrj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(nombrePrj.getText().toString() != "") {
                            ProyectosWS proyectosDAO = new ProyectosWS(getResources().getString(R.string.server));
                            //Crea el proyecto y comprueba que no ha ocurrido ningun error de red y a continuación sincroniza
                            if(proyectosDAO.createProject(prefs.getInt("id",0), nombrePrj.getText().toString(), descripcionPrj.getText().toString())){
                                resultOK.sendEmptyMessage(0);
                            }else{
                                errorVarius.sendEmptyMessage(0);
                            }
                        }else{
                            errorEmpty.sendEmptyMessage(0);
                        }
                    }
                }).start();
            }
        });
        deleteDialog.show();
    }
    /*
    * Metodo para agregar una tarea al proyecto actual
    * */
    public void addTask(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(R.layout.dialog_add_task, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(deleteDialogView);
        //Cargar los datos en los textviews
        final EditText nombre = (EditText)deleteDialogView.findViewById(R.id.taskName);
        final EditText desc = (EditText)deleteDialogView.findViewById(R.id.taskDesc);
        final EditText coste = (EditText)deleteDialogView.findViewById(R.id.taskCoste);
        final EditText horas = (EditText)deleteDialogView.findViewById(R.id.taskHours);
        final EditText inversion = (EditText)deleteDialogView.findViewById(R.id.taskValue);
        final Spinner spinnerUsers = (Spinner)deleteDialogView.findViewById(R.id.taskUser);
        Button taskBtn = (Button)deleteDialogView.findViewById(R.id.taskBtn);
        //Rellenar el spinner//
        final SQLiteDatabase db = dbManager.getWritableDatabase();
        final ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
        Cursor c = db.rawQuery("SELECT * FROM USUARIOS", null);
        if(c.moveToFirst()){
            Usuario user = new Usuario();
            user.setId(c.getInt(0));
            user.setNombre(c.getString(1));
            user.setApellidos(c.getString(2));
            user.setTelefono(c.getInt(3));
            user.setMail(c.getString(4));
            usuarios.add(user);
            while(c.moveToNext()){
                user = new Usuario();
                user.setId(c.getInt(0));
                user.setNombre(c.getString(1));
                user.setApellidos(c.getString(2));
                user.setTelefono(c.getInt(3));
                user.setMail(c.getString(4));
                usuarios.add(user);
            }
            ArrayList<String> userNames = new ArrayList<String>();
                //Comprobar si la lista de usuarios está vacia
                for(int a=0;a<usuarios.size();a++){
                    userNames.add(usuarios.get(a).getNombre()+" "+usuarios.get(a).getApellidos());
                }
                spinnerUsers.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userNames));

        }
        //Rellenar el spinner//
        //Acciones y demás
        taskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((nombre.getText().toString() !="")&&(desc.getText().toString()!="")&&(coste.getText().toString()!="")&(horas.getText().toString()!="")&&(inversion.getText().toString()!="")){
                    try{
                        Toast.makeText(getApplicationContext(), "Agregando tarea....", Toast.LENGTH_LONG).show();
                        db.execSQL("INSERT INTO TASK_PROJ (id_proyecto, nombre, descripcion, coste, valor, id_usuario, completado) VALUES ("+prj.get(selectedItem-2).getId()+", '"+nombre.getText().toString()+"', '"+desc.getText().toString()+"', "+horas.getText().toString()+", "+inversion.getText().toString()+", "+usuarios.get(spinnerUsers.getSelectedItemPosition()).getId()+",0)");
                        db.execSQL("INSERT INTO USER_PROJ (id_usuario, id_proyecto) VALUES ("+prj.get(selectedItem-2).getId()+","+usuarios.get(spinnerUsers.getSelectedItemPosition()).getId()+")");
                        Cursor checkQ = db.rawQuery("SELECT id FROM TASK_PROJ WHERE 1", null);
                        checkQ.moveToLast();
                        db.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (6, "+checkQ.getString(0)+")");
                        db.close();
                        deleteDialog.dismiss();
                        ListProyectos detalle = (ListProyectos) getFragmentManager().findFragmentByTag("vistaProyectos");
                        detalle.updateProjectStatus();
                        detalle.getProjects();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error Nº: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Introduzca todos los campos antes de continuar", Toast.LENGTH_LONG).show();
                }
            }
        });

        deleteDialog.show();
    }
    public Dialog dialogBorrar(){
        final String[] items = {"Si", "No"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Desea borrar este proyecto?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item){
                    case 0:
                        deleteProject();
                        break;
                    case 1:

                        break;
                }
            }
        });
        return builder.create();
    }
    /*
     * Metodo que muestra un dialog con todos los usuarios enrolados en la empresa
     * */
    public void gestCompanyUsers(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(R.layout.dialog_gest_users, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(deleteDialogView);
        //Cargar los datos en los textviews
        ListView listaUsuarios = (ListView)deleteDialogView.findViewById(R.id.listaUsers);
        final EditText mailUser = (EditText)deleteDialogView.findViewById(R.id.mailUser);
        Button addUserBtn = (Button)deleteDialogView.findViewById(R.id.addUserBtn);
        //Cargar los nombres de los usuarios en un array para evitar construir un custom adapter
        ArrayList<String>usuariosNames = new ArrayList<String>();
        for (int a=0;a<users.size();a++){
            usuariosNames.add(users.get(a).getNombre());
        }
        //Establecer el adaptador al listview
        listaUsuarios.setAdapter(new UserListAdapter(getApplicationContext(), usuariosNames));
        //Colocar un Listener al listview
        listaUsuarios.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBorrarUsuario(users.get(i).getId()).show();
                return false;
            }
        });
        //Listener del boton
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCompanyUserDialog(mailUser.getText().toString());
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();
    }
    public void addCompanyUserDialog(final String mail){
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_gest_users_add, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(view);
        //Establecer los listViews
        EditText mailTxt = (EditText)view.findViewById(R.id.mailUser);
        mailTxt.setText(mail);
        final EditText nombreUsuario = (EditText) view.findViewById(R.id.nombreUser);
        final EditText apellidoUsuario = (EditText) view.findViewById(R.id.apellidoUser);
        final EditText passUsuario = (EditText) view.findViewById(R.id.passUser);
        final EditText telfUsuario = (EditText) view.findViewById(R.id.telfUsuario);
        Button botonAddUsuario = (Button)view.findViewById(R.id.addBtbn);
        //Establecer el listener
        botonAddUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Activar el modo de carga del actionbar y mostrar un mensaje al usuario
                setProgressBarIndeterminateVisibility(true);
                Toast.makeText(getApplicationContext(), "Creando usuario", Toast.LENGTH_SHORT).show();
                //Handlers//
                final Handler resultOK = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(getApplicationContext(), "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                        deleteDialog.dismiss();
                        getProjects();
                    }
                };
                final Handler resultERR = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(getApplicationContext(), "Error al crear el usuario", Toast.LENGTH_SHORT).show();
                    }
                };
                //Handlers END//
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UsuariosWS userDAO = new UsuariosWS(getResources().getString(R.string.server));
                        if(userDAO.addUsuario(mail, nombreUsuario.getText().toString(), apellidoUsuario.getText().toString(),telfUsuario.getText().toString(), passUsuario.getText().toString())){
                            resultOK.sendEmptyMessage(0);
                        }else{
                            resultERR.sendEmptyMessage(0);
                        }
                    }
                }).start();
            }
        });
        deleteDialog.show();
    }
    public Dialog dialogBorrarUsuario(final int position){
        final String[] items = {"Si", "No"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Desea borrar este Usuario?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        setProgressBarIndeterminateVisibility(true);
                        Toast.makeText(getApplicationContext(), "Borrando usuario", Toast.LENGTH_SHORT).show();
                        //Handlers//
                        final Handler resultOK = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                setProgressBarIndeterminateVisibility(false);
                                Toast.makeText(getApplicationContext(), "Usuario borrado", Toast.LENGTH_SHORT).show();

                                getProjects();
                            }
                        };
                        final Handler resultERR = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                setProgressBarIndeterminateVisibility(false);
                                Toast.makeText(getApplicationContext(), "Error al borrar el usuario", Toast.LENGTH_SHORT).show();
                            }
                        };
                        //Handlers END//
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UsuariosWS userDAO = new UsuariosWS(getResources().getString(R.string.server));
                                if (userDAO.deleteUsuer(Integer.toString(position))) {
                                    resultOK.sendEmptyMessage(0);
                                } else {
                                    resultERR.sendEmptyMessage(0);
                                }
                            }
                        }).start();
                        break;
                    case 1:

                        break;
                }
            }
        });
        return builder.create();
    }
    /*
    * Dialogo de borrado de usuario de un proyecto
    * */
    public Dialog dialogBorrarUsuarioPrj(final int position){
        final String[] items = {"Si", "No"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Desea quitar a este usuario del proyecto actual?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item){
                    case 0:
                        setProgressBarIndeterminateVisibility(true);
                        Toast.makeText(getApplicationContext(), "Borrando usuario", Toast.LENGTH_SHORT).show();
                        //Handlers//
                        final Handler resultOK = new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                setProgressBarIndeterminateVisibility(false);
                                Toast.makeText(getApplicationContext(), "Usuario borrado", Toast.LENGTH_SHORT).show();

                                getProjects();
                            }
                        };
                        final Handler resultERR = new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                setProgressBarIndeterminateVisibility(false);
                                Toast.makeText(getApplicationContext(), "Error al borrar el usuario", Toast.LENGTH_SHORT).show();
                            }
                        };
                        //Handlers END//
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UsuariosProjWS userDAO = new UsuariosProjWS(getResources().getString(R.string.server));
                                if(userDAO.desEnrolUser(position)){
                                    resultOK.sendEmptyMessage(0);
                                }else{
                                    resultERR.sendEmptyMessage(0);
                                }
                            }
                        }).start();
                        break;
                    case 1:

                        break;
                }
            }
        });
        return builder.create();
    }
    /*
    * Dialogo de gestión de usuarios dentro de los proyectos
    * */
    public void gestUsersInProjects(int idProyecto){
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_gest_users_prj, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(view);
        //Inicializar widgets
        final Spinner spinner = (Spinner)view.findViewById(R.id.spinnerUsuarios);
        ListView listaUsuarios = (ListView)view.findViewById(R.id.listaUsers);
        Button boton = (Button)view.findViewById(R.id.addUserBtn);
        //Dar valores a los widgets
        ArrayList<String> userNames = new ArrayList<String>();
        for(int a=0;a<users.size();a++){
            userNames.add(users.get(a).getNombre()+" "+users.get(a).getApellidos());
        }
        spinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, userNames));
        //Array con los nombres de los usuarios actuales en el proyecto (para el listview)
        ArrayList<String> currentUserNames = new ArrayList<String>();
        //Array de modelos de usuario para obtener los ids en las interacciones del listview
        final ArrayList<Usuario> currentUserModel = new ArrayList<Usuario>();
        //Array que contiene los ids del User_proj
        final ArrayList<Integer> currentUserProj = new ArrayList<Integer>();
        //Realizar a consulta a la db de los usuarios actuales y printarlos
        final SQLiteDatabase db = dbManager.getWritableDatabase();
        Cursor usuariosEnProyecto = db.rawQuery("SELECT id_usuario, id FROM USER_PROJ WHERE id_proyecto="+prj.get(selectedItem-2).getId(), null);
        //Crear una lista de los usuarios asociados al proyecto y luego extraer sus credenciales
        if(usuariosEnProyecto.moveToFirst()){
            //Obtener el dato por cada uno de los usuarios asociados
            Cursor userData = db.rawQuery("SELECT * FROM USUARIOS where id="+usuariosEnProyecto.getInt(0), null);
            if(userData.moveToFirst()){
                //Crear el susodicho dato
                Usuario tempUser = new Usuario();
                tempUser.setId(userData.getInt(0));
                tempUser.setNombre(userData.getString(1));
                tempUser.setApellidos(userData.getString(2));
                currentUserModel.add(tempUser);
                currentUserProj.add(usuariosEnProyecto.getInt(1));
            }
            while(usuariosEnProyecto.moveToNext()){
                    //Obtener el dato por cada uno de los usuarios asociados
                    userData = db.rawQuery("SELECT * FROM USUARIOS where id="+usuariosEnProyecto.getInt(0), null);
                    if(userData.moveToFirst()){
                        //Crear el susodicho dato
                        Usuario tempUser = new Usuario();
                        tempUser.setId(userData.getInt(0));
                        tempUser.setNombre(userData.getString(1));
                        tempUser.setApellidos(userData.getString(2));
                        currentUserModel.add(tempUser);
                        currentUserProj.add(usuariosEnProyecto.getInt(1));
                }
            }
        }
        //Transformar el arrayList de los modelos en un array de Strings para el listview
        for(int a=0; a<currentUserModel.size();a++){
            currentUserNames.add(currentUserModel.get(a).getNombre()+" "+currentUserModel.get(a).getApellidos());
        }
        //Establecer el Adapter del ListView
        listaUsuarios.setAdapter(new UserListAdapter(getApplicationContext(), currentUserNames));
        //Establecer el listener del boton
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    SQLiteDatabase db2 = dbManager.getWritableDatabase();
                    db2.execSQL("INSERT INTO USER_PROJ (id_usuario, id_proyecto) VALUES ("+users.get(spinner.getSelectedItemPosition()).getId()+", "+prj.get(selectedItem-2).getId()+")");
                    SQLiteDatabase finalDB = dbManager.getReadableDatabase();
                    Cursor last = finalDB.rawQuery("SELECT id FROM USER_PROJ", null);
                    if(last.moveToLast()){
                        db2.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (7, "+last.getInt(0)+")");
                        finalDB.close();
                        finalDB = null;
                        db2.close();
                        db2 = null;
                        Toast.makeText(getApplicationContext(), "Asignacion guardada",Toast.LENGTH_SHORT).show();
                    }
                }catch (SQLiteException ex){
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error Nº: "+ex.getMessage(),Toast.LENGTH_LONG).show();
                }
                catch(ArrayIndexOutOfBoundsException ex){
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error Nº: "+ex.getMessage(),Toast.LENGTH_LONG).show();
                }
                catch(IndexOutOfBoundsException ex){
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error Nº: "+ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        listaUsuarios.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBorrarUsuarioPrj(currentUserProj.get(i)).show();
                return false;
            }
        });
        deleteDialog.show();
    }
    public void showMainScreen(){
        //Ejecutar el código normal para cada una de las selecciones de la lista
        Fragment fragment = new ListProyectos();
        Bundle args = new Bundle();
        args.putString("nombrePrj", "Tareas pendientes");
        args.putBoolean("isHome", true);
        fragment.setArguments(args);
        //Cambiar el fragment actual por el nuevo
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content, fragment, "vistaProyectos").commit();
        //Cerrar el drawer
        drawer.closeDrawer(drawerList);
        actionBar.setTitle(getResources().getString(R.string.toDo));
    }
    /**
     * Obtiene la vista OVERFLOW del menú incluso si el dispositivo tiene boton Menú
     */
    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    * Mostrar las opciones de la tarea
    * */
    public void showTaskOptions(final Tareas task){
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_gest_task, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(view);
        //Casteo de widgets
        TextView taskName = (TextView)view.findViewById(R.id.taskName);
        TextView taskHoras = (TextView)view.findViewById(R.id.taskHours);
        TextView taskRoi = (TextView)view.findViewById(R.id.taskROI);
        Button deleteTask = (Button)view.findViewById(R.id.delTaskBtn);
        Button overHourBtn = (Button)view.findViewById(R.id.sobrecosteBtn);
        final EditText horas = (EditText)view.findViewById(R.id.overHours);
        //Casteo de widgets END//
        //Trabajo con los widgets
        horas.setText(task.getCosteFinal()+"");
        taskName.setText(task.getNombre());
        taskHoras.setText(task.getCoste()+" Hrs");
        taskRoi.setText(task.getValor()+"");
        //Trabajo con los widgets END//
        //Funcion de los botones
        /*
        * Funcion de overrun
        * */
        overHourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(horas.getText().toString() != ""){
                    //Do stuff
                    try{
                        int newValor = Integer.valueOf(horas.getText().toString());
                        SQLiteDatabase db = dbManager.getWritableDatabase();
                        //Cambiar el dato
                        db.execSQL("UPDATE TASK_PROJ SET costeFinal = "+newValor+" WHERE id="+task.getId());
                        //Insertar entrada en la tabla de sincronización
                        db.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (8, "+task.getId()+")");
                        db.close();
                        deleteDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Modificando datos de la tarea", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "El campo de sobrecoste no puede estar vacio", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /**
         * Funcion borrar tarea
         * */
         deleteTask.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 SQLiteDatabase db = dbManager.getWritableDatabase();
                 //Borrar el dato de la tabla de tareas
                 db.execSQL("DELETE FROM TASK_PROJ WHERE id="+task.getId());
                 //Establecer su correspondiente entrada en la tabla de sincro
                 db.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (3, "+task.getId()+")");
                 //Actualizar la vista del fragment
                 ListProyectos detalle = (ListProyectos) getFragmentManager().findFragmentByTag("vistaProyectos");
                 if(detalle !=null){
                     detalle.getProjects();
                     detalle.updateProjectStatus();
                 }
                 deleteDialog.dismiss();
             }
         });
         //Funcion de los botones END//
        deleteDialog.show();
    }
   }