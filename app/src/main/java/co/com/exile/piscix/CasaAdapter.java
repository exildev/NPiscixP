package co.com.exile.piscix;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import co.com.exile.piscix.models.Casa;


public class CasaAdapter extends RecyclerView.Adapter<CasaAdapter.CasaViewHolder>{

    List<Casa> casas;

    CasaAdapter(List<Casa> casas){
        this.casas = casas;
    }

    public static class CasaViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        ImageView imageView;


        CasaViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return casas.size();
    }

    @Override
    public CasaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_card, viewGroup, false);
        v.setVisibility(View.VISIBLE);
        return new CasaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CasaViewHolder casaViewHolder, int i) {
        Casa contacto = casas.get(i);
        if (contacto.getLongitud() == null || contacto.getLatitud() == null || contacto.getLongitud().equals("null")  || contacto.getLatitud().equals("null")){
            casaViewHolder.subtitle.setVisibility(View.GONE);
        }else {
            casaViewHolder.subtitle.setText(contacto.getLatitud() + ", " + contacto.getLongitud());
        }
        casaViewHolder.title.setText(contacto.getDireccion());
        casaViewHolder.imageView.setImageResource(R.drawable.ic_home_100dp);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}