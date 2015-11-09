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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Bertrand Martel
 */
public class TemplateClientRoot extends Activity {

    private boolean goTo2ndLevel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_root);

        Button button2ndLevel = (Button) findViewById(R.id.button2ndlevel);

        button2ndLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Button button2ndLevel = (Button) findViewById(R.id.button2ndlevel);
                        button2ndLevel.setEnabled(false);
                        goTo2ndLevel = true;
                        Intent intent = new Intent(TemplateClientRoot.this, TemplateClientChild.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                });
            }
        });

        ServiceSingleton.getInstance().bindService(this);
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

                        final TextView text = (TextView) findViewById(R.id.propertyValues);
                        if (text != null)
                            text.setText(text.getText() + "\n" + value);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActivity();
        goTo2ndLevel = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!goTo2ndLevel)
            ServiceSingleton.getInstance().unbindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
