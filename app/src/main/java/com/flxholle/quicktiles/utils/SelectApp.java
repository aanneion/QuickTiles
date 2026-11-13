package com.flxholle.quicktiles.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.flxholle.quicktiles.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SelectApp {
    private static List<ApplicationInfo> getApplicationList(Context context) {
        // Get installed applications
        final PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // Remove system apps
        Iterator<ApplicationInfo> it = installedApplications.iterator();
        while (it.hasNext()) {
            ApplicationInfo appInfo = it.next();
            // Remove system apps or apps without a launcher intent
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) == null) {
                it.remove();
            }
        }

        // Sort applications by name
        installedApplications.sort((app1, app2) -> {
            String name1 = app1.loadLabel(packageManager).toString();
            String name2 = app2.loadLabel(packageManager).toString();
            return name1.compareToIgnoreCase(name2);
        });

        // Return installed applications
        return installedApplications;
    }

    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        List<ApplicationInfo> applicationInfoList = SelectApp.getApplicationList(context);
        for (ApplicationInfo appInfo : applicationInfoList) {
            if (appInfo.packageName.equals(packageName))
                return appInfo;
        }
        return context.getApplicationInfo();
    }

    /**
     * Opens the app picker as a bottom sheet with a search field, so an app can be found without
     * scrolling through every installed app.
     */
    public static Dialog selectApps(Context context, String key, Runnable runAfterSelection) {
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        View content = LayoutInflater.from(context).inflate(R.layout.select_app_sheet, null);

        ListView applicationsList = content.findViewById(R.id.app_list);
        EditText search = content.findViewById(R.id.app_search);

        final List<ApplicationInfo> allApps = getApplicationList(context);
        final ApplicationAdapter adapter =
                new ApplicationAdapter(context, allApps, dialog, key, runAfterSelection);
        applicationsList.setAdapter(adapter);

        // The list gets the space the sheet does not need for its header and search field
        int listHeight = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.5f);
        applicationsList.getLayoutParams().height = listHeight;

        final PackageManager packageManager = context.getPackageManager();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim().toLowerCase(Locale.getDefault());
                if (query.isEmpty()) {
                    adapter.setApps(allApps);
                    return;
                }
                List<ApplicationInfo> matches = new ArrayList<>();
                for (ApplicationInfo app : allApps) {
                    String label = app.loadLabel(packageManager).toString().toLowerCase(Locale.getDefault());
                    if (label.contains(query)) {
                        matches.add(app);
                    }
                }
                adapter.setApps(matches);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dialog.setContentView(content);
        try {
            BottomSheetBehavior<?> behavior = dialog.getBehavior();
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } catch (Exception ignored) {
            // The default behaviour is a half expanded sheet, which is fine as well
        }
        return dialog;
    }

    public static Dialog selectApps(Context context, String key) {
        return selectApps(context, key, () -> {
        });
    }

    public static Dialog insertCustomAppName(Context context, String key, String preselection, Runnable runAfterSelection) {
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(
                (int) context.getResources().getDimension(R.dimen.custom_app_padding),
                (int) context.getResources().getDimension(R.dimen.custom_app_top),
                (int) context.getResources().getDimension(R.dimen.custom_app_padding),
                0
        );

        final EditText input = new EditText(context);
        input.setText(preselection);
        layout.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.set_custom_app_name)
                .setView(layout)
                .setPositiveButton(R.string.ok, (d, which) -> {
                    String customName = input.getText().toString();
                    SharedPreferencesUtil.setCustomPackage(context, key, customName);
                    runAfterSelection.run();
                })
                .create();

        return dialog;
    }

    public static Dialog insertCustomAppName(Context context, String key, String preselection) {
        return insertCustomAppName(context, key, preselection, () -> {
        });
    }

    static class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
        private final List<ApplicationInfo> appsList = new ArrayList<>();
        private Context context;
        private PackageManager packageManager;
        private Dialog dialog;
        private String saveKey;
        private Runnable runAfterSelection;

        public ApplicationAdapter(Context context, List<ApplicationInfo> appsList, Dialog dialog, String saveKey, Runnable runAfterSelection) {
            super(context, 0, new ArrayList<>(appsList));
            this.appsList.addAll(appsList);
            this.context = context;
            packageManager = context.getPackageManager();
            this.dialog = dialog;
            this.saveKey = saveKey;
            this.runAfterSelection = runAfterSelection;
        }

        /**
         * Replaces the shown apps, used by the search field.
         */
        void setApps(List<ApplicationInfo> apps) {
            appsList.clear();
            appsList.addAll(apps);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return appsList.size();
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return position >= 0 && position < appsList.size() ? appsList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (null == view) {
                LayoutInflater layoutInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.app_listview, parent, false);
            }

            final ApplicationInfo data = appsList.get(position);
            if (null != data) {
                TextView appName = view.findViewById(R.id.app_name);
                ImageView iconView = view.findViewById(R.id.app_icon);

                appName.setText(data.loadLabel(packageManager));
                iconView.setImageDrawable(data.loadIcon(packageManager));
            }

            view.setOnClickListener((v) -> {
                SharedPreferencesUtil.setCustomPackage(context, saveKey, data.packageName);
                runAfterSelection.run();
                dialog.dismiss();
            });
            return view;
        }
    }
}
