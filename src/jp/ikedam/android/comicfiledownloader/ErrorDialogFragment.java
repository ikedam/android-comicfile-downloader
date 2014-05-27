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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;

/**
 *
 */
public class ErrorDialogFragment extends DialogFragment
{
    private String title;
    private String message;
    private View.OnClickListener okOnClickListener;
    
    public ErrorDialogFragment setTitle(String title)
    {
        this.title = title;
        return this;
    }
    
    public ErrorDialogFragment setMessage(String message)
    {
        this.message = message;
        return this;
    }
    
    public ErrorDialogFragment setOkOnClickListener(View.OnClickListener okOnClickListener)
    {
        this.okOnClickListener = okOnClickListener;
        return this;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(title != null)
        {
            builder.setTitle(title);
        }
        if(message != null)
        {
            builder.setMessage(message);
        }
        builder.setPositiveButton(R.string.dialog_ok, new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if(okOnClickListener != null)
                    {
                        okOnClickListener.onClick(getView());
                    }
                }
        });
        return builder.create();
    }
}
