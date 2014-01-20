package me.horzwxy.app.wordbook.model;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by horz on 1/19/14.
 */
public class EvernoteWordRecord extends WordRecord {

    public EvernoteWordRecord(String word, List<String> egSentences) {
        super(word, egSentences);
    }

    /**
     * Construct record from word and Evernote Note content.
     * @param word English word
     * @param noteContent ENML format string
     */
    public EvernoteWordRecord(String word, String noteContent) {
        super(word, new ArrayList<String>());
        List<String> egSentences = getEgSentences();

        // set up XML parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

//        factory.setValidating(false);
//        factory.setNamespaceAware(true);
        try {
////            factory.setFeature("http://xml.org/sax/features/namespaces", false);
////            factory.setFeature("http://xml.org/sax/features/validation", false);
////            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            DocumentBuilder db = factory.newDocumentBuilder();
            Document xmlDoc = db.parse(new ByteArrayInputStream( noteContent.getBytes() ));

            // get the word node list
            NodeList list = xmlDoc.getElementsByTagName( "p" );
            for( int i = 0; i < list.getLength(); i++ ) {
                Node node = list.item( i );
                egSentences.add(node.getTextContent());
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Construct ENML format XML string, representing arrays of example sentences
     * @return
     */
    public String getNoteContent() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                + "<en-note>";
        List<String> egSentences = getEgSentences();
        for(String egSentence : egSentences) {
            result += "<p>" + egSentence + "</p>";
        }
        result += "</en-note>";
        return result;
    }
}
