package net.gpstrackapp.format;

import net.gpstrackapp.GPSComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormatManager {
    private static Map<String, ImportFileFormat> importFormats = new HashMap<>();
    private static Map<String, ExportFileFormat> exportFormats = new HashMap<>();

    static {
        registerFormat(new KMLFileFormat());
        registerFormat(new GPXFileFormat(GPSComponent.getTrackModelManager()));
    }

    public static Map<String, ImportFileFormat> getImportFormats() {
        return importFormats;
    }

    public static Map<String, ExportFileFormat> getExportFormats() {
        return exportFormats;
    }

    private static void registerFormat(FileFormat fileFormat) {
        if (fileFormat instanceof ImportFileFormat) {
            importFormats.put(fileFormat.getFileExtensionString(), (ImportFileFormat) fileFormat);
        }
        if (fileFormat instanceof ExportFileFormat) {
            exportFormats.put(fileFormat.getFileExtensionString(), (ExportFileFormat) fileFormat);
        }
    }

    public static ImportFileFormat getImportFormatByFileExtension(String format) {
        return importFormats.get(format);
    }

    public static ExportFileFormat getExportFormatByFileExtension(String format) {
        return exportFormats.get(format);
    }

    public static ImportFileFormat getImportFormatByMediaType(String mediaType) {
        List<ImportFileFormat> formats = new ArrayList<>(importFormats.values());
        for (ImportFileFormat format : formats) {
            if (format.getMediaType().equals(mediaType))
                return format;
        }
        return null;
    }
}
