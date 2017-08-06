package com.teinvdlugt.android.piano;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class TagsActivity extends AppCompatActivity implements TagLayout.OnTagClickListener {
    public static final String TAGS_EXTRA = "tags";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String tags = getIntent().getStringExtra(TAGS_EXTRA);
        TagLayout tagLayout = (TagLayout) findViewById(R.id.tagLayout);

        if (tags == null || tags.isEmpty()) {
            findViewById(R.id.no_tags_yet_tv).setVisibility(View.VISIBLE);
            tagLayout.setVisibility(View.GONE);
        } else {
            findViewById(R.id.no_tags_yet_tv).setVisibility(View.GONE);
            tagLayout.setVisibility(View.VISIBLE);
            tagLayout.setTags(tags);
            tagLayout.setOnTagClickListener(this);
        }
    }

    @Override
    public void onClickTag(String tag) {
        setResult(RESULT_OK, new Intent().putExtra(MainActivity.CLICKED_TAG_EXTRA, tag));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
