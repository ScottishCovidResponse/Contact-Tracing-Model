package uk.co.ramp.io.readers;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class DirectoryList {
  public Stream<String> listFiles(String directory) {
    return Arrays.stream(Objects.requireNonNull(new File(directory).listFiles()))
        .map(File::getName);
  }
}
