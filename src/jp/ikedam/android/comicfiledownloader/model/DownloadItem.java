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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DownloadItem
{
    public static enum DownloadItemType {
        Folder,
        File,
    };
    
    private static final Pattern TITLEPATTERN = Pattern.compile("^\\s*\\[\\s*(.*?)\\s*\\]\\s*(.*)\\s*$");
    private static final Pattern FILEPATTERN = Pattern.compile("^(.*)\\.zip$", Pattern.CASE_INSENSITIVE);
    
    private String title;
    private String author;
    private String uri;
    private DownloadItemType type;
    
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }
    /**
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }
    /**
     * @return the uri
     */
    public String getUri()
    {
        return uri;
    }
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }
    
    /**
     * @return the type
     */
    public DownloadItemType getType()
    {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(DownloadItemType type)
    {
        this.type = type;
    }
    public static DownloadItem newInstanceFromText(String text)
    {
        DownloadItem item = new DownloadItem();
        
        {
            Matcher m = TITLEPATTERN.matcher(text);
            if(m.find())
            {
                item.setAuthor(m.group(1));
                text = m.group(2);
            }
            else
            {
                item.setAuthor(null);
            }
        }
        
        {
            Matcher m = FILEPATTERN.matcher(text);
            if(m.find())
            {
                item.setTitle(m.group(1));
                item.setType(DownloadItemType.File);
            }
            else
            {
                item.setTitle(text);
                item.setType(DownloadItemType.Folder);
            }
        }
        
        return item;
    }
}
