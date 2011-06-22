package at.ofai.gate.inoutprocessing;

import gate.Document;
import gate.FeatureMap;
import gate.Factory;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.util.GateRuntimeException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;


@CreoleResource(
    name = "DocSourceFileDir",
    //interfaceName = "",
    comment = "A document source modeling a directory of files on the file system",
    icon = "ontology"
    //helpURL = "http://gate.ac.uk/userguide/sec:ontologies:ontoplugin:owlim"
    )
public class DocSourceFileDir extends DocSource {

  // Parameters specific to this LR
  @CreoleParameter(
    comment="Directory that contains the files to access as documents",
    defaultValue = "")
  public void setDirectoryURL(URL dirURL)   {
    directoryURL = dirURL;
  }
  public URL getDirectoryURL() {
    return directoryURL;
  }
  private URL directoryURL;

  @CreoleParameter(
    comment="Exclude hidden files", defaultValue="true")
  public void setExcludeHidden(Boolean yesno) {
    excludeHidden = yesno;
  }
  public Boolean getExcludeHidden() {
    return excludeHidden;
  }
  Boolean excludeHidden = true;

  @CreoleParameter(
    comment="Maximum number of documents to access, 0=all",
    defaultValue = "0")
  public void setLimit(Integer max) {
    limit = max;
  }
  public Integer getLimit() {
    return limit;
  }
  Integer limit = 0;

  @CreoleParameter(
    comment="Encoding of the file contents, empty: use system default encoding",
    defaultValue="utf-8")
  public void setEncoding(String enc) {
    encoding = enc;
  }
  public String getEncoding() {
    return encoding;
  }
  String encoding = "utf-8";

  @CreoleParameter(
    comment="Mime type of the file contents, empty: guess from extension",
    defaultValue="application/xml")
  public void setMimeType(String mtype) {
    mimeType = mtype;
  }
  public String getMimeType() {
    return mimeType;
  }
  String mimeType = "application/xml";

  @CreoleParameter(
    comment="If the contents of the file is compressed",
    defaultValue = "FROM_EXTENSION")
  public void setCompressed(ParamCompressed c) {
    compressed = c;
  }
  public ParamCompressed getCompressed() {
    return compressed;
  }
  ParamCompressed compressed = ParamCompressed.FROM_EXTENSION;

  public enum ParamCompressed {
    COMPRESSED, NOT_COMPRESSED, FROM_EXTENSION
  }

  @CreoleParameter(
    comment="If the file extension should be included in the document name",
    defaultValue="true")
  public void setIncludeExtension(Boolean yesno) {
    includeExtension = yesno;
  }
  public Boolean getIncludeExtension() {
    return includeExtension;
  }
  Boolean includeExtension = true;

  @CreoleParameter(
    comment="If all subdirectories should be recursively be used too",
    defaultValue="false")
  public void setRecurseDirectories(Boolean yesno) {
    recurseDirectories = yesno;
  }
  public Boolean getRecurseDirectories() {
    return recurseDirectories;
  }
  Boolean recurseDirectories =  false;

  @CreoleParameter(
    comment="A regular expression pattern that must match for the file to be used",
    defaultValue="")
  public void setFilenamePattern(String pattern) {
    filenamePattern = pattern;
  }
  public String getFilenamePattern() {
    return filenamePattern;
  }
  String filenamePattern = "";

  // FIELDS

  Pattern mPattern = null;
  String mEncoding = null;
  public List<String> fileNames = new ArrayList<String>();
  
  public List<String> soso() {
    return fileNames;
  }
  Iterator<String> fileNamesIterator;
  
