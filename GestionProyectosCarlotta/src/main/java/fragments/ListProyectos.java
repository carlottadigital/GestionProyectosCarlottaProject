package fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import carlotta.digital.gestionproyectoscarlotta.R;

/**
 * Created by Borja on 27/06/13.
 */
public class ListProyectos extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_lista_proyectos, container, false);

        return vista;
    }
}
