package pl.kskoczyk.todoandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<TODOtask> mData;
    //private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private Context mCtx;
    private ListActivity mActivity;

    // data is passed into the constructor
    RecyclerViewAdapter(ListActivity activity, Context context, List<TODOtask> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mCtx = context;
        this.mActivity = activity;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.todo_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int getPosition = position;
//        // MENU
        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mCtx, holder.menuButton);
                //inflating menu from xml resource
                popup.inflate(R.menu.popup_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.editOption:
                                // przekaż Task do edycji
                                Intent myIntent = new Intent(mCtx, EditTaskActivity.class);
                                myIntent.putExtra("task", mData.get(position));

                                // informacja o aktualnie modyfikowanym indeksie
                                SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putInt("EditedTaskIndex", position);
                                editor.commit();

                                mActivity.startActivityForResult(myIntent, 1); // requestCode=1 poinformuje, że task był edytowany, a nie dodawany
                                return true;
                            case R.id.deleteOption:
                                 // poinformuj RecyclerView o usunięciu
                                mData.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount()); // NIE UŻYWAĆ samego itemRemoved()! Wraz z usunięciem zmienia się zakres, trzeba zaktualizować bindowanie menu

                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();
            }
        });
//        //

        Date currentTime = Calendar.getInstance().getTime();
        if(currentTime.after(mData.get(position).getDate())) { // jeśli czas zadania minął
            holder.itemView.setBackgroundColor(Color.RED); // czerwony
        }
        else {
            holder.itemView.setBackgroundColor(Color.WHITE); // przyda się, jeśli zmienimy datę
        }

        String taskString = mData.get(position).toString();
        holder.myTextView.setText(taskString);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // TODO: usunąć
    public void removeItem(int position) {
        mData.remove(position);
        notifyItemRangeChanged(position, getItemCount());
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView myTextView;
        AppCompatImageButton menuButton; // to nie zwykły button!

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.taskRowText);
            menuButton = itemView.findViewById(R.id.menuButton);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return false;
        }
    }

    // convenience method for getting data at click position
//    TODOtask getItem(int id) {
    TODOtask getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}