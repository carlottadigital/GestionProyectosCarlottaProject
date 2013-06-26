package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import carlotta.digital.gestionproyectoscarlotta.R;

/**
 * Created by Borja on 26/06/13.
 */
public class DrawerProyectosAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    ArrayList<String> projects;

    public DrawerProyectosAdapter(Context context, ArrayList<String> projects) {

        mInflater = LayoutInflater.from(context);
        this.projects = projects;

    }

    public View getView(int position, View convertView, ViewGroup parent) {


        TextView text;
        ImageView img1;


        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.lista_proyectos, null);

        }
        text = (TextView)convertView.findViewById(R.id.textoLista);
        img1 = (ImageView)convertView.findViewById(R.id.imagenLista);

        text.setText(projects.get(position));
        switch (position){
            case 0:
                img1.setImageResource(R.drawable.lst_fav);
                break;
            case 1:
                img1.setImageResource(R.drawable.lst_done);
                break;
            default:
                img1.setImageResource(R.drawable.lst_prj);
                break;
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
