package com.example.michal.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;

public class MainActivity extends Activity implements SensorEventListener
{
    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter=0;
    boolean isOpen=false;
    boolean NeedToSend=false;
    volatile boolean stopWorker;
    private float deltaX,deltaY,deltaZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private byte[] packet = {0,0,0};
    boolean calibration;

    private TextView xText, yText, zText;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = (Button)findViewById(R.id.open);
        Button closeButton = (Button)findViewById(R.id.close);
        Button mLbutton= (Button)findViewById(R.id.mouseL);
        Button mRbutton= (Button)findViewById(R.id.mouseR);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);
        NumberPicker picker = new NumberPicker(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        } else {
            // fail! we dont have an accelerometer!
        }
        //Picker initialize
        picker.setMinValue(0);
        picker.setMaxValue(2);
        picker.setDisplayedValues( new String[] { "Mariusz-LAPTOK", "France", "United Kingdom" } );

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
        //Left button
        mLbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        packet[2]=(byte) 1;
                        return true;
                    case MotionEvent.ACTION_UP:
                        packet[2]=(byte) 0;
                        return true;
                }
                return false;
            }
        });
        //Right button
        mRbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        packet[2]=(byte) 2;
                        return true;
                    case MotionEvent.ACTION_UP:
                        packet[2]=(byte) 0;
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the change of the x,y,z values of the accelerometer
        if (Math.abs(event.values[0])>0.05) {
            deltaY = -35*event.values[0];
            NeedToSend=true;
        }
        else
        {
            deltaY=0;
            NeedToSend=false;
        }
        if (Math.abs(event.values[2])>0.05) {
            deltaX = -35*event.values[2];
            NeedToSend=true;
        }
        else
        {
            NeedToSend=false;
            deltaX=0;
        }
        if ((isOpen==true && NeedToSend==true) || packet[2]!=0) {
            try
            {
                packet[0]=(byte) deltaX;
                packet[1]=(byte) deltaY;
                mmOutputStream.write(packet);
                myLabel.setText("Data Sent");
            }
            catch (IOException ex) {
                myLabel.setText("Sending error!");
                isOpen=false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("MARIUSZ-LAPTOK"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
        isOpen=true;
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException
    {
        String msg ="aa";// myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(new byte[]{(byte) deltaX, (byte) deltaZ,0});
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
        isOpen=false;
    }

    public void displayCurrentValues() {
        xText.setText(Float.toString(deltaX));
        yText.setText(Float.toString(deltaY));
        zText.setText(Float.toString(deltaZ));
    }

    public void displayCleanValues() {
        xText.setText("0.0");
        yText.setText("0.0");
        zText.setText("0.0");
    }
}