package net.gpstrackapp;

import android.content.Context;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

public interface ImportFileFormat extends FileFormat {
    void importFromFile(InputStream inputStream) throws IOException;
}
