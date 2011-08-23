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
package edu.illinois.ncsa.isda.imagetools.ext.camera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;
import edu.illinois.ncsa.isda.imagetools.core.display.ProgressBlocker;
import edu.illinois.ncsa.isda.imagetools.core.display.ProgressBlockerRun;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * Options pane for the Indigo Omega camera.
 */
public class IndigoOmegaCameraOptions extends JDialog {
    private IndigoOmegaCamera camera;
    private boolean boolStatus;
    private NumberFormat nf;

    private JLabel lblStatus;

    private JCheckBox chkFCC;
    private JTextField txtCounter;
    private JTextField txtTemperature;

    private ImageComponent icPreview;

    private JCheckBox chkDigitalOutput;

    private JComboBox cmbImageOptimization;
    private JSpinner spinContrast;
    private JSpinner spinBrightness;
    private JSpinner spinBrightnessBias;

    private JComboBox cmbDynamicRange;
    private JComboBox cmbVideoMode;
    private JComboBox cmbTestPattern;
    private JCheckBox chkImageOrientation;
    private JCheckBox chkPolarity;
    private JComboBox cmbSymbology;
    private JComboBox cmbFrameScale;
    private JTextField txtCaseTemp;
    private JComboBox cmbCaseScale;

    static private Log logger = LogFactory.getLog(IndigoOmegaCameraOptions.class);

    public IndigoOmegaCameraOptions(IndigoOmegaCamera camera) {
        super((Frame) null, "Indigo Camera Options", false);
        setResizable(false);

        this.camera = camera;

        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        createUI();
        pack();
    }

    public void show() {
        resetUI();
        super.show();
    }

    private void createUI() {
        getContentPane().setLayout(new BorderLayout());

        lblStatus = new JLabel(camera.toString());
        getContentPane().add(lblStatus, BorderLayout.SOUTH);

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

        JPanel panel = new JPanel(new GridBagLayout());

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        panel.add(getFCC(), gc0);
        panel.add(getImageOptimization(), gc1);

        gc0.gridy = 1;
        gc1.gridy = gc0.gridy;
        panel.add(getPreview(), gc0);
        panel.add(getMisc(), gc1);

        gc0.gridy = 2;
        gc1.gridy = gc0.gridy;
        panel.add(getDigitalOutput(), gc0);
        panel.add(getButtons(), gc1);

        getContentPane().add(panel, BorderLayout.CENTER);
    }

    private void resetUI() {
        boolStatus = true;

        chkFCC.setSelected(camera.getFCCMode() == IndigoOmegaCamera.FCC_AUTOMATIC);
        try {
            txtCounter.setText(nf.format(camera.getFCCPeriod() / 30.0f));
        } catch (IOException exc) {
            logger.info("Could not read FCC period.");
        }
        try {
            txtTemperature.setText("" + camera.getFCCTempDelta());
        } catch (IOException exc) {
            logger.info("Could not read FCC temperature.");
        }

        chkDigitalOutput.setSelected(camera.getDigitalOutputMode() == IndigoOmegaCamera.DIGITAL_OUTPUT_14_BIT);

        cmbImageOptimization.setSelectedIndex(camera.getImageOptimization());
        try {
            spinContrast.setValue(new Integer(camera.getContrast()));
        } catch (IOException exc) {
            logger.info("Could not read contrast.");
        }
        try {
            spinBrightness.setValue(new Integer(camera.getBrightness()));
        } catch (IOException exc) {
            logger.info("Could not read brightness.");
        }
        try {
            spinBrightnessBias.setValue(new Integer(camera.getBrightnessBias()));
        } catch (IOException exc) {
            logger.info("Could not read brightness bias.");
        }

        cmbDynamicRange.setSelectedIndex(camera.getDynamicRangeControlMode());
        cmbVideoMode.setSelectedIndex(camera.getAnalogVideoMode());
        cmbTestPattern.setSelectedIndex(camera.getTestPattern());
        chkImageOrientation.setSelected(camera.getImageOrientation() == IndigoOmegaCamera.IMAGE_ORIENTATION_NORMAL);
        chkPolarity.setSelected(camera.getPolarity() == IndigoOmegaCamera.POLARITY_WHITE_HOT);
        cmbSymbology.setSelectedIndex(camera.getSymbolColor());
        cmbFrameScale.setSelectedIndex(camera.getFrameScale());

        boolStatus = false;
    }

