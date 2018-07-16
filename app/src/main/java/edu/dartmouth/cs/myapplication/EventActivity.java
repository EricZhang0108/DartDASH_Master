package edu.dartmouth.cs.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs.myapplication.Adapters.DartDASHPagerAdapter;
import edu.dartmouth.cs.myapplication.Fragments.SummaryFragment;
import edu.dartmouth.cs.myapplication.Fragments.TransactionHistoryFragment;
import edu.dartmouth.cs.myapplication.Model.DartDASHDataSource;
import edu.dartmouth.cs.myapplication.Model.Event;
import edu.dartmouth.cs.myapplication.Model.EventsLoader;
import edu.dartmouth.cs.myapplication.Model.Preferences;
import edu.dartmouth.cs.myapplication.Model.Transaction;
import edu.dartmouth.cs.myapplication.Tasks.EventTask;
import edu.dartmouth.cs.myapplication.Tasks.EventTaskInterface;

/**
 * Displays an Event's information, has SummaryFragment and TransactionHistoryFragment
 */
public class EventActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Event>>,
        BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = EventActivity.class.getSimpleName();

    private static final int LOAD_CURRENT_EVENT_ID = 1;
    private static final int TAKE_PHOTO = 2;

    private DartDASHPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;

    private String name;
    private String date;
    private Double amount = (double)0;
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private String currIdNumber;
    private Event currentEvent;

    //OCR variables
    private FirebaseVisionImage image;
    private String imageName;
    private long imageDash;
    private Boolean nameSuccess = false;
    private Boolean dashSuccess = false;
    private Uri mImageUri;

    // Receives broadcasts from ConfirmationActivity with new Transactions
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConfirmationActivity.NEW_TRANSACTION)) {
                // Get the information from the intent
                String name = intent.getStringExtra(ConfirmationActivity.NEW_NAME);
                long dash = intent.getLongExtra(ConfirmationActivity.NEW_DASH, -1);
                double trans_amount = intent.getDoubleExtra(ConfirmationActivity.NEW_AMOUNT, -1);
                Bitmap signature = intent.getParcelableExtra(ConfirmationActivity.NEW_SIGNATURE);

                // If all the information is there
                if (name != null && dash != -1 && trans_amount != -1) {
                    // Add the new transaction to the transaction list
                    Transaction transaction = new Transaction(name, dash, trans_amount, false, false, signature);

                    boolean shouldAdd = true;
                    for (Transaction existingTransaction : transactions) {
                        if (existingTransaction.getName().equals(name) && existingTransaction.getDash() == dash &&
                                existingTransaction.getAmount() == trans_amount) {
                            Toast.makeText(getApplicationContext(), "Transaction already exists!",
                                    Toast.LENGTH_SHORT).show();
                            shouldAdd = false;
                        }
                    }

                    if (shouldAdd) {
                        transactions.add(transaction);
                        amount += transaction.getAmount();
                        Log.d(TAG, transactions.toString());
                        Toast.makeText(EventActivity.this, "New transaction added!", Toast.LENGTH_SHORT).show();


//                        // If viewing previously-created event
//                        if (getIntent().hasExtra("ID")) {
//                            // Reset transactions and set to unsynced
//                            Event toEdit = currentEvent;
//                            toEdit.setTransactions(transactions);
//                            toEdit.setSynced(false);
//
//                            // Update the exercise in SQLite
//                            final DartDASHDataSource dataSource= new DartDASHDataSource(EventActivity.this);
//                            EventTask eventTask=new EventTask(dataSource, 3, new EventTaskInterface() {
//                                @Override
//                                public void onTaskComplete(ArrayList<Event> toReturn) {
//                                    dataSource.close();
//                                }
//                            });
//                            eventTask.execute(toEdit);
//                        }

                    }
                    ((TransactionHistoryFragment) mSectionsPagerAdapter.getItem(1)).refresh(transactions);
                }
                // Otherwise tell the user something went wrong
                else {
                    Toast.makeText(EventActivity.this, "Transaction unable to be added!", Toast.LENGTH_SHORT).show();
                }
            }

            else if (intent.getAction().equals(ConfirmationActivity.DELETE_TRANSACTION)) {
                // Get the information from the intent
                String name = intent.getStringExtra(ConfirmationActivity.DELETE_NAME);
                long dash = intent.getLongExtra(ConfirmationActivity.DELETE_DASH, -1);
                double trans_amount = intent.getDoubleExtra(ConfirmationActivity.DELETE_AMOUNT, -1);

                // If all the information is there
                if (name != null && dash != -1 && trans_amount != -1) {
                    // Delete the transaction from the list

                    ArrayList<Transaction> toRemove = new ArrayList<Transaction>();

                    for (Transaction transaction : transactions) {
                        if (transaction.getName().equals(name) && transaction.getDash() == dash &&
                                transaction.getAmount() == trans_amount) {
                            toRemove.add(transaction);
                        }
                    }

                    for (Transaction transaction : toRemove) {
                        transactions.remove(transaction);
                    }

                    // Decrement the total amount for the event
                    amount -= trans_amount;
                    Toast.makeText(EventActivity.this, "Transaction deleted!", Toast.LENGTH_SHORT).show();
                    ((TransactionHistoryFragment)mSectionsPagerAdapter.getItem(1)).refresh(transactions);

//                    // If viewing previously-created event
//                    if (getIntent().hasExtra("ID")) {
//                        // Reset transactions and set to unsynced
//                        Event toEdit = currentEvent;
//                        toEdit.setTransactions(transactions);
//                        toEdit.setSynced(false);
//
//                        // Update the exercise in SQLite
//                        final DartDASHDataSource dataSource= new DartDASHDataSource(EventActivity.this);
//                        EventTask eventTask=new EventTask(dataSource, 3, new EventTaskInterface() {
//                            @Override
//                            public void onTaskComplete(ArrayList<Event> toReturn) {
//                                dataSource.close();
//                            }
//                        });
//                        eventTask.execute(toEdit);
//                    }

                }
                // Otherwise tell the user something went wrong
                else {
                    Toast.makeText(EventActivity.this, "Unable to delete transaction!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure no fragments are saved when screen orientation changes
        Bundle newBundle = new Bundle();
        super.onCreate(newBundle);
        setContentView(R.layout.activity_event);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create fragments
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new SummaryFragment());
        fragments.add(new TransactionHistoryFragment());

        // Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter = new DartDASHPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set the onClickListener for the manual input button and the camera ocr button
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventActivity.this, ConfirmationActivity.class);
                startActivity(intent);
            }
        });
        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        // Bring buttons to front
        addButton.bringToFront();
        cameraButton.bringToFront();

        // If viewing a previously-created event, load and display its information
        if (getIntent().hasExtra("ID")) {
            currIdNumber = getIntent().getStringExtra("ID");
            // Start the LoaderManager to get the event information
            LoaderManager mLoaderManager = getSupportLoaderManager();
            mLoaderManager.restartLoader(LOAD_CURRENT_EVENT_ID, null, this).forceLoad();
        }
        // Otherwise load the name and date of the new event being created
        else {
            name = getIntent().getStringExtra("NAME");                  // NEED TO CHANGE THESE HARD-CODED STRINGS TO VARIABLES
            date = getIntent().getStringExtra("DATE");
        }

        // Register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConfirmationActivity.NEW_TRANSACTION);
        intentFilter.addAction(ConfirmationActivity.DELETE_TRANSACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                intentFilter);

        // If viewing previously created event, adjust the UI
        if (getIntent().hasExtra("ID")) {
            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("DartDASH");
            }
