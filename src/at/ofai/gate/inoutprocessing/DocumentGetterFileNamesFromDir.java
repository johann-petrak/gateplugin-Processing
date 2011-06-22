package at.ofai.gate.inoutprocessing;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;

public class DocumentGetterFileNamesFromDir extends DocumentGetterFile {
  public DocumentGetterFileNamesFromDir(BatchProcessingOptions options, String filepath) {
    mOptions = options;
    mIndirName = filepath;
    init();
  }
  Vector<String> fileNames = new Vector<String>();
  Iterator<String> fileNamesIterator;
  
  public void setIndir(String inDirName) {
    mIndirName = inDirName;
    System.err.println("Set input directory to: "+mIndirName+"\n");
  }
  public void init() {
    File theDir = new File(mIndirName);
    System.err.println("Getting filenames in directory "+theDir.toString());
    if(!theDir.isDirectory()) {
      System.err.println("Not a directory: "+theDir.getAbsolutePath());
      System.exit(2);
    }
    String[] filenames = theDir.list();
    for(String f : filenames) {
      File theFile = new File(mIndirName,f);
      if(theFile.isDirectory()) {
        continue;
      } else if (!isAcceptableFileName(f)) {
        continue;
      } else {
        fileNames.add(f);
      }
    }
    fileNamesIterator = fileNames.iterator();
  }
  public void setDebug(Boolean onOff) {
    mDebug = onOff;
  }
  public String getFileName() {
    return mFileName;
  }
  
  Boolean mDebug = Boolean.TRUE;


  public gate.Document getNextDocument() {
    gate.Document d = null;

    String fileName = null;
    if(!fileNamesIterator.hasNext()) {
      mHaveNext = false;
      mFileName = null;
      return null;
    } else {
      mFileName = fileNamesIterator.next();
    }
    fileName = mFileName;
    if(mDebug.booleanValue()) {
      System.err.println("Read from file:"+fileName);
    }
    return newDocumentFromFile(fileName);
  }

  public void close() {}

}