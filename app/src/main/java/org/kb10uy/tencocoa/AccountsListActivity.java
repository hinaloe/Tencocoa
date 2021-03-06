package org.kb10uy.tencocoa;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.kb10uy.tencocoa.adapters.GeneralListAdapter;
import org.kb10uy.tencocoa.model.TencocoaHelper;
import org.kb10uy.tencocoa.model.TwitterAccountInformation;
import org.kb10uy.tencocoa.model.TwitterHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class AccountsListActivity extends AppCompatActivity {

    String mCallback;
    Twitter mTwitter;
    RequestToken mRequestToken;

    ListView mListView;
    GeneralListAdapter<TwitterAccountInformation> accountsAdapter;
    Intent resultIntent = new Intent();
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        TencocoaHelper.setCurrentTheme(this, pref.getString(getString(R.string.preference_appearance_theme), "Black"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        accountsAdapter = new GeneralListAdapter<>(
                this,
                R.layout.item_accounts_list,
                (targetView, item) -> {
                    ((TextView) targetView.findViewById(R.id.AccountsListListViewItemScreenName)).setText(item.getScreenName());
                    ((TextView) targetView.findViewById(R.id.AccountsListListViewItemUserId)).setText(Long.toString(item.getUserId()));
                    return targetView;
                });
        mListView = (ListView) findViewById(R.id.AccountsListListView);
        mListView.setAdapter(accountsAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> onAccountSelected(position));


        String ck = pref.getString(getString(R.string.preference_twitter_consumer_key), "");
        String cs = pref.getString(getString(R.string.preference_twitter_consumer_secret), "");
        mTwitter = new TwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(ck, cs);
        mCallback = getString(R.string.uri_twitter_oauth_callback);
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && intent.getData().toString().startsWith(mCallback)) {
            onNewIntent(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("accounts", new ArrayList<>(accountsAdapter.getList()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null || intent.getData() == null || !intent.getData().toString().startsWith(mCallback))
            return;
        Uri data = intent.getData();
        String verifier = intent.getData().getQueryParameter("oauth_verifier");
        final Activity ta = this;

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    if (mRequestToken == null) {
                        String rt = pref.getString(getString(R.string.preference_twitter_request_token), "");
                        String rts = pref.getString(getString(R.string.preference_twitter_request_token_secret), "");
                        mRequestToken = new RequestToken(rt, rts);
                    }
                    return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    Toast.makeText(ta, R.string.text_activity_accounts_list_success, Toast.LENGTH_SHORT).show();
                    registerAuthorization(accessToken);
                } else {
                    Toast.makeText(ta, R.string.text_activity_accounts_list_failed, Toast.LENGTH_SHORT).show();
                }
                pref.edit()
                        .putString(getString(R.string.preference_twitter_request_token), mRequestToken.getToken())
                        .putString(getString(R.string.preference_twitter_request_token_secret), mRequestToken.getTokenSecret())
                        .apply();
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accounts_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_accounts_list_new_account:
                newOAuthAuthorize();
                return true;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<TwitterAccountInformation> accounts = (ArrayList<TwitterAccountInformation>) savedInstanceState.getSerializable("accounts");
        accountsAdapter.setList(accounts);
    }

    private void newOAuthAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallback);
                    pref.edit()
                            .putString(getString(R.string.preference_twitter_request_token), mRequestToken.getToken())
                            .putString(getString(R.string.preference_twitter_request_token_secret), mRequestToken.getTokenSecret())
                            .apply();
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void initialize() {
        loadAccounts();
        if (accountsAdapter.getList() == null) {
            accountsAdapter.setList(new ArrayList<>());
            saveAccounts();
        }
        accountsAdapter.notifyDataSetChanged();
    }


    private void registerAuthorization(AccessToken accessToken) {
        final TwitterAccountInformation info = new TwitterAccountInformation(accessToken);
        Handler h = new Handler();
        h.post(() -> {
            accountsAdapter.add(info);
            saveAccounts();
        });
    }

    private void saveAccounts() {
        try {
            FileOutputStream acfile = openFileOutput(getString(R.string.accounts_file_name), MODE_PRIVATE);
            TencocoaHelper.serializeObjectToFile(new ArrayList<>(accountsAdapter.getList()), acfile);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit = pref.edit();
            edit.putInt(getString(R.string.preference_twitter_accounts_count), accountsAdapter.getCount());
            edit.apply();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadAccounts() {
        try {
            FileInputStream acfile = openFileInput(getString(R.string.accounts_file_name));
            ArrayList<TwitterAccountInformation> accounts = TencocoaHelper.deserializeObjectFromFile(acfile);
            accountsAdapter.setList(accounts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onAccountSelected(int position) {
        resultIntent.putExtra("Information", (TwitterAccountInformation) accountsAdapter.getItem(position));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
