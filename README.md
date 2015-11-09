# Android Service Oriented Architecture #


Project template featuring multi processing service oriented architecture in Android


## General architecture

![architecture](https://raw.github.com/akinaru/android-service-template/master/img/architecture.png)

Target architecture is multi process. One service running on one process and several clients than can potentially run on different process.

## Interface Description Language

Android Interface Description Language (AIDL) is necessary to ensure Inter Processing Communication (IPC).

These interfaces aim at defining common interface used by the service and clients :

```
interface IServiceTemplate {

	void setProperty(String value);

	String getProperty();

	String registerListener(IPropertyListener listener);

	void removeListener(String listenerId);

	void removeListeners();
}
```

These interfaces are located in a project named `servicelib` which only contains aidl files :

![servicelib](https://raw.github.com/akinaru/android-service-template/master/img/servicelib.png)

Service and all clients have in their dependency : 

```
dependencies {
	compile fileTree(dir: 'libs', include: ['*.jar'])
	compile project(':servicelib')
}
```

This way, service and clients will share the same interfaces.

`servicelib` is built as a library project. You will be able to import the android archive file to your client or giving it to someone who will use your service's API.

It is important to care about your library version that may break or provoke undefined behavior if your api change significatively from a version to another.

see http://developer.android.com/intl/zh-cn/guide/components/aidl.html for more information

## Project Structure

This is an Android Studio project featuring 3 modules : 

| module name             | Type           |   package name                                   |  comment                |
|-------------------------|----------------|--------------------------------------------------|-------------------------|
| service                 | application    |   fr.bmartel.android.servicetemplate.service     | service implementation  |
| serviceclient           | application    |   fr.bmartel.android.servicetemplate.client      | client implementation   |
| servicelib              | library        |   fr.bmartel.android.servicetemplate.servicelib  | interfaces (AIDL)       |


![project_structure](https://raw.github.com/akinaru/android-service-template/master/img/project_structure2.png)

## Multiple activities using the same service

There are several options for using a service across multiple activities : 

* you create a Singleton object that will assure one single instance to be used across activities and managing its lifecycle manually
* you bind to the service for each activity in onResume() or onCreate() and unbind in onPause()

In this project template, we are using singleton :

![singleton2Service](https://raw.github.com/akinaru/android-service-template/master/img/singleton2Service.png)

## Receive notifications from client

A service is useful to execute task and notify clients that execution is complete or to just notify all clients at once that one precise event has happened.

We can use listeners to be notified from client for events occuring on service. Each listener is identified by an identifier (for the user to be able to remove it at will) :

* Interface :

```
import fr.bmartel.android.servicetemplate.servicelib.IPropertyListener;

.......................

String registerListener(IPropertyListener listener);
```

* Service :

```
ListenerList<IPropertyListener> listenerList = propertyListenerList.get(callingPid);
listenerId = listenerList.add(listener);
```

=> ``callingPid`` is client application PID gotten from `Binder.getCallingPid()`
=> ``ListenerList`` is an object storing listener and attributing a random string value as identifier that will be returned to be able for the user to remove it

* Client :

```
IServiceTemplate serviceTemplate = IServiceTemplate.Stub.asInterface(service);

serviceTemplate.registerListener(new IPropertyListener.Stub() {

	@Override
	public void onPropertyChange(final String propertyValue) {

		Log.i(TAG, "onPropertyChange : " + propertyValue);

	}
});
```

## Issue regarding listeners

If application does not remove all listeners it has built, it may result with `DeadObjectException` when service dispatch a specific event.

Furthermore, in some version of Android, the system may stop/restart services which are not active. This way, if you have a service bound to another service, listeners wont be cleaned. Here is a way to workaround that issue :

Here ``propertyListenerList`` is of type : ``HashMap<Integer, ListenerList<IPropertyListener>>`` which stack a specific client PID for a list of ``IPropertyListener`` listener : 

```
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
```

We use a native method ``boolean nativeIsPid() `` to know if calling PID actually exists or not. POSIX function ``getpgid()`` is used natively to get that information.

## Stop/Install/Run script

Bash script `run-service.sh` will do the following things :


* stop services
* uninstall specified packages
* install specified packages
* start services / intent in the correct order

Usage :
```
./run-service.sh  <list of apk files space separated>
```

Option : ``-u`` before package specify package that need to be uninstalled before reinstallation

Exemple :

```
./run-service.sh  -u service-debug-1.0-09112015.apk -u  serviceclient-debug-1.0-09112015.apk
```

![script](https://raw.github.com/akinaru/android-service-template/master/img/script.png)

## Build

* To build service and client :

```
./gradlew clean build
```

* To build only service : 

```
./gradlew :service:clean :service:build
```

* To build only client 

```
./gradlew :serviceclient:clean :serviceclient:build
```

## Inheritance through AIDL

* A great post for using inheritance in AIDL : http://kevinhartman.github.io/blog/2012/07/23/inheritance-through-ipc-using-aidl-in-android/

It may be useful though you cant hide implementation if you are using inheritance in AIDL as your objects needs to be Serializable.

## External link

* AIDL doc : http://developer.android.com/intl/zh-cn/guide/components/aidl.html
