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

/**
 *
 */
public class ServerInfo
{
    private String serverName;
    private String serverUri;
    
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
    
    @Override
    public String toString()
    {
        return getServerName();
    }
}
