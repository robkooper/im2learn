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
package edu.illinois.ncsa.isda.imagetools.core.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Help Display. This will display the help that is described for each dialog.
 * The dialogs that want to be able to display help will need to implement the
 * HelpEntry interface. This class will add all the help into a tree and display
 * the resulting tree, allowing the user to browe through the entries. The user
 * can also save the help into a directry by selecting File->Save from the menu.
 * 
 * @author Rob Kooper
 * @version 1.0
 */
public class HelpViewer extends Im2LearnFrame {
    private final JTree                   treeHelp;
    private Node                          nodeRoot;
    private Node                          nodeMenu;
    private Node                          nodePanel;
    private final JEditorPane             editorPane;
    private final DefaultTreeModel        dtm;
    private final HashMap<String, String> props;

    static private Log                    logger = LogFactory.getLog(HelpViewer.class);
    static private HelpViewer             instance;

    /**
     * Display the help window in the middle of the screen with the last entry
     * selected.
     */
    static public void showHelp() {
        showHelp(null, null);
    }

    /**
     * Display the help window in the middle of the screen with the given entry
     * selected. If it does not exist it will show the last entry selected.
     * 
     * @param menu
     *            the menu entry to be shown.
     */
    static public void showHelp(String menu) {
        showHelp(menu, null);
    }

    /**
     * Display the help window in the middle of the given window with the last
     * entry selected.
     * 
     * @param win
     *            the window the helpwindow is centered to.
     */
    static public void showHelp(Window win) {
        showHelp(null, win);
    }

    /**
     * Display the help window in the middle of the given window with the given
     * entry selected. If it does not exist it will show the last entry
     * selected.
     * 
     * @param menu
     *            the menu entry to be shown.
     * @param win
     *            the window the helpwindow is centered to.
     */
    static public void showHelp(String menu, Window win) {
        HelpViewer helpviewer = getHelpViewer();
        if (menu != null) {
            helpviewer.selectHelp(menu, helpviewer.nodeRoot);
        }
        if (!helpviewer.isVisible()) {
            helpviewer.setLocationRelativeTo(win);
            helpviewer.setVisible(true);
        }
    }

