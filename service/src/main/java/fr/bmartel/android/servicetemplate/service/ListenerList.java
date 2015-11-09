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

import java.util.HashMap;

/**
 * Build a list of <T> object identified with a random string identifier
 *
 * @author Bertrand Martel
 */
public class ListenerList<T> {

    private HashMap<String, T> listenerMap = new HashMap<String, T>();

    private RandomGen randomGen = new RandomGen(15);

    public ListenerList() {
    }

    public String add(T obj) {
        String generatedId = randomGen.nextString();
        listenerMap.put(generatedId, obj);
        return generatedId;
    }

    public void remove(String id) {
        if (listenerMap.containsKey(id))
            listenerMap.remove(id);
    }

    public void clear() {
        listenerMap.clear();
    }

    public int size() {
        return listenerMap.size();
    }

    public HashMap<String, T> getMap() {
        return listenerMap;
    }

}
