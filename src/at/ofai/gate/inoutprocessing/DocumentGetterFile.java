/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.inoutprocessing;

import gate.Document;
import java.util.regex.Pattern;

/**
 *
 * @author johann
 */
public abstract class DocumentGetterFile extends DocumentGetter {
  protected String mIndirName;
  protected String mFileName;
  protected Pattern mPattern = null;
  
  protected Document newDocumentFromFile(String fileName) {
    Document d = null;
    mFileName = fileName;
    if(mOptions.isAllowCompressedIn()) {
      mFileName = mFileName.replaceAll(".gz$", "");
    }
    try {
      String content = Utils.getStringFromFile(fileName, mIndirName, mOptions);
      d = Utils.newDocument(content, fileName);
    } catch (Exception ex) {
      System.err.println("Exception reading file "+fileName+": "+ex);
    }
    return d;
  }

  // this will always return true if no name pattern option is given
  // If the option is specified, match the file name against the pattern
  protected boolean isAcceptableFileName(String name) {
    boolean acceptable = true;
    if(mOptions.isNamePattern()) {
      // compile the pattern only once
      if(mPattern == null) {
        mPattern = Pattern.compile(mOptions.getNamePattern());
      }
      acceptable = mPattern.matcher(name).matches();
    }
    return acceptable;
  }
}
