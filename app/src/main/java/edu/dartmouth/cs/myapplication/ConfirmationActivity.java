package edu.dartmouth.cs.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * For creation of new Transactions
 */
public class ConfirmationActivity extends AppCompatActivity {

    private Bitmap signature;
    Button signButton;
    byte[] signatureArray;
    boolean ending = false;

    // For broadcasting new transaction and deleting current transaction
    public static final String NEW_TRANSACTION = "new transaction";
    public static final String NEW_NAME = "new name";
    public static final String NEW_DASH = "new dash";
    public static final String NEW_AMOUNT = "new amount";
    public static final String NEW_SIGNATURE = "new signature";
    public static final String DELETE_TRANSACTION = "delete transaction";
    public static final String DELETE_NAME = "delete name";
    public static final String DELETE_DASH = "delete dash";
    public static final String DELETE_AMOUNT = "delete amount";


    // Receives broadcasts from SignActivity containing new signatures
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SignActivity.NEW_SIGNATURE)) {
                Bitmap newSignature = (Bitmap) intent.getParcelableExtra(SignActivity.IMAGE);
                if (newSignature != null) {
                    signature = newSignature;
                    signButton.setEnabled(false);
                    if (!ending) {
                        ending = true;
                        toWaitTest();
                    }
                }
            }
        }
    };

    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Set up the action bar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("DartDASH");
        }

        signButton = findViewById(R.id.signature_button);

        // Register the broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(SignActivity.NEW_SIGNATURE));

        final EditText amountField = findViewById(R.id.confirmation_trans_amount);

        // If viewing previously created transaction, set the information in the EditTexts and disable them
        if (getIntent().hasExtra("name")) {
            EditText nameField = findViewById(R.id.confirmation_trans_name);
            nameField.setText(getIntent().getStringExtra("name"));
            nameField.setEnabled(false);

            EditText dashField = findViewById(R.id.confirmation_trans_dash);
            dashField.setText(Long.toString(getIntent().getLongExtra("dash",0)));
            dashField.setEnabled(false);

            NumberFormat formatter = new DecimalFormat("#0.00");
            amountField.setText("$" + formatter.format(getIntent().getDoubleExtra("amount",0)));
            amountField.setEnabled(false);

            signatureArray = getIntent().getByteArrayExtra("signature");

            Button signButton = findViewById(R.id.signature_button);
            signButton.setText("View Signature");

        // If generated from OCR
        }
        else if(getIntent().hasExtra("ocrName")) {

            //get values from OCR
            EditText nameField = findViewById(R.id.confirmation_trans_name);
            String editedName = getIntent().getStringExtra("ocrName").replace(",", "");
            nameField.setText(editedName);

            EditText dashField = findViewById(R.id.confirmation_trans_dash);
            dashField.setText(Long.toString(getIntent().getLongExtra("ocrDash",0)));
        }

        // Make sure input amount is correctly formatted
        amountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            // Makes sure there is a "$" in the EditText
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    if (!s.toString().substring(0, 1).equals("$")) {
                        String newString = "$" + s.toString();
                        amountField.setText(newString);
                        amountField.setSelection(newString.length());
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }


    // ********************************* SET UP OPTIONS MENU ************************************ //

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Only show delete button if viewing previously created transaction
        if (!getIntent().hasExtra("name")) {
            menu.findItem(R.id.delete_transaction).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

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
        else if (id == R.id.delete_transaction) {
            // If viewing previously created transaction, get its information and send to EventActivity
            if (getIntent().hasExtra("name")) {
                Intent intent = new Intent(DELETE_TRANSACTION);
                intent.putExtra(DELETE_NAME, getIntent().getStringExtra("name"));
                intent.putExtra(DELETE_DASH, getIntent().getLongExtra("dash",0));
                intent.putExtra(DELETE_AMOUNT, getIntent().getDoubleExtra("amount",0));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    // **************************** TO PROCEED TO OTHER ACTIVITIES ****************************** //

    // Saves transactions and sends UI to WaitActivity
    public void toWaitTest() {
        // Get the new transaction's information

        String name = ((TextView)findViewById(R.id.confirmation_trans_name)).getText().toString();

        Long dash = Long.parseLong(((TextView)findViewById(R.id.confirmation_trans_dash)).getText().toString());

        Double amount;
        String originalAmount = ((TextView)findViewById(R.id.confirmation_trans_amount)).getText().toString();
        String correctedAmount = originalAmount.substring(1);
        try {
            amount = Double.parseDouble(correctedAmount);
        }
        catch (Exception e) {
            amount = (double)-1;
        }

        // If the information is in valid format, send the broadcast
        if (Long.toString(dash).length() == 9 && amount != -1 && originalAmount.substring(0,1).equals("$")) {
            // Put the information in an intent and broadcast so that EventActivity will have it
            Intent intent = new Intent(NEW_TRANSACTION);
            intent.putExtra(NEW_NAME, name);
            intent.putExtra(NEW_DASH, dash);
            intent.putExtra(NEW_AMOUNT, amount);
            intent.putExtra(NEW_SIGNATURE, signature);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // Start WaitActivity and finish
            Intent intent2 = new Intent(ConfirmationActivity.this, WaitActivity.class);
            startActivity(intent2);
            finish();
        }
        // Otherwise let the user know there is a problem
        else {
            Toast.makeText(ConfirmationActivity.this, "Information incorrectly formatted!", Toast.LENGTH_SHORT).show();
        }

    }

    // Brings up SignActivity for the user to input signature
    public void toSign(View view) {
        // If creating new transaction
        if (!getIntent().hasExtra("name")) {
            EditText nameInput = findViewById(R.id.confirmation_trans_name);
            EditText dashInput = findViewById(R.id.confirmation_trans_dash);
            EditText amountInput = findViewById(R.id.confirmation_trans_amount);

            // See is dash is formatted correctly
            boolean dashOkay = false;
            if (dashInput.getText().toString().length() == 9) {
                dashOkay = true;
                try {
                    Long.parseLong(dashInput.getText().toString());
                } catch (Exception e) {
                    dashOkay = false;
                }
            }

            // See if valid amount given
            Double testAmount = (double) -1;
            try {
                testAmount = Double.parseDouble(amountInput.getText().toString().substring(1));
            } catch (Exception e) {
            }

            // If name given and dash and amount valid, proceed
            if (!nameInput.getText().toString().equals("") && dashOkay && testAmount > 0) {
                Intent intent = new Intent(ConfirmationActivity.this, SignActivity.class);
                startActivity(intent);
            }
            // Otherwise let the user know what's wrong
            else {
                if (nameInput.getText().toString().equals("")) {
                    nameInput.setError("Please enter a name!");
                }
                if (!dashOkay) {
                    dashInput.setError("Invalid dash number!");
                }
                if (testAmount <= 0) {
                    amountInput.setError("Invalid amount!");
                }
            }
        }

        // If viewing previously created transaction
        else {
            Intent intent = new Intent(ConfirmationActivity.this, SignActivity.class);
            intent.putExtra("signature", signatureArray);
            startActivity(intent);
        }
    }

}
