package jp.ikedam.android.comicfiledownloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import jp.ikedam.android.comicfiledownloader.model.ServerInfo;
import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class DownloadItemListActivity extends Activity
{
    private static String TAG = DownloadItemListActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_item_list);
        
        if(savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.download_item_list, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        protected class RetrieveItemListTask extends AsyncTask<Void, Integer, String>
        {
            private ProgressDialogFragment progressDialog;
            
            protected void showError(int id, Object... params)
            {
                ErrorDialogFragment dialog = new ErrorDialogFragment()
                    .setTitle(getResources().getString(R.string.dialog_error_title))
                    .setMessage(getResources().getString(id, params))
                    .setOkOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v)
                        {
                            getActivity().onBackPressed();
                        }
                    });
                dialog.show(getFragmentManager(), "error");
            }
            
            @Override
            protected void onPreExecute()
            {
                progressDialog = new ProgressDialogFragment()
                    .setTitle(getResources().getString(R.string.dialog_progress_title))
                    .setCancelOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v)
                        {
                            RetrieveItemListTask.this.cancel(true);
                        }
                    });
                progressDialog.show(getFragmentManager(), "progress");
            }
            
            @Override
            protected String doInBackground(Void... params)
            {
                HttpURLConnection urlConnection = null;
                try
                {
                    if(!TextUtils.isEmpty(serverInfo.getUsername()))
                    {
                        Authenticator.setDefault(new Authenticator()
                        {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication()
                            {
                                return new PasswordAuthentication(
                                        serverInfo.getUsername(),
                                        serverInfo.getPassword().toCharArray()
                                );
                            }
                        });
                    }
                    urlConnection = (HttpURLConnection)new URL(serverInfo.getServerUri()).openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
                    
                    int status = urlConnection.getResponseCode();
                    if(status != HttpURLConnection.HTTP_OK)
                    {
                        showError(R.string.error_httpserver_error, status, urlConnection.getResponseMessage());
                        return null;
                    }
                    
                    InputStream in = urlConnection.getInputStream();
                    int length = urlConnection.getContentLength();
                    int totalReadSize = 0;
                    publishProgress(0, length);
                    
                    ByteArrayOutputStream os = (length > 0)?new ByteArrayOutputStream(length):new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int readSize;
                    while((readSize = in.read(buffer)) > 0)
                    {
                        totalReadSize += readSize;
                        publishProgress(totalReadSize, length);
                        os.write(buffer);
                    }
                    return os.toString("UTF-8");
                }
                catch(MalformedURLException e)
                {
                    Log.e(TAG, "URI Parse error", e);
                    showError(R.string.error_invalid_uri_with, serverInfo.getServerUri());
                }
                catch(IOException e)
                {
                    Log.e(TAG, "IO Error in accessing server", e);
                    showError(R.string.error_server_io);
                }
                finally
                {
                    urlConnection.disconnect();
                    Authenticator.setDefault(null);
                }
                return null;
            }
            
            @Override
            protected void onProgressUpdate(Integer... values)
            {
                int cur = values[0];
                int max = values[1];
                progressDialog.setProgress(cur, max);
            }
            
            @Override
            protected void onPostExecute(String result)
            {
                progressDialog.dismiss();
                if(result != null)
                {
                    Log.d("TEST", result);
                }
            }
            
            @Override
            protected void onCancelled()
            {
                showError(R.string.error_canceled);
            }
        }
        
        private ServerInfo serverInfo;
        private RetrieveItemListTask retrieveTask;
        
        public PlaceholderFragment()
        {
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(
                    R.layout.fragment_download_item_list, container, false);
            
            if(getActivity().getIntent() != null)
            {
                serverInfo = (ServerInfo)getActivity().getIntent().getSerializableExtra(ServerInfo.Intent.EXTRA);
                startRetrieveItemList();
            }
            
            return rootView;
        }
        
        protected void startRetrieveItemList()
        {
            retrieveTask = new RetrieveItemListTask();
            retrieveTask.execute();
        }
    }
    
}
