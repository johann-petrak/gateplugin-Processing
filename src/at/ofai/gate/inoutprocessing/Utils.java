/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.inoutprocessing;

import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
        
/**
 *
 * @author johann
 */
public class Utils {
  static public void saveStringToFile(
          String string, OutputStream out, BatchProcessingOptions options) throws UnsupportedEncodingException, IOException {
    // convert the string to a byte buffer using the encoding specified
    // as an option or the default encoding if the output format is string,
    // otherwise use UTF-8
    String outputEncoding = "UTF-8";
    if(options.isSaveAs() && options.getSaveAs().equals("string") && options.isOutputEncoding()) {
      outputEncoding = options.getOutputEncoding();
    }
    byte[] buf = string.getBytes(outputEncoding);
    // if we want compression, wrap the output stream into a gzip stream
    // otherwise just use the stream we got
    if(options.isCompressOut()) {
      OutputStream ourOut = new GZIPOutputStream(out);
      ourOut.write(buf);
      ourOut.close();
    } else {
      out.write(buf);
    }

  }

  static public String getStringFromFile(String filename, String indir, BatchProcessingOptions options) throws FileNotFoundException, IOException {
    String content = "";
    // if input compression is allowed, check if the name ends in .gz
    // if yes, read the file using a decompressing stream, otherwise using
    // a normal stream
    String encoding = "UTF-8";
    if(options.isInputEncoding() &&
       !filename.endsWith(".xml") &&
       !filename.endsWith(".xml.gz")) {
      encoding = options.getInputEncoding();
    }
    InputStream isorig = new FileInputStream(new File(new File(indir),filename));
    if(options.isAllowCompressedIn() && filename.endsWith(".gz")) {
      InputStream isdec = new GZIPInputStream(isorig);
      content = IOUtils.toString(isdec, encoding);
      isdec.close();
    } else {
      content = IOUtils.toString(isorig,encoding);
    }
    isorig.close();
    return content;
  }

  static public String getDefaultEncoding() {
    return Charset.defaultCharset().toString();
  }

  static public Document newDocument(String content, String fileName) 
          throws ResourceInstantiationException {
    return Factory.newDocument(content);
  }
  
}
