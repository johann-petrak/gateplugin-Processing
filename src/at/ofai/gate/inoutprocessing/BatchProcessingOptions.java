package at.ofai.gate.inoutprocessing;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

import java.util.List;

@CommandLineInterface(application="OFAIProcessFiles")
public interface BatchProcessingOptions {

  @Option(shortName="l",longName="listfrom",pattern="^stdin|file|dir|dircorpus$",defaultValue="dir",description="Where to get the list of filenames from: stdin, file, dir, dircorpus (default: dir)")
  String getListFrom();
  boolean isListFrom();

  // Directory that contains the files to process (default: current)
  @Option(longName="indir",defaultValue="",description="Directory that contains the files to process (default: current)")
  String getInDir();
  boolean isInDir();

  @Option(longName="outdir",defaultValue="",description="Directory where processed files are strored (default: same es indir)")
  String getOutDir();
  boolean isOutDir();

  @Option(shortName="c",longName="controller",description="Name of the controller/pipeline file")
  String getController();

  @Option(longName="saveas",defaultValue="xml",description="Save as xml, xmlorig, xmlfull, string (default: xml, ignored for dircorpus)")
  String getSaveAs();
  boolean isSaveAs();

  @Option(longName="saveto",defaultValue="file",description="Save to file, none, stdout (default: file, ignored for dircorpus)")
  String getSaveTo();
  boolean isSaveTo();

  @Option(longName="replext",description="Replace existing extension with this (include dot, ignored for dircorpus)")
  String getReplExt();
  boolean isReplExt();

  @Option(longName="pluginsdir",description="The directory where the needed plugins are located")
  String getPluginsDir();
  boolean isPluginsDir();

  // if this is set to gzip or zip, the output file will be compressed and
  // an additional extension .gz or .zip will be added
  @Option(longName="compressout",description="If output should be compressed using gzip")
  boolean isCompressOut();

  // if this is specified, any input file name with an extension .gz or .zip
  // indicates a compressed file. The document name will be the file name without
  // the .gz or .zip extension. 
  @Option(longName="allowcompressedin",description="If specified, .gz indicates compressed files")
  boolean isAllowCompressedIn();

  // if this is specified, it is interpreted as a regular expression that
  // is matched against the file name (excluding any .gz or .zip extension)
  @Option(longName="namepattern",description="Regular expression pattern that must match a name for it to be used")
  String getNamePattern();
  boolean isNamePattern();

  @Option(longName="outputEncoding",description="Encoding to use for the output if string, default is system encoding, XML is always encoded as UTF-8")
  String getOutputEncoding();
  boolean isOutputEncoding();

  @Option(longName="inputEncoding",description="Encoding to use for the input, default is system encoding for non XML files, XML is always read as UTF-8")
  String getInputEncoding();
  boolean isInputEncoding();

  @Option(longName="nrthreads",defaultValue="1",description="Number of threads to use. Use with care!")
  String getNrThreads();

  @Option(longName="log4jconfig",description="Path to a log4j configuration properties file")
  String getLog4jConfig();
  boolean isLog4jConfig();

  @Option(shortName="d")
  boolean isDebug();
  
  @Option(helpRequest=true,description="Display help and exit")
  boolean isHelp();

  @Unparsed
  List<String> getArgs();
  boolean isArgs();

}


