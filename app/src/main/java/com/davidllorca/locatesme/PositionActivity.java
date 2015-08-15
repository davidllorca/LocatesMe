package com.davidllorca.locatesme;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class PositionActivity extends ActionBarActivity implements SensorEventListener, LocationListener {

    // Managers and sensors
    private SensorManager mSensorManager = null;
    private LocationManager mLocationManager = null;
    private Sensor accelerometer = null;
    private Sensor orientation = null;
    private Sensor magnetic = null;
    // TextViews
    private TextView xViewA = null;
    private TextView yViewA = null;
    private TextView zViewA = null;
    private TextView xViewO = null;
    private TextView yViewO = null;
    private TextView zViewO = null;
    private TextView gpsLoc = null;
    private TextView magneticView = null;
    // Variables
    private static final int matrix_size = 16;
    private float[] Rm = new float[matrix_size];
    private float[] outR = new float[matrix_size];
    private float[] I = new float[matrix_size];
    private float[] values = new float[3];
    private float[] mags = new float[3];
    private float[] accels = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        // Get sensor's references
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Location
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            Toast.makeText(this, "Fail in LocationManager", Toast.LENGTH_SHORT).show();
        }
        try {
            // Notify by onLocationChanged() every 30 seconds or 20 meters
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 20, this);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize GPS", Toast.LENGTH_SHORT).show();
        }

        // Magnetic
        magneticView = (TextView) findViewById(R.id.magneticbox);

        // View's references
        xViewA = (TextView) findViewById(R.id.xbox);
        yViewA = (TextView) findViewById(R.id.ybox);
        zViewA = (TextView) findViewById(R.id.zbox);
        xViewO = (TextView) findViewById(R.id.xboxo);
        yViewO = (TextView) findViewById(R.id.yboxo);
        zViewO = (TextView) findViewById(R.id.zboxo);
        gpsLoc = (TextView) findViewById(R.id.gpsbox);
    }

    @Override
    public void onLocationChanged(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        gpsLoc.setText("Provider: " + location.getProvider() + ": lat=" + lat + " lon= " + lon);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, provider + " enabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, provider + " disabled", Toast.LENGTH_SHORT).show();
        if("GPS".equalsIgnoreCase(provider)){
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 20, this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                xViewA.setText("Acceleration X: " + event.values[0]);
                yViewA.setText("Acceleration Y: " + event.values[1]);
                zViewA.setText("Acceleration Z: " + event.values[2]);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                magneticView.setText(String.format("x: %f\ny: %f\nz: %f", new Object[]{event.values[0], event.values[1], event.values[2]}));
                break;
        }
        
        if (mags != null && accels != null) {
            SensorManager.getRotationMatrix(Rm, I, accels, mags);
            // Correct screen if that is landscape
            SensorManager.remapCoordinateSystem(Rm, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, values);
            xViewO.setText("Orientation X: " + values[0]);
            yViewO.setText("Orientation Y: " + values[1]);
            zViewO.setText("Orientation Z: " + values[2]);
            // Default values in Radians. Use Convert.radToDeg(values[0]) for degrees
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_position, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
