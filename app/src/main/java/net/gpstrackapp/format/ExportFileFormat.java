package net.gpstrackapp.format;

import android.content.Context;

import net.gpstrackapp.geomodel.track.Track;

import java.io.OutputStream;
import java.util.Set;

public interface ExportFileFormat extends FileFormat {
    void exportToFile(Context ctx, Set<Track> tracksToExport, String fileName, OutputStream outputStream, CharSequence ownerName) throws Exception;
}
