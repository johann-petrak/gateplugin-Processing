/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.inoutprocessing;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author johann
 */
public class PipelineRunner implements Runnable {

  protected CorpusController mController = null;
  protected Corpus mCorpus = null;
  protected Document mDocument = null;
  protected boolean mDebug = false;
  protected BatchProcessingOptions mOptions = null;
  protected String mFileName = null;
  protected int mThreadNr = -1;

  public void setDocument(Document doc, String filename) {
    mDocument = doc;
    mFileName = filename;
  }
  public int getNr() {
    return mThreadNr;
  }
  private PipelineRunner() {}

  public PipelineRunner(BatchProcessingOptions options, int nr)
          throws ResourceInstantiationException {
    mOptions = options;
    mThreadNr = nr;
    String filename = options.getController();
    File controllerFile = new File(filename);
    try {
        mController =
          (gate.CorpusController)gate.util.persistence.PersistenceManager.
          loadObjectFromFile(controllerFile);
        System.err.println("Created controller "+mController.getName());
      } catch (Exception e) {
        throw new ResourceInstantiationException("Could not load controller: "+e);
      }
      try {
        mCorpus = gate.Factory.newCorpus("tmpcorpus_"+Gate.genSym());
        System.err.println("Created corpus "+mCorpus.getName());
      } catch (Exception e) {
        System.err.println("Could not create a corpus: "+e);
        System.exit(2);
      }
      mController.setCorpus(mCorpus);
  }
  
  public void run() {
    if(mOptions.isDebug()) {
      System.err.println("Processing document "+mDocument.getName()+" in thread "+mThreadNr+" using "+mController.getName()+"\n");
    }
    
    mCorpus.add(mDocument);
    try {
      mController.execute();
      System.err.println("Document "+mDocument.getName()+" processed!\n");
    } catch (gate.creole.ExecutionException e) {
      System.err.println("Error processing document "+mDocument.getName()+": "+e);
    }
    mCorpus.clear();

    if (!mOptions.getSaveTo().equals("none")) {
      // we save the document
      String toSave = null;
      if (mOptions.getSaveAs().equals("string")) {
        System.err.println("Converting output document to string");
        toSave = mDocument.toString();
      } else if (mOptions.getSaveAs().equals("xml")) {
        System.err.println("Converting output document to xml");
        toSave = mDocument.toXml();
      } else if (mOptions.getSaveAs().equals("xmlorig")) {
        System.err.println("Converting output document to xmlorig");
        toSave = mDocument.toXml(new gate.annotation.AnnotationSetImpl(mDocument));
      }
      if (mOptions.getSaveTo().equals("stdout")) {
        try {
          saveStringToFile(toSave, System.out);
        } catch (Exception ex) {
          System.err.println("Exception when writing to stdout: " + ex);
        }
      } else if (mOptions.getSaveTo().equals("file")) {
        System.err.println("Save to file");
        // create the full path by combining the outdir(or curdir)
        // with the name of document
        String fileName;
        fileName = mFileName;
        if (fileName == null) {
          fileName = mDocument.getName() + ".xml";
        }
        if (mOptions.isReplExt()) {
          fileName = fileName.replaceAll("\\.[^.]+$", mOptions.getReplExt());
        }
        try {
          if (mOptions.isCompressOut()) {
            fileName = fileName + ".gz";
          }
          File outFile = new File(mOptions.getOutDir(), fileName);
          System.err.println("Writing document to file " + outFile.toString());
          FileOutputStream outstream = new FileOutputStream(outFile);
          saveStringToFile(toSave, outstream);
          outstream.close();
        } catch (Exception ex) {
          System.err.println("Exception when writing to file: " + ex);
        }
      }
    }
    mCorpus.clear();
    gate.Factory.deleteResource(mDocument);

    System.err.println("Trying to put myself back: "+this.getNr());
    try {
      BatchProcessing.putRunner(this);
    } catch (InterruptedException ex) {
      System.err.println("Got an interrupted exception in "+this.getNr());
    }






  }

  protected void saveStringToFile(
          String string, OutputStream out) throws UnsupportedEncodingException, IOException {
    // convert the string to a byte buffer using the encoding specified
    // as an option or the default encoding if the output format is string,
    // otherwise use UTF-8
    String outputEncoding = "UTF-8";
    if(mOptions.isSaveAs() && mOptions.getSaveAs().equals("string") && mOptions.isOutputEncoding()) {
      outputEncoding = mOptions.getOutputEncoding();
    }
    byte[] buf = string.getBytes(outputEncoding);
    // if we want compression, wrap the output stream into a gzip stream
    // otherwise just use the stream we got
    if(mOptions.isCompressOut()) {
      OutputStream ourOut = new GZIPOutputStream(out);
      ourOut.write(buf);
      ourOut.close();
    } else {
      out.write(buf);
    }

  }



}
