package com.exam.productsapp;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private int imageRes;
    private String imageUri;
    private int quantity;
    private boolean onOffer;

    public Product(String name, int imageRes, int quantity, boolean onOffer) {
        this.name = name;
        this.imageRes = imageRes;
        this.imageUri = null;
        this.quantity = quantity;
        this.onOffer = onOffer;
    }

    @Ignore
    public Product(String name, int imageRes, String imageUri, int quantity, boolean onOffer) {
        this.name = name;
        this.imageRes = imageRes;
        this.imageUri = imageUri;
        this.quantity = quantity;
        this.onOffer = onOffer;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isOnOffer() {
        return onOffer;
    }

    public void setOnOffer(boolean onOffer) {
        this.onOffer = onOffer;
    }
}
