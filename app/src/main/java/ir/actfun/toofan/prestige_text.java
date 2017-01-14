package ir.actfun.toofan;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Ali Gholami on 8/8/2016.
 */
public class prestige_text extends TextView {
    public prestige_text(Context context, AttributeSet attributeSet)
    {

        super(context,attributeSet);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/prestige.ttf"));


    }
}
