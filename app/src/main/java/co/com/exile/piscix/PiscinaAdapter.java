package co.com.exile.piscix;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class PiscinaAdapter extends RecyclerView.Adapter<PiscinaAdapter.PiscinaViewHolder>{

    private List<Piscina> piscinas;
    private RecyclerViewClickListener listener;

    PiscinaAdapter(List<Piscina> piscinas, RecyclerViewClickListener listener){
        this.piscinas = piscinas;
        this.listener = listener;
    }

    static class PiscinaViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        ImageView imageView;
        View view;


        PiscinaViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            view = itemView;
        }
    }

    @Override
    public int getItemCount() {
        return piscinas.size();
    }

    @Override
    public PiscinaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_card, viewGroup, false);
        v.setVisibility(View.VISIBLE);
        return new PiscinaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PiscinaViewHolder piscinaViewHolder, int i) {
        final  int position = i;
        Piscina piscina = piscinas.get(i);
        piscinaViewHolder.subtitle.setText(piscina.getTipo());
        piscinaViewHolder.title.setText(piscina.getNombre());
        piscinaViewHolder.imageView.setImageResource(R.drawable.ic_pool_100dp);
        piscinaViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.recyclerViewListClicked(view, position);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}