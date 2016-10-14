package com.orange.discover;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TeachingTopicsActivity extends AppCompatActivity {

    private String TAG = "Topics Activity";

    private static final int HIGHLIGHT_COLOR = 0x999be6ff;

    private TextView doneButtonText;
    private Toolbar mToolbar;

    private ArrayList<ListData> mDataList;
    private ArrayList<String> teachingTopics;

    private ParseUser currentParseUser;
    private SampleAdapter s;

    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teaching_topics);

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mDrawableBuilder = TextDrawable.builder().round();

        mDataList = new ArrayList<ListData>();

        teachingTopics = new ArrayList<String>();

        currentParseUser = ParseUser.getCurrentUser();

        doneButtonText = (TextView) mToolbar.findViewById(R.id.DoneText);


        // init the list view and its adapter
        ListView listView = (ListView) findViewById(R.id.listView);
        s = new SampleAdapter();
        listView.setAdapter(s);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText topicEditText = new EditText(TeachingTopicsActivity.this);

                AlertDialog dialog = new AlertDialog.Builder(TeachingTopicsActivity.this)
                        .setTitle("Add a new topic")
                        .setMessage("What topic do you want to add newly?")
                        .setView(topicEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String currentTopic = String.valueOf(topicEditText.getText());
                                ParseObject Topics = new ParseObject("Topics");
                                Topics.put("topicName", currentTopic);
                                Topics.saveInBackground();
                                mDataList.add(new ListData(currentTopic));
                                s.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });

        // Populate the list
        ParseQuery<ParseObject> querytopics = ParseQuery.getQuery("Topics");
        querytopics.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> topicsList, ParseException e) {
                Log.w(TAG, "Parse Querying");

                if (e == null) {
                    for (ParseObject t : topicsList) {
                        String currentTopic = t.getString("topicName");
                        mDataList.add(new ListData(currentTopic));
                    }
                } else {
                    e.printStackTrace();
                }
                s.notifyDataSetChanged();
            }
        });



        doneButtonText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.w(TAG, "DONE BUTTON CLICKED");

                currentParseUser.addAllUnique("topicsToTeach", Arrays.asList(teachingTopics));

                // Start the next activity

                Log.w(TAG, "STARTING THE MAIN ACTIVITY FROM Teaching TOPIC");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class SampleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public ListData getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(TeachingTopicsActivity.this, R.layout.list_item_layout, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData item = getItem(position);

            // provide support for selected state
            updateCheckedState(holder, item);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // when the list item is clicked, update the selected state
                    ListData data = getItem(position);
                    data.setChecked(!data.isChecked);
                    updateCheckedState(holder, data);
                }
            });
            holder.textView.setText(item.data);

            return convertView;
        }

        private void updateCheckedState(ViewHolder holder, ListData item) {
            if (item.isChecked) {
                holder.imageView.setImageDrawable(mDrawableBuilder.build(" ", 0xff616161));
                holder.view.setBackgroundColor(HIGHLIGHT_COLOR);
                holder.checkIcon.setVisibility(View.VISIBLE);
                teachingTopics.add(item.data);
            }
            else {
                TextDrawable drawable = mDrawableBuilder.build(String.valueOf(item.data.charAt(0)), mColorGenerator.getColor(item.data));
                holder.imageView.setImageDrawable(drawable);
                holder.view.setBackgroundColor(Color.TRANSPARENT);
                holder.checkIcon.setVisibility(View.GONE);
                teachingTopics.remove(item.data);
            }
        }
    }

    private static class ViewHolder {

        private View view;

        private ImageView imageView;

        private TextView textView;

        private ImageView checkIcon;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            checkIcon = (ImageView) view.findViewById(R.id.check_icon);
        }
    }

    private static class ListData {

        private String data;

        private boolean isChecked;

        public ListData(String data) {
            this.data = data;
        }

        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }
    }
}