    private JPanel getFCC() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Flat Field Correction"));

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc2 = new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 2;
        panel.add(new JLabel("Flat Field Correction Mode"), gc0);
        chkFCC = new JCheckBox("Automatic FCC?");
        chkFCC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    if (chkFCC.isSelected()) {
                        camera.setFCCMode(IndigoOmegaCamera.FCC_AUTOMATIC);
                    } else {
                        camera.setFCCMode(IndigoOmegaCamera.FCC_MANUAL);
                    }
                    lblStatus.setText("FCC set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set FCC.", exc);
                }
            }
        });
        panel.add(chkFCC, gc1);

        gc0.gridy = 1;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 1;
        panel.add(new JLabel("Counter"), gc0);
        txtCounter = new JTextField(5);
        txtCounter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    float x = Float.parseFloat(txtCounter.getText());
                    camera.setFCCPeriod((int) (x * 30));
                    lblStatus.setText("FCC Counter set successfully");
                } catch (NumberFormatException exc) {
                    logger.warn("Could not set FCC Counter.", exc);
                } catch (IOException exc) {
                    logger.warn("Could not set FCC Counter.", exc);
                }

            }
        });
        panel.add(txtCounter, gc1);
        panel.add(new JLabel("seconds"), gc2);

        gc0.gridy = 2;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Temperature Change"), gc0);
        txtTemperature = new JTextField(5);
        txtTemperature.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    float x = Float.parseFloat(txtTemperature.getText());
                    camera.setFCCTempDelta(x);
                    lblStatus.setText("FCC Temperature set successfully");
                } catch (NumberFormatException exc) {
                    logger.warn("Could not set FCC Temperature.", exc);
                } catch (IOException exc) {
                    logger.warn("Could not set FCC Temperature.", exc);
                }

            }
        });
        panel.add(txtTemperature, gc1);
        panel.add(new JLabel("Celsius"), gc2);

        gc0.gridy = 3;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc0.gridwidth = 3;
        gc0.anchor = GridBagConstraints.CENTER;
        JButton btn = new JButton(new AbstractAction("Do FCC") {
            public void actionPerformed(ActionEvent e) {
                try {
                    camera.doFCC();
                    lblStatus.setText("FCC successfully");
                } catch (IOException exc) {
                    logger.warn("Could not do FCC.", exc);
                }
            }
        });
        panel.add(btn, gc0);

        return panel;
    }

    private JPanel getPreview() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Preview"));

        icPreview = new ImageComponent();
        icPreview.setPreferredSize(new Dimension(170, 130));
        panel.add(icPreview, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());
        panel.add(buttons, BorderLayout.SOUTH);

        JButton btn = new JButton(new AbstractAction("Preview") {
            public void actionPerformed(ActionEvent e) {
                ProgressBlocker pb = new ProgressBlocker(null, "Reading image from camera.");
                camera.addProgressListener(pb);
                pb.showDialog(new ProgressBlockerRun() {
                    public void run(ProgressBlocker blocker) throws Exception {
                        icPreview.setImageObject(camera.getFrame());
                        lblStatus.setText("Captured frame successfully");
                    }
                });
                camera.removeProgressListener(pb);
            }
        });
        buttons.add(btn);

        return panel;
    }

    private JPanel getDigitalOutput() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Digital Output"));

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        panel.add(new JLabel("Digital Output Mode"), gc0);
        chkDigitalOutput = new JCheckBox("14 bit mode?");
        chkDigitalOutput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    if (chkDigitalOutput.isSelected()) {
                        camera.setDigitalOutputMode(IndigoOmegaCamera.DIGITAL_OUTPUT_14_BIT);
                    } else {
                        camera.setDigitalOutputMode(IndigoOmegaCamera.DIGITAL_OUTPUT_8_BIT);
                    }
                    lblStatus.setText("Digital Output set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Digital Output.", exc);
                }
            }
        });
        panel.add(chkDigitalOutput, gc1);

        return panel;
    }

    private JPanel getImageOptimization() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Image Optimization"));

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc2 = new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 2;
        panel.add(new JLabel("Image Optimization Mode"), gc0);
        cmbImageOptimization = new JComboBox(new String[]{"Smart Scene", "Auto Bright", "Manual", "Fixed"});
        cmbImageOptimization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    camera.setImageOptimization((byte) cmbImageOptimization.getSelectedIndex());
                    lblStatus.setText("Image Optimization set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Image Optimization.", exc);
                }
            }
        });
        panel.add(cmbImageOptimization, gc1);

        gc0.gridy = 1;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 1;
        panel.add(new JLabel("Contrast"), gc0);
        spinContrast = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        spinContrast.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (boolStatus) return;
                try {
                    camera.setContrast(Integer.parseInt(spinContrast.getValue().toString()));
                    lblStatus.setText("Image Contrast set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Image Contrast.", exc);
                }
            }
        });
        panel.add(spinContrast, gc1);
        panel.add(new JLabel("(0, 255)"), gc2);

        gc0.gridy = 2;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Brightness"), gc0);
        spinBrightness = new JSpinner(new SpinnerNumberModel(0, 0, 16383, 1));
        spinBrightness.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (boolStatus) return;
                try {
                    camera.setBrightness(Integer.parseInt(spinBrightness.getValue().toString()));
                    lblStatus.setText("Image Brightness set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Image Brightness.", exc);
                }
            }
        });
        panel.add(spinBrightness, gc1);
        panel.add(new JLabel("(0, 16383)"), gc2);

        gc0.gridy = 3;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Brightness Bias"), gc0);
        spinBrightnessBias = new JSpinner(new SpinnerNumberModel(0, -2047, 2047, 1));
        spinBrightnessBias.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (boolStatus) return;
                try {
                    camera.setBrightnessBias(Integer.parseInt(spinBrightnessBias.getValue().toString()));
                    lblStatus.setText("Image Brightness Bias set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Image Brightness Bias.", exc);
                }
            }
        });
        panel.add(spinBrightnessBias, gc1);
        panel.add(new JLabel("(-2047, 2047)"), gc2);

        return panel;
    }

    private JPanel getMisc() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Misc"));

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc2 = new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 2;
        panel.add(new JLabel("Dynamic Range Control"), gc0);
        cmbDynamicRange = new JComboBox(new String[]{"Automatic", "High Temp", "Low Temp", "Disabled"});
        cmbDynamicRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    camera.setDynamicRangeControlMode((byte) cmbDynamicRange.getSelectedIndex());
                    lblStatus.setText("Dynamic Range Control set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Dynamic Range Control.", exc);
                }
            }
        });
        panel.add(cmbDynamicRange, gc1);

        gc0.gridy = 1;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Video Modes"), gc0);
        cmbVideoMode = new JComboBox(new String[]{"Real-Time", "Freeze-Frame", "Disabled"});
        cmbVideoMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    camera.setAnalogVideoMode((byte) cmbVideoMode.getSelectedIndex());
                    lblStatus.setText("Analog Video Mode set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Analog Video Mode.", exc);
                }
            }
        });
        panel.add(cmbVideoMode, gc1);

        gc0.gridy = 2;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Test Pattern"), gc0);
        cmbTestPattern = new JComboBox(new String[]{"Off", "Ramp", "Vertical Shade"});
        cmbTestPattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    camera.setTestPattern((byte) cmbTestPattern.getSelectedIndex());
                    lblStatus.setText("Test Pattern set successfully");
                } catch (IOException exc) {
                    logger.warn("Could set Test pattern.", exc);
                }
            }
        });
        panel.add(cmbTestPattern, gc1);

        gc0.gridy = 3;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Image Orientation"), gc0);
        chkImageOrientation = new JCheckBox("Normal orientation?");
        chkImageOrientation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    if (chkImageOrientation.isSelected()) {
                        camera.setImageOrientation(IndigoOmegaCamera.IMAGE_ORIENTATION_NORMAL);
                    } else {
                        camera.setImageOrientation(IndigoOmegaCamera.IMAGE_ORIENTATION_REVERT);
                    }
                    lblStatus.setText("Image Orientation set successfully");
                } catch (IOException exc) {
                    logger.warn("Could set Image Orientation.", exc);
                }
            }
        });
        panel.add(chkImageOrientation, gc1);

        gc0.gridy = 4;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Polarity"), gc0);
        chkPolarity = new JCheckBox("Use white for hot?");
        chkPolarity.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    if (chkPolarity.isSelected()) {
                        camera.setPolarity(IndigoOmegaCamera.POLARITY_WHITE_HOT);
                    } else {
                        camera.setPolarity(IndigoOmegaCamera.POLARITY_BLACK_HOT);
                    }
                    lblStatus.setText("Polarity set successfully");
                } catch (IOException exc) {
                    logger.warn("Could set Polarity.", exc);
                }
            }
        });
        panel.add(chkPolarity, gc1);

        gc0.gridy = 5;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Symbology"), gc0);
        cmbSymbology = new JComboBox(new String[]{"Black/White", "Overbright", "Off"});
        cmbSymbology.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                try {
                    camera.setSymbolColor((byte) cmbSymbology.getSelectedIndex());
                    lblStatus.setText("Symbology set successfully");
                } catch (IOException exc) {
                    logger.warn("Could not set Symbology.", exc);
                }
            }
        });
        panel.add(cmbSymbology, gc1);

        gc0.gridy = 6;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        panel.add(new JLabel("Frame Scale"), gc0);
        cmbFrameScale = new JComboBox(new String[]{"Kelvin", "Centigrade", "Fahrenheit", "RAW"});
        cmbFrameScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (boolStatus) return;
                camera.setFrameScale(cmbFrameScale.getSelectedIndex());
                lblStatus.setText("Frame Scale set successfully");
            }
        });
        panel.add(cmbFrameScale, gc1);

        gc0.gridy = 7;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 1;
        JButton btn = new JButton(new AbstractAction("Case Temperature") {
            public void actionPerformed(ActionEvent e) {
                try {
                    float temp = camera.getCaseTemp(cmbCaseScale.getSelectedIndex());
                    txtCaseTemp.setText(nf.format(temp));
                    lblStatus.setText("Case Temperature read successfully");
                } catch (IOException exc) {
                    logger.warn("Could not read Case Temperature.", exc);
                }
            }
        });
        panel.add(btn, gc0);
        txtCaseTemp = new JTextField(5);
        txtCaseTemp.setEditable(false);
        panel.add(txtCaseTemp, gc1);
        cmbCaseScale = new JComboBox(new String[]{"Kelvin", "Celsius", "Fahrenheit"});
        cmbCaseScale.setSelectedIndex(1);
        panel.add(cmbCaseScale, gc2);

        return panel;
    }

    private JPanel getButtons() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setAlignmentY(1);

        JButton btn = new JButton(new AbstractAction("Save State") {
            public void actionPerformed(ActionEvent e) {
                try {
                    camera.setDefaults();
                    lblStatus.setText("Save State successfully .");
                } catch (IOException exc) {
                    logger.warn("Could not save state.", exc);
                }
            }
        });
        panel.add(btn);

        btn = new JButton(new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent e) {
                try {
                    camera.cameraReset();
                    resetUI();
                    lblStatus.setText("Reset successfully .");
                } catch (IOException exc) {
                    logger.warn("Could not save state.", exc);
                }
            }
        });
        panel.add(btn);

        btn = new JButton(new AbstractAction("Factory Reset") {
            public void actionPerformed(ActionEvent e) {
                try {
                    camera.restoreFactoryDefaults();
                    resetUI();
                    lblStatus.setText("Factory Reset successfully .");
                } catch (IOException exc) {
                    logger.warn("Could not save state.", exc);
                }
            }
        });
        panel.add(btn);

        btn = new JButton(new AbstractAction("Status") {
            public void actionPerformed(ActionEvent e) {
                try {
                    lblStatus.setText(camera.getStatus());
                    resetUI();
                } catch (IOException exc) {
                    logger.warn("Could not get state.", exc);
                }
            }
        });
        panel.add(btn);

        return panel;
    }
}
