package essths.li3.drawly;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
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

import essths.li3.drawly.databinding.ActivityMainBinding;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private int defaultColor = 0x000000; // rouge par défaut
    private ImageButton btnColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false); // clair par défaut
        if(darkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnColor = findViewById(R.id.btnColor);
        DrawingView drawingView = findViewById(R.id.drawingView);
        SeekBar brushSizeSeekBar = findViewById(R.id.brushSizeSeekBar);
        View undoBtn = findViewById(R.id.undoBtn);
        View clearBtn = findViewById(R.id.clearBtn);
        View redoBtn = findViewById(R.id.redoBtn);

        // Couleur
        btnColor.setOnClickListener(v -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainActivity.this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    // alert changement
                    defaultColor = color;
                    btnColor.setColorFilter(color);
                    drawingView.setColor(color);
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) { }
            });
            colorPicker.show();
        });

        // Épaisseur pinceau
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // alert changement
                float brushSize = progress > 0 ? progress : 1; // jamais 0
                drawingView.setBrushSize(brushSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Toolbar et navigation
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.toolbar.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.toolbar).show());

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.upload, R.id.share)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //  clic sur l'item "Mode"
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_theme) {
                int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                boolean isDark;

                if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    isDark = false;
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    isDark = true;
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("dark_mode", isDark);
                editor.apply();
                recreate();

                DrawerLayout drawer1 = binding.drawerLayout;
                drawer1.closeDrawer(GravityCompat.START);

                return true;
            }

            return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main));
        });

        // Undo
        if (undoBtn != null) {
            undoBtn.setOnClickListener(v -> {
                // alert changement
                drawingView.undo();
            });
        }

        // Redo
        if (redoBtn != null) {
            redoBtn.setOnClickListener(v -> {
                // alert changement
                drawingView.redo();
            });
        }

        // Clear
        if (clearBtn != null) {
            clearBtn.setOnClickListener(v -> {
                // alert changement
                drawingView.clear();
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
