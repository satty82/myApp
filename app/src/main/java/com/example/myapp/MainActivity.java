package com.example.myapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.TextExtractor;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.itextpdf.text.pdf.XfaXpathConstructor.XdpPackage.Pdf;


public class MainActivity extends AppCompatActivity {

    public int FILE_SELECT_CODE = 101;
    public String TAG;
    public String actualfilepath = "";
    public String txs;

    PDFView pdfView;
     File myFile;
     EditText editText;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Click on Press to get data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Easy PDF To Text");

        editText = findViewById(R.id.editText);
        pdfView = findViewById(R.id.pdfView);


        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }

    public void onPress(View view) {


        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select your file"), FILE_SELECT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e(TAG, " result is "+ data + "  uri  "+ data.getData()+ " auth "+ data.getData().getAuthority()+ " path "+ data.getData().getPath());
        String fullerror = "";
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri imageuri = data.getData();
                    InputStream stream = null;
                    String tempID = "", id = "";
                    Uri uri = data.getData();
                    Log.e(TAG, "file auth is " + uri.getAuthority());
                    fullerror = fullerror + "file auth is " + uri.getAuthority();

                    if (imageuri.getAuthority().equals("media")) {
                        tempID = imageuri.toString();
                        tempID = tempID.substring(tempID.lastIndexOf("/") + 1);
                        id = tempID;
                        Uri contenturi = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        String selector = MediaStore.Images.Media._ID + "=?";
                        actualfilepath = getColunmData(contenturi, selector, new String[]{id});

                    } else if (imageuri.getAuthority().equals("com.android.providers.media.documents")) {
                        tempID = DocumentsContract.getDocumentId(imageuri);
                        String[] split = tempID.split(":");
                        String type = split[0];
                        id = split[1];
                        Uri contenturi = null;
                        if (type.equals("image")) {
                            contenturi = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if (type.equals("video")) {
                            contenturi = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if (type.equals("audio")) {
                            contenturi = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }
                        String selector = "_id=?";
                        actualfilepath = getColunmData(contenturi, selector, new String[]{id});

                    } else if (imageuri.getAuthority().equals("com.android.providers.downloads.documents"))
                    {
                        tempID = imageuri.toString();
                        tempID = tempID.substring(tempID.lastIndexOf("/") + 1);
                        id = tempID;
                        Uri contenturi = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        // String selector = MediaStore.Images.Media._ID+"=?";
                        actualfilepath = getColunmData(contenturi, null, null);

                    } else if (imageuri.getAuthority().equals("com.android.externalstorage.documents"))
                    {
                        tempID = DocumentsContract.getDocumentId(imageuri);
                        String[] split = tempID.split(":");
                        String type = split[0];
                        id = split[1];
                        Uri contenturi = null;
                        if (type.equals("primary")) {
                            actualfilepath = Environment.getExternalStorageDirectory() + "/" + id;
                        }
                    }

                    myFile = new File(actualfilepath);
                    // MessageDialog dialog = new MessageDialog(Home.this, " file details --"+actualfilepath+"\n---"+ uri.getPath() );
                    // dialog.displayMessageShow();
                    String temppath = uri.getPath();
                    if (temppath.contains("//"))
                    {
                        temppath = temppath.substring(temppath.indexOf("//") + 1);
                    }
                    Log.e(TAG, " temppath is " + temppath);
                    fullerror = fullerror + "\n" + " file details -  " + actualfilepath + "\n --" + uri.getPath() + "\n--" + temppath;

                    if (actualfilepath.equals("") || actualfilepath.equals(" ")) {
                        myFile = new File(temppath);
                        // Actual File name

                    } else {

                        myFile = new File(actualfilepath);
                    }
                    //File file = new File(actualfilepath);
                    //Log.e(TAG, " actual file path is "+ actualfilepath + "  name ---"+ file.getName());
//                    File myFile = new File(actualfilepath);
                    Log.e(TAG, " myfile is " + myFile.getAbsolutePath());

                    readPdfFile(myFile); // calling pdf fun

                    // file path  - /storage/emulated/0/kolektap/04-06-2018_Admin_1528088466207_file.xls
                } catch (Exception e) {
                    Log.e(TAG, " read errro " + e.toString());
                }
            }
        }
    }


    public void readPdfFile(File myFile)
    {

        String file = myFile.toString();

        if (file.endsWith(".pdf")) {
            pdfView.fromFile(myFile).load();

            Toast.makeText(this, "This is my file" + myFile, Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, "Select PDF file only", Toast.LENGTH_SHORT).show();
        }

    }

    public void onConvert(View view) {

        editText.setText("");
        convertPDf(myFile);

    }

    public void convertPDf(File myFile)
    {

        TextExtractor txt = null;

        for (int i = 0; i<10; i++)
        {
            try{

                PDFDoc doc = new PDFDoc(myFile.toString());

                Page page = doc.getPage(i);

                txt = new TextExtractor();
                txt.begin(page);  // Read the page.
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            String arg ="";

            // Extract words one by one.
            TextExtractor.Word word;
            for (TextExtractor.Line line = txt.getFirstLine(); line.isValid(); line = line.getNextLine())
            {
                for (word = line.getFirstWord(); word.isValid(); word = word.getNextWord())
                {
                    txs = word.getString();
                    editText.append(txs);
                    editText.append("\t");
                }

                editText.append("\n");
            }

        }
    }


    public String getColunmData( Uri uri, String selection, String[] selectarg){
        String filepath ="";
        Cursor cursor = null;
        String colunm = "_data";
        String[] projection = {colunm};
        cursor =  getContentResolver().query( uri, projection, selection, selectarg, null);
        if (cursor!= null){
            cursor.moveToFirst();
            Log.e(TAG, " file path is "+  cursor.getString(cursor.getColumnIndex(colunm)));
            filepath = cursor.getString(cursor.getColumnIndex(colunm));
        }
        if (cursor!= null)
            cursor.close();
        return  filepath;
    }


}


