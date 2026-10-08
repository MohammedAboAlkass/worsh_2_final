package com.exam.productsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    private List<Product> productList = new ArrayList<>();
    private final OnItemClickListener listener;

    public ProductAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(product.getName());

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            holder.ivImage.setImageURI(android.net.Uri.parse(product.getImageUri()));
        } else {
            int img = product.getImageRes() != 0 ? product.getImageRes() : R.drawable.ic_product_placeholder;
            holder.ivImage.setImageResource(img);
        }

        if (product.getQuantity() == 0) {
            holder.tvQuantity.setText(R.string.out_of_stock);
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.red_out_of_stock));
        } else {
            holder.tvQuantity.setText(context.getString(R.string.quantity_available, product.getQuantity()));
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
        }

        if (product.isOnOffer()) {
            holder.tvOffer.setText(R.string.on_offer);
            holder.tvOffer.setBackgroundResource(R.drawable.bg_offer_active);
            holder.tvOffer.setTextColor(ContextCompat.getColor(context, R.color.green_offer));
        } else {
            holder.tvOffer.setText(R.string.no_offer);
            holder.tvOffer.setBackgroundResource(R.drawable.bg_offer_inactive);
            holder.tvOffer.setTextColor(ContextCompat.getColor(context, R.color.gray_no_offer));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvQuantity;
        TextView tvOffer;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivItemProduct);
            tvName = itemView.findViewById(R.id.tvItemProductName);
            tvQuantity = itemView.findViewById(R.id.tvItemProductQuantity);
            tvOffer = itemView.findViewById(R.id.tvItemProductOffer);
        }
    }
}
