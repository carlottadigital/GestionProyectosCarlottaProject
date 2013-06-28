package fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import adapters.ListProyectosAdapter;
import carlotta.digital.gestionproyectoscarlotta.R;
import sqlite.DBManager;

/**
 * Created by Borja on 27/06/13.
 */
public class ListProyectos extends Fragment {

    DBManager dbManager;
    ArrayList<String> proyectos;
    ListView listaProyectos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_lista_proyectos, container, false);

        listaProyectos = (ListView) vista.findViewById(R.id.listaProyectos);
        //Cargar los datos//
        getProjects();
        return vista;
    }
    public void getProjects(){
        dbManager = new DBManager(getActivity().getApplicationContext(), "database", null, 1);
        SQLiteDatabase dbRead = dbManager.getReadableDatabase();

        //Realizar la query
        Cursor data = dbRead.rawQuery("SELECT * FROM PROYECTOS", null);
        proyectos = new ArrayList<String>();
        if(data.moveToFirst()){
            proyectos.add(data.getString(1));
            while(data.moveToNext()){
                proyectos.add(data.getString(1));
            }
        }
        listaProyectos.setAdapter(new ListProyectosAdapter(getActivity().getApplicationContext(), proyectos));
    }
}
