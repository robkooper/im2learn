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
package edu.illinois.ncsa.isda.im2learn.ext.encryption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.*;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.misc.JSelectFile;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 *
	<p>
	<B>The class Steganography provides a tool for encrypting and decrypting text messages and images in
	another image. </B>
	 </p>
	 <p>
	 <b>Description:</b>
	
	 According to the web wikipedia: 
	 "Steganography is the art and science of writing hidden messages in such a way that no one apart from 
	 the intended recipient knows of the existence of the message; this is in contrast to cryptography, 
	 where the existence of the message is clear, but the meaning is obscured. ..."
	 <BR>
	 </p>
	 
	 <p>
	 This tool is allows a user to specify text messages and images that should be encrypted into the image shown in 
	 the main frame. The secret message is entered into the text area and the secret image is loaded 
	 by clicking the button "..." next to the field labeled as "Secret Image" (see the dialog below).
	 </p>
	 <img src="steganographyDialog.jpg">
	 <p>
	 The choice of encrypting secret text or secret image is made by clicking on the corresponding radio buttons.
	 The slider bar denoted as "# of bits" specifies how many least significant bits will be used for encryption. The entry 
	 labeled as "Offset" refers to the encryption offset from the left upper corner of the visible image. Decryptions of secret
	 text or secret image is succesful only if the "# of bits" and "Offset" parameters are consistent with the encryption.
	 The encryption is executed using the button "Encrypt" and the decryption is excuted using the button "Decrypt". The button
	 "Preview" allows a user to view the image in the main frame after encryption. The button "Apply" transfers the preview image into the main frame.
	 </p>
	 <BR>
	 The images below demonstrate three different settings of the "# of bits" entry with a fixed offset.
	 <BR>
	 <img src="steganographyDialog4.jpg">
<BR> Fig. 1: The cat image to be encrypted.
<BR>
	 <img src="steganographyDialog1.jpg">
	 <BR> Fig. 2: The resulting image after encryption with the cat image and # of bits equal to 8 bits (the original 
	 image contains 8 bits per pixel). The cat image encryption is visible.
	 <BR>
	 <img src="steganographyDialog2.jpg">
	 <BR> Fig. 3: The resulting image after encryption with the cat image and # of bits equal to 4 bits (the original 
	 image contains 8 bits per pixel). The cat image encryption is slightly visible.
	 <BR>
	 
	 <img src="steganographyDialog3.jpg">
<BR>
	 <BR> Fig. 4: The resulting image after encryption with the cat image and # of bits equal to 1 bit (the original 
	 image contains 8 bits per pixel). The cat image becomes completely invisible.
<BR>
<BR>

	 * @author Rob Kooper, Peter Bajcsy (documentation)
	 *
 */
