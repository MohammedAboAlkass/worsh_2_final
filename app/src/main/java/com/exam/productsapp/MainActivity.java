package com.exam.productsapp;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // Top Product Details Views (Q1)
    private ImageView ivDetailImage;
    private TextView tvDetailName;
    private TextView tvDetailQuantity;
    private TextView tvDetailOffer;

    // RecyclerView (Q1 & Q2)
    private RecyclerView rvProducts;
    private FloatingActionButton fabAddProduct;

    // Image Pickers (Camera & Gallery)
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;
    private String currentSelectedImageUri = null;
    private ImageView currentDialogImageView = null;

    private ProductRepository productRepository;
    private ProductAdapter productAdapter;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupImagePickers();
        setupRepository();
        setupRecyclerView();
        setupListeners();
        setupOnBackPressed();
    }

    private void initViews() {
        ivDetailImage = findViewById(R.id.ivDetailImage);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvDetailOffer = findViewById(R.id.tvDetailOffer);
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
    }

    private void setupImagePickers() {
        // Gallery (الاستديو) launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String savedPath = saveImageToInternalStorage(uri);
                if (savedPath != null) {
                    currentSelectedImageUri = savedPath;
                    if (currentDialogImageView != null) {
                        currentDialogImageView.setImageURI(Uri.parse(savedPath));
                    }
                }
            }
        });

        // Camera (الكاميرا) launcher
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                String savedPath = saveBitmapToInternalStorage(bitmap);
                if (savedPath != null) {
                    currentSelectedImageUri = savedPath;
                    if (currentDialogImageView != null) {
                        currentDialogImageView.setImageURI(Uri.parse(savedPath));
                    }
                }
            }
        });
    }

    private void setupRepository() {
        productRepository = new ProductRepository(getApplication());
        // Insert sample products on first launch if table is empty (تفاح، حليب، أرز)
        productRepository.seedInitialDataIfEmpty();

        // Observe alphabetical products from Room Database
        productRepository.getAllAlphabetical().observe(this, products -> {
            if (products != null) {
                productAdapter.setProducts(products);

                if (currentProduct == null && !products.isEmpty()) {
                    displayProduct(products.get(0));
                } else if (currentProduct != null) {
                    for (Product p : products) {
                        if (p.getId() == currentProduct.getId()) {
                            displayProduct(p);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this::displayProduct);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // 1) Long press on Product Name copies to clipboard and shows Toast "تم النسخ"
        tvDetailName.setOnLongClickListener(v -> {
            if (currentProduct != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ProductName", currentProduct.getName());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        // 2) Click on Product Image opens Dialog with larger image
        ivDetailImage.setOnClickListener(v -> {
            if (currentProduct != null) {
                showImagePreviewDialog(currentProduct);
            }
        });

        // FAB: Opens Add Product Pop-up Dialog
        fabAddProduct.setOnClickListener(v -> showAddProductDialog());

        // Open ViewPager2 Multiplication Tables (Q3)
        findViewById(R.id.btnOpenTables).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TablesActivity.class);
            startActivity(intent);
        });
    }

    // Pop-up dialog for adding a new product
    private void showAddProductDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_product);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.etProductName);
        EditText etQuantity = dialog.findViewById(R.id.etProductQuantity);
        CheckBox cbOnOffer = dialog.findViewById(R.id.cbProductOnOffer);
        ImageView ivDialogImage = dialog.findViewById(R.id.ivDialogProductImage);
        Button btnSave = dialog.findViewById(R.id.btnSaveProduct);
        Button btnCancel = dialog.findViewById(R.id.btnCancelProduct);

        currentSelectedImageUri = null;
        currentDialogImageView = ivDialogImage;

        // Image picker row inside the dialog
        dialog.findViewById(R.id.layoutDialogSelectImage).setOnClickListener(v -> showImagePickerDialog());
        ivDialogImage.setOnClickListener(v -> showImagePickerDialog());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();

            if (name.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(qtyStr);
            boolean onOffer = cbOnOffer.isChecked();

            Product newProduct = new Product(name, R.drawable.ic_product_placeholder, currentSelectedImageUri, quantity, onOffer);
            productRepository.insert(newProduct);

            currentSelectedImageUri = null;
            currentDialogImageView = null;
            dialog.dismiss();
        });

        dialog.setOnDismissListener(d -> currentDialogImageView = null);
        dialog.show();
    }

    private void showImagePickerDialog() {
        final String[] options = {
                "التقاط صورة بالكاميرا",
                "اختيار من المعرض / الاستديو"
        };

        new AlertDialog.Builder(this)
                .setTitle("اختر مصدر صورة المنتج")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        cameraLauncher.launch(null);
                    } else if (which == 1) {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File file = new File(getFilesDir(), "product_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "product_cam_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void displayProduct(Product product) {
        this.currentProduct = product;
        if (product == null) {
            return;
        }

        tvDetailName.setText(product.getName());

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            ivDetailImage.setImageURI(Uri.parse(product.getImageUri()));
        } else {
            int imgRes = product.getImageRes() != 0 ? product.getImageRes() : R.drawable.ic_product_placeholder;
            ivDetailImage.setImageResource(imgRes);
        }

        // 3) Quantity: if 0, red text and "نفدت الكمية"
        if (product.getQuantity() == 0) {
            tvDetailQuantity.setText(R.string.out_of_stock);
            tvDetailQuantity.setTextColor(ContextCompat.getColor(this, R.color.red_out_of_stock));
        } else {
            tvDetailQuantity.setText(getString(R.string.quantity_available, product.getQuantity()));
            tvDetailQuantity.setTextColor(ContextCompat.getColor(this, R.color.black));
        }

        // 4) Offer: light green background if onOffer, otherwise light gray
        if (product.isOnOffer()) {
            tvDetailOffer.setText(R.string.on_offer);
            tvDetailOffer.setBackgroundResource(R.drawable.bg_offer_active);
            tvDetailOffer.setTextColor(ContextCompat.getColor(this, R.color.green_offer));
        } else {
            tvDetailOffer.setText(R.string.no_offer);
            tvDetailOffer.setBackgroundResource(R.drawable.bg_offer_inactive);
            tvDetailOffer.setTextColor(ContextCompat.getColor(this, R.color.gray_no_offer));
        }
    }

    private void showImagePreviewDialog(Product product) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image_preview);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvPreviewTitle);
        ImageView ivLarge = dialog.findViewById(R.id.ivLargePreview);
        Button btnClose = dialog.findViewById(R.id.btnClosePreview);

        tvTitle.setText(product.getName());

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            ivLarge.setImageURI(Uri.parse(product.getImageUri()));
        } else {
            int imgRes = product.getImageRes() != 0 ? product.getImageRes() : R.drawable.ic_product_placeholder;
            ivLarge.setImageResource(imgRes);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Q4: Confirm exit on back pressed
    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        });
    }

    private void showExitConfirmDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_exit_confirm);
        dialog.setCancelable(false); // cannot be dismissed by clicking outside
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        Button btnYes = dialog.findViewById(R.id.btnExitYes);
        Button btnNo = dialog.findViewById(R.id.btnExitNo);

        // Red Yes button finishes activity
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        // Green No button dismisses dialog
        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
