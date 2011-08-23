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
package edu.illinois.ncsa.isda.imagetools.core.io.hdf;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.ScalarDS;
import ncsa.hdf.object.h4.H4File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class will differ from the regular file chooser since it knows about HDF
 * files. It will show the normal file dialog, but with the content of a HDF
 * file as if it was a normal directory. Once the user selects a file it will
 * return the name of the file selected seperating HDF file and internal name
 * with a #.
 */
public class FileChooserHDF extends Dialog implements ActionListener, ListSelectionListener {
    static public int LOAD = 0;
    static public int SAVE = 1;

    private JComboBox cmbdirectory = new JComboBox();
    private JLabel lbldirectory = new JLabel("Look in:");
    private JTextField txtstatus = new JTextField("");
    private JLabel lblstatus = new JLabel("File info:");
    private JTextField txtfilename = new JTextField("");
    private JLabel lblfilename = new JLabel("File name:");
    private JButton btnOpen = new JButton("Open");
    private JButton btnCancel = new JButton("Cancel");
    private JButton btnUp = new JButton("U");
    private JButton btnNewFolder = new JButton("N");
    private FileListModel flm = new FileListModel();
    private JList lstDirectory = new JList(flm);
    private int mode = LOAD;

    private String filename = null;
    private boolean updating = false;

    private static Log logger = LogFactory.getLog(FileChooserHDF.class);

    /**
     * Create a filechooser with no starting directory specified.
     */
    public FileChooserHDF(String hdffile) {
        this(hdffile, "", LOAD);
    }

    public FileChooserHDF(String hdffile, String title) {
        this(hdffile, title, LOAD);
    }

    public FileChooserHDF(String hdffile, int mode) {
        this(hdffile, "", mode);
    }

    public FileChooserHDF(String hdffile, String title, int mode) {
        super(new Frame(), title, true);
        this.mode = mode;
        createGUI();
        setHDFFile(hdffile);
    }

