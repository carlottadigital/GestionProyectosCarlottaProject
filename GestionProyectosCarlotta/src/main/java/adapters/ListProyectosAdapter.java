package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import carlotta.digital.gestionproyectoscarlotta.R;
import models.Tareas;

/**
 * Created by Borja on 26/06/13.
 */
public class ListProyectosAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    ArrayList<Tareas> projects;

    public ListProyectosAdapter(Context context, ArrayList<Tareas> projects) {

        mInflater = LayoutInflater.from(context);
        this.projects = projects;

    }

    public View getView(int position, View convertView, ViewGroup parent) {


        TextView text;
        CheckBox chCompletado;
        //ImageView img1;


        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.lista_proyectos_adapter, null);

        }
        text = (TextView)convertView.findViewById(R.id.textoProyecto);
        chCompletado = (CheckBox) convertView.findViewById(R.id.chCompletado);
        text.setTextColor(Color.parseColor("#000000"));
        //img1 = (ImageView)convertView.findViewById(R.id.imagenLista);

        text.setText(projects.get(position).getNombre());
        if(projects.get(position).getCompletado()==1){
            chCompletado.setChecked(true);
        }else{
            chCompletado.setChecked(false);
        }

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
