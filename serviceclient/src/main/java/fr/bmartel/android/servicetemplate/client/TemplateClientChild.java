/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.servicetemplate.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Child activity called from root activity
 *
 * @author Bertrand Martel
 */
public class TemplateClientChild extends Activity {

    private String TAG = TemplateClientChild.class.getName();

    private boolean gotoRoot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        Button button2ndLevel = (Button) findViewById(R.id.button2ndlevel);
        button2ndLevel.setEnabled(true);

        button2ndLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Button button2ndLevel = (Button) findViewById(R.id.button2ndlevel);
                        button2ndLevel.setEnabled(false);
                        onBackPressed();
                    }
                });
            }
        });
    }

    /**
     * refresh singleton listener when onResume is called (creation activity or returning from launcher)
     */
    private void refreshActivity() {

        Button button2ndLevel = (Button) findViewById(R.id.button2ndlevel);
        button2ndLevel.setEnabled(true);

        ServiceSingleton.getInstance().registerListener(new ISingletonListener() {
            @Override
            public void onPropertyValueChanged(final String value) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        TextView text = (TextView) findViewById(R.id.propertyValues);
                        if (text != null)
                            text.setText(text.getText() + "\n" + value);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gotoRoot = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        gotoRoot = false;
        refreshActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!gotoRoot)
            ServiceSingleton.getInstance().unbindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Child activity onDestroy");
        super.onDestroy();
    }
}
