package co.com.exile.piscix;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import co.com.exile.piscix.models.Contacto;


public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder>{

    private List<Contacto> contactos;
    private RecyclerViewClickListener listener;


    ContactoAdapter(List<Contacto> contactos, RecyclerViewClickListener listener){
        this.contactos = contactos;
        this.listener = listener;
    }

    static class ContactoViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        ImageView imageView;
        View view;


        ContactoViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            view = itemView;
        }
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    @Override
    public ContactoViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_card, viewGroup, false);
        return new ContactoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactoViewHolder contactoViewHolder, int i) {
        final  int position = i;
        Contacto contacto = contactos.get(i);
        contactoViewHolder.title.setText(contacto.getNombre() + " " + contacto.getApellidos());
        contactoViewHolder.subtitle.setText(contacto.getTelefono());
        contactoViewHolder.imageView.setImageResource(R.drawable.ic_person);
        contactoViewHolder.view.setOnClickListener(new View.OnClickListener() {
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