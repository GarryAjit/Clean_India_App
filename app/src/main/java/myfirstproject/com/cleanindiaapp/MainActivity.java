package myfirstproject.com.cleanindiaapp;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    ImageView im1;
    Button btnTakePhoto , btnSetWallpaper , btnSavePhoto , btnShare;
    TextView tvAddress;

    Bitmap photo;
    GoogleApiClient mLocationClient;
    Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        im1 = (ImageView) findViewById(R.id.im1);
        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        btnSetWallpaper = (Button) findViewById(R.id.btnSetWallpaper);
        btnSavePhoto = (Button) findViewById(R.id.btnSavePhoto);
        btnShare = (Button) findViewById(R.id.btnShare);
        tvAddress = (TextView) findViewById(R.id.tvAddress);

        mLocationClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();



        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,1234);

            }
        });

        btnSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                try
                {
                    wallpaperManager.setBitmap(photo);
                    Toast.makeText(MainActivity.this, "Wallpaper Set Successfully", Toast.LENGTH_SHORT).show();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        btnSavePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = Environment.getExternalStorageState();

                if(Environment.MEDIA_MOUNTED.equalsIgnoreCase(state))
                {
                    File root = Environment.getExternalStorageDirectory();
                    File dir = new File(root + "/ImageSaveTest");
                    if(!dir.exists())
                        dir.mkdir();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                    Date date = new Date();
                    String fname = sdf.format(date) + ".jpg";

                    File file = new File(dir ,fname);

                    try
                    {
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        photo.compress(Bitmap.CompressFormat.PNG , 100 , bos);
                        Toast.makeText(MainActivity.this, "File saved successfully", Toast.LENGTH_SHORT).show();
                        bos.flush();
                        bos.close();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "File could not be saved", Toast.LENGTH_SHORT).show();
                    }
                }

                else
                {
                    Toast.makeText(MainActivity.this, "External storage issue", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    File file = new File(getExternalCacheDir() , "my_image.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.PNG , 100 , fOut);
                    fOut.flush();
                    fOut.close();

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    String shareBody = tvAddress.getText().toString();
                    sharingIntent.putExtra(Intent.EXTRA_STREAM , Uri.fromFile(file));
                    sharingIntent.putExtra(Intent.EXTRA_TEXT , shareBody);
                    sharingIntent.setType("image/png");
                    startActivity(sharingIntent);
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mLocationClient !=null)
        {
            mLocationClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if(mLastLocation!=null)
        {
            Toast.makeText(this, "Latitude: " + mLastLocation.getLatitude() + " ,Longitude: " + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            getMyLocationAddress(mLastLocation.getLatitude() , mLastLocation.getLongitude());
        }

    }

    public void getMyLocationAddress(double latitude , double longitude)
    {
        Geocoder geocoder = new Geocoder(this , Locale.ENGLISH);
        try
        {
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude , longitude , 1);

            if(addresses!=null)
            {
                android.location.Address fetchedAddress = addresses.get(0);
                tvAddress.setText("This photo was taken at: \n" +
                fetchedAddress.getFeatureName() + ", " +
                fetchedAddress.getSubLocality() + ", " +
                fetchedAddress.getLocality() + ", " +
                fetchedAddress.getPostalCode() + ", " +
                fetchedAddress.getAdminArea() + ", " +
                fetchedAddress.getCountryName());
            }
            else
            {
                tvAddress.setText("No location found!");
            }
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Could not get address!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if(requestCode == 1234)
            {
                photo = (Bitmap) data.getExtras().get("data");
                im1.setImageBitmap(photo);

                btnShare.setEnabled(true);
                btnSetWallpaper.setEnabled(true);
                btnSavePhoto.setEnabled(true);
            }
        }
    }
}
