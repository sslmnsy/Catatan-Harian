package com.example.catatanharian;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class InsertAndViewActivity extends AppCompatActivity {
    EditText editFileName, editContent;
    Button btnSimpan;
    String fileName = "", tempCatatan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_and_view);

        editFileName = findViewById(R.id.editFilename);
        editContent = findViewById(R.id.editContent);
        btnSimpan = findViewById(R.id.btnSimpan);

        btnSimpan.setOnClickListener(v -> {
            if (!tempCatatan.equals(editContent.getText().toString())) {
                tampilkanDialogKonfirmasiPenyimpanan();
            } else {
                Toast.makeText(InsertAndViewActivity.this, "Tidak ada perubahan yang dilakukan", Toast.LENGTH_SHORT).show();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            getSupportActionBar().setTitle("Ubah Catatan");
            fileName = extras.getString("filename");
            editFileName.setText(fileName);
            editFileName.setEnabled(false);
            bacaFile();
        } else {
            getSupportActionBar().setTitle("Tambah Catatan");
        }

        buatFolder();
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

    void bacaFile() {
        String path = getAppDir();
        File file = new File(path, fileName);
        if (file.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                Log.e("CatatanHarian", "Error " + e.getMessage());
            }
            tempCatatan = text.toString();
            editContent.setText(tempCatatan);
        } else {
            Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    void buatDanUbah() {
        String path = getAppDir();
        File file = new File(path, editFileName.getText().toString());
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
            streamWriter.append(editContent.getText());
            streamWriter.flush();
            streamWriter.close();
            outputStream.flush();
            outputStream.close();
            Toast.makeText(this, "Catatan disimpan", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan catatan", Toast.LENGTH_SHORT).show();
        }
    }

    void tampilkanDialogKonfirmasiPenyimpanan() {
        new AlertDialog.Builder(this)
                .setTitle("Simpan Catatan")
                .setMessage("Apakah Anda yakin ingin menyimpan Catatan ini?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> buatDanUbah())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (!tempCatatan.equals(editContent.getText().toString())) {
            tampilkanDialogKonfirmasiPenyimpanan();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}