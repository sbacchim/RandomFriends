package com.example.userdetails;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.userdetails.model.Results;

public class UsersHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView nameView;
    private final TextView ageView;
    private final ImageView imageView;
    private Results result;
    private Context context;

    public UsersHolder(Context context, View itemView)  {
        super(itemView);
        this.context = context;
        this.nameView = itemView.findViewById(R.id.fullName);
        this.ageView = itemView.findViewById(R.id.person_age);
        this.imageView = itemView.findViewById(R.id.person_photo);
        itemView.setOnClickListener(this);
    }

    public void bindResult(Results result) {
        new DownloadImageTask(this.imageView).execute(result.getPicture().getThumbnail());
        this.result = result;
        this.nameView.setText(new StringBuilder()
                .append(Utils.caps(result.getName().getTitle())).append(" ")
                .append(Utils.caps(result.getName().getFirst())).append(" ")
                .append(Utils.capsMulti(result.getName().getLast())).toString());
        this.ageView.setText(new StringBuilder().append("Age: ")
                .append(result.getDob().getAge().toString()).toString());
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent (context, DetailActivity.class);
        intent.putExtra("clicked", result);
        intent.getParcelableExtra("clicked");
        context.startActivity(intent);
        if(this.result!=null) {
            Toast.makeText(this.context, "Clicked on " +
                    "" + Utils.caps(this.result.getName().getFirst()) + " " +
                    Utils.capsMulti(this.result.getName().getLast())+ "", Toast.LENGTH_SHORT).show();
        }
    }
}
