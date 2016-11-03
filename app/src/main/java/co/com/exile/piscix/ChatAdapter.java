package co.com.exile.piscix;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import co.com.exile.piscix.models.Mensaje;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ContactoViewHolder> {

    private List<Mensaje> mensajes;
    private Context context;


    ChatAdapter(List<Mensaje> contactos, Context context) {
        this.mensajes = contactos;
        this.context = context;
    }

    static class ContactoViewHolder extends RecyclerView.ViewHolder {
        TextView mensaje;
        TextView user;
        CardView bubble;
        ImageView status;


        ContactoViewHolder(View itemView) {
            super(itemView);

            user = (TextView) itemView.findViewById(R.id.user);
            mensaje = (TextView) itemView.findViewById(R.id.message);
            bubble = (CardView) itemView.findViewById(R.id.bubble);
            status = (ImageView) itemView.findViewById(R.id.status);
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    @Override
    public ContactoViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_bubble, viewGroup, false);
        return new ContactoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactoViewHolder contactoViewHolder, int i) {
        final int position = i;
        Mensaje mensaje = mensajes.get(i);

        float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        final int max_margin = (int) (72 * scale + 0.5f);
        final int min_margin = (int) (16 * scale + 0.5f);
        int top_margin = (int) (16 * scale + 0.5f);
        int bottom_margin = 0;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (position > 0 && mensajes.get(position - 1).getUser().equals(mensaje.getUser())) {
            top_margin = (int) (2 * scale + 0.5f);
        }

        if ((position + 1) >= mensajes.size()) {
            bottom_margin = (int) (16 * scale + 0.5f);
        }

        contactoViewHolder.mensaje.setText(mensaje.getMensaje());
        if (!mensaje.isTu()) {
            contactoViewHolder.mensaje.setTextColor(ContextCompat.getColor(context, R.color.white));
            contactoViewHolder.user.setText(mensaje.getUser());
            if (position > 0 && mensajes.get(position - 1).getUser().equals(mensaje.getUser())) {
                contactoViewHolder.user.setVisibility(View.GONE);
            } else {
                contactoViewHolder.user.setVisibility(View.VISIBLE);
            }
            contactoViewHolder.status.setVisibility(View.GONE);
            contactoViewHolder.bubble.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            layoutParams.setMargins(min_margin, top_margin, max_margin, bottom_margin);
        } else {
            contactoViewHolder.user.setVisibility(View.GONE);
            contactoViewHolder.bubble.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            contactoViewHolder.mensaje.setTextColor(ContextCompat.getColor(context, R.color.grey));
            contactoViewHolder.status.setVisibility(View.VISIBLE);
            if (mensaje.isStatus()) {
                contactoViewHolder.status.setImageResource(R.drawable.ic_done_24dp);
            } else {
                contactoViewHolder.status.setImageResource(R.drawable.ic_schedule_black_24_grey);
            }
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.setMargins(max_margin, top_margin, min_margin, bottom_margin);
        }
        contactoViewHolder.bubble.setLayoutParams(layoutParams);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}