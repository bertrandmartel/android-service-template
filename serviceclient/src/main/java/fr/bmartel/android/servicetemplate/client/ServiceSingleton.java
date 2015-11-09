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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fr.bmartel.android.servicetemplate.servicelib.IPropertyListener;
import fr.bmartel.android.servicetemplate.servicelib.IServiceTemplate;

/**
 * Singleton object used to bind with service and interact with activities
 *
 * @author Bertrand Martel
 */
public class ServiceSingleton {

    private static String TAG = ServiceSingleton.class.getName();

    private static ServiceSingleton instance = null;

    private ServiceConnection serviceConnection = null;

    /**
     * serviceTemplate binding
     */
    private IServiceTemplate serviceTemplate = null;

    /**
     * determine if service has been bound or not
     */
    private boolean bound = false;

    private Context context = null;

    /**
     * service package to bind
     */
    private static String SERVICE_PACKAGE = "fr.bmartel.android.servicetemplate.service";

    /**
     * service class to bind
     */
    private static String SERVICE_CLASS = "TemplateService";

    private ISingletonListener singletonListener = null;

    private ServiceSingleton() {
    }

    /**
     * retrieve an instance of singleton
     *
     * @return
     */
    public static ServiceSingleton getInstance() {

        if (instance == null) {
            Log.i(TAG, "create singleton wrapper");
            instance = new ServiceSingleton();
        }
        return instance;
    }

    /**
     * bind the service if not already bound
     *
     * @param context Application context
     * @return
     */
    public boolean bindService(Context context) {

        if (!bound) {

            Log.i(TAG, "binding service ...");

            this.context = context;

            Intent intent = buildExplicitIntent(SERVICE_PACKAGE, SERVICE_CLASS);

            serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    Log.i(TAG, "onServiceConnected");
                    serviceTemplate = IServiceTemplate.Stub.asInterface(service);

                    try {

                        String ret = serviceTemplate.registerListener(new IPropertyListener.Stub() {

                            @Override
                            public void onPropertyChange(final String propertyValue) {

                                Log.i(TAG, "onPropertyChange : " + propertyValue);
                                if (singletonListener != null)
                                    singletonListener.onPropertyValueChanged(propertyValue);

                            }
                        });

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "onServiceConnected end");
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                    Log.i(TAG, "onServiceDisconnected");
                    serviceTemplate = null;
                }
            };

            bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

            if (!bound) {
                Log.e(TAG, "Error cant bind to service !");
            }
        }
        return bound;
    }

    /**
     * Build an explicit intent from package name and service class name
     *
     * @param packageName
     * @param className
     * @return
     */
    public Intent buildExplicitIntent(String packageName, String className) {
        Intent ret = new Intent(packageName + "." + className);
        ret.setComponent(new ComponentName(packageName, packageName + "." + className));
        return ret;
    }

    /**
     * unbind from service
     */
    public void unbindService() {

        if (this.context != null && serviceConnection != null) {

            Log.i(TAG, "unbinding service ...");

            //if you remove serviceTemplate.removeListeners() listeners will be cleaned automatically when application will be killed
            try {
                if (serviceTemplate != null)
                    serviceTemplate.removeListeners();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            this.context.unbindService(serviceConnection);
            serviceConnection = null;

            bound = false;
        }
    }

    /**
     * register a listener between an activity and singleton
     *
     * @param singletonListener
     */
    public void registerListener(ISingletonListener singletonListener) {
        this.singletonListener = singletonListener;
    }
}
