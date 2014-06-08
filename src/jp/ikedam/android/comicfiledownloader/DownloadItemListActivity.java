package jp.ikedam.android.comicfiledownloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jp.ikedam.android.comicfiledownloader.model.DownloadItem;
import jp.ikedam.android.comicfiledownloader.model.ServerInfo;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.TextView;

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
                    .add(R.id.container, new DownloadItemListFragment()).commit();
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
    public class DownloadItemListFragment extends ListFragment
    {
        protected class DownloadItemAdapter extends BaseAdapter
        {
            private final List<DownloadItem> downloadItemList = new ArrayList<DownloadItem>();
            private final LayoutInflater mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            void clear()
            {
                downloadItemList.clear();
            }
            
            boolean add(DownloadItem item)
            {
                return downloadItemList.add(item);
            }
            
            @Override
            public int getCount()
            {
                // "Back" link and items.
                return downloadItemList.size() + 1;
            }
            
            @Override
            public DownloadItem getItem(int position)
            {
                if(position == 0)
                {
                    return null;
                }
                return downloadItemList.get(position - 1);
            }
            
            @Override
            public long getItemId(int position)
            {
                return position;
            }
            
            @Override
            public int getViewTypeCount()
            {
                return 4;
            }
            
            @Override
            public int getItemViewType(int position)
            {
                return getItemViewType(getItem(position));
            }
            
            protected int getItemViewType(DownloadItem item)
            {
                if(item == null)
                {
                    // item_downloaditem_back
                    return 0;
                }
                
                if(TextUtils.isEmpty(item.getAuthor()))
                {
                    // item_downloaditem_wo_author
                    return 1;
                }
                
                // item_downloaditem
                return 2;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                DownloadItem item = getItem(position);
                int type = getItemViewType(item);
                switch(type)
                {
                case 0:
                    // item_downloaditem_back
                    return getViewForBack(item, convertView, parent);
                case 1:
                    // item_downloaditem_wo_authr
                    return getViewForItemWithoutAuthor(item, convertView, parent);
                case 2:
                    // item_downloaditem
                    return getViewForItem(item, convertView, parent);
                }
                return null;
            }
            
            protected View getViewForBack(DownloadItem item, View convertView, ViewGroup parent)
            {
                if(convertView == null)
                {
                    convertView = mInflater.inflate(R.layout.item_downloaditem_back, parent, false);
                }
                return convertView;
            }
            
            protected View getViewForItemWithoutAuthor(DownloadItem item,
                    View convertView, ViewGroup parent)
            {
                if(convertView == null)
                {
                    convertView = mInflater.inflate(R.layout.item_downloaditem_wo_author, parent, false);
                }
                ((TextView)convertView.findViewById(R.id.itemNameView)).setText(item.getTitle());
                return convertView;
            }
            
            protected View getViewForItem(DownloadItem item, View convertView,
                    ViewGroup parent)
            {
                if(convertView == null)
                {
                    convertView = mInflater.inflate(R.layout.item_downloaditem, parent, false);
                }
                ((TextView)convertView.findViewById(R.id.itemNameView)).setText(item.getTitle());
                ((TextView)convertView.findViewById(R.id.authorView)).setText(item.getAuthor());
                return convertView;
            }
        }
        protected class RetrieveItemListTask extends AsyncTask<Void, Integer, String>
        {
            private ProgressDialogFragment progressDialog;
            private URL currentUrl;
            
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
                    currentUrl = new URL(serverInfo.getServerUri());
                    urlConnection = (HttpURLConnection)currentUrl.openConnection();
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
                if(result == null)
                {
                    // Error handling would be already done in doInBackground.
                    return;
                }
                Document doc = Jsoup.parse(result);
                
                adapter.clear();
                
                // http://comicglass.net/mediaserver_index/
                // * Treats <a> tags as items.
                Elements links = doc.select("a[href]");
                for(Element link: links)
                {
                    String title = null;
                    if(link.hasAttr("booktitle"))
                    {
                        title = link.attr("booktitle");
                    }
                    else
                    {
                        title = link.text();
                    }
                    
                    // accepts "[author] title" format.
                    DownloadItem item = DownloadItem.newInstanceFromText(title);
                    
                    try
                    {
                        URL url = new URL(currentUrl, link.attr("href"));
                        item.setUri(url.toExternalForm());
                    }
                    catch(MalformedURLException e)
                    {
                        Log.w(TAG, String.format("Ignored invalid link: %s", link.attr("href")), e);
                        continue;
                    }
                    
                    if(link.hasAttr("bookfile") && "true".equalsIgnoreCase(link.attr("bookfile")))
                    {
                        item.setType(DownloadItem.DownloadItemType.File);
                    }
                    
                    adapter.add(item);
                }
                
                adapter.notifyDataSetChanged();
            }
            
            @Override
            protected void onCancelled()
            {
                showError(R.string.error_canceled);
            }
        }
        
        private ServerInfo serverInfo;
        private RetrieveItemListTask retrieveTask;
        private DownloadItemAdapter adapter;
        
        public DownloadItemListFragment()
        {
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            adapter = new DownloadItemAdapter();
            getListView().setAdapter(adapter);
            
            super.onActivityCreated(savedInstanceState);
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
