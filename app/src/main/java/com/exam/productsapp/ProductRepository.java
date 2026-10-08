package com.exam.productsapp;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {

    private final ProductDao productDao;
    private final LiveData<List<Product>> allProductsAlphabetical;
    private final ExecutorService executorService;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        productDao = db.productDao();
        allProductsAlphabetical = productDao.getAllAlphabetical();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getAllAlphabetical() {
        return allProductsAlphabetical;
    }

    public void insert(Product product) {
        executorService.execute(() -> productDao.insert(product));
    }

    public void seedInitialDataIfEmpty() {
        executorService.execute(() -> {
            if (productDao.count() == 0) {
                productDao.insert(new Product("تفاح", R.drawable.ic_apple, 12, true));
                productDao.insert(new Product("حليب", R.drawable.ic_milk, 0, false));
                productDao.insert(new Product("أرز", R.drawable.ic_rice, 5, false));
            }
        });
    }
}
