package com.whomentors.sadajura.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import com.whomentors.sadajura.ui.view.SJUnbind;
import com.whomentors.sarajura.R;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 *  [SJChatActivity] CLASS
 *  DESCRIPTION: The Class Chat is the Activity class that holds main chat screen. It shows all the
 *  conversation messages between two users and also allows the user to send and receive messages.
 *  -----------------------------------------------------------------------------------------------
 */
public class SJChatActivity extends CustomActivity {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // ACTIVITY VARIABLES
    private boolean isRunning; // Flag to hold if the activity is running or not.

    // CAMERA DIALOG VARIABLES:
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int MEDIA_TYPE_IMAGE = 5;
    protected Uri mMediaUri;

    // CHAT VARIABLES
    private ArrayList<Conversation> converseList; // The Conversation list.
    private ChatAdapter chatAdapter; // The chat adapter.
    private Date lastMsgDate; // The date of last message in conversation.
    private String chatFriend; // The user name of chat recipient.

    // LOGGING VARIABLES
    private static final String LOG_TAG = SJChatActivity.class.getSimpleName();

    // PARSE VARIABLES
    public static ParseUser currentUser;

    // THREAD VARIABLES
    private static Handler handler;

    // VIEW INJECTION VARIABLES
    @Bind(R.id.sj_chat_edit_text) EditText chatEditText; // The Editext to compose the message.

    /** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

    // onCreate(): The initial function that is called when the activity is run. onCreate() only
    // runs when the activity is first started.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLayout(); // Sets up the layout for the activity.
        setUpConverstation(); // Sets up the conversation view for the activity.

        handler = new Handler();
        currentUser = ParseUser.getCurrentUser(); // Retrieves the current Parse user.
        Log.d(LOG_TAG, "onCreate(): Current selected ParseUser: " + currentUser.getUsername());

        getIntentBundle(); // Retrieves the bundle from the previous activity.
        setUpButtons(); // Sets up the listeners for the Button objects in the activity.
    }

    // onResume(): This function runs immediately after onCreate() finishes and is always re-run
    // whenever the activity is resumed from an onPause() state.
    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        loadConversationList();
    }

    // onPause(): This function is called whenever the current activity is suspended or another
    // activity is launched.
    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    // onDestroy(): This function runs when the activity has terminated and is being destroyed.
    // Calls recycleMemory() to free up memory allocation.
    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Recycles all View objects to free up memory resources.
        SJUnbind.recycleMemory(findViewById(R.id.sj_chat_activity_layout));
    }

    /** PHYSICAL BUTTON FUNCTIONALITY __________________________________________________________ **/

    // BACK KEY:
    // onBackPressed(): Defines the action to take when the physical back button key is pressed.
    @Override
    public void onBackPressed() {
        finish(); // Finishes the activity.
    }

    /** ACTIVITY EXTENSION METHODS _____________________________________________________________ **/

    // onActivityResult(): Run after returning to the activity after taking or choosing a photo.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST) {

                if (data == null) {
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                }

