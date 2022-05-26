package com.example.writetosharedtests;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity {

    private SambaFileHandler mFileHandler;

    TextView lblDebugText;
    Button btnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFileHandler= new SambaFileHandler();

        lblDebugText = findViewById(R.id.lblDebugText);
        btnAction = findViewById(R.id.btnAction);

    }

    public void ONbtnClick(View view) throws MalformedURLException {

        new AsyncFileWriting().execute();

        lblDebugText.setText("You pressed the button");
    }

    private class AsyncFileWriting extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Thread.currentThread().setName("AsyncFileWriting");
            try {
                mFileHandler.SetCredentials(mFileHandler.ANONYMOUS_CREDENTIALS);
                /**
                if (mFileHandler.SavetoSharedFolder("hello world".getBytes(), "Androidtest.txt", "test Folder 2")) {
                    Log.d("SUCCESS Log", "\t" + "File Created Successfully");
                } else {
                    Log.d("ERROR Log", "\t" + "Could NOT create file");
                }*/
                mFileHandler.FindDirectories();

            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            lblDebugText.setText("Post execution");
        }


    }

}