package co.com.exile.piscix;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.com.exile.piscix.models.Contacto;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_TELEFONO = "telefono";
    private static final String ARG_CORREO = "correo";
    private static final String ARG_RELACION = "relacion";

    // TODO: Rename and change types of parameters
    private String nombre;
    private String apellidos;
    private String telefono;
    private String correo;
    private String relacion;


    public ContactoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param contacto contacto a renderizar.
     * @return A new instance of fragment ContactoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactoFragment newInstance(Contacto contacto) {
        ContactoFragment fragment = new ContactoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOMBRE, contacto.getNombre() + " " + contacto.getApellidos());
        args.putString(ARG_TELEFONO, contacto.getTelefono());
        args.putString(ARG_CORREO, contacto.getCorreo());
        args.putString(ARG_RELACION, contacto.getRelacion());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombre  = getArguments().getString(ARG_NOMBRE);
            telefono = getArguments().getString(ARG_TELEFONO);
            correo = getArguments().getString(ARG_CORREO);
            relacion = getArguments().getString(ARG_RELACION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_contacto, container, false);
        TextView nombre = (TextView) fragment.findViewById(R.id.nombre);
        final TextView telefono = (TextView) fragment.findViewById(R.id.telefono);
        TextView correo = (TextView) fragment.findViewById(R.id.correo);
        TextView relacion = (TextView) fragment.findViewById(R.id.relacion);

        View phone = fragment.findViewById(R.id.phone);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "tel:" + ContactoFragment.this.telefono.trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        nombre.setText(this.nombre);
        SpannableString content = new SpannableString(this.telefono);
        content.setSpan(new UnderlineSpan(), 0, this.telefono.length(), 0);
        telefono.setText(content);
        correo.setText(this.correo);
        relacion.setText(this.relacion);




        return fragment;
    }

}
