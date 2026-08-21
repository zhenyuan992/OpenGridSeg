package org.opengridseg.export;
import java.nio.file.Path;
public final class ExportResult { private final Path omeTiff,csv,config; ExportResult(Path omeTiff,Path csv,Path config){this.omeTiff=omeTiff;this.csv=csv;this.config=config;} public Path omeTiff(){return omeTiff;}public Path csv(){return csv;}public Path config(){return config;} }