    /**
     * Add the MenuEntry to the list of help. This allows the user to select and
     * read the help.
     * 
     * @param helpentry
     *            the help to be added to the tree.
     */
    static public void addHelp(Im2LearnMenu menuentry) {
        HelpViewer helpviewer = getHelpViewer();

        // add all popup menuitems
        JMenuItem[] topics = menuentry.getPanelMenuItems();
        if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
                helpviewer.addHelp(menuentry, topics[i], helpviewer.nodePanel);
            }
        }

        // add all main menuitems
        topics = menuentry.getMainMenuItems();
        if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
                helpviewer.addHelp(menuentry, topics[i], helpviewer.nodeMenu);
            }
        }
    }

    /**
     * Write all the help that is in the tree to disk as HTML. This allows for
     * reading outside of the application.
     * 
     * @param directory
     *            the directory where all the files will be stored.
     */
    static public void exportHelp(File directory) {
        getHelpViewer().writeHelp(directory);
    }

    /**
     * Returns the singleton instance of the HelpViewer
     * 
     * @return singleton of HelpViewer
     */
    static private HelpViewer getHelpViewer() {
        if (instance == null) {
            instance = new HelpViewer();
        }
        return instance;
    }

    /**
     * Creates the HelpViewer. This will create the UI and the tree with the
     * single rootnode.
     */
    private HelpViewer() {
        super("Im2Learn Help");

        props = new HashMap<String, String>();
        props.put("im2learn.authors", getAuthors());
        props.put("im2learn.version", getVersion());
        props.put("im2learn.url", getURL());

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menubar.add(menu);

        menu.add(new JMenuItem(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fc.showSaveDialog(HelpViewer.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    writeHelp(fc.getSelectedFile());
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }));

        setJMenuBar(menubar);

        createHelpTree();

        dtm = new DefaultTreeModel(nodeRoot);
        treeHelp = new JTree(dtm);
        treeHelp.setShowsRootHandles(true);
        treeHelp.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath tp = e.getNewLeadSelectionPath();
                if (tp == null) {
                    editorPane.setText("");
                } else {
                    Node node = (Node) tp.getLastPathComponent();
                    Im2LearnMenu menu = node.getHelp();
                    displayHelp(menu, node.toString());
                }
            }
        });
        JScrollPane scrollTree = new JScrollPane(treeHelp);

        // Update only one tree instance
        MyRenderer renderer = new MyRenderer();
        treeHelp.setCellRenderer(renderer);
        //DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) treeHelp.getCellRenderer();

        // Remove the icons
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);

        // Force the htmldocument to be synchronous
        HTMLEditorKit synchronousHTMLkit = new HTMLEditorKit() {
            @Override
            public Document createDefaultDocument() {
                HTMLDocument doc = (HTMLDocument) super.createDefaultDocument();
                doc.setAsynchronousLoadPriority(-1);
                StyleSheet ss = doc.getStyleSheet();
                ss.addRule("body { background:white;margin-left:0.1in;margin-right:0.1in;}");
                ss.addRule("h1 { text-align:center; }");
                ss.addRule("pre { background:f2f2f2;margin-left:0.2in;margin-right:0.2in;}");
                return doc;
            }
        };
        editorPane = new JEditorPane();
        editorPane.setEditorKit(synchronousHTMLkit);
        editorPane.setEditable(false);
        JScrollPane scrollText = new JScrollPane(editorPane);

        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, scrollText);
        splitpane.setDividerLocation(200);
        getContentPane().add(splitpane, BorderLayout.CENTER);

        setSize(800, 600);

        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    JEditorPane pane = (JEditorPane) e.getSource();
                    if (e instanceof HTMLFrameHyperlinkEvent) {
                        HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                        HTMLDocument doc = (HTMLDocument) pane.getDocument();
                        doc.processHTMLFrameHyperlinkEvent(evt);
                    } else {
                        if (e.getURL() != null) {
                            try {
                                BrowserLauncher.openURL(e.getURL().toString());
                            } catch (Throwable t) {
                                logger.error("Error opening URL.", t);
                            }
                        }
                    }
                }
            }
        });

        treeHelp.setSelectionPath(new TreePath(nodeRoot));
    }

    /**
     * Create a list of authors which is returned. This can be accessed by
     * adding a tag with id="im2learn.authors".
     * 
     * @return table with authors.
     */
    private String getAuthors() {
        StringBuffer authors = new StringBuffer(50);

        authors.append("<table width=\"100%\">\n");
        authors.append("<tr><th colspan=\"3\">Authors:</th></tr>\n");
        authors.append("<tr>\n");
        for (int i = 0; i < About.getAuthors().length; i++) {
            if ((i != 0) && (i % 3 == 0)) {
                authors.append("</tr><tr>\n");
            }
            if ((i == About.getAuthors().length - 1)) {
                if (i % 3 != 2) {
                    authors.append("<td align=\"center\" width=\"33%\">&nbsp;</td>\n");
                }
            }
            authors.append("<td align=\"center\" width=\"33%\">").append(About.getAuthors()[i]).append("</td>\n");
        }
        authors.append("</tr></table>\n");

        return authors.toString();
    }

    /**
     * Create a string with the version number. This can be accessed by adding a
     * tage with id="im2learn.version".
     * 
     * @return version and build number
     */
    private String getVersion() {
        return "Im2Learn version " + About.getVersion() + " (" + About.getBuild() + ")";
    }

    /**
     * Create a string with the url to the Im2Learn homepage. This can be
     * accessed by adding a tage with id="im2learn.url".
     * 
     * @return url to the Im2Learn homepage.
     */
    private String getURL() {
        return "<a href=\"" + About.getHomePage() + "\">" + About.getHomePage() + "</a>";
    }

    private void addHelp(Im2LearnMenu menuentry, MenuElement menutopic, Node parent) {
        String menulabel = "unknown";
        if (menutopic instanceof JMenuItem) {
            menulabel = ((JMenuItem) menutopic).getText();
        }
        Node root = parent.getChild(menulabel);
        if (root == null) {
            root = new Node(menulabel, menuentry);
        }

        MenuElement[] topics;
        if (menutopic instanceof JMenu) {
            JPopupMenu popup = ((JMenu) menutopic).getPopupMenu();
            topics = popup.getSubElements();
        } else {
            topics = menutopic.getSubElements();
        }
        if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
                addHelp(menuentry, topics[i], root);
            }
        }

        if (!root.isLeaf() || (menuentry.getHelp(menulabel) != null)) {
            // TODO insert in right place
            parent.add(root);
            if (parent == nodePanel) {
                parent.sortChilderen();
            }
        }
    }

    private void selectHelp(String menu, Node node) {
        if (node.equals(menu)) {
            treeHelp.setSelectionPath(new TreePath(node));
            return;
        }
        for (Enumeration e = node.children(); e.hasMoreElements();) {
            selectHelp(menu, (Node) e.nextElement());
        }
    }

    private void displayHelp(Im2LearnMenu menuentry, String menu) {
        // force an empty page
        editorPane.setText("");
        try {
            editorPane.getDocument().putProperty(Document.StreamDescriptionProperty, new URL("http://"));
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // see if there is help here, if not assume plugin takes care of it.
        URL url = getHelp(menuentry, menu);

        // if found url, show help text
        if (url != null) {
            try {
                editorPane.setPage(url);
            } catch (IOException exc) {
                logger.debug("Error loading page.", exc);
            }

            HTMLDocument doc = (HTMLDocument) editorPane.getDocument();

            // some search and replace for Im2Learn variables
            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String val = props.get(key);

                Element find = doc.getElement(key);
                if (find != null) {
                    try {
                        doc.setInnerHTML(find, val);
                    } catch (BadLocationException exc) {
                        logger.debug("Error replacing " + key + ".", exc);
                    } catch (IOException exc) {
                        logger.debug("Error replacing " + key + ".", exc);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // write help to disk
    // ------------------------------------------------------------------------
    private void writeHelp(File directory) {
        TreePath curr = treeHelp.getSelectionPath();
        treeHelp.setSelectionPath(null);

        try {
            FileOutputStream fs = new FileOutputStream(new File(directory, "index.html"));
            PrintStream out = new PrintStream(fs);
            out.println("<html><title>Im2Learn</title>");
            out.println("<frameset cols=\"200, *\">");
            out.println(" <frame name=\"toc\" src=\"toc.html\">");
            out.println(" <frame name=\"body\" src=\"Im2Learn.html\">");
            out.println("</frameset>");
            out.println("</html>");
            fs.close();

            fs = new FileOutputStream(new File(directory, "toc.html"));
            out = new PrintStream(fs);
            out.println("<html><head>");
            out.println("<title>Im2Learn TOC</title>");
            out.println("<script>");
            out.println("function view(span, id) {");
            out.println("    div = document.getElementById(\"div_\" + id);");
            out.println("    if (div.style.display == 'none') {");
            out.println("        span.innerHTML = \"-&nbsp;\";");
            out.println("        div.style.display = 'block';");
            out.println("    } else {");
            out.println("        span.innerHTML = \"+&nbsp;\";");
            out.println("        div.style.display = 'none';");
            out.println("    }");
            out.println("}");
            out.println("</script>");
            out.println("<base target=\"body\">");
            out.println("</head><body>");
            writeNode(directory, out, nodeRoot, 0);
            out.println("</body></html>");
            fs.close();
        } catch (Exception exc) {
            logger.error("Error writing help.", exc);
        }

        treeHelp.setSelectionPath(curr);
    }

    private int writeNode(File directory, PrintStream out, Node node, int id) {
        String file = writeHelp(directory, node.getHelp(), node.toString());
        String menu = node.toString().replace(" ", "&nbsp;");
        menu = "<a href=\"" + file + "\">" + menu + "</a><br>";

        if (node.getChildCount() > 0) {
            if (!node.toString().equals("Im2Learn")) {
                out.print("<span onClick=\"view(this, " + id + ")\" style=\"cursor:pointer\">+</span>");
                out.println("&nbsp;" + menu);
                out.println("<div id=\"div_" + id + "\" style=\"display:none;padding-left:15pt;\">");
            } else {
                out.println(menu);
                out.println("<div id=\"div_" + id + "\" style=\"display:block;padding-left:15pt;\">");
            }
            id++;
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                id = writeNode(directory, out, (Node) e.nextElement(), id);
            }
            out.println("</div>");
        } else {
            out.println(menu);
        }

        return id;
    }

    private String writeHelp(File directory, Im2LearnMenu menuentry, String menu) {
        displayHelp(menuentry, menu);

        HashMap<String, String> replace = new HashMap<String, String>();
        ElementIterator ei = new ElementIterator(editorPane.getDocument());
        Element element = ei.next();
        while (element != null) {
            if (element.getName().equals(HTML.Tag.IMG.toString())) {
                AttributeSet as = element.getAttributes();
                if (as != null) {
                    String src = (String) as.getAttribute(HTML.Attribute.SRC);
                    String img = writeHelpImage(src, directory);
                    if (img != null) {
                        replace.put(src, img);
                    }
                }

            }
            element = ei.next();
        }

        String help = editorPane.getText();
        for (Iterator iter = replace.keySet().iterator(); iter.hasNext();) {
            String src = (String) iter.next();
            String img = replace.get(src);
            help = help.replaceAll(src, img);
        }

        try {
            String file = menu.replaceAll("[\\?\\*\\%\\s]", "") + ".html";
            FileWriter fw = new FileWriter(new File(directory, file));
            fw.write(help);
            fw.close();
            return file;
        } catch (IOException exc) {
            logger.error("Error writing html.", exc);
            return null;
        }
    }

    private String writeHelpImage(String src, File directory) {
        String name = new File(src).getName();
        File imagedir = new File(directory, "images");
        imagedir.mkdirs();
        File file = new File(imagedir, name);
        URL base = ((HTMLDocument) editorPane.getDocument()).getBase();
        try {
            URL img = new URL(base, src);
            InputStream inp = img.openStream();
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len = 1024;
            do {
                len = inp.read(buf);
                if (len > 0) {
                    out.write(buf, 0, len);
                }
            } while (len >= 0);
            out.close();
            inp.close();
        } catch (FileNotFoundException exc) {
            logger.info("Error finding image.", exc);
            return null;
        } catch (Exception exc) {
            logger.error("Error writing image.", exc);
            return null;
        }

        return "images/" + name;
    }

    // ------------------------------------------------------------------------
    //  HelpEntry implementation
    // ------------------------------------------------------------------------
    public void createHelpTree() {
        nodePanel = new Node("Panel", null);

        nodeMenu = new Node("Menu", null, new Node[] { new Node("File", null, new Node[] { new Node("Open", null), new Node("Open URL", null), new Node("Open Special", null), }),
                new Node("Edit", null, new Node[] { new Node("Copy", null), new Node("Paste", null), }), nodePanel, });

        nodeRoot = new Node("Im2Learn", null, new Node[] {
                new Node("Acknowledgement", null),

                new Node("Introduction", null, new Node[] { new Node("Functionality", null), new Node("User Interface", null), new Node("Code Architecture", null),
                        new Node("Application Examples", null), }),

                new Node("How to use Im2Learn", null, new Node[] {
                        new Node("Core Code", null, new Node[] { new Node("ImageObject", null), }),
                        new Node("Extending Im2Learn", null, new Node[] { new Node("Adding Functionality", null, new Node[] { new Node("Im2LearnMenu", null), }),
                                new Node("ImageLoader", null, new Node[] { new Node("ImageReader", null), new Node("ImageWriter", null), }), })

                }),

                new Node("Example Plugin", null),

                new Node("Speed Improvements", null),

                nodeMenu,

                new Node("License", null), });
    }

    private URL getHelp(Im2LearnMenu menuentry, String menu) {
        URL url = null;

        if ((url == null) && (menuentry != null)) {
            url = menuentry.getHelp(menu);
        }

        if (url == null) {
            String file = menu.toLowerCase().replaceAll("[\\s\\?\\*]", "") + ".html";
            url = this.getClass().getResource("help/" + file);
        }

        return url;
    }

    class MyRenderer extends DefaultTreeCellRenderer {
        public MyRenderer() {
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            if (value instanceof Node) {
                Node node = (Node) value;
                boolean plugin = leaf && (node.getHelp() != null);
                value = node.toString() + (plugin ? " [P]" : "");
            }
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            return c;
        }
    }

    // ------------------------------------------------------------------------
    //  Node containing all help entries
    // ------------------------------------------------------------------------
    class Node implements TreeNode, Comparable {
        private final String          name;
        private final ArrayList<Node> childeren;
        private Im2LearnMenu          menu;
        private boolean               plugin;
        private Node                  parent;

        public Node(String name, Im2LearnMenu menu) {
            this(name, menu, null);
        }

        public Node(String name, Im2LearnMenu menu, Node[] childeren) {
            this.name = name;
            this.menu = menu;

            this.childeren = new ArrayList<Node>();
            if (childeren != null) {
                for (Node child : childeren) {
                    this.childeren.add(child);
                }
            }
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public boolean isPlugin() {
            return plugin;
        }

        public void add(Node child) {
            if ((child != null) && !childeren.contains(child)) {
                childeren.add(child);
                child.setParent(this);
            }
        }

        public void add(int index, Node child) {
            if ((child != null) && !childeren.contains(child)) {
                childeren.add(index, child);
                child.setParent(this);
            }
        }

        public void sortChilderen() {
            Collections.sort(childeren);
        }

        public Node getChild(String child) {
            for (Iterator iter = childeren.iterator(); iter.hasNext();) {
                Node node = (Node) iter.next();
                if (node.name.equals(child)) {
                    return node;
                }
            }
            return null;
        }

        public void removeChild(Node child) {
            childeren.remove(child);
        }

        public void setHelp(Im2LearnMenu menu) {
            this.menu = menu;
        }

        public Im2LearnMenu getHelp() {
            return menu;
        }

        public URL getHelp(String topic) {
            return this.menu.getHelp(topic);
        }

        public String toString() {
            return name;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                return name.equals(((Node) obj).name);
            } else {
                return name.equals(obj);
            }
        }

        // -------------------------------------------------------------------
        // TreeNode interface
        // -------------------------------------------------------------------
        public TreeNode getParent() {
            return parent;
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public Enumeration children() {
            final Iterator iter = childeren.iterator();
            return new Enumeration() {
                public boolean hasMoreElements() {
                    return iter.hasNext();
                }

                public Object nextElement() {
                    return iter.next();
                }

            };
        }

        public boolean isLeaf() {
            return childeren.size() == 0;
        }

        public TreeNode getChildAt(int childIndex) {
            return (Node) childeren.get(childIndex);
        }

        public int getIndex(TreeNode child) {
            return childeren.indexOf(child);
        }

        public int getChildCount() {
            return childeren.size();
        }

        // -------------------------------------------------------------------
        // Comparable interface
        // -------------------------------------------------------------------
        public int compareTo(Object o) {
            if (o instanceof Node) {
                return name.compareTo(((Node) o).name);
            }
            return 0;
        }
    }
}
