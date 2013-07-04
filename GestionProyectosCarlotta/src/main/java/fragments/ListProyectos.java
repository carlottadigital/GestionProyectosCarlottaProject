package fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import carlotta.digital.gestionproyectoscarlotta.R;
import models.Tareas;
import sqlite.DBManager;

/**
 * Created by Borja on 27/06/13.
 */
public class ListProyectos extends Fragment {

    DBManager dbManager;
    ArrayList<Tareas> proyectos;
    ListView listaProyectos;
    TextView tituloProyecto, textHour, textTask;
    ProgressBar horas, tareas;
    int todoTask =0;
    int doneTask =0;
    int todoHour =0;
    int doneHour =0;
    int prjID;
    boolean canPinchar = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_lista_proyectos, container, false);
        prjID = getArguments().getInt("prjID");
        listaProyectos = (ListView) vista.findViewById(R.id.listaProyectos);
        tituloProyecto = (TextView) vista.findViewById(R.id.tituloProyecto);
        textTask = (TextView)vista.findViewById(R.id.textTask);
        textHour = (TextView)vista.findViewById(R.id.textHours);
        horas = (ProgressBar) vista.findViewById(R.id.progressHoras);
        tareas = (ProgressBar) vista.findViewById(R.id.progressTareas);
        tituloProyecto.setText(getArguments().getString("nombrePrj"));
        //Cargar los datos//
        getProjects();
        //stablecer el Listener
        addListener();
        return vista;
    }
    public void getProjects(){
        dbManager = new DBManager(getActivity().getApplicationContext(), "database", null, 1);
        listaProyectos.setAdapter(null);
        //Realizar la query
        updateProjectStatus();
        ListProyectosAdapter adapter = new ListProyectosAdapter(getActivity().getApplicationContext(), proyectos);
        listaProyectos.setAdapter(adapter);
    }
    public void addListener(){
        listaProyectos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity().getApplicationContext(), "Item "+i+" pulsado", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void updateProjectStatus(){
        doneHour=0;
        doneTask=0;
        todoHour=0;
        todoTask=0;
        SQLiteDatabase dbRead = dbManager.getReadableDatabase();
        Cursor data = dbRead.rawQuery("SELECT * FROM TASK_PROJ WHERE id_proyecto="+prjID, null);
        proyectos = new ArrayList<Tareas>();
        if(data.moveToFirst()){
            Tareas tarea = new Tareas();
            if(data.getInt(8)!=0){
                doneHour = doneHour + data.getInt(4);
                doneTask++;
            }else{
                todoHour = todoHour + data.getInt(4);
                todoTask++;
            }
            tarea.setId(data.getInt(0));
            tarea.setProyecto(data.getInt(1));
            tarea.setNombre(data.getString(2));
            tarea.setDescripcion(data.getString(3));
            tarea.setCoste(data.getInt(4));
            tarea.setValor(data.getInt(6));
            tarea.setUsuario(data.getInt(7));
            tarea.setCompletado(data.getInt(8));
            proyectos.add(tarea);
            while(data.moveToNext()){
                if(data.getInt(8)!=0){
                    doneHour = doneHour + data.getInt(4);
                    doneTask++;
                }else{
                    todoHour = todoHour + data.getInt(4);
                    todoTask++;
                }
                Tareas tarea2 = new Tareas();
                tarea2.setId(data.getInt(0));
                tarea2.setProyecto(data.getInt(1));
                tarea2.setNombre(data.getString(2));
                tarea2.setDescripcion(data.getString(3));
                tarea2.setCoste(data.getInt(4));
                tarea2.setValor(data.getInt(6));
                tarea2.setUsuario(data.getInt(7));
                tarea2.setCompletado(data.getInt(8));
                proyectos.add(tarea2);
            }
            //Establecer el maximo de tareas y horas (Barras de progreso
            tareas.setMax(todoTask+doneTask);
            horas.setMax(todoHour+doneHour);
            //Establecer el progreso total de horas y tareas
            tareas.setProgress(doneTask);
            horas.setProgress(doneHour);
            //Establecer el progreso total y maximo de los textviews
            textTask.setText(getResources().getString(R.string.tasks)+" ("+doneTask+"/"+(todoTask+doneTask)+")");
            textHour.setText(getResources().getString(R.string.horas)+" ("+doneHour+"/"+(todoHour+doneHour)+")");
        }
    }

    /*
    * Innerclass del Adapter
    * */
    public class ListProyectosAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        ArrayList<Tareas> projects;
        DBManager dbManager;
        Context context;
        int count =0;

        public ListProyectosAdapter(Context context, ArrayList<Tareas> projects) {

            mInflater = LayoutInflater.from(context);
            this.context = context;
            this.projects = projects;
            dbManager = new DBManager(context, "database", null, 1);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {


            TextView text;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.lista_proyectos_adapter, null);

            }
            text = (TextView)convertView.findViewById(R.id.textoProyecto);
           final CheckBox chCompletado = (CheckBox) convertView.findViewById(R.id.chCompletado);
            text.setTextColor(Color.parseColor("#000000"));

            text.setText(projects.get(position).getNombre());
            if(projects.get(position).getCompletado()==1){
                chCompletado.setChecked(true);
            }else{
                chCompletado.setChecked(false);
            }
            //Listener de cambio del item
            chCompletado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(false){

                        canPinchar = false;
                        count = 0;
                        //getProjects();
                    }
                }
            });
            chCompletado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SQLiteDatabase db = dbManager.getWritableDatabase();
                    try{
                    if(chCompletado.isChecked()){
                        db.execSQL("UPDATE TASK_PROJ SET completado=1 WHERE id="+projects.get(position).getId());
                        projects.get(position).setCompletado(1);
                        proyectos.get(position).setCompletado(1);
                    }else{
                        db.execSQL("UPDATE TASK_PROJ SET completado=0 WHERE id="+projects.get(position).getId());
                        projects.get(position).setCompletado(0);
                        proyectos.get(position).setCompletado(0);
                    }
                    db.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (1, "+projects.get(position).getId()+")");
                    db.close();
                    updateProjectStatus();
                    }catch (SQLiteException ex){
                        ex.printStackTrace();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            //Listener de cambio del item END//

            //Tapon anti bucle-incremental
            count++;
            if(count ==projects.size())canPinchar=true;
            //EXEC-END
            return convertView;

        }


        public int getCount() {

            return projects.size();

        }


        public Object getItem(int position) {

            return position;

        }


        public long getItemId(int position) {

            return position;

        }

    }
}
