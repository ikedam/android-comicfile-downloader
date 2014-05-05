package jp.ikedam.android.comicfiledownloader;

import java.net.URI;
import java.net.URISyntaxException;

import jp.ikedam.android.comicfiledownloader.model.ServerInfo;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class ServerInfoActivity extends Activity
{
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_info);
        
        if(savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ServerInfoFragment()).commit();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server_info, menu);
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
    public static class ServerInfoFragment extends Fragment
            implements OnCheckedChangeListener, OnClickListener
    {
        private ServerInfo serverInfo;
        
        private EditText serverNameEditText;
        private EditText serverUriEditText;
        private CheckBox authenticationCheckBox;
        private View authenticationBlock;
        private EditText usernameEditText;
        private EditText passwordEditText;
        private Button commitButton;
        
        public ServerInfoFragment()
        {
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_server_info,
                    container, false);
            serverNameEditText = (EditText)rootView.findViewById(R.id.serverNameEditText);
            serverUriEditText = (EditText)rootView.findViewById(R.id.serverUriEditText);
            authenticationCheckBox = (CheckBox)rootView.findViewById(R.id.authenticationCheckBox);
            authenticationBlock = rootView.findViewById(R.id.authenticationBlock);
            usernameEditText = (EditText)rootView.findViewById(R.id.usernameEditText);
            passwordEditText = (EditText)rootView.findViewById(R.id.passwordEditText);
            commitButton = (Button)rootView.findViewById(R.id.commitButton);
            
            authenticationCheckBox.setOnCheckedChangeListener(this);
            commitButton.setOnClickListener(this);
            
            serverInfo = null;
            
            if(getActivity().getIntent() != null)
            {
                serverInfo = (ServerInfo)getActivity().getIntent().getSerializableExtra(ServerInfo.Intent.EXTRA);
            }
            
            if(serverInfo == null)
            {
                serverInfo = new ServerInfo();
                commitButton.setText(getResources().getString(R.string.text_server_add));
            }
            else
            {
                commitButton.setText(getResources().getString(R.string.text_server_update));
            }
            
            return rootView;
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            poplulateFromModel();
            
            super.onActivityCreated(savedInstanceState);
        }
        
        protected void setError(int placeid, int messageid, Object ... args)
        {
            TextView errorText = (TextView)getView().findViewById(placeid);
            View v = errorText;
            while(v != null && !("ToggleOnError".equals(v.getTag())))
            {
                v = (v.getParent() instanceof View)?(View)v.getParent():null;
            }
            if(v == null)
            {
                return;
            }
            v.setVisibility(View.VISIBLE);
            
            if(args.length > 0)
            {
                errorText.setText(getResources().getString(messageid));
            }
            else
            {
                errorText.setText(getResources().getString(messageid, args));
            }
        }
        
        protected void resetError(int placeid)
        {
            TextView errorText = (TextView)getView().findViewById(placeid);
            View v = errorText;
            while(v != null && !("ToggleOnError".equals(v.getTag())))
            {
                v = (v.getParent() instanceof View)?(View)v.getParent():null;
            }
            if(v != null)
            {
                v.setVisibility(View.GONE);
            }
        }
        
        protected void poplulateFromModel()
        {
            serverNameEditText.setText(serverInfo.getServerName());
            serverUriEditText.setText(serverInfo.getServerUri());
            if(!TextUtils.isEmpty(serverInfo.getUsername()))
            {
                authenticationCheckBox.setChecked(true);
                authenticationBlock.setVisibility(View.VISIBLE);
                usernameEditText.setText(serverInfo.getUsername());
                passwordEditText.setText(serverInfo.getPassword());
            }
            else
            {
                authenticationCheckBox.setChecked(false);
                authenticationBlock.setVisibility(View.GONE);
                usernameEditText.setText(serverInfo.getUsername());
                passwordEditText.setText(serverInfo.getPassword());
            }
            suppressValidates();
        }
        
        protected boolean populateToModel()
        {
            if(!validate())
            {
                return false;
            }
            serverInfo.setServerName(serverNameEditText.getText().toString());
            serverInfo.setServerUri(serverUriEditText.getText().toString());
            if(authenticationCheckBox.isChecked())
            {
                serverInfo.setUsername(usernameEditText.getText().toString());
                serverInfo.setPassword(passwordEditText.getText().toString());
            }
            else
            {
                serverInfo.setUsername(null);
                serverInfo.setPassword(null);
            }
            return true;
        }
        
        protected void suppressValidates()
        {
            resetError(R.id.serverNameErrorText);
            resetError(R.id.serverUriErrorText);
            resetError(R.id.usernameErrorText);
            resetError(R.id.passwordErrorText);
        }
        
        protected boolean validate()
        {
            boolean result = true;
            suppressValidates();
            {
                String serverName = serverNameEditText.getText().toString();
                if(TextUtils.isEmpty(serverName))
                {
                    result = false;
                    setError(R.id.serverNameErrorText, R.string.error_required);
                }
            }
            {
                String serverUri = serverUriEditText.getText().toString();
                if(TextUtils.isEmpty(serverUri))
                {
                    result = false;
                    setError(R.id.serverUriErrorText, R.string.error_required);
                }
                else
                {
                    try
                    {
                        URI uri = new URI(serverUri);
                        if(!uri.isAbsolute())
                        {
                            result = false;
                            setError(R.id.serverUriErrorText, R.string.error_invalid_uri);
                        }
                        else if(
                                !"http".equalsIgnoreCase(uri.getScheme())
                                && !"https".equalsIgnoreCase(uri.getScheme())
                        )
                        {
                            result = false;
                            setError(R.id.serverUriErrorText, R.string.error_invalid_schema);
                        }
                    }
                    catch(URISyntaxException e)
                    {
                        result = false;
                        setError(R.id.serverUriErrorText, R.string.error_invalid_uri);
                    }
                }
            }
            if(authenticationCheckBox.isChecked())
            {
                {
                    String username = usernameEditText.getText().toString();
                    if(TextUtils.isEmpty(username))
                    {
                        result = false;
                        setError(R.id.usernameErrorText, R.string.error_required);
                    }
                }
                {
                    String password = passwordEditText.getText().toString();
                    if(TextUtils.isEmpty(password))
                    {
                        result = false;
                        setError(R.id.passwordErrorText, R.string.error_required);
                    }
                }
            }
            return result;
        }
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if(buttonView.equals(authenticationCheckBox))
            {
                authenticationBlock.setVisibility(isChecked?View.VISIBLE:View.GONE);
            }
        }
        
        @Override
        public void onClick(View v)
        {
            if(v.equals(commitButton))
            {
                if(populateToModel())
                {
                    Intent intent = new Intent();
                    intent.putExtra(ServerInfo.Intent.EXTRA, serverInfo.clone());
                    getActivity().setResult(ServerInfo.Intent.RESPONSECODE_OK, intent);
                    getActivity().finish();
                }
            }
        }
    }
}