    public void setHDFFile(String hdffile) {
        try {
            Object obj = HDF.openFile(hdffile, (mode == SAVE), false);
            if (obj instanceof FileFormat) {
                flm.setDirectory(obj);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Show the dialogue to the user. Changing the text of the open/save button
     * and the window title based on the mode.
     *
     * @return OK or CANCEL based on user action
     */
    public synchronized String showDialog() {
        filename = null;

        if (mode == LOAD) {
            btnOpen.setText("Open");
            setTitle("Select a file to load");
        } else {
            btnOpen.setText("Save");
            setTitle("Select a file to save");
        }

        pack();
        setLocationRelativeTo(null);
        show();

        return filename;
    }

    /**
     * Return the currently selected file.
     *
     * @return current selected filename
     */
    public synchronized String getFileName() {
        return filename;
    }

    /**
     * Return the current directory (Group) of the file.
     *
     * @return path to the current directory.
     */
    public String getDirectory() {
        Object obj = cmbdirectory.getItemAt(0);

        if (obj instanceof Group) {
            Group group = (Group) obj;
            if (group.getPath() == null) {
                return group.getFile() + "#" + Group.separator;
            } else {
                return group.getFile() + "#" + group.getPath() + group.getName() + Group.separator;
            }
        }
        return "";
    }

    /**
     * Called when the user hits a ok or cancel button or selects a directory
     * from the combo box.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(btnCancel)) {
            cancel();
        } else if (e.getSource().equals(cmbdirectory)) {
            if (!updating && (cmbdirectory.getSelectedIndex() != 0)) {
                flm.setDirectory(cmbdirectory.getSelectedItem());
                lstDirectory.clearSelection();
            }
        } else if (e.getSource().equals(btnUp)) {
            if (cmbdirectory.getItemCount() > 1) {
                cmbdirectory.setSelectedIndex(1);
            }
        } else if (e.getSource().equals(txtfilename) || e.getSource().equals(btnOpen)) {
            try {
                selectListItem(txtfilename.getText());
                txtfilename.setText("");
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } else if (e.getSource().equals(btnNewFolder)) {
            String result = JOptionPane.showInputDialog("New folder name:");
            if (!result.equals("")) {
                try {
                    String dir = getDirectory() + result + Group.separator + "x";
                    HDF.openFile(dir, true, false);
                    flm.setDirectory(HDF.openFile(getDirectory(), false, false));

                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        } else {
            logger.debug("Unknown action. " + e);
        }
    }

    /**
     * Called whenever the value of the selection changes. This will display
     * information about the file that is currently selected.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            String status = "";
            if (lstDirectory.isSelectionEmpty()) {
                txtfilename.setText("");
                status = "";
            } else {
                Object obj = lstDirectory.getSelectedValue();
                if (obj instanceof ScalarDS) {
                    ScalarDS scalards = (ScalarDS) obj;
                    if (scalards.isText()) {
                        status = "Text";
                    } else {
                        if (scalards.isImage()) {
                            if (scalards.isTrueColor()) {
                                status = "True Color Image";
                            } else {
                                status = "Image";
                            }
                        } else {
                            status = "Array";
                        }
                        try {
                            String tmpname = scalards.getFile() + "#" +
                                             scalards.getPath() + Group.separator +
                                             scalards.getName();

                            scalards = (ScalarDS) HDF.openFile(tmpname, false, true);
                            scalards.init();
                            long[] dims = scalards.getDims();
                            if (dims.length > 0) {
                                status += " (" + dims[0];
                                for (int i = 1; i < dims.length; i++) {
                                    status += ", " + dims[i];
                                }
                                status += ")";
                            }
                            scalards.getFileFormat().close();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                    txtfilename.setText(scalards.getName());
                } else if (obj instanceof Dataset) {
                    status = "Unknown";
                    txtfilename.setText(obj.toString());
                } else if (obj instanceof Group) {
                    status = "File Folder";
                    txtfilename.setText(obj.toString());
                } else {
                    status = "??";
                }
            }
            txtstatus.setText(status);
        }
    }

    private void selectListItem(String filename) throws Exception {
        filename = getDirectory() + filename;

        // open the file
        Object obj = HDF.openFile(filename, (mode == SAVE), false);

        if (obj == null) {
            if (mode == SAVE) {
                ok(filename);
                return;
            }
            throw(new Exception("Could not open file " + filename));
        } else {
            selectListItem(obj);
        }
    }

    private void selectListItem(Object obj) throws Exception {
        // handle different types returned from openFile
        if (obj instanceof Dataset) {
            // Dataset inside a HDF file, equivalent to a file.
            Dataset dataset = (Dataset) obj;
            if (mode == SAVE) {
                if (dataset.getFileFormat() instanceof H4File) {
                    dataset.getFileFormat().close();
                    throw(new Exception("Can't overwrite existing file."));
                }
                if (JOptionPane.showConfirmDialog(this, obj.toString() +
                                                        " already exists.\n" + "Do you want to replace it?") != JOptionPane.OK_OPTION) {
                    dataset.getFileFormat().close();
                    return;
                }
                dataset.getFileFormat().delete(dataset);
            }
            dataset.getFileFormat().close();
            ok(dataset);
        } else if (obj instanceof Group) {
            // Group inside a HDF file, equivalent to a directory.
            flm.setDirectory(obj);
            //cmbdirectory.setModel(new DefaultComboBoxModel(flm.getDirectories()));
            lstDirectory.clearSelection();
            ((Group) obj).getFileFormat().close();
        } else if (obj instanceof FileFormat) {
            // FileFormat is a HDF file, equivalent to a drive.
            flm.setDirectory(obj);
            //cmbdirectory.setModel(new DefaultComboBoxModel(flm.getDirectories()));
            lstDirectory.clearSelection();
            ((FileFormat) obj).close();
        } else {
            throw(new Exception("Unknow file " + filename));
        }
    }

    /**
     * called when the user hits the cancel button or closes the dialog. It will
     * set the return value and notify showDialog
     */
    private void cancel() {
        hide();
    }

    /**
     * called when the user hits the ok button or double clicks on a file. It
     * will set the return value and notify showDialog
     */
    private void ok(Object obj) {
        if (obj == null) {
            filename = null;
        } else if (obj instanceof Dataset) {
            Dataset dataset = (Dataset) obj;
            filename = dataset.getFile() + "#" + dataset.getPath() + Group.separator + dataset.getName();
        } else {
            filename = obj.toString();
        }
        hide();
    }

    /**
     * Create the GUI that contains the file browser.
     */
    private void createGUI() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        setLayout(gbl);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);

        // top part has buttons, and combo box
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(lbldirectory, gbc);
        add(lbldirectory);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(cmbdirectory, gbc);
        add(cmbdirectory);
        lbldirectory.setLabelFor(cmbdirectory);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.gridy = 0;
        JPanel buttonpane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gbl.setConstraints(buttonpane, gbc);
        add(buttonpane);

        buttonpane.add(btnUp);
        buttonpane.add(btnNewFolder);

        // middle contains the directory listing
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 3;
        lstDirectory.setLayoutOrientation(JList.VERTICAL_WRAP);
        JScrollPane scrollPane = new JScrollPane(lstDirectory);
        scrollPane.setPreferredSize(new Dimension(450, 150));
        gbl.setConstraints(scrollPane, gbc);
        add(scrollPane);

        // bottom contains the open cancel buttons and filename box
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbl.setConstraints(lblfilename, gbc);
        add(lblfilename);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbl.setConstraints(lblstatus, gbc);
        add(lblstatus);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbl.setConstraints(txtfilename, gbc);
        add(txtfilename);
        lblfilename.setLabelFor(txtfilename);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbl.setConstraints(txtstatus, gbc);
        add(txtstatus);
        txtstatus.setEnabled(false);
        lblstatus.setLabelFor(txtstatus);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbl.setConstraints(btnOpen, gbc);
        add(btnOpen);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbl.setConstraints(btnCancel, gbc);
        add(btnCancel);

        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed. The
             * close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                cancel();
                super.windowClosing(e);
            }
        });

        lstDirectory.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse has been clicked on a component.
             */
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        selectListItem(lstDirectory.getSelectedValue());
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
                super.mouseClicked(e);
            }
        });
        lstDirectory.addListSelectionListener(this);
        lstDirectory.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed. This event occurs when a key
             * press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    try {
                        selectListItem(lstDirectory.getSelectedValue());
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
                super.keyTyped(e);
            }
        });
        lstDirectory.setCellRenderer(new MyCellRenderer());
        lstDirectory.setVisibleRowCount(0);

        cmbdirectory.setModel(flm.getDirectories());
        cmbdirectory.addActionListener(this);

        txtfilename.addActionListener(this);

        btnUp.addActionListener(this);
        btnNewFolder.addActionListener(this);
        btnOpen.addActionListener(this);
        btnCancel.addActionListener(this);
    }

    /**
     * Class to hold the files currently visible in the file browser. This class
     * knows how to open an archive and show the files inside the archive.
     */
    private class FileListModel extends AbstractListModel {
        private Vector files = null;
        private DefaultComboBoxModel directories = null;


        public FileListModel() {
            files = new Vector();
            directories = new DefaultComboBoxModel();
        }

        /**
         * Change the directory listing all the files in the directory. If the
         * directory is an archive show the files inside the archive. Also
         * update the list of directories that are the path to the directory.
         *
         * @param directory to list
         */
        public void setDirectory(Object directory) {
            if (directory instanceof Group) {
                Group group = (Group) directory;

                files = new Vector();
                Iterator iter = group.getMemberList().iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (!((obj instanceof Group) && (obj.toString().endsWith(".properties")))) {
                        files.add(obj);
                    }
                }

                Group dir = group;
                updating = true;
                directories.removeAllElements();
                while (dir != null) {
                    directories.addElement(dir);
                    dir = dir.getParent();
                }
                updating = false;
            } else if (directory instanceof FileFormat) {
                FileFormat ff = (FileFormat) directory;
                try {
                    ff.open();
                    TreeNode node = ff.getRootNode();

                    files.clear();
                    for (Enumeration e = node.children(); e.hasMoreElements();) {
                        Object obj = ((DefaultMutableTreeNode) e.nextElement()).getUserObject();
                        if (!((obj instanceof Group) && (obj.toString().endsWith(".properties")))) {
                            files.add(obj);
                        }
                    }
                    ff.close();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }

                updating = true;
                directories.removeAllElements();
                Group group = (Group) ((DefaultMutableTreeNode) ff.getRootNode()).getUserObject();
                directories.addElement(group);
                updating = false;
            }

            fireContentsChanged(this, 0, files.size());
        }

        /**
         * Returns the path to the file as a list of directories.
         *
         * @return path as a list of directories
         */
        public DefaultComboBoxModel getDirectories() {
            return directories;
        }

        /**
         * Returns the value at the specified index.
         *
         * @param index the requested index
         * @return the value at <code>index</code>
         */
        public Object getElementAt(int index) {
            return files.elementAt(index);
        }

        /**
         * Returns the length of the list.
         *
         * @return the length of the list
         */
        public int getSize() {
            return files.size();
        }
    }

    /**
     * This class knows about the different items we will display inside the
     * listbox. It will know how to look up the icons that are associated with a
     * file, or will know about the special files inside a HDF or other files
     * and display their icons.
     */
    class MyCellRenderer extends JLabel implements ListCellRenderer {

        /**
         * This is the only method defined by ListCellRenderer. We just
         * reconfigure the JLabel each time we're called.
         *
         * @param list         to which value belongs
         * @param value        to display
         * @param index        cell index
         * @param isSelected   is the cell selected
         * @param cellHasFocus the list and the cell have the focus
         * @return
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // based on type set the icon and label
            if (value instanceof Group) {
                setText(value.toString());
                setIcon(UIManager.getIcon("FileView.directoryIcon"));
            } else if (value instanceof Dataset) {
                setText(value.toString());
                // TODO if possible would like to set to image icon
                setIcon(UIManager.getIcon("FileView.fileIcon"));
            } else {
                setText(value.toString());
                setIcon(UIManager.getIcon("FileView.fileIcon"));
            }

            // if selected, set the colors correctly
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            // copy enabled and font from the list.
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
}
