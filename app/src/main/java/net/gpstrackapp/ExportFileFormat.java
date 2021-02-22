package net.gpstrackapp;

import android.content.Context;

import net.gpstrackapp.geomodel.track.Track;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

public interface ExportFileFormat extends FileFormat {
    void exportToFile(Context ctx, Set<Track> tracksToExport, String trackName, OutputStream outputStream) throws ParserConfigurationException, IOException;
}
