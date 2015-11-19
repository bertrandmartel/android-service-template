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
package fr.bmartel.android.servicetemplate.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.bmartel.android.servicetemplate.servicelib.IPropertyListener;
import fr.bmartel.android.servicetemplate.servicelib.IServiceTemplate;

/**
 * Service implementation
 *
 * @author Bertrand Martel
 */
public class TemplateService extends Service {

    private String TAG = TemplateService.class.getName();

    /**
     * task scheduler
     */
    private ScheduledThreadPoolExecutor threadPoolExecutor = null;

    private String property = "";

    private RandomGen randomGen = new RandomGen(15);

    /**
     * list of listener identified by a specific PID
     */
    private HashMap<Integer, ListenerList<IPropertyListener>> propertyListenerList = new HashMap<>();

    private ScheduledFuture<?> task = null;

    static {
        System.loadLibrary("pidutils");
    }

    /**
     * function used to know if PID exists or not
     *
     * @param pid process id
     * @return true if process exists / false if dont exist
     */
    private native boolean nativeIsPid(int pid);

    @Override
    public void onCreate() {

        super.onCreate();

        threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        //dispatch listener every 2 seconds for testing
        task = threadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                property = randomGen.nextString();
                dispatchPropertyListener(property);

            }
        }, 0, 2, TimeUnit.SECONDS);

        Log.i(TAG, "create template service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroy template service");

        if (task != null)
            task.cancel(true);

    }

    /**
     * Binder used by service
     */
    private IServiceTemplate.Stub templateService = new IServiceTemplate.Stub() {

        public void setProperty(String value) {
            TemplateService.this.property = value;
        }

        public String getProperty() {
            return property;
        }

        public String registerListener(IPropertyListener listener) {
            return registerPropertyListener(listener, getCallingPid());
        }

        public void removeListener(String listenerId) {
            removeListenerById(listenerId, getCallingPid());
        }

        public void removeListeners() {
            removeAllListener(getCallingPid());
        }
    };

    /**
     * Register a listener
     *
     * @param listener   listener to be registered
     * @param callingPid PID of application that called binder
     * @return listener identifier
     */
    private String registerPropertyListener(IPropertyListener listener, int callingPid) {

        String listenerId = "";

        if (propertyListenerList.containsKey(callingPid)) {

            ListenerList<IPropertyListener> listenerList = propertyListenerList.get(callingPid);
            listenerId = listenerList.add(listener);

        } else {

            ListenerList<IPropertyListener> listenerList = new ListenerList<>();
            listenerId = listenerList.add(listener);
            propertyListenerList.put(callingPid, listenerList);

        }
        return listenerId;
    }

    /**
     * Dispatch a value in complete list of listeners
     *
     * @param value value to be dispatched
     */
    private void dispatchPropertyListener(String value) {

        try {
            Iterator it = propertyListenerList.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<Integer, ListenerList<IPropertyListener>> pidMap = (Map.Entry) it.next();

                ListenerList<IPropertyListener> listenersList = pidMap.getValue();

                if (listenersList != null) {

                    if (nativeIsPid(pidMap.getKey())) {

                        Iterator it2 = listenersList.getMap().entrySet().iterator();
                        while (it2.hasNext()) {
                            Map.Entry<String, IPropertyListener> listenerMap = (Map.Entry) it2.next();
                            listenerMap.getValue().onPropertyChange(value);
                        }
                    } else {
                        Log.i(TAG, "process " + pidMap.getKey() + " doesnt exist anymore. Removing all listeners associated to it.");
                        it.remove();
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a listener by identifier for a specific PID
     *
     * @param listenerId listener identifier
     * @param pid        process id that created that listener
     */
    private void removeListenerById(String listenerId, int pid) {

        if (propertyListenerList.containsKey(pid)) {

            ListenerList<IPropertyListener> listenerList = propertyListenerList.get(pid);

            Iterator it = listenerList.getMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, IPropertyListener> listenerMap = (Map.Entry) it.next();
                if (listenerMap.getKey().equals(listenerId))
                    it.remove();
            }
        }
    }

    /**
     * Remove all listener associated to a PID
     *
     * @param pid
     */
    private void removeAllListener(int pid) {
        propertyListenerList.remove(pid);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return templateService;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
