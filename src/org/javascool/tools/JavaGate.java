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

import javax.swing.*;
import java.applet.Applet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Java Gateway Applet Canvas.
 * This class is a kind of Applet designed to talk with JavaScript. So this applet implement security manager (We are on web so secure all) and an JSGate to send event to JS.
 *
 * @author Philippe Vienne
 */
public class JavaGate extends Applet implements SystemOutputController.SystemOutputListener {

    protected SystemOutputController systemOutputController = new SystemOutputController();
    protected JSGate jsGate;

    /**
     * Send a print out to browser.
     *
     * @param out The data to print into console
     */
    @Override
    public void print(String out) {
        jsGate.triggerOff("java.System.out", out);
    }

    /**
     * Execute an Runnable in a new thread.
     *
     * @param run The runnable to put in the thread
     */
    protected void runInNewThread(Runnable run) {
        Thread thread = new Thread(run);
        thread.run();
    }

    /**
     * Execute an Runnable in a new thread with All rights on system like full file access (beware jar has to be signed).
     *
     * @param run The runnable to put in the all rights thread
     */
    protected void runInNewThreadWithAllRights(final Runnable run) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AccessController.doPrivileged(
                        new PrivilegedAction<Integer>() {
                            public Integer run() {
                                run.run();
                                return 0;
                            }
                        });
            }
        });
        thread.run();
    }

    /**
     * Pop the last Java exception which happened
     *
     * @return The Exception
     */
    public Exception popException() {
        if (lastError != null) {
            Exception e = lastError;
            lastError = null;
            return e;
        }
        return null;
    }

    private Exception lastError;

    /**
     * Add a new error in Applet.
     *
     * @param e The Exception to give to JavaScript
     */
    protected void popException(Exception e) {
        lastError = e;
    }

    /**
     * Create a File object from the Path.
     *
     * @param path
     * @return
     */
    protected File getFile(final String path) {
        return AccessController.doPrivileged(
                new PrivilegedAction<File>() {
                    public File run() {
                        return new File(path);
                    }
                }
        );
    }

    /**
     * Create a temporary File object with a content
     *
     * @param content The data to write into
     * @param suffix  The extension ( '.java' e.g.)
     * @param prefix  The prefix for the file
     * @return The path of file
     */
    protected String getTmpFile(final String content, final String suffix, final String prefix) {
        return AccessController.doPrivileged(
                new PrivilegedAction<String>() {
                    public String run() {
                        try {
                            // Create temp file.
                            File temp = File.createTempFile("javaTempFile", ".java");

                            // Delete temp file when program exits.
                            temp.deleteOnExit();

                            if (content != null) {
                                // Write to temp file
                                BufferedWriter out = new BufferedWriter(new FileWriter(temp));
                                out.write(content);
                                out.close();
                            }

                            return temp.toString();
                        } catch (IOException e) {
                            throw new IllegalStateException("Can't create a tempory file", e);
                        }
                    }
                }
        );
    }

    /**
     * Security flag.
     * If the applet have to be locked for security reasons, put this variable to true.
     */
    protected boolean appletLocked = false;

    /**
     * Initialize the applet.
     * This function will check if we are in a safe environment.
     * NOTE: You can edit it to adapt to your own environment
     */
    @Override
    public void init() {
        if (!getCodeBase().getProtocol().equals("file")) {
            appletLocked = true;
            return;
        }
        systemOutputController.startListening(this);
        jsGate = new JSGate(this);
    }

    @Override
    public void stop() {
        systemOutputController.stopListening();
    }

    /**
     * Message non-spam flag.
     * This flag is used to be sure that we show the security message only one time.
     */
    protected boolean showMessage = true;

    /**
     * Perform a security assertion.
     * If the applet is locked, this function will interrupt the current thread.
     *
     * @throws SecurityException
     */
    protected void assertSafeUsage() {
        if (appletLocked) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "This website (" + getCodeBase().getHost() + ") tried to hack" +
                        " your computer by accessing to the local file system (Attack stopped)", "Error",
                        JOptionPane.ERROR_MESSAGE);
                showMessage = false;
            }
            SecurityException e = new SecurityException("This website is not authorized to use this applet");
            popException(e);
            throw e;
        } else {
        }
    }


}

