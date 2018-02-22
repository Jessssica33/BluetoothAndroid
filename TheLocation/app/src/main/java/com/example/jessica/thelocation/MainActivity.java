package com.example.jessica.thelocation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    TextView mLatitude;
    TextView mLongitude;

    String latitude;
    String longitude;

    BluetoothClient mBluetoothClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);

        mBluetoothClient = new BluetoothClient(mHandler);
        mBluetoothClient.init();
        //mBluetoothServer.read();

    }

    private void setPosition(String msg) {

        String[] pos = msg.split(" ");
        mLatitude.setText(pos[0]);
        mLongitude.setText(pos[1]);
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    setPosition(readMessage);
                    break;
            }
        }
    };


}
