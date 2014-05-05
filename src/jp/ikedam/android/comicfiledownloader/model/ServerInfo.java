/*
 * The MIT License
 * 
 * Copyright (c) 2014 IKEDA Yasuyuki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jp.ikedam.android.comicfiledownloader.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 *
 */
@DatabaseTable(tableName="server_info")
public class ServerInfo
{
    @DatabaseField(generatedId=true)
    private int id = -1;
    
    @DatabaseField(columnName="server_type", defaultValue="1")
    private int serverType;
    
    @DatabaseField(columnName="server_name")
    private String serverName;
    
    @DatabaseField(columnName="server_uri")
    private String serverUri;
    
    @DatabaseField(canBeNull=true)
    private String username;
    
    @DatabaseField(canBeNull=true)
    private String password;
    
    @DatabaseField(columnName="sort_order", index=true)
    private int sortOrder;
    
    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return serverName;
    }
    
    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }
    
    /**
     * @return the serverUri
     */
    public String getServerUri()
    {
        return serverUri;
    }
    
    /**
     * @param serverUri the serverUri to set
     */
    public void setServerUri(String serverUri)
    {
        this.serverUri = serverUri;
    }
    
    /**
     * @return the sortOrder
     */
    public int getSortOrder()
    {
        return sortOrder;
    }
    
    /**
     * @param order the sort order to set
     */
    public void setSortOrder(int sortOrder)
    {
        this.sortOrder = sortOrder;
    }
    
    @Override
    public String toString()
    {
        return getServerName();
    }
}
