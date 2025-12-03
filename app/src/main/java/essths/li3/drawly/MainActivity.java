package essths.li3.drawly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import essths.li3.drawly.databinding.ActivityMainBinding;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private int defaultColor = 0x000000; // couleur par défaut
    private ImageButton btnColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gestion mode sombre
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnColor = findViewById(R.id.btnColor);
        DrawingView drawingView = findViewById(R.id.drawingView);

        SeekBar brushSizeSeekBar = findViewById(R.id.brushSizeSeekBar);
        View undoBtn = findViewById(R.id.undoBtn);
        View clearBtn = findViewById(R.id.clearBtn);
        View redoBtn = findViewById(R.id.redoBtn);
        View saveBtn = findViewById(R.id.saveBtn);

        // Choix couleur
        btnColor.setOnClickListener(v -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainActivity.this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    defaultColor = color;
                    btnColor.setColorFilter(color);
                    drawingView.setColor(color);
                }
                @Override
                public void onCancel(AmbilWarnaDialog dialog) { }
            });
            colorPicker.show();
        });

        // Taille pinceau
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setBrushSize(progress > 0 ? progress : 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.toolbar.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.toolbar).show()
        );

        // Navigation
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.share)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Gestion clic items navigation personnalisés
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_theme) {
                toggleDarkMode();
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            if (id == R.id.share) {
                shareDrawing(drawingView);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Navigation normale pour les autres items
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // Undo / Redo / Clear
        if (undoBtn != null) undoBtn.setOnClickListener(v -> drawingView.undo());
        if (redoBtn != null) redoBtn.setOnClickListener(v -> drawingView.redo());
        if (clearBtn != null) clearBtn.setOnClickListener(v -> drawingView.clear());
        if (saveBtn != null) {
            saveBtn.setOnClickListener(v -> {
                drawingView.saveToGallery(MainActivity.this, "drawing_" + System.currentTimeMillis());
            });
        }


    }

    // Toggle mode sombre
    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = currentMode != Configuration.UI_MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("dark_mode", isDark);
        editor.apply();
        recreate();
    }

    // Partage du dessin
    private void shareDrawing(DrawingView drawingView) {
        Bitmap bitmap = drawingView.getBitmap();

        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "drawing.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Partager votre dessin via"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du partage", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
