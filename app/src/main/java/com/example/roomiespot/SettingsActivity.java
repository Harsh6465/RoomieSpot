package com.example.roomiespot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // Change Password Preference
            Preference changePasswordPref = findPreference("change_password");
            if (changePasswordPref != null) {
                changePasswordPref.setOnPreferenceClickListener(preference -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireContext(), 
                                            "Password reset email sent", 
                                            Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), 
                                            "Failed to send reset email", 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), 
                            "Please log in to change password", 
                            Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }

            // Privacy Policy Preference
            Preference privacyPolicyPref = findPreference("privacy_policy");
            if (privacyPolicyPref != null) {
                privacyPolicyPref.setOnPreferenceClickListener(preference -> {
                    String url = "https://roomiespot.com/privacy-policy";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                });
            }

            // Notifications Switch
            SwitchPreference notificationsPref = findPreference("notifications_enabled");
            if (notificationsPref != null) {
                notificationsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (Boolean) newValue;
                    // TODO: Implement actual notification toggle logic
                    Toast.makeText(requireContext(), 
                        isEnabled ? "Notifications Enabled" : "Notifications Disabled", 
                        Toast.LENGTH_SHORT).show();
                    return true;
                });
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}