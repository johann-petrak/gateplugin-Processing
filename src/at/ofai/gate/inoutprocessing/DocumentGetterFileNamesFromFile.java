package at.ofai.gate.inoutprocessing;

import gate.Factory;
import gate.util.GateRuntimeException;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentGetterFileNamesFromFile extends DocumentGetterFile {
  public DocumentGetterFileNamesFromFile(BatchProcessingOptions options, String filepath) {
    mOptions = options;
    mFileName = filepath;
    init();
  }
  public void setIndir(String inDirName) {
    mIndirName = inDirName;
    System.err.println("Set input directory to: "+mIndirName+"\n");
  }
  public void init() {
    File filenamesFile = new File(mFileName);
    System.err.println("Reading filenames from file "+filenamesFile.toString());
    FileReader inFileStream = null;
    try {
      inFileStream = new FileReader(filenamesFile);
    } catch(IOException ioe) {
       System.err.println("IO error trying to open filenames file: "+ioe);
       System.exit(2);
    }
    mInstream = new BufferedReader(inFileStream);
  }
  public void setDebug(Boolean onOff) {
    mDebug = onOff;
  }
  public String getFileName() {
    return mFileName;
  }
  Boolean mDebug = Boolean.TRUE;
  BufferedReader mInstream;
  public gate.Document getNextDocument() {
    gate.Document d = null;

    String fileName = null;
    try {
      fileName = mInstream.readLine();
      while(fileName != null && !isAcceptableFileName(fileName)) {
        fileName = mInstream.readLine();
      }
    } catch (IOException ioe) {
       System.err.println("IO error trying to read a line from the filenames file:"+ioe);
       System.exit(2);
    }
    if(fileName == null) {
      mHaveNext = false;
      mFileName = null;
      return null;
    }
    if(mDebug.booleanValue()) {
      System.err.println("Read from file:"+fileName);
    }
    return newDocumentFromFile(fileName);
  }

  public void close()  {
    try {
      mInstream.close();
    } catch (IOException ex) {
      throw new GateRuntimeException("Error when closing file: "+ex);
    }
  }
}