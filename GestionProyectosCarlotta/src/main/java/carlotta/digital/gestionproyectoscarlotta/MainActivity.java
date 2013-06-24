package carlotta.digital.gestionproyectoscarlotta;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;

public class MainActivity extends Activity {

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);
        //Inicializar ActionBar
        initActionBar();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void initActionBar(){
        actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FE0000")));
    }
}
