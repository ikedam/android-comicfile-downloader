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

package jp.ikedam.android.comicfiledownloader;

import java.util.List;

import android.content.Context;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 *
 */
public abstract class DragSortableArrayAdapter<T> extends ArrayAdapter<T> implements OnDragListener, OnScrollListener
{
    public static class DragInfo
    {
        private final int fromPosition;
        
        /**
         * @return the fromPosition
         */
        public int getFromPosition()
        {
            return fromPosition;
        }
        
        public DragInfo(int fromPosition)
        {
            this.fromPosition = fromPosition;
        }
    }
    
    public static enum SwapType {
        SwapDragging,
        SwapDropped,
        SwapCanceled,
    };
    
    private ListView listView;
    private boolean scrolling = false;
    private boolean dragging = false;
    private int draggingFrom;
    private int draggingTo;
    
    /**
     * @return the listView
     */
    public ListView getListView()
    {
        return listView;
    }
    
    
    /**
     * @return the scrolling
     */
    public boolean isScrolling()
    {
        return scrolling;
    }
    
    /**
     * @param scrolling the scrolling to set
     */
    public void setScrolling(boolean scrolling)
    {
        this.scrolling = scrolling;
    }
    
    /**
     * @return the dragging
     */
    protected boolean isDragging()
    {
        return dragging;
    }
    
    /**
     * @param dragging the dragging to set
     */
    protected void setDragging(boolean dragging)
    {
        this.dragging = dragging;
    }
    
    /**
     * @return the draggingFrom
     */
    protected int getDraggingFrom()
    {
        return draggingFrom;
    }
    
    /**
     * @param draggingFrom the draggingFrom to set
     */
    protected void setDraggingFrom(int draggingFrom)
    {
        this.draggingFrom = draggingFrom;
    }
    
    /**
     * @return the draggingTo
     */
    protected int getDraggingTo()
    {
        return draggingTo;
    }
    
    /**
     * @param draggingTo the draggingTo to set
     */
    protected void setDraggingTo(int draggingTo)
    {
        this.draggingTo = draggingTo;
    }
    
    /**
     * @param position
     * @return
     */
    protected boolean isDragging(int position)
    {
        return isDragging() && position == getDraggingTo();
    }
    
    public DragSortableArrayAdapter(Context context, int resource,
            int textViewResourceId, List<T> objects)
    {
        super(context, resource, textViewResourceId, objects);
    }
    
    public void setupFor(ListView view)
    {
        listView = view;
        view.setAdapter(this);
        view.setOnDragListener(this);
        view.setOnScrollListener(this);
    }
    
    protected boolean swapItem(int fromPosition, int toPosition, SwapType swapType)
    {
        switch(swapType)
        {
        case SwapDragging:
        {
            if(!isSortable(toPosition))
            {
                break;
            }
            T item = getItem(fromPosition);
            remove(item);
            insert(item, toPosition);
            notifyDataSetChanged();
            return true;
        }
        case SwapDropped:
        {
            onSorted(fromPosition, toPosition);
            return true;
        }
        case SwapCanceled:
        {
            T item = getItem(fromPosition);
            remove(item);
            insert(item, toPosition);
            notifyDataSetChanged();
            listView.invalidate();
            return true;
        }
        }
        return false;
    }
    
    protected boolean isSortable(int position)
    {
        return true;
    }
    
    protected void onSorted(int fromPosition, int toPosition)
    {
    }
    
    /**
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount)
    {
    }
    
    /**
     * @param view
     * @param scrollState
     * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        setScrolling(scrollState != OnScrollListener.SCROLL_STATE_IDLE);
    }
    
    public boolean startDrag(View v)
    {
        if(isScrolling())
        {
            return false;
        }
        
        if(isDragging())
        {
            return false;
        }
        
        int position;
        try{
            position= listView.getPositionForView(v);
        }
        catch(NullPointerException e)
        {
            // This is a case view is already removed from list
            // (e.g. scrolled to outside of the screen)
            return false;
        }
        if(position == ListView.INVALID_POSITION)
        {
            return false;
        }
        
        v.startDrag(null, new DragShadowBuilder(v), new DragInfo(position), 0);
        
        return true;
    }
    
    /**
     * @param v
     * @param event
     * @return
     * @see android.view.View.OnDragListener#onDrag(android.view.View, android.view.DragEvent)
     */
    @Override
    public boolean onDrag(View v, DragEvent event)
    {
        switch(event.getAction())
        {
        case DragEvent.ACTION_DRAG_STARTED:
            return onDragStarted(v, event);
        case DragEvent.ACTION_DRAG_LOCATION:
            return onDragging(v, event);
        case DragEvent.ACTION_DROP:
            return onDragDropped(v, event);
        case DragEvent.ACTION_DRAG_ENDED:
            return onDragEnded(v, event);
        }
        return false;
    }
    
    protected boolean onDragStarted(View v, DragEvent event)
    {
        if(!(event.getLocalState() instanceof DragInfo))
        {
            return false;
        }
        if(isDragging())
        {
            return false;
        }
        int position = ((DragInfo)event.getLocalState()).getFromPosition();
        
        setDragging(true);
        setDraggingFrom(position);
        setDraggingTo(position);
        
        return true;
    }
    
    protected boolean onDragging(View v, DragEvent event)
    {
        if(!isDragging())
        {
            return false;
        }
        
        int position = getListView().pointToPosition((int)event.getX(), (int)event.getY());
        if(position == ListView.INVALID_POSITION || position == getDraggingTo())
        {
            return false;
        }
        
        if(swapItem(getDraggingTo(), position, SwapType.SwapDragging))
        {
            setDraggingTo(position);
        }
        return true;
    }
    
    protected boolean onDragDropped(View v, DragEvent event)
    {
        if(!isDragging())
        {
            return false;
        }
        
        int position = getListView().pointToPosition((int)event.getX(), (int)event.getY());
        if(position == ListView.INVALID_POSITION)
        {
            return false;
        }
        
        if(position != getDraggingTo())
        {
            if(swapItem(getDraggingTo(), position, SwapType.SwapDragging))
            {
                setDraggingTo(position);
            }
        }
        return true;
    }
    
    protected boolean onDragEnded(View v, DragEvent event)
    {
        if(!isDragging())
        {
            return false;
        }
        if(event.getResult())
        {
            swapItem(getDraggingFrom(), getDraggingTo(), SwapType.SwapDropped);
        }
        else
        {
            swapItem(getDraggingTo(), getDraggingFrom(), SwapType.SwapCanceled);
        }
        
        setDragging(false);
        listView.invalidateViews();
        return true;
    }
}
