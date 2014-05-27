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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

/**
 *
 */
public class ProgressDialogFragment extends DialogFragment
{
    private String title;
    private String message;
    private View.OnClickListener cancelOnClickListener;
    private ProgressDialog progressDialog;
    private int currentProgress;
    private int maxProgress;
    
    public ProgressDialogFragment setTitle(String title)
    {
        this.title = title;
        return this;
    }
    
    public ProgressDialogFragment setMessage(String message)
    {
        this.message = message;
        return this;
    }
    
    public ProgressDialogFragment setMaxProgress(int maxProgress)
    {
        this.maxProgress = maxProgress;
        updateProgress();
        return this;
    }
    
    public ProgressDialogFragment setCurrentProgress(int currentProgress)
    {
        this.currentProgress = currentProgress;
        updateProgress();
        return this;
    }
    
    public ProgressDialogFragment setProgress(int currentProgress, int maxProgress)
    {
        this.maxProgress = maxProgress;
        this.currentProgress = currentProgress;
        updateProgress();
        return this;
    }
    
    public ProgressDialogFragment setCancelOnClickListener(View.OnClickListener cancelOnClickListener)
    {
        this.cancelOnClickListener = cancelOnClickListener;
        return this;
    }
    
    protected ProgressDialogFragment updateProgress()
    {
        if(progressDialog != null)
        {
            if(maxProgress <= 0)
            {
                progressDialog.setIndeterminate(true);
            }
            else
            {
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(maxProgress);
                progressDialog.setProgress(currentProgress);
            }
        }
        return this;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(title);
        if(this.message != null)
        {
            progressDialog.setMessage(message);
        }
        if(this.cancelOnClickListener != null)
        {
            progressDialog.setButton(
                    Dialog.BUTTON_NEGATIVE,
                    getResources().getString(R.string.dialog_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            onCancel(dialog);
                        }
                    }
            );
        }
        updateProgress();
        
        return progressDialog;
    }
    
    @Override
    public void onCancel(DialogInterface dialog)
    {
        if(cancelOnClickListener != null)
        {
            cancelOnClickListener.onClick(getView());
        }
    }
}
