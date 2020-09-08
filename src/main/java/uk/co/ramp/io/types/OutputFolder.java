package uk.co.ramp.io.types;

import java.io.File;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface OutputFolder {

  File outputFolder();
}