                else { mMediaUri = data.getData(); }
            }

            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {

                Intent recipientsIntent = new Intent(getApplicationContext(), RecipientsActivity.class);
                recipientsIntent.setData(mMediaUri);

                String fileType = ParseConstants.TYPE_IMAGE;

                recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                startActivity(recipientsIntent);

            }
        }

        else if (resultCode == RESULT_CANCELED) {}
    }

    // onClick(): Runs when a button is clicked.
    @Override
    public void onClick(View v) {
        super.onClick(v);

        // SEND MESSAGE:
        if (v.getId() == R.id.sj_chat_send_button) { sendMessage(); }
    }

    // onOptionsItemSelected(): Defines the action to take when the menu options are selected.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) { finish(); }
        return super.onOptionsItemSelected(item);
    }

    /** LAYOUT METHODS _________________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the activity.
    private void setUpLayout() {
        setContentView(R.layout.sj_chat_activity); // Sets the XML layout file.
        ButterKnife.bind(this); // ButterKnife view injection initialization.
    }

    // setUpButton(): Set up the Button objects for the activity.
    private void setUpButtons() {

        ImageButton cameraButton = (ImageButton) findViewById(R.id.sj_chat_camera_button);

        // CAMERA BUTTON:
        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

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

                cameraOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        customDialog.cancel();

                        switch (position) {

                            // TAKE A PICURE:
                            case 0: // Take picture

                                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                                if (mMediaUri == null) {
                                    // display an error
                                    Toast.makeText(SJChatActivity.this, R.string.error_external_storage,
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                                }
                                break;

                            // CHOOSE A PICTURE:
                            case 2:
                                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                                break;
                        }
                    }

                    private Uri getOutputMediaFileUri(int mediaType) {

                        // To be safe, you should check that the SDCard is mounted
                        // using Environment.getExternalStorageState() before doing this.
                        if (isExternalStorageAvailable()) {

                            // 1. Get the external storage directory
                            String appName = SJChatActivity.this.getString(R.string.app_name);
                            File mediaStorageDir = new File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                    appName);

                            // 2. Create our subdirectory
                            if (!mediaStorageDir.exists()) {
                                if (!mediaStorageDir.mkdirs()) {
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
                            } else {
                                return null;
                            }

                            Log.d(LOG_TAG, "File: " + Uri.fromFile(mediaFile));

                            // 5. Return the file's URI
                            return Uri.fromFile(mediaFile);
                        } else {
                            return null;
                        }
                    }

                    private boolean isExternalStorageAvailable() {
                        String state = Environment.getExternalStorageState();

                        if (state.equals(Environment.MEDIA_MOUNTED)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            }
        });
    }

    /** CHAT METHODS ___________________________________________________________________________ **/

    // setUpConverstation(): Sets up the conversation view.
    private void setUpConverstation() {

        converseList = new ArrayList<Conversation>();
        ListView list = (ListView) findViewById(R.id.sj_chat_list);
        chatAdapter = new ChatAdapter();
        list.setAdapter(chatAdapter);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        chatEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setTouchNClick(R.id.sj_chat_send_button);
    }

    // sendMessage(): Call this method to Send message to opponent. It does nothing if the text is
    // empty otherwise it creates a Parse object for Chat message and send it to Parse server.
    private void sendMessage() {

        if (chatEditText.length() == 0)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(chatEditText.getWindowToken(), 0);

        String s = chatEditText.getText().toString();
        final Conversation c = new Conversation(s, new Date(),
                currentUser.getUsername());
        c.setStatus(Conversation.STATUS_SENDING);
        converseList.add(c);
        chatAdapter.notifyDataSetChanged();
        chatEditText.setText(null);

        ParseObject po = new ParseObject("Chat");
        po.put("sender", currentUser.getUsername());
        po.put("receiver", chatFriend);
        // po.put("createdAt", "");
        po.put("message", s);
        po.saveEventually(new SaveCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null)
                    c.setStatus(Conversation.STATUS_SENT);
                else
                    c.setStatus(Conversation.STATUS_FAILED);
                chatAdapter.notifyDataSetChanged();
            }
        });
    }

    // loadConverstationList(): Load the conversation list from Parse server and save the date of
    // last message that will be used to load only recent new messages
    private void loadConversationList() {

        ParseQuery<ParseObject> q = ParseQuery.getQuery("Chat");

        // Load all messages.
        if (converseList.size() == 0) {
            ArrayList<String> al = new ArrayList<String>();
            al.add(chatFriend);
            al.add(currentUser.getUsername());
            q.whereContainedIn("sender", al);
            q.whereContainedIn("receiver", al);
        }

        // Loads only newly received messages.
        else {

            if (lastMsgDate != null) { q.whereGreaterThan("createdAt", lastMsgDate); }
            q.whereEqualTo("sender", chatFriend);
            q.whereEqualTo("receiver", currentUser.getUsername());
        }

        q.orderByDescending("createdAt");
        q.setLimit(30);
        q.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> li, ParseException e) {

                if (li != null && li.size() > 0) {

                    for (int i = li.size() - 1; i >= 0; i--) {
                        ParseObject po = li.get(i);
                        Conversation c = new Conversation(po
                                .getString("message"), po.getCreatedAt(), po
                                .getString("sender"));
                        converseList.add(c);

                        if (lastMsgDate == null || lastMsgDate.before(c.getDate())) {
                            lastMsgDate = c.getDate();
                        }

                        chatAdapter.notifyDataSetChanged();
                    }
                }

                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isRunning)
                            loadConversationList();
                    }
                }, 1000);
            }
        });
    }

    /** ADDITIONAL METHODS _____________________________________________________________________ **/

    // getIntentBundle(): Retrieves the Bundle data from the previous activity.
    private void getIntentBundle() {

        Bundle extras = getIntent().getExtras();

        // Tries to retrieve the additional information from the bundle.
        if (extras != null) {

            // Sets the friend name.
            chatFriend = getIntent().getStringExtra(Const.EXTRA_DATA);
            chatFriend = getIntent().getExtras().getString("selectedUsername");
            Log.d(LOG_TAG, "getIntentBundle(): Friend name is: " + chatFriend);

            getActionBar().setTitle("Chat with " + chatFriend); // Sets the title of the action bar.
        }
    }

    /** --------------------------------------------------------------------------------------------
     *  [ChatAdapter] CLASS
     *  DESCRIPTION: The Class ChatAdapter is the adapter class for Chat ListView. This
     *  adapter shows the Sent or Receieved Chat message in each list item.
     *  --------------------------------------------------------------------------------------------
     */

    private class ChatAdapter extends BaseAdapter {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return converseList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0)
        {
            return converseList.get(arg0);
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
}
