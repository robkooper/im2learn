import ncsa.im2learn.core.display.About;
import ncsa.im2learn.core.io.ImageLoader;
import ncsa.im2learn.core.io.ImageReader;

import java.io.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Jul 7, 2004 Time: 4:58:43 PM To
 * change this template use File | Settings | File Templates.
 */
public class InfoPlist {
    static public void main(String[] args) {
        // read the Info.plist file
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            PrintStream ps = new PrintStream(args[1]);

            String line = null;
            while ((line = br.readLine()) != null) {
                ps.println(line);
                if (line.indexOf("<key>CFBundleDocumentTypes</key>") != -1) {
                    ps.println(br.readLine()); // <array>
                    // add Im2Learn filters
                    Vector readers = ImageLoader.getReaders();
                    for (int i = 0; i < readers.size(); i++) {
                        ImageReader reader = (ImageReader) readers.get(i);
                        String[] ext = reader.readExt();
                        if (ext != null) {
                            ps.println("        <dict>");
                            ps.println("            <key>CFBundleTypeExtensions</key>");
                            ps.println("            <array>");
                            for (int j = 0; j < ext.length; j++) {
                                ps.println("                <string>" + ext[j] + "</string>");
                            }
                            ps.println("            </array>");
                            ps.println("            <key>CFBundleTypeName</key>");
                            ps.println("            <string>" + reader.getDescription() + "</string>");
                            ps.println("            <key>CFBundleTypeRole</key>");
                            ps.println("            <string>Editor</string>");
                            ps.println("        </dict>");
                        }
                    }
                    ps.println(br.readLine()); // </array>
                } else if (line.indexOf("<key>CFBundleShortVersionString</key>") != -1) {
                    br.readLine(); // skip original
                    ps.println("    <string>" + About.getVersion() + "</string>");
                } else if (line.indexOf("<key>CFBundleVersion</key>") != -1) {
                    br.readLine(); // skip original
                    ps.println("    <string>" + About.getBuild() + "</string>");
                } else if (line.indexOf("<key>CFBundleGetInfoString</key>") != -1) {
                    br.readLine(); // skip original
                    ps.println("    <string>" + About.getCopyright() + "</string>");
                }
            }
            br.close();
            ps.close();
        } catch (Throwable exc) {
            System.err.println("Something went wrong creating info.plist");
            exc.printStackTrace();
            System.exit(-1);
        }
    }
}
