package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import carlotta.digital.gestionproyectoscarlotta.R;

/**
 * Created by Borja on 4/07/13.
 */
public class UserListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;

    ArrayList<String> projects;

    public UserListAdapter(Context context, ArrayList<String> projects) {

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
        text.setText(projects.get(position));
        text.setTextColor(Color.BLACK);
        //Imagen
        img1 = (ImageView)convertView.findViewById(R.id.imagenLista);
        img1.setImageResource(R.drawable.single_user);
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
