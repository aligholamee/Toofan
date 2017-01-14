package ir.actfun.toofan;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Ali Gholami on 8/8/2016.
 */
public class Nastaligh extends TextView {
    public Nastaligh(Context context, AttributeSet attributeSet)
    {

        super(context,attributeSet);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/nastaligh.ttf"));


    }
}
