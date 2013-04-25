/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License according to
 * http://www.otm.uiuc.edu/faculty/forms/opensource.asp
 * 
 * Copyright (c) 2006,    NCSA/UIUC.  All rights reserved.
 * 
 * Developed by:
 * 
 * Name of Development Groups:
 * Image Spatial Data Analysis Group (ISDA Group)
 * http://isda.ncsa.uiuc.edu/
 * 
 * Name of Institutions:
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 *   Neither the names of University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.isda.im2learn.core.display;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.TimerTask;

/**
 * Create a modal dialog, blocking all user input except for cancle, and if that
 * does not work, quit. This class can be used when running a long computation
 * task. A dialog will be placed over the frame blocking all user input. This
 * class implements the progressListener interface. Calling this from within the
 * task will update the progress bar giving additional feedback to the user.
 * Following is a simple example of the use of this class.
 * <p/>
 * <pre>
 *   ...
 *   ProgressBlocker blocker = new ProgressBlocker("Calculating something");
 *   blocker.showDialog(new ProgressBlockerRun() {
 *     public void run(ProgressBlocker blocker) throws Exception {
 *       for(i=0; i<1000; i++) {
 *         blocker.progress(i, 1000);
 *         ... compute something ...
 *       }
 *     }
 *   });
 * </pre>
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class ProgressBlocker extends JDialog implements ProgressListener {
    private JLabel lblMessage;
    private JProgressBar pbStatus;
    private Thread thrUser;
    private JButton btnQuit;

    private Log logger = LogFactory.getLog(ProgressBlocker.class);

    /**
     * Create default blocker that will sit in the middle of the screen with a
     * Please wait ... message.
     */
    public ProgressBlocker() {
        this(null, "Please wait ...");
    }

    /**
     * Create default blocker that will sit in the middle of the frame with a
     * Please wait ... message.
     *
     * @param owner the frame of which this dialog belongs to.
     */
    public ProgressBlocker(Frame owner) {
        this(owner, "Please wait ...");
    }

    /**
     * Create default blocker that will sit in the middle of the screen with the
     * given message.
     *
     * @param message the message to be displayed.
     */
    public ProgressBlocker(String message) {
        this(null, message);
    }

    /**
     * Create the blocker that sits in the middle of the given frame, showing
     * the given message.
     *
     * @param owner the frame to whom this blocker belongs.
     * @param message the message to be shown.
     */
    public ProgressBlocker(Frame owner, String message) {
        super(owner, "Progress Blocker", true);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pbStatus = new JProgressBar();
        getContentPane().add(pbStatus, BorderLayout.CENTER);

        lblMessage = new JLabel(message);
        getContentPane().add(lblMessage, BorderLayout.NORTH);

        buttons.add(new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 5 && thrUser != null; i++) {
                    thrUser.interrupt();
                    try {
                        thrUser.join(500);
                    } catch (InterruptedException exc) {
                        logger.debug("Exception in join.", exc);
                    }
                }
                if (thrUser == null) {
                    setVisible(false);
                } else {
                    btnQuit.setVisible(true);
                }
            }
        }));
        btnQuit = new JButton(new AbstractAction("Quit") {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 5 && thrUser != null; i++) {
                    thrUser.interrupt();
                    try {
                        thrUser.join(500);
                    } catch (InterruptedException exc) {
                        logger.debug("Exception in join.", exc);
                    }
                }
                if (thrUser == null) {
                    setVisible(false);
                } else {
                    System.exit(0);
                }
            }
        });
        btnQuit.setVisible(false);
        buttons.add(btnQuit);

        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pack();
    }

    /**
     * Change the message that is displayed.
     *
     * @param message the new message to be displayed.
     */
    public void setMessage(String message) {
        lblMessage.setText(message);
    }

    /**
     * Shows the blocker while the passed in function is running. Once the
     * passed in function is finished the dialog will disappear and this
     * function will return.
     *
     * @param runme the code to be executed.
     * @throws RuntimeException thrown if another thread is already running.
     */
    public void showDialog(ProgressBlockerRun runme) throws RuntimeException {
        if (thrUser != null) {
            throw(new RuntimeException("Already a thread running."));
        }

        // change the cursor
        if (getOwner() != null) {
            getOwner().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        // start the blocking function
        thrUser = new Thread(new UserThread(runme));
        thrUser.setName("Im2Learn - Blocker Thread");
        thrUser.start();

        // start a timer
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new ProgressTimer(), 500, 500);

        // show the dialog that will block any UI
        setLocationRelativeTo(getOwner());
        setVisible(true);

        // return to the normal cursor
        if (getOwner() != null) {
            getOwner().setCursor(Cursor.getDefaultCursor());
        }

        // stop the timer
        timer.cancel();
    }

    /**
     * Calling this function will update the progressbar in the blocker, giving
     * the user an indication of the progress.
     *
     * @param processed how much work has been done.
     * @param total the total amount of work to be done.
     */
    public void progress(int processed, int total) {
        pbStatus.setMaximum(total);
        pbStatus.setValue(processed);

        pbStatus.setStringPainted(true);
    }

    /**
     * Helper class to run the usercode.
     */
    class UserThread implements Runnable {
        private ProgressBlockerRun runme;

        public UserThread(ProgressBlockerRun runme) {
            this.runme = runme;
        }

        public void run() {
        	Throwable exception = null;
            try {
                runme.run(ProgressBlocker.this);
            } catch (Throwable thr) {
            	exception = thr;
            } finally {
                setVisible(false);
                thrUser = null;
            }
            if (exception != null) {
            	logger.warn("Error in blocking function.", exception);
            }
        }

    }

    /**
     * Timer task that checks to see if thread is done. This is to prevent the
     * dialog from showing after the user thread is done showing the blocking
     * dialog and never hiding it.
     */
    class ProgressTimer extends TimerTask {
        public void run() {
            if (thrUser == null) {
                setVisible(false);
            }
        }
    }
}