  public Resource init() throws ResourceInstantiationException {
    if(getDirectoryURL() == null) {
      throw new ResourceInstantiationException("directoryURL must not be empty");
    }
    File theDir = gate.util.Files.fileFromURL(getDirectoryURL());
    try {
      theDir = theDir.getCanonicalFile();
    } catch (IOException ex) {
      throw new ResourceInstantiationException("Could not get canonical name for "+theDir,ex);
    }
    if(!theDir.exists()) {
      throw new ResourceInstantiationException("directory "+theDir.getAbsolutePath()+" does not exist");
    }
    if(!theDir.isDirectory()) {
      throw new ResourceInstantiationException("File "+theDir.getAbsolutePath()+" is not a directory");
    }
    // if we do have a file pattern, compile it
    if(getFilenamePattern() != null && !getFilenamePattern().equals("")) {
      mPattern = Pattern.compile(getFilenamePattern());
    }

    if(getEncoding() == null || getEncoding().equals("")) {
      mEncoding = System.getProperty("file.encoding");
      if(mEncoding == null) {
        mEncoding = "UTF-8";
      }
    }

    Queue<File> dirsToProcess = new LinkedList<File>();
    dirsToProcess.add(theDir);
    while(!dirsToProcess.isEmpty()) {
      File curDir = dirsToProcess.remove();
      String[] fns = curDir.list();
      for(String f : fns) {
        File theFile = new File(curDir,f);
        if(theFile.isDirectory()) {
          if(getRecurseDirectories()) {
            dirsToProcess.add(theFile);
          }
        } else if(theFile.isHidden() && getExcludeHidden()) {
          // do nothing
        } else {
          if(mPattern != null) {
            if(mPattern.matcher(f).matches()) {
              // TODO: add the path relative to theDir
              fileNames.add(getRelativePathName(theDir,theFile));
            }
          } else {
            // TODO: add the path relative to theDir
            fileNames.add(getRelativePathName(theDir,theFile));
          }
        }
      }
    }
    return this;
  }

  private String getRelativePathName(File theDir, File theFile) {
    String dirName = theDir.getAbsolutePath();
    String fileName = theFile.getAbsolutePath();
    return fileName.substring(dirName.length()+1);
  }

  @Override
  public int getTotalNumberOfDocuments() {
    return fileNames.size();
  }

  public void open() {
    fileNamesIterator = fileNames.iterator();
  }

  public void close() {
    fileNamesIterator = null;
  }

  public boolean hasNext() {
    return fileNamesIterator.hasNext();
  }

  public Document next() {
    Document doc = null;
    if(!fileNamesIterator.hasNext()) {
      return null;
    }
    String fileName = fileNamesIterator.next();
    // TODO: get the document and return it
    // if there is an error and we do not fail on conversion, return null
    try {
      // TODO: create the full file or filename from what we have and
      // the root dir name
      String content = getStringFromFile(fileName);
      // if we have a mime type specified, create the document with that
      // mime type
      FeatureMap parms = Factory.newFeatureMap();
      parms.put("encoding", mEncoding);
      if(!getMimeType().equals("")) {
        parms.put("mimeType", getMimeType());
      }
      // TODO!!!! If we do not have a compressed file, and we do not have
      // a mimetype, use sourceUrl parm for the factory. Otherwise use
      // the content. This may help getting the mimetype processing based
      // on the extension right? How does the factory do that???
      parms.put("stringContent", content);
      FeatureMap fm = Factory.newFeatureMap();
      // TODO: correct way to create a document name from the file name?
      // TODO: include the whole path or just the basename, include the extension?
      String docname = fileName;
      doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",parms,fm,docname);
    } catch (Exception ex) {
      if(getFailOnConversionError()) {
        throw new GateRuntimeException("Exception getting document from file "+fileName,ex);
      } else {
        // TODO: just log the error!
      }
    }
    return doc;
  }

  public String getStringFromFile(String filename)
    throws FileNotFoundException, IOException {
    String content = "";
    // if input compression is allowed, check if the name ends in .gz
    // if yes, read the file using a decompressing stream, otherwise using
    // a normal stream
    InputStream isorig = new FileInputStream(new File(filename));
    if(getCompressed().equals(ParamCompressed.COMPRESSED) ||
       (getCompressed().equals(ParamCompressed.FROM_EXTENSION)
        && filename.endsWith(".gz"))) {
      InputStream isdec = new GZIPInputStream(isorig);
      content = IOUtils.toString(isdec, mEncoding);
      isdec.close();
    } else {
      content = IOUtils.toString(isorig,mEncoding);
    }
    isorig.close();
    return content;
  }

}