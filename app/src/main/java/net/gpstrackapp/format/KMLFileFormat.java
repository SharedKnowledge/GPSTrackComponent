package net.gpstrackapp.format;

import android.content.Context;
import android.util.Log;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackPoint;
import net.gpstrackapp.geomodel.track.TrackSegment;

import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//TODO use OSMBonusPack once they implement <gx:MultiTrack>
public class KMLFileFormat implements ExportFileFormat {
    private String trackStyleTag = "trackStyle";
    private DateTimeFormatter formatterWhen = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public String getFileExtensionString() {
        return "kml";
    }

    @Override
    public String getMediaType() {
        return "application/vnd.google-earth.kml+xml";
    }

    @Override
    public void exportToFile(Context ctx, Set<Track> tracksToExport, String fileName, OutputStream outputStream, CharSequence ownerName) throws Exception {
        Document document = generateKML(fileName, tracksToExport);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);
    }

    public Document generateKML(String fileName, Set<Track> tracksToExport) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document document = dBuilder.newDocument();

        Element kmlDocumentTag = generateKMLDocumentTag(document);
        attachMetadataToDocumentTag(document, kmlDocumentTag, fileName);
        attachTracksToDocumentTag(document, kmlDocumentTag, tracksToExport);
        return document;
    }

    private Element generateKMLDocumentTag(Document document) throws Exception {
        // root element
        Element kmlElement = document.createElement("kml");
        Attr namespaceKML = document.createAttribute("xmlns");
        namespaceKML.setValue("http://www.opengis.net/kml/2.2");
        Attr namespaceGX = document.createAttribute("xmlns:gx");
        namespaceGX.setValue("http://www.google.com/kml/ext/2.2");
        Attr namespaceAtom = document.createAttribute("xmlns:atom");
        namespaceAtom.setValue("http://www.w3.org/2005/Atom");
        kmlElement.setAttributeNode(namespaceKML);
        kmlElement.setAttributeNode(namespaceGX);
        kmlElement.setAttributeNode(namespaceAtom);
        document.appendChild(kmlElement);

        // document
        Element kmlDocument = document.createElement("Document");
        kmlElement.appendChild(kmlDocument);

        return kmlDocument;
    }

    private void attachMetadataToDocumentTag(Document document, Element parent, String fileName) {
        //visibility (whether to draw in 3D viewer when initially loaded)
        Element visibility = document.createElement("visibility");
        visibility.appendChild(document.createTextNode("1"));
        parent.appendChild(visibility);

        //name
        Element name = document.createElement("name");
        name.appendChild(document.createTextNode(fileName));
        parent.appendChild(name);

        //author
        Element author = document.createElement("atom:author");
        Element authorName = document.createElement("atom:name");
        authorName.appendChild(document.createTextNode(
                GPSComponent.getGPSComponent().getASAPApplication().getOwnerName().toString()));
        author.appendChild(authorName);
        parent.appendChild(author);

        //style
        Element trackStyle = document.createElement("Style");
        Attr trackStyleID = document.createAttribute("id");
        trackStyleID.setValue(trackStyleTag);
        trackStyle.setAttributeNode(trackStyleID);

        Element lineStyle = document.createElement("LineStyle");
        Element color = document.createElement("color");
        color.appendChild(document.createTextNode("#ff9900ff"));
        lineStyle.appendChild(color);
        Element width = document.createElement("width");
        width.appendChild(document.createTextNode("4"));
        lineStyle.appendChild(width);
        trackStyle.appendChild(lineStyle);

        Element iconStyle = document.createElement("IconStyle");
        Element scale = document.createElement("scale");
        scale.appendChild(document.createTextNode("1.5"));
        iconStyle.appendChild(scale);
        Element icon = document.createElement("Icon");
        Element href = document.createElement("href");
        href.appendChild(document.createTextNode(
                "http://earth.google.com/images/kml-icons/track-directional/track-0.png"));
        icon.appendChild(href);
        iconStyle.appendChild(icon);
        trackStyle.appendChild(iconStyle);
        parent.appendChild(trackStyle);
    }

    private void attachTracksToDocumentTag(Document document, Element parent, Set<Track> tracksToExport) {
        for (Track track : tracksToExport) {
            //placemark
            Element placemark = document.createElement("Placemark");
            parent.appendChild(placemark);

            String multiTrackName = track.getObjectName().toString();
            if (multiTrackName != null) {
                Element placemarkName = document.createElement("name");
                placemarkName.appendChild(document.createTextNode(multiTrackName));
                placemark.appendChild(placemarkName);
            }
            Element styleUrl = document.createElement("styleUrl");
            styleUrl.appendChild(document.createTextNode("#" + trackStyleTag));
            placemark.appendChild(styleUrl);

            Element dateOfCreation = document.createElement("TimeStamp");
            Element whenDateOfCreation = document.createElement("when");
            String formattedDateOfCreation = track.getDateOfCreation().format(formatterWhen);
            whenDateOfCreation.appendChild(document.createTextNode(formattedDateOfCreation));
            dateOfCreation.appendChild(whenDateOfCreation);
            placemark.appendChild(dateOfCreation);

            //multiTrack (what is called Track in this app is called MultiTrack in KML)
            Element multiTrack = document.createElement("gx:MultiTrack");
            if (multiTrackName != null) {
                Attr multiTrackID = document.createAttribute("id");
                multiTrackID.setValue(multiTrackName);
                multiTrack.setAttributeNode(multiTrackID);
            }
            Element altitudeMode = document.createElement("altitudeMode");
            altitudeMode.appendChild(document.createTextNode("absolute"));
            multiTrack.appendChild(altitudeMode);

            Element interpolate = document.createElement("gx:interpolate");
            interpolate.appendChild(document.createTextNode("0"));
            multiTrack.appendChild(interpolate);

            placemark.appendChild(multiTrack);

            for (TrackSegment trackSegment : track.getTrackSegments()) {
                //track (what is called TrackSegment in this app is called Track in KML)
                Element kmlTrack = document.createElement("gx:Track");
                multiTrack.appendChild(kmlTrack);

                List<TrackPoint> trackPoints = trackSegment.getTrackPoints();
                if (trackPoints != null) {
                    for (TrackPoint trackPoint : trackPoints) {
                        LocalDateTime trackPointDate = trackPoint.getDate();
                        GeoPoint geoPoint = trackPoint.getGeoPoint();
                        // dates and coordinates have to be present for all points for an export to KML
                        if (trackPointDate != null && geoPoint != null) {
                            //when
                            Element when = document.createElement("when");
                            String formattedDate = trackPointDate.format(formatterWhen);
                            when.appendChild(document.createTextNode(formattedDate));
                            kmlTrack.appendChild(when);

                            //coordinates
                            Element coord = document.createElement("gx:coord");
                            String formattedCoord = geoPoint.getLongitude() + " "
                                    + geoPoint.getLatitude() + " "
                                    + geoPoint.getAltitude();
                            coord.appendChild(document.createTextNode(formattedCoord));
                            kmlTrack.appendChild(coord);
                        } else {
                            Log.d(getLogStart(), "A TrackPoint was skipped because values were missing");
                        }
                    }
                }
            }
        }
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
