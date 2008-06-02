package org.bbop.io;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;

import org.apache.log4j.*;

public class FileUtil {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(FileUtil.class);

    /**
     * Checks to see if a file exists. If it doesn't, the file is created
     * by copying the specified resource from the System class loader.
     * Based on code written by Matthew Pocock
     */
    public static void ensureExists(File file, String resource)
	throws IOException {
	ClassLoader loader = getSystemClassLoader();
	ensureExists(file, loader, resource);
    }

    /**
     * Checks to see if a file exists. If it doesn't, the file is created
     * by copying the specified resource from the specified class loader.
     * Based on code written by Matthew Pocock
     */
    public static void ensureExists(File file, ClassLoader loader, String resource)
      throws IOException {

      // if file exists nothgin to - return
      if (file.exists())
        return;

      // Get InputStream from resource
      if (loader == null) {
        throw new IOException("ClassLoader is null,"+
                              " can't load resource.");
      }
        
      if (resource == null) {
        throw new NullPointerException("Can't load from null resource");
      }
      
      InputStream is = loader.getResourceAsStream(resource);
      if (is == null) {
//        logger.debug("ensureExists: couldn't find resource "+resource+" gonna try "
//                           +"class.getResource");
        try { is = findInputStream(resource); }
        catch (FileNotFoundException e) {
//          logger.debug("ensureExists: couldn't find resource "+resource+" gonna try "
//                             +"/"+resource);
          is = findInputStream("/"+resource); //let ex fly
        }

        if (is == null)
          throw new FileNotFoundException("Couldn't find resource "+resource);
      }

      // Got InputStream from resource copy to file
      (new File(file.getParent())).mkdirs();
      OutputStream os = new BufferedOutputStream(
        new FileOutputStream(file));
      
      logger.info("ensureExists: creating " + file.getAbsolutePath() + " from resource "
                         + resource); // DEL
      for(int next = is.read(); next != -1; next = is.read()) {
        os.write(next);
      }
      
      os.flush();
      os.close();
      //}
    }

  private static InputStream findInputStream(String resource)
    throws FileNotFoundException {
    URL u = FileUtil.class.getResource(resource);
    if (u == null) throw new FileNotFoundException("cant get resource "+resource);
    InputStream is=null;
    try { is = u.openStream(); }
    catch (IOException e) {}
    if (is == null) {throw new FileNotFoundException("failed to open stream "+resource);}
    return is;
  }

    public static ClassLoader getSystemClassLoader() {
	try {
	    Method loaderMethod = ClassLoader.class.getMethod(
						    "getSystemClassLoader",
						    new Class[0]);
	    Object out = loaderMethod.invoke(null, new Object[0]);
	    return (ClassLoader) out;
	} catch (Exception e) {
	    return Class.class.getClassLoader();
	}	
    }
}
