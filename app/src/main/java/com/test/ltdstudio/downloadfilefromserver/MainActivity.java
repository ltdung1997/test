package com.test.ltdstudio.downloadfilefromserver;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    public static final int PROGRESS_BAR_TYPE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case PROGRESS_BAR_TYPE:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file. Please wait...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, Boolean> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(PROGRESS_BAR_TYPE);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected Boolean doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                File rootPath = new File(Environment.getExternalStorageDirectory()+"/download");
                if(!rootPath.exists()){
                    rootPath.mkdir();
                }

                final File localFile = new File(rootPath, "update.apk");
                if(localFile.exists()){
                    localFile.delete();
                }


                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(localFile.toString());

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

                return true;
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return false;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // dismiss the dialog after the file was downloaded
            dismissDialog(PROGRESS_BAR_TYPE);
            if(result){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(FileProvider.getUriForFile(
                        MainActivity.this
                        , getApplicationContext().getPackageName() + ".my.package.name.provider"
                        , new File(Environment.getExternalStorageDirectory() + "/download/" + "update.apk"))
                        , "application/vnd.android.package-archive");

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                Log.e("firebase ",";local tem file not created ");
            }
        }
    }
}
