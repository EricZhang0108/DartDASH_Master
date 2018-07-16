package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.github.gcacace.signaturepad.views.SignaturePad;

/**
 * Allows user to sign or view previously entered signature
 */
public class SignActivity extends AppCompatActivity {

    public static final String NEW_SIGNATURE = "new signature";
    public static final String IMAGE = "image";

    Bitmap signature;


    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("DartDASH");
        }

        // If viewing previously created transaction, display the information and update the UI
        if (getIntent().hasExtra("signature")) {
            byte[] signatureArray = getIntent().getByteArrayExtra("signature");
            signature = BitmapFactory.decodeByteArray(signatureArray,0,signatureArray.length);

            ImageView signatureView = findViewById(R.id.image_view_signature);
            signatureView.setImageBitmap(signature);

            Button confirmButton = findViewById(R.id.confirm_button);
            Button clearButton = findViewById(R.id.clear_button);
            confirmButton.setVisibility(View.INVISIBLE);
            clearButton.setVisibility(View.INVISIBLE);

            com.github.gcacace.signaturepad.views.SignaturePad signaturePad = findViewById(R.id.signature_pad);
            signaturePad.setVisibility(View.INVISIBLE);

            View line = findViewById(R.id.line);
            line.setVisibility(View.INVISIBLE);
        }
        // If creating new transaction, update the UI
        else {
            ImageView signatureView = findViewById(R.id.image_view_signature);
            signatureView.setVisibility(View.INVISIBLE);
        }
    }


    // ********************************* SET UP OPTIONS MENU ************************************ //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirmation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    // **************************** TO PROCEED TO OTHER ACTIVITIES ****************************** //

    // Saves signature when confirm button pressed
    public void confirmSignature(View view) {
        // Get signature from signature pad
        SignaturePad signaturePad = findViewById(R.id.signature_pad);
        Bitmap img = signaturePad.getTransparentSignatureBitmap();

        // Put the image in an intent and broadcast so that ConfirmationActivity will have the image
        Intent intent = new Intent(NEW_SIGNATURE);
        intent.putExtra(IMAGE, img);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        finish();
    }

    // Clears signature when clear button clicked
    public void clearSignature(View view) {
        SignaturePad signaturePad = findViewById(R.id.signature_pad);
        signaturePad.clear();
    }

}
