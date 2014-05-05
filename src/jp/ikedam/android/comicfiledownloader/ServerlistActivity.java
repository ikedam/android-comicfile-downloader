package jp.ikedam.android.comicfiledownloader;

import java.util.ArrayList;
import java.util.List;

import jp.ikedam.android.comicfiledownloader.model.ServerInfo;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

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
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ServerlistFragment extends ListFragment
    {
        public ServerlistFragment()
        {
        }
        
        protected List<ServerInfo> loadServerInfoList()
        {
            String[] serverNameList = getResources().getStringArray(R.array.testServerNameList);
            List<ServerInfo> serverInfoList = new ArrayList<ServerInfo>(serverNameList.length);
            for(String serverName: serverNameList)
            {
                ServerInfo info = new ServerInfo();
                info.setServerName(serverName);
                serverInfoList.add(info);
            }
            return serverInfoList;
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
            new ServerlistAdapter(getActivity(), loadServerInfoList()).setupFor(getListView());
            
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
                        public boolean onSingleTapUp(MotionEvent e)
                        {
                            return ServerlistAdapter.this.onSingleTapUp(target, e);
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
            
            public ServerlistAdapter(Context c, List<ServerInfo> serverInfoList)
            {
                super(c, R.layout.item_serverlist, R.id.serverNameView, serverInfoList);
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
                return (pos != null && pos instanceof Integer)?(Integer)pos:-1;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
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
                return view;
            }
            
            protected View getServerView(int position, View convertView,
                    ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                if(view != convertView)
                {
                    // It's not reasonable to set listener every time.
                    view.setOnTouchListener(new GestureEventConverter(view));
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
            }
            
            protected boolean onSingleTapUp(View v, MotionEvent e)
            {
                return false;
            }
            
            protected boolean onLongPress(View v, MotionEvent e)
            {
                int position = getPosition(v);
                if(position < 0 || position == getAddPosition())
                {
                    return false;
                }
                
                return startDrag(v);
            }
        }
    }
}
