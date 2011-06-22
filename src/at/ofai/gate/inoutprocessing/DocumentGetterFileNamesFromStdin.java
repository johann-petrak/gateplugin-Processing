package at.ofai.gate.inoutprocessing;

import java.io.*;

public class DocumentGetterFileNamesFromStdin extends DocumentGetterFile {
  public DocumentGetterFileNamesFromStdin(BatchProcessingOptions options) {
    mOptions = options;
  }
  public void setIndir(String inDirName) {
    mIndirName = inDirName;
    System.err.println("Set input directory to: "+mIndirName+"\n");
  }
  public void init() {
    mReader = new BufferedReader(new InputStreamReader(System.in));
  }
  private BufferedReader mReader = null;
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
    try {
      fileName = mReader.readLine();
      while(fileName != null && !isAcceptableFileName(fileName)) {
        fileName = mReader.readLine();
      }
    } catch (IOException ioe) {
       System.err.println("IO error trying to read a line from standard input:"+ioe);
       System.exit(2);
    }
    if(fileName == null) {
      mHaveNext = false;
      mFileName = null;
      return null;
    }
    if(mDebug.booleanValue()) {
      System.err.println("Read from standard input:"+fileName);
    }
    return newDocumentFromFile(fileName);
  }

  public void close() {
    // close mReader here?
  }
}