package co.com.exile.piscix;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HomeFragment extends Fragment {

    private static final String ARG_PISCINERO = "piscinero";
    private boolean piscinero;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(boolean piscinero) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PISCINERO, piscinero);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.piscinero = getArguments().getBoolean(ARG_PISCINERO);
        View fragment = inflater.inflate(R.layout.fragment_home, container, false);

        setMenu(fragment);

        return fragment;
    }

    private void setMenu(View fragment) {
        if (piscinero) {
            fragment.findViewById(R.id.piscieros_btn).setVisibility(View.GONE);
        } else {
            fragment.findViewById(R.id.rutas_btn).setVisibility(View.GONE);
        }
    }


}