public class SteganographyDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImagePanel imagepanel;
    private JTextArea txtMessage;
    private JSelectFile sfImage;
    private JRadioButton radMessage;
    private JRadioButton radImage;
    private ImageFrame frmPreview;
    private JSlider sldBits;
    private JTextField txtBits;
    private JTextField txtOffset;

    private JFrame frmSecretMessage;
    private JTextArea txtSecretMessage;
    private ImageFrame frmSecretImage;

    private Steganography steganography;

    static private Log logger = LogFactory.getLog(SteganographyDialog.class);

    public SteganographyDialog() {
        steganography = new Steganography();

        createUI();

        pack();
    }

    public void closing() {
        frmPreview.setVisible(false);
        frmPreview.setImageObject(null);

        frmSecretImage.setVisible(false);
        frmSecretImage.setImageObject(null);

        frmSecretMessage.setVisible(false);
        txtSecretMessage.setText("");

        sfImage.reset(true);
        txtMessage.setText("");
        sldBits.setValue(1);

        steganography.setOriginalImage(null);
        steganography.setSecretImage(null);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    private void createUI() {
        JScrollPane pane;

        frmPreview = new ImageFrame("Preview Generated Image");

        frmSecretImage = new ImageFrame("Secret Image");

        frmSecretMessage = new JFrame("Secret Message");
        txtSecretMessage = new JTextArea();
        pane = new JScrollPane(txtSecretMessage);
        pane.setPreferredSize(new Dimension(320, 240));
        frmSecretMessage.getContentPane().add(pane, BorderLayout.CENTER);
        frmSecretMessage.pack();

        // -------------------------------------------------------------------

        Box box = Box.createVerticalBox();
        getContentPane().add(box, BorderLayout.CENTER);

        // -------------------------------------------------------------------
        JPanel pnl = new JPanel(new BorderLayout());
        box.add(pnl);

        ButtonGroup group = new ButtonGroup();
        radMessage = new JRadioButton("Encrypt Message.", true);
        group.add(radMessage);
        pnl.add(radMessage, BorderLayout.NORTH);

        pnl.add(new JLabel("Secret Message :"), BorderLayout.WEST);

        txtMessage = new JTextArea(4, 30);
        pane = new JScrollPane(txtMessage,
                               JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                               JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pnl.add(pane, BorderLayout.CENTER);

        // -------------------------------------------------------------------
        pnl = new JPanel(new BorderLayout());
        box.add(pnl);

        radImage = new JRadioButton("Encrypt Image.", false);
        group.add(radImage);
        pnl.add(radImage, BorderLayout.NORTH);

        sfImage = new JSelectFile(new String[]{"Secret Image :"});
        pnl.add(sfImage, BorderLayout.CENTER);

        // -------------------------------------------------------------------
        pnl = new JPanel(new BorderLayout());
        box.add(pnl);

        pnl.add(new JLabel("# of bits :"), BorderLayout.WEST);

        txtBits = new JTextField("1");
        txtBits.setEditable(false);
        pnl.add(txtBits, BorderLayout.EAST);

        sldBits = new JSlider(1, 8, 1);
        sldBits.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtBits.setText(sldBits.getValue() + "");
            }
        });
        pnl.add(sldBits, BorderLayout.CENTER);

        // -------------------------------------------------------------------
        pnl = new JPanel(new BorderLayout());
        box.add(pnl);

        pnl.add(new JLabel("Offset :"), BorderLayout.WEST);

        txtOffset = new JTextField("0");
        pnl.add(txtOffset, BorderLayout.CENTER);

        
        final JCheckBox dist = new JCheckBox("Distribute", steganography.getDistribute());
        
        dist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	steganography.setDistribute(dist.isSelected()); 
            }
        });
        
        pnl.add(dist, BorderLayout.SOUTH);
        
        
        // -------------------------------------------------------------------
        JPanel buttons = new JPanel(new FlowLayout());
        getContentPane().add(buttons, BorderLayout.SOUTH);

        buttons.add(new JButton(new AbstractAction("Encrypt") {
            public void actionPerformed(ActionEvent e) {
                try {
                    int offset = Integer.parseInt(txtOffset.getText());
                    if (radMessage.isSelected()) {
                        steganography.setOriginalImage(imagepanel.getImageObject());
                        steganography.encryptText(txtMessage.getText(), sldBits.getValue(), offset);
                    } else if (radImage.isSelected()) {
                        steganography.setOriginalImage(imagepanel.getImageObject());
                        steganography.encryptImage(sfImage.getImageObject(0), sldBits.getValue(), offset);
                   }
                } catch (IOException exc) {
                    logger.error("Error encrypting message.", exc);
                }
            }
        }));

        buttons.add(new JButton(new AbstractAction("Decrypt") {
            public void actionPerformed(ActionEvent e) {
                try {
                    int offset = Integer.parseInt(txtOffset.getText());
                    steganography.setSecretImage(imagepanel.getImageObject());
                    Object obj = steganography.decryptObject(sldBits.getValue(), offset);
                    if (obj instanceof String) {
                        txtSecretMessage.setText((String) obj);
                        txtSecretMessage.setCaretPosition(0);
                        frmSecretMessage.setLocationRelativeTo(SteganographyDialog.this);
                        frmSecretMessage.setVisible(true);
                    } else if (obj instanceof ImageObject) {
                        frmSecretImage.setImageObject((ImageObject) obj);
                        frmSecretImage.setLocationRelativeTo(SteganographyDialog.this);
                        frmSecretImage.setVisible(true);
                    }
                } catch (IOException exc) {
                    logger.error("Error decrypting message.", exc);
                }
            }
        }));

        buttons.add(new JButton(new AbstractAction("Preview") {
            public void actionPerformed(ActionEvent e) {
                ImageObject secret = steganography.getSecretImage();
                if (secret != null) {
                    frmPreview.setImageObject(secret);
                    frmPreview.setVisible(true);
                }
            }
        }));

        buttons.add(new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                ImageObject secret = steganography.getSecretImage();
                if (secret != null) {
                    imagepanel.setImageObject(secret);
                }
            }
        }));
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu menu = new JMenu("Tools");

        JMenuItem item = new JMenuItem(new AbstractAction("Steganography") {
            public void actionPerformed(ActionEvent e) {
                Window win = SwingUtilities.getWindowAncestor(imagepanel);
                setLocationRelativeTo(win);
                setVisible(true);
                toFront();
            }
        });
        menu.add(item);

        return new JMenuItem[]{menu};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }
    
    public URL getHelp(String menu) {
        return getClass().getResource("help/SteganographyDialog.html");
    }
}
