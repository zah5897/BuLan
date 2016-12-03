package com.mingmay.bulan.view;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.text.Editable;
import android.text.Html.TagHandler;

public class MxgsaTagHandler implements TagHandler{
    private int sIndex = 0;  
    private  int eIndex=0;
    private final Context mContext;
    
    public MxgsaTagHandler(Context context){
        mContext=context;
    }
    
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        // TODO Auto-generated method stub
        if (tag.toLowerCase().equals("mxgsa")) {
            if (opening) {
                sIndex=output.length();
            }else {
                eIndex=output.length();
            }
        }
    }
   

}