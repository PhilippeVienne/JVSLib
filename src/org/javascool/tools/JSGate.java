/*
 * $file.name
 *     Copyright (C) 2012  Philippe VIENNE
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.javascool.tools;

// We use reflection instead import the class to not include plugin.jar in compilation path .
// And use reflection make that this code compile and rune even if we are not in a browser.
//      (Do not uncomment) import netscape.javascript.JSObject;

import org.json.simple.JSONArray;

import java.applet.Applet;
import java.lang.reflect.Method;

/** Java to JS Communication Gate.
 * This class allow an applet to trigger off jQuery Events on the web page. Of course you must have jQuery Lib on your web page, otherwise, errors are thrown.
 * @author Philippe VIENNE
 */
public class JSGate {

    /** The applet which is liked with this gateway. */
    private Applet applet;

    /** Build the gate.
     * @param applet The applet which will trig events
     */
    public JSGate(Applet applet){
        setApplet(applet);
    }

    /**
     * @see #applet
     */
    private Applet getApplet() {
        return applet;
    }

    /**
     * @see #applet
     */
    private void setApplet(Applet applet) {
        this.applet = applet;
    }

    /** Trigger off an jQuery Event on the HTML document.
     * @param event The name for event (e.g. myfamousjavaevent)
     * @param data The data to pass in event.data, You may pass a string, it's more secure (to not loose data).
     * @return true if there is no error and the hand is left to JS, false otherwise (print error out on {System.out}
     */
    public boolean triggerOff(String event, Object data){
        try{
            JSONArray d=new JSONArray();
            d.add(data);
            Method getWindow = null, evaluationMethod = null;
            Object JSObject = null;
            Class c = Class.forName("netscape.javascript.JSObject"); /* does it in IE too */
            Method ms[] = c.getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().compareTo("getWindow") == 0)
                    getWindow = ms[i];
                else if (ms[i].getName().compareTo("eval") == 0)
                    evaluationMethod = ms[i];
            }
            Object a[] = new Object[1];
            a[0] = getApplet();               /* this is the applet */
            JSObject = getWindow.invoke(c, a); /* this yields the JSObject */
            a[0] = "$(document).trigger(\""+event+"\","+d.toJSONString()+");";
            evaluationMethod.invoke(JSObject, a);
            return true;
        }catch(Exception e){
            e.printStackTrace(System.err);
            return false;
        }
    }
}
