package com.exam.productsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TablesPagerAdapter extends RecyclerView.Adapter<TablesPagerAdapter.TableViewHolder> {

    private static final int TOTAL_PAGES = 500;

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table_page, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        int number = position + 1;

        holder.tvTitle.setText(context.getString(R.string.table_header, number));

        // Programmatically build the multiplication table lines (from 1 to 12 as per exam figure)
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            builder.append(number)
                   .append(" × ")
                   .append(i)
                   .append(" = ")
                   .append(number * i);
            if (i < 12) {
                builder.append("\n");
            }
        }

        holder.tvContent.setText(builder.toString());
    }

    @Override
    public int getItemCount() {
        return TOTAL_PAGES;
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTableTitle);
            tvContent = itemView.findViewById(R.id.tvTableContent);
        }
    }
}
