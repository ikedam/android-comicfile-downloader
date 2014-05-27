package jp.ikedam.android.comicfiledownloader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;

import jp.ikedam.android.comicfiledownloader.db.DatabaseHelper;
import jp.ikedam.android.comicfiledownloader.model.ServerInfo;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;

public class ServerlistActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serverlist);
        
        if(savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ServerlistFragment()).commit();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.serverlist, menu);
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
    
    
    public static interface ServerlistItemListener
    {
        void onSelected(ServerInfo item);
        void onReordered(List<ServerInfo> reordered);
        void onAdd();
        void onEdit(ServerInfo item);
        void onDelete(ServerInfo item);
    }
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ServerlistFragment extends ListFragment implements ServerlistItemListener
    {
        private static String TAG = ServerlistFragment.class.getSimpleName();
        private static final int REQUESTCODE_SERVERINFO_ADD = 1;
        private static final int REQUESTCODE_SERVERINFO_EDIT = 2;
        private ServerlistAdapter adapter;
        
        public ServerlistFragment()
        {
        }
        
        protected List<ServerInfo> loadServerInfoList()
        {
            List<ServerInfo> serverInfoList = Collections.<ServerInfo>emptyList();
            final DatabaseHelper helper = new DatabaseHelper(getActivity());
            try
            {
                Dao<ServerInfo, ?> dao = helper.getDao(ServerInfo.class);
                serverInfoList = dao.query(dao.queryBuilder().orderBy("sort_order", true).prepare());
            }
            catch(SQLException e)
            {
                Log.e(TAG, "Failed to query server_info", e);
            }
            
            /*
            // Debugging code
            if(serverInfoList == null || serverInfoList.isEmpty())
            {
                try
                {
                    serverInfoList = TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<List<ServerInfo>>(){
                        @Override
                        public List<ServerInfo> call() throws Exception
                        {
                            String[] serverNameList = getResources().getStringArray(R.array.testServerNameList);
                            List<ServerInfo> serverInfoList = new ArrayList<ServerInfo>(serverNameList.length);
                            Dao<ServerInfo, ?> dao = helper.getDao(ServerInfo.class);
                            int index = 0;
                            for(String serverName: serverNameList)
                            {
                                ServerInfo info = new ServerInfo();
                                info.setSortOrder(++index);
                                info.setServerName(serverName);
                                info.setServerUri(serverName);
                                serverInfoList.add(info);
                                
                                dao.create(info);
                            }
                            return serverInfoList;
                        }
                    });
                }
                catch(SQLException e)
                {
                    Log.e(TAG, "Failed to query server_info", e);
                }
            }
            */
            return serverInfoList;
        }
        
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            switch(requestCode)
            {
            case REQUESTCODE_SERVERINFO_ADD:
            {
                if(resultCode == ServerInfo.Intent.RESPONSECODE_OK)
                {
                    ServerInfo serverInfo = (ServerInfo)data.getSerializableExtra(ServerInfo.Intent.EXTRA);
                    addServerInfo(serverInfo);
                }
                break;
            }
            case REQUESTCODE_SERVERINFO_EDIT:
            {
                if(resultCode == ServerInfo.Intent.RESPONSECODE_OK)
                {
                    ServerInfo serverInfo = (ServerInfo)data.getSerializableExtra(ServerInfo.Intent.EXTRA);
                    updateServerInfo(serverInfo);
                }
                break;
            }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
        
        public void updateServerInfo(final ServerInfo serverInfo)
        {
            final DatabaseHelper helper = new DatabaseHelper(getActivity());
            
            try
            {
                TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>(){
                    @Override
                    public Void call() throws Exception
                    {
                        Dao<ServerInfo, Integer> dao = helper.getDao(ServerInfo.class);
                        dao.update(serverInfo);
                        return null;
                    }
                });
                int position = adapter.getPosition(serverInfo);
                if(position != ListView.INVALID_POSITION)
                {
                    adapter.remove(serverInfo);
                    adapter.insert(serverInfo, position);
                    adapter.notifyDataSetChanged();
                }
            }
            catch(SQLException e)
            {
                Log.e(TAG, "Failed to update server_info", e);
            }
        }
        
        public void addServerInfo(final ServerInfo serverInfo)
        {
            final DatabaseHelper helper = new DatabaseHelper(getActivity());
            
            try
            {
                TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>(){
                    @Override
                    public Void call() throws Exception
                    {
                        Dao<ServerInfo, Integer> dao = helper.getDao(ServerInfo.class);
                        QueryBuilder<ServerInfo, Integer> qb = dao.queryBuilder();
                        qb.selectRaw("MAX(sort_order) + 1");
                        GenericRawResults<String[]> r = dao.queryRaw(qb.prepareStatementString());
                        
                        int nextSortOrder = 0;
                        if(r.getResults() != null && r.getResults().size() > 0
                                && r.getFirstResult() != null && r.getFirstResult()[0] != null)
                        {
                            nextSortOrder = Integer.parseInt(r.getFirstResult()[0]);
                        }
                        
                        serverInfo.setSortOrder(nextSortOrder);
                        dao.create(serverInfo);
                        
                        return null;
                    }
                });
                adapter.add(serverInfo);
                adapter.notifyDataSetChanged();
            }
            catch(SQLException e)
            {
                Log.e(TAG, "Failed to update server_info", e);
            }
        }
        
        @Override
        public void onSelected(ServerInfo item)
        {
            Intent intent = new Intent(getActivity(), DownloadItemListActivity.class);
            intent.putExtra(ServerInfo.Intent.EXTRA, item);
            startActivity(intent);
        }
        
        @Override
        public void onAdd()
        {
            Intent intent = new Intent(getActivity(), ServerInfoActivity.class);
            startActivityForResult(intent, REQUESTCODE_SERVERINFO_ADD);
        }
        
        @Override
        public void onEdit(ServerInfo item)
        {
            Intent intent = new Intent(getActivity(), ServerInfoActivity.class);
            intent.putExtra(ServerInfo.Intent.EXTRA, item.clone());
            startActivityForResult(intent, REQUESTCODE_SERVERINFO_EDIT);
        }
        
        @Override
        public void onDelete(final ServerInfo item)
        {
            ConfirmDialogFragment dialog = new ConfirmDialogFragment()
                .setTitle(getResources().getString(R.string.dialog_delete_title))
                .setMessage(getResources().getString(R.string.dialog_delete_message, item.getServerName()))
                .setOkOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                        onConfirmDelete(item);
                    }
                });
            dialog.show(getFragmentManager(), "delete");
        }
        
        public void onConfirmDelete(final ServerInfo item)
        {
            final DatabaseHelper helper = new DatabaseHelper(getActivity());
            
            try
            {
                TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>(){
                    @Override
                    public Void call() throws Exception
                    {
                        Dao<ServerInfo, Integer> dao = helper.getDao(ServerInfo.class);
                        dao.delete(item);
                        
                        return null;
                    }
                });
                adapter.remove(item);
                adapter.notifyDataSetChanged();
            }
            catch(SQLException e)
            {
                Log.e(TAG, "Failed to update server_info", e);
            }
        }
        
        @Override
        public void onReordered(final List<ServerInfo> reordered)
        {
            final DatabaseHelper helper = new DatabaseHelper(getActivity());
            
            try
            {
                TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>(){
                    @Override
                    public Void call() throws Exception
                    {
                        Dao<ServerInfo, Integer> dao = helper.getDao(ServerInfo.class);
                        for(ServerInfo info: reordered)
                        {
                            dao.update(info);
                        }
                        
                        return null;
                    }
                });
            }
            catch(SQLException e)
            {
                Log.e(TAG, "Failed to update server_info", e);
            }
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_serverlist,
                    container, false);
            
            return rootView;
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            adapter = new ServerlistAdapter(getActivity(), loadServerInfoList());
            adapter.setupFor(getListView());
            adapter.setServerlistItemListener(this);
            
            super.onActivityCreated(savedInstanceState);
        }
        
        protected static class ServerlistAdapter extends DragSortableArrayAdapter<ServerInfo>
        {
            private class GestureEventConverter implements OnTouchListener
            {
                private final View target;
                private final GestureDetector gesture;
                
                public GestureEventConverter(View v){
                    this.target = v;
                    gesture = new GestureDetector(new SimpleOnGestureListener(){
                        @Override
                        public void onLongPress(MotionEvent e)
                        {
                            ServerlistAdapter.this.onLongPress(target, e);
                        }
                        
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e)
                        {
                            return ServerlistAdapter.this.onSingleTapConfirmed(target, e);
                        }
                        
                        @Override
                        public boolean onDown(MotionEvent e)
                        {
                            // ondown should be consumed to catch onSingleTap.
                            return true;
                        }
                    });
                }
                
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    return gesture.onTouchEvent(event);
                }
            }
            
            private enum ItemType {
                Server,
                Add,
            };
            
            private ServerlistItemListener listener;
            
            /**
             * @return the listener
             */
            public ServerlistItemListener getListener()
            {
                return listener;
            }
            
            public ServerlistAdapter(Context c, List<ServerInfo> serverInfoList)
            {
                super(c, R.layout.item_serverlist, R.id.serverNameView, serverInfoList);
            }
            
            public void setServerlistItemListener(ServerlistItemListener listener)
            {
                this.listener = listener;
            }
            
            /**
             * Returns +1 count for the "Add" row.
             * @return
             * @see android.widget.ArrayAdapter#getCount()
             */
            @Override
            public int getCount()
            {
                return super.getCount() + 1;
            }
            
            public int getAddPosition()
            {
                return super.getCount();
            }
            
            @Override
            public ServerInfo getItem(int position)
            {
                if(position == getAddPosition())
                {
                    return null;
                }
                return super.getItem(position);
            }
            
            @Override
            public long getItemId(int position)
            {
                if(position == getAddPosition())
                {
                    return -1;
                }
                return super.getItemId(position);
            }
            
            @Override
            public int getViewTypeCount()
            {
                return ItemType.values().length;
            }
            
            @Override
            public int getItemViewType(int position)
            {
                if(position == getAddPosition())
                {
                    return ItemType.Add.ordinal();
                }
                return ItemType.Server.ordinal();
            }
            
            @Override
            public int getPosition(ServerInfo item)
            {
                if(item == null)
                {
                    return getAddPosition();
                }
                return super.getPosition(item);
            }
            
            public int getPosition(View view)
            {
                Object pos = view.getTag();
                return (pos != null && pos instanceof Integer)?(Integer)pos:ListView.INVALID_POSITION;
            }
            
            @Override
            public View getViewBeforeDrag(int position, View convertView, ViewGroup parent)
            {
                View v = null;
                if(position == getAddPosition())
                {
                    v = getAddView(convertView, parent);
                }
                else
                {
                    v = getServerView(position, convertView, parent);
                }
                v.setTag(position);
                return v;
            }
            
            protected View getAddView(View convertView, ViewGroup parent)
            {
                if(convertView != null)
                {
                    // convertView is always right type.
                    return convertView;
                }
                
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.item_serverlist_add, parent, false);
                view.setTag(null);
                view.setOnTouchListener(new GestureEventConverter(view));
                return view;
            }
            
            protected View getServerView(int position, View convertView,
                    ViewGroup parent)
            {
                final View view = super.getViewBeforeDrag(position, convertView, parent);
                if(view != convertView)
                {
                    // It's not reasonable to set listener every time.
                    view.setOnTouchListener(new GestureEventConverter(view));
                    view.findViewById(R.id.editButton).setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v)
                        {
                            int position = getPosition(view);
                            if(position != ListView.INVALID_POSITION)
                            {
                                listener.onEdit(getItem(position));
                            }
                        }
                    });
                    view.findViewById(R.id.deleteButton).setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v)
                        {
                            int position = getPosition(view);
                            if(position != ListView.INVALID_POSITION)
                            {
                                listener.onDelete(getItem(position));
                            }
                        }
                    });
                }
                view.setTag(position);
                view.setAlpha(isDragging(position)?0.5f:1.0f);
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView,
                    ViewGroup parent)
            {
                return getView(position, convertView, parent);
            }
            
            @Override
            protected boolean isSortable(int position)
            {
                return (position != getAddPosition());
            }
            
            @Override
            protected void onSorted(int fromPosition, int toPosition)
            {
                List<ServerInfo> reordered = null;
                ServerInfo fromItem = getItem(fromPosition);
                ServerInfo toItem = getItem(toPosition);
                fromItem.setSortOrder(toItem.getSortOrder());
                
                if(fromPosition < toPosition)
                {
                    reordered = new ArrayList<ServerInfo>(toPosition - fromPosition + 1);
                    for(int pos = fromPosition + 1; pos <= toPosition; ++pos)
                    {
                        ServerInfo item = getItem(pos);
                        item.setSortOrder(item.getSortOrder() - 1);
                        reordered.add(item);
                    }
                    reordered.add(fromItem);
                }
                else // toPosition < fromPosition
                {
                    reordered = new ArrayList<ServerInfo>(fromPosition - toPosition + 1);
                    reordered.add(fromItem);
                    for(int pos = toPosition; pos <= fromPosition - 1; ++pos)
                    {
                        ServerInfo item = getItem(pos);
                        item.setSortOrder(item.getSortOrder() + 1);
                        reordered.add(item);
                    }
                }
                getListener().onReordered(reordered);
                super.onSorted(fromPosition, toPosition);
            }
            
            protected boolean onSingleTapConfirmed(View v, MotionEvent e)
            {
                int position = getPosition(v);
                if(position == ListView.INVALID_POSITION)
                {
                    return false;
                }
                
                if(position == getAddPosition())
                {
                    getListener().onAdd();
                    return true;
                }
                
                getListener().onSelected(getItem(position));
                
                return true;
            }
            
            protected boolean onLongPress(View v, MotionEvent e)
            {
                int position = getPosition(v);
                if(position == ListView.INVALID_POSITION || position == getAddPosition())
                {
                    return false;
                }
                
                return startDrag(v);
            }
        }
    }
}
