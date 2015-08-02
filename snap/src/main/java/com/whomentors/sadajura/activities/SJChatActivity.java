package com.whomentors.sadajura.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.whomentors.sadajura.chat.custom.CustomActivity;
import com.whomentors.sadajura.chat.model.Conversation;
import com.whomentors.sadajura.chat.utils.Const;
import com.whomentors.sadajura.data.ParseConstants;
import com.whomentors.sadajura.ui.dialog.SJDialogBuilder;
import com.whomentors.sarajura.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Michael Yoon Huh on 8/1/2015.
 */

/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */
public class SJChatActivity extends CustomActivity {

    /** SJChat Additions **/

    // CAMERA DIALOG VARIABLES:
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;
    public static final int MEDIA_TYPE_IMAGE = 5;
    public static final int MEDIA_TYPE_VIDEO = 6;
    public static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10 MB
    protected Uri mMediaUri;
    protected Uri mOutputUri;

    // LOGGING VARIABLES
    private static final String LOG_TAG = SJChatActivity.class.getSimpleName();

    // PARSE VARIABLES
    public static ParseUser user;

    // getIntentBundle(): Retrieves the data from the previous activity.
    private void getIntentBundle() {

        Bundle extras = getIntent().getExtras();

        // Tries to retrieve the additional information from the bundle.
        if (extras != null) {

            // Sets the friend name.
            friend = getIntent().getExtras().getString("selectedUsername");
            Log.d(LOG_TAG, "getIntentBundle(): Friend name is: " + friend);

            getActionBar().setTitle(friend); // Sets the title of the action bar.
        }
    }

    // setUpButton(): Set up the Button objects for the activity.
    private void setUpButtons() {

        ImageButton cameraButton = (ImageButton) findViewById(R.id.btnPicture);

        // CAMERA BUTTON:
        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: Add the dialog popup for the Camera menu.

                final SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJChatActivity.this);

                ListView cameraOptions = new ListView(SJChatActivity.this);
                cameraOptions.setBackgroundColor(Color.WHITE);
                cameraOptions.setSelector(R.drawable.list_item_selector);

                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(SJChatActivity.this, android.R.layout.simple_list_item_1,
                        android.R.id.text1, getResources().getStringArray(R.array.camera_choices));

                cameraOptions.setAdapter(modeAdapter);
                SJDialogBuilder.setView(cameraOptions);

                final AlertDialog customDialog = SJDialogBuilder.create();
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();

                cameraOptions.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                    {
                        customDialog.cancel();

                        switch(position) {
                            case 0: // Take picture

                                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                                if (mMediaUri == null) {
                                    // display an error
                                    Toast.makeText(SJChatActivity.this, R.string.error_external_storage,
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                                }
                                break;
                            case 1: // Take video
                                Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                                if (mMediaUri == null) {
                                    // display an error
                                    Toast.makeText(SJChatActivity.this, R.string.error_external_storage,
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                    videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);

                                    String manufacturer = android.os.Build.MANUFACTURER;

                                    if (manufacturer.equals("HTC") || manufacturer.equals("HTC Corporation"))
                                    {
                                        System.out.println("manufacturer = HTC");

                                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 0 = lowest res
                                    }
                                    else
                                    {
                                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 0 = lowest res

                                        final Toast tag = Toast.makeText(getBaseContext(), R.string.video_size_warning,Toast.LENGTH_SHORT);

                                        tag.show();

                                        new CountDownTimer(7000, 1000)
                                        {
                                            public void onTick(long millisUntilFinished) {tag.show();}
                                            public void onFinish() {tag.show();}

                                        }.start();

                                        long maxVideoSize = 7*1024*1024; // 10 MB // 10491520L

                                        videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                                    }

                                    startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
                                }
                                break;
                            case 2: // Choose picture
                                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                                break;
                            case 3: // Choose video
                                Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                chooseVideoIntent.setType("video/*");
                                Toast.makeText(SJChatActivity.this, R.string.video_file_size_warning, Toast.LENGTH_LONG).show();
                                startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                                break;
                        }
                    }

                    private Uri getOutputMediaFileUri(int mediaType) {
                        // To be safe, you should check that the SDCard is mounted
                        // using Environment.getExternalStorageState() before doing this.
                        if (isExternalStorageAvailable()) {
                            // get the URI

                            // 1. Get the external storage directory
                            String appName = SJChatActivity.this.getString(R.string.app_name);
                            File mediaStorageDir = new File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                    appName);

                            // 2. Create our subdirectory
                            if (! mediaStorageDir.exists()) {
                                if (! mediaStorageDir.mkdirs()) {
                                    Log.e(LOG_TAG, "Failed to create directory.");
                                    return null;
                                }
                            }

                            // 3. Create a file name
                            // 4. Create the file
                            File mediaFile;
                            Date now = new Date();

                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                            String path = mediaStorageDir.getPath() + File.separator;
                            if (mediaType == MEDIA_TYPE_IMAGE) {
                                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                            }
                            else if (mediaType == MEDIA_TYPE_VIDEO) {

                                String manufacturer = android.os.Build.MANUFACTURER;

                                if (manufacturer.equals("HTC") || manufacturer.equals("HTC Corporation"))
                                {
                                    System.out.println("manufacturer = HTC");

                                    mediaFile = new File(path + "VID_" + timestamp + ".3gp");
                                }
                                else
                                {
                                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                                }
                            }
                            else {
                                return null;
                            }

                            Log.d(LOG_TAG, "File: " + Uri.fromFile(mediaFile));

                            // 5. Return the file's URI
                            return Uri.fromFile(mediaFile);
                        }
                        else {
                            return null;
                        }
                    }

