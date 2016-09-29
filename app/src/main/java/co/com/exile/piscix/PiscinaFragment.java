package co.com.exile.piscix;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PiscinaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PiscinaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_ESTADO = "estado";
    private static final String ARG_ANCHO = "ancho";
    private static final String ARG_LARGO = "largo";
    private static final String ARG_PROFUNDIDAD = "profundidad";

    // TODO: Rename and change types of parameters
    private String nombre;
    private String ancho;
    private String largo;
    private String profundidad;
    private String estado;


    public PiscinaFragment() {
        // Required empty public constructor
    }


    public static PiscinaFragment newInstance(Piscina piscina) {
        PiscinaFragment fragment = new PiscinaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ANCHO, piscina.getAncho() + " metros");
        args.putString(ARG_ESTADO, piscina.getTipo() + ", " + (piscina.isEstado()? "En buen estado" : "En mal estado"));
        args.putString(ARG_LARGO, piscina.getLargo() + " metros");
        args.putString(ARG_NOMBRE, piscina.getNombre());
        args.putString(ARG_PROFUNDIDAD, piscina.getProfundidad() + " metros");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombre = getArguments().getString(ARG_NOMBRE);
            ancho = getArguments().getString(ARG_ANCHO);
            largo = getArguments().getString(ARG_LARGO);
            profundidad = getArguments().getString(ARG_PROFUNDIDAD);
            estado = getArguments().getString(ARG_ESTADO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_piscina, container, false);
        TextView nombre = (TextView) fragment.findViewById(R.id.nombre);
        TextView ancho = (TextView) fragment.findViewById(R.id.ancho);
        TextView largo = (TextView) fragment.findViewById(R.id.largo);
        TextView profundidad = (TextView) fragment.findViewById(R.id.profundidad);
        TextView estado = (TextView) fragment.findViewById(R.id.estado);

        nombre.setText(this.nombre);
        ancho.setText(this.ancho);
        largo.setText(this.largo);
        profundidad.setText(this.profundidad);
        estado.setText(this.estado);

        return fragment;
    }

}
