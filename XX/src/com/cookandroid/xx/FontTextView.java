package com.cookandroid.xx;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class FontTextView extends TextView{
	
	public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeface(context, attrs);
    }
 
    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeface(context, attrs);
    }
 
    public FontTextView(Context context) {
        super(context);
    }
    
    private void applyTypeface(Context context, AttributeSet attrs){
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.StyledButton);
        String typefaceName = arr.getString(R.styleable.StyledButton_typeface);
        Typeface typeface = null;
        try{
            typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            setTypeface(typeface);
        }catch(Exception e){
            e.printStackTrace();
        }   
    }

}