                    private boolean isExternalStorageAvailable() {
                        String state = Environment.getExternalStorageState();

                        if (state.equals(Environment.MEDIA_MOUNTED)) {
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                }
                else {
                    mMediaUri = data.getData();
                }

                if (requestCode == PICK_VIDEO_REQUEST) {
                    // make sure the file is less than 10 MB
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    }
                    catch (FileNotFoundException e) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
                        return;
                    }
                    catch (IOException e) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) { /* Intentionally blank */ }
                    }

                    if (fileSize >= FILE_SIZE_LIMIT) {
                        Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            if (requestCode == PICK_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST) {

                String fileType;
                Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
                recipientsIntent.setData(mMediaUri);

                fileType = ParseConstants.TYPE_VIDEO;

                recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                startActivity(recipientsIntent);
            }

            else if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST)
            {

                Intent recipientsIntent = new Intent(getApplicationContext(), RecipientsActivity.class);
                recipientsIntent.setData(mMediaUri);

                String fileType = ParseConstants.TYPE_IMAGE;

                recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                startActivity(recipientsIntent);

            }
        }

        else if (resultCode == RESULT_CANCELED) {

        }
    }


    //----------------------------------------------------------------------------------------------
    /** END ADDITIONS **/


    /** The Conversation list. */
    private ArrayList<Conversation> convList;

    /** The chat adapter. */
    private ChatAdapter adp;

    /** The Editext to compose the message. */
    private EditText txt;

    /** The user name of buddy. */
    private String friend;

    /** The date of last message in conversation. */
    private Date lastMsgDate;

    /** Flag to hold if the activity is running or not. */
    private boolean isRunning;

    /** The handler. */
    private static Handler handler;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        convList = new ArrayList<Conversation>();
        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(R.id.btnSend);

        friend = getIntent().getStringExtra(Const.EXTRA_DATA);


        handler = new Handler();

        // TODO: New code for retrieving the current user.
        user = ParseUser.getCurrentUser();
        Log.d(LOG_TAG, "onCreate(): Current selected ParseUser: " + user.getUsername());

        getIntentBundle(); // Retrieves the bundle from the previous activity.
        setUpButtons(); // Sets up the listeners for the Button objects in the activity.
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        isRunning = true;
        loadConversationList();
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onPause()
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        isRunning = false;
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */
    @Override
    public void onClick(View v)
    {
        super.onClick(v);
        if (v.getId() == R.id.btnSend)
        {
            sendMessage();
        }

    }

    /**
     * Call this method to Send message to opponent. It does nothing if the text
     * is empty otherwise it creates a Parse object for Chat message and send it
     * to Parse server.
     */
    private void sendMessage()
    {
        if (txt.length() == 0)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();
        final Conversation c = new Conversation(s, new Date(),
                user.getUsername());
        c.setStatus(Conversation.STATUS_SENDING);
        convList.add(c);
        adp.notifyDataSetChanged();
        txt.setText(null);

        ParseObject po = new ParseObject("Chat");
        po.put("sender", user.getUsername());
        po.put("receiver", friend);
        // po.put("createdAt", "");
        po.put("message", s);
        po.saveEventually(new SaveCallback() {

            @Override
            public void done(ParseException e)
            {
                if (e == null)
                    c.setStatus(Conversation.STATUS_SENT);
                else
                    c.setStatus(Conversation.STATUS_FAILED);
                adp.notifyDataSetChanged();
            }
        });
    }

    /**
     * Load the conversation list from Parse server and save the date of last
     * message that will be used to load only recent new messages
     */
    private void loadConversationList()
    {
        ParseQuery<ParseObject> q = ParseQuery.getQuery("Chat");
        if (convList.size() == 0)
        {
            // load all messages...
            ArrayList<String> al = new ArrayList<String>();
            al.add(friend);
            al.add(user.getUsername());
            q.whereContainedIn("sender", al);
            q.whereContainedIn("receiver", al);
        }
        else
        {
            // load only newly received message..
            if (lastMsgDate != null)
                q.whereGreaterThan("createdAt", lastMsgDate);
            q.whereEqualTo("sender", friend);
            q.whereEqualTo("receiver", user.getUsername());
        }
        q.orderByDescending("createdAt");
        q.setLimit(30);
        q.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> li, ParseException e)
            {
                if (li != null && li.size() > 0)
                {
                    for (int i = li.size() - 1; i >= 0; i--)
                    {
                        ParseObject po = li.get(i);
                        Conversation c = new Conversation(po
                                .getString("message"), po.getCreatedAt(), po
                                .getString("sender"));
                        convList.add(c);
                        if (lastMsgDate == null
                                || lastMsgDate.before(c.getDate()))
                            lastMsgDate = c.getDate();
                        adp.notifyDataSetChanged();
                    }
                }
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run()
                    {
                        if (isRunning)
                            loadConversationList();
                    }
                }, 1000);
            }
        });

    }

    /**
     * The Class ChatAdapter is the adapter class for Chat ListView. This
     * adapter shows the Sent or Receieved Chat message in each list item.
     */
    private class ChatAdapter extends BaseAdapter
    {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return convList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0)
        {
            return convList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            Conversation c = getItem(pos);
            if (c.isSent())
                v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
            else
                v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);

            TextView lbl = (TextView) v.findViewById(R.id.lbl1);
            lbl.setText(DateUtils.getRelativeDateTimeString(SJChatActivity.this, c
                            .getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) v.findViewById(R.id.lbl3);
            if (c.isSent())
            {
                if (c.getStatus() == Conversation.STATUS_SENT)
                    lbl.setText("Delivered");
                else if (c.getStatus() == Conversation.STATUS_SENDING)
                    lbl.setText("Sending...");
                else
                    lbl.setText("Failed");
            }
            else
                lbl.setText("");

            return v;
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
