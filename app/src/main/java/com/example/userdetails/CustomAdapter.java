package com.example.userdetails;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.userdetails.model.Results;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<UsersHolder> implements Filterable {

    private List<Results> results;
    private List<Results> finalFiltered;
    private int resource;
    private Context context;

    public CustomAdapter(Context context, int resource, List<Results> list) {
        this.results = list;
        this.context = context;
        this.resource = resource;
        this.finalFiltered = list;
    }

    @NonNull
    @Override
    public UsersHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(this.resource, viewGroup, false);
        return new UsersHolder(this.context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersHolder usersHolder, int i) {
        Results result = this.finalFiltered.get(i);
        usersHolder.bindResult(result);
    }

    @Override
    public int getItemCount() {
        return this.finalFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString();
                List<Results> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered = results;
                } else {
                    for (Results row : results) {
                        if (row.getName().getFirst().toLowerCase().contains(query.toLowerCase())
                                ||row.getName().getTitle().toLowerCase().contains(query.toLowerCase())
                                || row.getName().getLast().toLowerCase().contains(query.toLowerCase())
                                || row.getDob().getAge().toString().contains(constraint)) {
                            filtered.add(row);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = filtered.size();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                finalFiltered = (ArrayList<Results>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface CustomAdapterListener {
        void onSelected(String item);
    }
}