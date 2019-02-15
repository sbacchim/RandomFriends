package com.example.userdetails;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.userdetails.model.Results;
import com.squareup.picasso.Picasso;
import java.util.HashSet;
import java.util.Set;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;

public class CustomRealmAdapter extends RealmRecyclerViewAdapter<Results, CustomRealmAdapter.RealmUsersHolder>{


    Realm realm;

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<>();


    public CustomRealmAdapter(@Nullable OrderedRealmCollection<Results> data, boolean autoUpdate) {
        super(data, autoUpdate);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public RealmUsersHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);
        return new RealmUsersHolder(viewGroup.getContext(), view);
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public void onBindViewHolder(@NonNull RealmUsersHolder realmUsersHolder, int i) {
        Results result = getItem(i);
        realmUsersHolder.data = result;
        realmUsersHolder.nameView.setText(new StringBuilder()
                .append(Utils.caps(result.getName().getTitle())).append(" ")
                .append(Utils.caps(result.getName().getFirst())).append(" ")
                .append(Utils.capsMulti(result.getName().getLast())).toString());
        realmUsersHolder.ageView.setText(new StringBuilder().append("Age: ")
                .append(result.getDob().getAge().toString()).toString());
        Picasso.get().load(result.getPicture().getThumbnail()).into(realmUsersHolder.imageView);
    }


    public class RealmUsersHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public Results data;
        public TextView nameView;
        public TextView ageView;
        public ImageView imageView;
        public Context context;

        public RealmUsersHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            this.nameView = itemView.findViewById(R.id.fullName);
            this.ageView = itemView.findViewById(R.id.person_age);
            this.imageView = itemView.findViewById(R.id.person_photo);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this::onLongClick);
        }

        @Override
        public void onClick(View v) {
            Results clicked = this.data;
            Intent intent = new Intent (this.context, DetailActivity.class);
            intent.putExtra("clicked", clicked.getDbId());
            context.startActivity(intent);
            if(clicked !=null) {
                Toast.makeText(this.context, "Clicked on " +
                        "" + Utils.caps(clicked.getName().getFirst()) + " " +
                        Utils.capsMulti(clicked.getName().getLast()) + "", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            deleteSingleItemAlert(this.data);
            return true;
        }

        public void deleteSingleItemAlert(Results deleting){
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setMessage(R.string.cancel_single_alert_msg)
                    .setTitle(R.string.cancel_alert_title);

            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id) {
                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            deleting.deleteFromRealm();
                        }
                    });
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void filterResults(String text) {
        realm = Realm.getDefaultInstance();
        text = text == null ? null : text.toLowerCase().trim();
        if(text == null || "".equals(text)) {
            updateData(realm.where(Results.class).findAll().sort("dbId", Sort.DESCENDING));
        } else {
            updateData(realm.where(Results.class)
                    .contains("name.first", text)
                    .or()
                    .contains("name.last", text)
                    .or()
                    .contains("name.title", text)
                    .findAll());
        }
    }

    public Filter getFilter() {
        return new MyNamesFilter(this);
    }

    private class MyNamesFilter
            extends Filter {
        private final CustomRealmAdapter adapter;

        private MyNamesFilter(CustomRealmAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filterResults(constraint.toString());
        }
    }
}
