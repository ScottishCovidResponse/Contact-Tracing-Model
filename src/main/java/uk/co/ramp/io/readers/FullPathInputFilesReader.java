package uk.co.ramp.io.readers;

import java.io.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.io.types.ImmutableInputFiles;
import uk.co.ramp.io.types.InputFiles;

public class FullPathInputFilesReader {
  private static final Logger LOGGER = LogManager.getLogger(FullPathInputFilesReader.class);

  private final InputFilesReader inputFilesReader;
  private final DirectoryList directoryList;
  private final String overrideInputFolderLocation;
  private final String defaultInputFolderLocation;

  public FullPathInputFilesReader(
      InputFilesReader inputFilesReader,
      DirectoryList directoryList,
      String overrideInputFolderLocation,
      String defaultInputFolderLocation) {
    this.inputFilesReader = inputFilesReader;
    this.directoryList = directoryList;
    this.overrideInputFolderLocation = overrideInputFolderLocation;
    this.defaultInputFolderLocation = defaultInputFolderLocation;
  }

  public InputFiles read(Reader reader) {
    var baseInputFiles = inputFilesReader.read(reader);
    var inputFiles =
        ImmutableInputFiles.builder()
            .contactData(getFilePath(baseInputFiles.contactData()))
            .diseaseSettings(getFilePath(baseInputFiles.diseaseSettings()))
            .initialExposures(getFilePath(baseInputFiles.initialExposures()))
            .isolationPolicies(getFilePath(baseInputFiles.isolationPolicies()))
            .populationSettings(getFilePath(baseInputFiles.populationSettings()))
            .runSettings(getFilePath(baseInputFiles.runSettings()))
            .tracingPolicies(getFilePath(baseInputFiles.tracingPolicies()))
            .build();

    LOGGER.info("Loaded input files from locations: {}", inputFiles);
    return inputFiles;
  }

  private String getFilePath(String fileName) {
    return directoryList
        .listFiles(overrideInputFolderLocation)
        .filter(f -> f.equals(fileName))
        .map(f -> overrideInputFolderLocation + "/" + f)
        .findFirst()
        .orElse(defaultInputFolderLocation + "/" + fileName);
  }
}
