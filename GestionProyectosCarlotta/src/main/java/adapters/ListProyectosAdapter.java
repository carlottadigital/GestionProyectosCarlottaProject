package adapters;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import carlotta.digital.gestionproyectoscarlotta.R;
import models.Tareas;
import sqlite.DBManager;

/**
 * Created by Borja on 26/06/13.
 */
public class ListProyectosAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    ArrayList<Tareas> projects;
    DBManager dbManager;
    Context context;

    public ListProyectosAdapter(Context context, ArrayList<Tareas> projects) {

        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.projects = projects;
        dbManager = new DBManager(context, "database", null, 1);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {


        TextView text;
        CheckBox chCompletado;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.lista_proyectos_adapter, null);

        }
        text = (TextView)convertView.findViewById(R.id.textoProyecto);
        chCompletado = (CheckBox) convertView.findViewById(R.id.chCompletado);
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
                SQLiteDatabase db = dbManager.getWritableDatabase();
                if(b){
                    db.execSQL("UPDATE TASK_PROJ SET completado=1 WHERE id="+projects.get(position).getId());
                }else{
                    db.execSQL("UPDATE TASK_PROJ SET completado=0 WHERE id="+projects.get(position).getId());
                }
                db.execSQL("INSERT INTO SYNCRO (tipo, id_dato) VALUES (1, "+projects.get(position).getId()+")");
            }
        });
        //Listener de cambio del item END//

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
