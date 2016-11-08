package co.com.exile.piscix;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.com.exile.piscix.helper.ItemTouchHelperAdapter;
import co.com.exile.piscix.helper.ItemTouchHelperViewHolder;
import co.com.exile.piscix.models.Asignacion;

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private int from = -1;
    private int to = -1;

    interface OnStartDragListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private final Context context;
    static List<Asignacion> itemList = new ArrayList<>();


    private final RutaPActivity dragStartListener;

    ItemAdapter(Context context, RutaPActivity dragStartListener) {
        this.context = context;
        this.dragStartListener = dragStartListener;

    }

    @Override
    public void onItemDismiss(final int position) {
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        from = fromPosition;
        to = toPosition;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(itemList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(itemList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

    }

    void addItem(int position, Asignacion item) {

        itemList.add(position, item);
        notifyItemInserted(position);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, final int position) {

        final Asignacion piscina = itemList.get(position);

        itemViewHolder.title.setText(context.getString(R.string.piscina_title, piscina.getNombre(), piscina.getTipo()));
        itemViewHolder.cliente.setText(piscina.getCliente());
        String medidas = context.getString(R.string.piscina_medidas, piscina.getAncho(), piscina.getLargo(), piscina.getProfundidad());
        itemViewHolder.medidas.setText(piscina.getOrden() + " " + medidas);
        itemViewHolder.relativeReorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(itemViewHolder);
                }
                return false;
            }
        });
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ruta_piscinero, viewGroup, false);
        return new ItemViewHolder(itemView);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder, View.OnClickListener {

        protected LinearLayout container;
        TextView title;
        TextView cliente;
        TextView medidas;
        ImageView ivReorder;
        RelativeLayout relativeReorder;
        CardView iconCard;
        protected ImageView icon;


        ItemViewHolder(final View v) {
            super(v);
            container = (LinearLayout) v.findViewById(R.id.root_layout);
            title = (TextView) v.findViewById(R.id.title);
            title = (TextView) v.findViewById(R.id.title);
            cliente = (TextView) v.findViewById(R.id.cliente);
            medidas = (TextView) v.findViewById(R.id.medidas);
            ivReorder = (ImageView) v.findViewById(R.id.ivReorder);
            relativeReorder = (RelativeLayout) v.findViewById(R.id.relativeReorder);
            iconCard = (CardView) v.findViewById(R.id.icon_card);
            icon = (ImageView) v.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public void onItemSelected(Context context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            iconCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            icon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }

        @Override
        public void onItemClear(Context context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            iconCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            icon.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            if (from > -1 && to > -1) {
                Log.i("from", "" + from);
                Log.i("to", "" + to);
                int asignacion = itemList.get(from).getId();
                int orden = itemList.get(to).getOrden();
                dragStartListener.saveOrden(asignacion, orden);
            }
            from = -1;
            to = -1;
        }

    }

}
