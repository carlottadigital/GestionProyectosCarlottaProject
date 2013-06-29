package fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import adapters.ListProyectosAdapter;
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
    TextView tituloProyecto;
    ProgressBar horas, tareas;
    int todoTask =0;
    int doneTask =0;
    int todoHour =0;
    int doneHour =0;
    int prjID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_lista_proyectos, container, false);
        prjID = getArguments().getInt("prjID");
        listaProyectos = (ListView) vista.findViewById(R.id.listaProyectos);
        tituloProyecto = (TextView) vista.findViewById(R.id.tituloProyecto);
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
        SQLiteDatabase dbRead = dbManager.getReadableDatabase();

        //Realizar la query
        Cursor data = dbRead.rawQuery("SELECT * FROM TASK_PROJ WHERE id_proyecto="+prjID, null);
        proyectos = new ArrayList<Tareas>();
        if(data.moveToFirst()){
            Tareas tarea = new Tareas();
            if(data.getInt(7)!=0){
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
            tarea.setValor(data.getInt(5));
            tarea.setUsuario(data.getInt(6));
            tarea.setCompletado(data.getInt(7));
            proyectos.add(tarea);
            while(data.moveToNext()){
                if(data.getInt(7)!=0){
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
                tarea2.setValor(data.getInt(5));
                tarea2.setUsuario(data.getInt(6));
                tarea2.setCompletado(data.getInt(7));
                proyectos.add(tarea2);
            }
            //Establecer el maximo de tareas y horas (Barras de progreso
            tareas.setMax(todoTask+doneTask);
            horas.setMax(todoHour+doneHour);
            //Establecer el progreso total de horas y tareas
            tareas.setProgress(doneTask);
            horas.setProgress(doneHour);
        }
        listaProyectos.setAdapter(new ListProyectosAdapter(getActivity().getApplicationContext(), proyectos));
    }
    public void addListener(){
        listaProyectos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }
}
