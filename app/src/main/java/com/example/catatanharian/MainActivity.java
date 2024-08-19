package com.example.catatanharian;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Aplikasi Catatan Harian");

        listView = findViewById(R.id.listView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), InsertAndViewActivity.class);
            Map<String, Object> data = (Map<String, Object>) parent.getAdapter().getItem(position);
            intent.putExtra("filename", data.get("name").toString());
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Map<String, Object> data = (Map<String, Object>) parent.getAdapter().getItem(position);
            tampilkanDialogKonfirmasiHapusCatatan(data.get("name").toString());
            return true;
        });

        buatFolder();
        verifyStoragePermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyStoragePermissions();
    }

    private String getAppDir() {
        return getExternalFilesDir(null) + File.separator + "catatan";
    }

    private void buatFolder() {
        String path = getAppDir();
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.d("CatatanHarian", "Folder created: " + path);
            } else {
                Log.e("CatatanHarian", "Failed to create folder: " + path);
            }
        }
    }

    public void verifyStoragePermissions() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            mengambilListFilePadaFolder();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mengambilListFilePadaFolder();
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void mengambilListFilePadaFolder() {
        String path = getAppDir();
        Log.d("CatatanHarian", "Mengambil list file dari: " + path);
        File directory = new File(path);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                ArrayList<Map<String, Object>> itemDataList = new ArrayList<>();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

                for (File file : files) {
                    Map<String, Object> listItemMap = new HashMap<>();
                    listItemMap.put("name", file.getName());
                    Date lastModDate = new Date(file.lastModified());
                    listItemMap.put("date", simpleDateFormat.format(lastModDate));
                    itemDataList.add(listItemMap);
                }

                SimpleAdapter simpleAdapter = new SimpleAdapter(this, itemDataList,
                        android.R.layout.simple_list_item_2,
                        new String[]{"name", "date"},
                        new int[]{android.R.id.text1, android.R.id.text2});
                listView.setAdapter(simpleAdapter);
                simpleAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Tidak ada catatan", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder catatan tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_tambah) {
            Intent i = new Intent(this, InsertAndViewActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void tampilkanDialogKonfirmasiHapusCatatan(final String filename) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Catatan ini?")
                .setMessage("Apakah Anda yakin ingin menghapus Catatan " + filename + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> hapusFile(filename))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    void hapusFile(String filename) {
        String path = getAppDir();
        File file = new File(path, filename);
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "File berhasil dihapus", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gagal menghapus file", Toast.LENGTH_SHORT).show();
            }
        }
        mengambilListFilePadaFolder();
    }
}