//            addButton.setVisibility(View.INVISIBLE);
//            cameraButton.setVisibility(View.INVISIBLE);
        }

//        // If there are transactions that were saved in savedInstanceState, update TransactionHistoryFragment with them
//        if (savedInstanceState != null) {
//            transactions = new ArrayList<Transaction>();
//            // For each saved transactions
//            for (int i = 0; i <= savedInstanceState.getInt("number of transactions"); i++) {
//                // Get its information
//                String name = savedInstanceState.getString(Integer.toString(i) + "name");
//                long dash = savedInstanceState.getLong(Integer.toString(i) + "dash");
//                double amount = savedInstanceState.getDouble(Integer.toString(i) + "amount");
//                boolean synced = savedInstanceState.getBoolean(Integer.toString(i) + "synced");
//                boolean deleted = savedInstanceState.getBoolean(Integer.toString(i) + "deleted");
//
//                byte[] signatureArray = savedInstanceState.getByteArray(Integer.toString(i) + "signature");
//                Bitmap signature = BitmapFactory.decodeByteArray(signatureArray,0,signatureArray.length);
//
//                // Make the transaction and add it to the list
//                Transaction toAdd = new Transaction(name, dash, amount, synced, deleted, signature);
//                transactions.add(toAdd);
//            }
//            // Refresh ListView in TransactionHistoryFragment
//            ((TransactionHistoryFragment) mSectionsPagerAdapter.getItem(1)).refresh(transactions);
//        }

        // Check permissions (writing to external storage and taking pictures)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA},0);
        }

        // Set the listener on the BottomNavigationView
        mBottomNavigationView = findViewById(R.id.bottom_nav_bar_event);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        // When the fragment changes, change the navigation button that is highlighted
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // If creating a new event, add the event to the list of ongoing events
        if (!getIntent().hasExtra("ID")) {
            // Get the email
            Preferences preferences = new Preferences(EventActivity.this);
            String email = preferences.getEmail();
            // Get reference to event's new location
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("ongoing").child(name);
            // Add the date and email
            mDatabase.child("date").setValue(date);
            mDatabase.child("email").setValue(email);
        }

    }

    @Override
    // Switch the fragment being viewed when other fragment selected in navigation bar
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_summary:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.navigation_history_transactions:
                mViewPager.setCurrentItem(1);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // Remove event from ongoing events in Firebase if still there
        removeFromOngoing();
    }


    // ********************************* SET UP OPTIONS MENU ************************************ //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    // Changes the "Save" button to "Delete" and "Save and Export" to "Export" when viewing an already-created event
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If viewing already created event, make the necessary changes
        if (getIntent().hasExtra("ID")) {
            menu.findItem(R.id.action_save_and_export).setTitle("Export and Email");
            menu.findItem(R.id.action_cancel).setVisible(false);
        }
        // Only show delete button if viewing previously created event
        else {
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_make_ongoing).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            removeFromOngoing();
            finish();
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(EventActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_save) {
            // If created a new event
            if (!getIntent().hasExtra("ID")) {
                // Save the new event
                saveEvent(false);
            }
            // If viewing prevously created event
            else {
                // If viewing previously-created event
                if (getIntent().hasExtra("ID")) {
                    // Reset transactions and set to unsynced
                    Event toEdit = currentEvent;
                    toEdit.setTransactions(transactions);
                    toEdit.setSynced(false);

                    // Update the exercise in SQLite
                    final DartDASHDataSource dataSource= new DartDASHDataSource(EventActivity.this);
                    EventTask eventTask=new EventTask(dataSource, 3, new EventTaskInterface() {
                        @Override
                        public void onTaskComplete(ArrayList<Event> toReturn) {
                            dataSource.close();
                            Toast.makeText(getApplicationContext(), "Edits saved!", Toast.LENGTH_SHORT).show();
                            removeFromOngoing();
                            finish();
                        }
                    });
                    eventTask.execute(toEdit);
                }
            }
        }
        else if (id == R.id.action_delete) {
            // Set the newly uploaded exercise to not synced
            Event toSetSynced = new Event();

            toSetSynced.setId(currentEvent.getId());
            toSetSynced.setTransactions(currentEvent.getTransactions());
            toSetSynced.setName(currentEvent.getName());
            toSetSynced.setDate(currentEvent.getDate());
            toSetSynced.setAmount(currentEvent.getAmount());
            toSetSynced.setSynced(false);
            toSetSynced.setDeleted(true);

            // Update event
            final DartDASHDataSource dataSource= new DartDASHDataSource(EventActivity.this);
            EventTask eventTask=new EventTask(dataSource, 3, new EventTaskInterface() {
                @Override
                public void onTaskComplete(ArrayList<Event> toReturn) {
                    dataSource.close();
                    Toast.makeText(EventActivity.this, "Event deleted!", Toast.LENGTH_SHORT).show();
                    removeFromOngoing();
                    EventActivity.this.finish();
                }
            });
            eventTask.execute(toSetSynced);
        }
        else if (id == R.id.action_make_ongoing) {
            // Get the email
            Preferences preferences = new Preferences(EventActivity.this);
            String email = preferences.getEmail();
            // Get reference to event's new location
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("ongoing").child(currentEvent.getName());
            // Add the date and email
            mDatabase.child("date").setValue(currentEvent.getDate());
            mDatabase.child("email").setValue(email);
        }
        else if (id == R.id.action_save_and_export) {
            // If created a new event
            if (!getIntent().hasExtra("ID")) {
                // Save and export the new event
                saveEvent(true);
            }
            // If exporting previously saved event
            else {
                // Just export the event
                ExportTask exportTask = new ExportTask();
                exportTask.execute(currentEvent);
                removeFromOngoing();
                finish();
            }
        }
        else if (id == R.id.action_cancel) {
            // If created a new event
            if (!getIntent().hasExtra("ID")) {
                Toast.makeText(EventActivity.this, "Event cancelled!", Toast.LENGTH_SHORT).show();
            }
            removeFromOngoing();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If creating new event, don't allow the back button to be pressed (could delete data)
        if (getIntent().hasExtra("ID")) {
            super.onBackPressed();
        }
    }


    // *********************************** PICTURE TAKING *************************************** //

    // Launch intent to take picture
    public void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        ContentValues values = new ContentValues(1);

        //get camera intent
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,mImageUri);
        try{
            startActivityForResult(intent, TAKE_PHOTO);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Fail to save photo!", Toast.LENGTH_LONG).show();
        }
    }

    // Process result of taking picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TAKE_PHOTO && resultCode == RESULT_OK){
            Bitmap mBitmap = null;
            try {
                //get the picture
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //analyze picture using MLKit from Firebase
            image = FirebaseVisionImage.fromBitmap(mBitmap);
            FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
            Task<FirebaseVisionText> result =
                    detector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    // ...
                                    for (FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {
                                        Rect boundingBox = block.getBoundingBox();
                                        Point[] cornerPoints = block.getCornerPoints();

                                        //look at each line from each block of text
                                        for (FirebaseVisionText.Line line: block.getLines()) {
                                            // ...

                                            //check if the text is the name(has comma) or the DASH number(has 9 characters and "1" in it)
                                            String text = line.getText();
                                            Log.d("OCR", text);
                                            if(text.length() == 9 && text.contains("1")){
                                                Log.d("OCR", text + " wow DASH!");
                                                imageDash = Long.parseLong(text);
                                                dashSuccess = true;
                                            }

                                            if(text.contains(",")){
                                                Log.d("OCR", text + " wow name!");
                                                imageName = text;
                                                nameSuccess = true;
                                            }

                                            for (FirebaseVisionText.Element element: line.getElements()) {
                                                // ...
                                            }
                                        }
                                    }

                                    //if found both name and DASH, go to the confirmation activity with the info
                                    if(nameSuccess && dashSuccess){
                                        Intent intent = new Intent(EventActivity.this, ConfirmationActivity.class);
                                        intent.putExtra("ocrName", imageName);
                                        intent.putExtra("ocrDash", imageDash);
                                        startActivity(intent);

                                    //tell users that it failed
                                    }else{
                                        Toast.makeText(getApplicationContext(),
                                                "Please retake picture! Maybe try using a lower angle.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
        }
    }


    // **************************************** OTHER ******************************************* //

    // Saves event (and exports if necessary)
    public void saveEvent(final boolean shouldExport) {
        // Create new Event
        Event event = new Event();

        // Set its information
        if (transactions != null) {
            event.setTransactions(transactions);
        }
        else {
            event.setTransactions(new ArrayList<Transaction>());
        }
        event.setName(name);
        event.setDate(date);
        if (transactions != null) {
            event.setAmount(amount);
        }
        else {
            event.setAmount((double)0);
        }
        event.setSynced(false);
        event.setDeleted(false);

        // Save the new Event, and export it if required
        final Event toExport = event;
        final DartDASHDataSource dataSource= new DartDASHDataSource(EventActivity.this);
        EventTask eventTask=new EventTask(dataSource, 1, new EventTaskInterface() {
            @Override
            public void onTaskComplete(ArrayList<Event> toReturn) {
                dataSource.close();
                Toast.makeText(EventActivity.this, "Event saved!", Toast.LENGTH_SHORT).show();
                if (shouldExport) {
                    // Just export the event
                    ExportTask exportTask = new ExportTask();
                    exportTask.execute(toExport);
                }
                removeFromOngoing();
                finish();
            }
        });
        eventTask.execute(event);
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public int getTransactionsLength() {
        if (transactions != null) {
            return transactions.size();
        }
        else {
            return 0;
        }
    }

    public double getAmount() {
        if (amount != null) {
            return amount;
        }
        else {
            return (double)0;
        }
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    // Task for exporting Event
    class ExportTask extends AsyncTask<Event, Event, Boolean> {

        @Override
        protected Boolean doInBackground(Event... events) {
            return export(events[0]);
        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            super.onPostExecute(succeeded);
            if (succeeded) {
                Toast.makeText(getApplicationContext(), "Exported to downloads!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Could not export!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Exports to excel file
    public synchronized boolean export(Event event) {
        // Get information
        String currName = event.getName();
        String currDate = event.getDate();
        double currAmount = event.getAmount();
        ArrayList<Transaction> currTransactions = event.getTransactions();

        try {
            // Make the workbook
            HSSFWorkbook workbook = new HSSFWorkbook();
            // Make a sheet in the workbook
            HSSFSheet sheet = workbook.createSheet("Sheet 1");

            // Make title row
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue(currName);
            cell = row.createCell(1);
            cell.setCellValue(currDate);
            cell = row.createCell(2);
            NumberFormat formatter = new DecimalFormat("#0.00");
            cell.setCellValue("$" + formatter.format(currAmount));

            // Make headings bold
            HSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            HSSFCellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);
            for (int i = 0; i <=2; i++) {
                row.getCell(i).setCellStyle(boldStyle);
            }

            // Make transaction heading row
            row = sheet.createRow(1);
            cell = row.createCell(0);
            cell.setCellValue("Name");
            cell = row.createCell(1);
            cell.setCellValue("Dash");
            cell = row.createCell(2);
            cell.setCellValue("Amount");

            for (int i = 0; i <=2; i++) {
                row.getCell(i).setCellStyle(boldStyle);
            }

            // For each transaction, add it to file
            int currentRow = 2;
            for (Transaction transaction : currTransactions) {
                row = sheet.createRow(currentRow);
                cell = row.createCell(0);
                cell.setCellValue(transaction.getName());
                cell = row.createCell(1);
                cell.setCellValue(transaction.getDash());
                cell = row.createCell(2);
                cell.setCellValue("$" + formatter.format(transaction.getAmount()));
                cell = row.createCell(3);

                // If user wants to export signatures, export the signatures to excel
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EventActivity.this);
                boolean exportSignatures = prefs.getBoolean("signature_switch", false);
                if (exportSignatures) {
                    String signatureString = "";
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if (transaction.getSignature() != null) {
                        transaction.getSignature().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        signatureString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                    }
                    cell.setCellValue(signatureString);
                }

                currentRow ++;
            }

            // Make file
            String date = event.getDate();
            String[] dateParts = date.split("/");
            String eventId = event.getName() + dateParts[0] + dateParts[1] + dateParts[2];

            File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + eventId + ".xls");
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();

            // Write file
            OutputStream outputStream = new FileOutputStream(newFile, false);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
//            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"ericzhang0108@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "DartDASH Event Transactions");
            intent.putExtra(Intent.EXTRA_TEXT, "Attached is your event's transactions. Thank you for using DartDASH!");
            if (!newFile.exists() || !newFile.canRead()) {
                Toast.makeText(this, "Fail to send out email!", Toast.LENGTH_LONG).show();
            }
            else {
                Uri imageUri = FileProvider.getUriForFile(
                        EventActivity.this,
                        "edu.dartmouth.cs.myapplication.provider", newFile);
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                try {
                    startActivity(Intent.createChooser(intent, "Send email..."));
                }
                catch (Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(getApplicationContext(), "No email clients found!", Toast.LENGTH_SHORT).show();
                }
            }
            // Successfully exported
            return true;
        }

        catch (Exception e) {
            // Couldn't export
            e.printStackTrace();
            return false;
        }

    }

    public void removeFromOngoing() {
        // Get reference to event's location
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("ongoing");
        // Remove the event from the ongoing list
        if (name == null) {
            mDatabase.getRef().child(currentEvent.getName()).removeValue();
        }
        else {
            mDatabase.getRef().child(name).removeValue();
        }
    }


    // *********************************** LOADER METHODS *************************************** //

    @NonNull
    @Override
    // Creates an EventsLoader
    public Loader<List<Event>> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == LOAD_CURRENT_EVENT_ID) {
            return new EventsLoader(EventActivity.this, false, currIdNumber);
        }
        return null;
    }

    @Override
    // Puts the information from the desired event into the TextViews.
    public void onLoadFinished(@NonNull Loader<List<Event>> loader, List<Event> events) {
        if(loader.getId() == LOAD_CURRENT_EVENT_ID){
            if (events.size() > 0) {

                // Go to the one event entry in the list
                Event event = events.get(0);
                currentEvent = event;

                name = currentEvent.getName();
                date = currentEvent.getDate();
                amount = currentEvent.getAmount();
                transactions = currentEvent.getTransactions();

                ((SummaryFragment)mSectionsPagerAdapter.getItem(0)).refresh();
                ((TransactionHistoryFragment)mSectionsPagerAdapter.getItem(1)).refresh(transactions);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Event>> loader) {
        // Required to implement LoaderManager, but not necessary to do anything for functionality
        // of the app.
    }

}
