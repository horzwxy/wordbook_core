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
import java.util.*;

/**
 * Created by horz on 1/29/14.
 */
public class NoteManipulator {

    public static String getNoteContent(Map<String, Word> words) {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                + "<en-note>";
        List<Word> wordList = new ArrayList<Word>(words.values());
        Collections.sort(wordList, new Comparator<Word>() {
            @Override
            public int compare(Word w1, Word w2) {
                return w1.getContent().compareTo(w2.getContent());
            }
        });
        for(Word word : wordList) {
            result += "<p><em>" + word.getContent() + "</em>";
            for(String sentence : word.getSentences()) {
                if(word.getState() != WordState.BASIC
                        && word.getState() != WordState.IGNORED
                        && word.getState() != WordState.FAMILIAR) {
                    result += "<span>" + sentence + "</span>";
                }
            }
            result += "</p>";
        }
        result += "</en-note>";
        return result;
    }

    public static Map<String, Word> getWords(String noteContent, WordState state) {
        Map<String, Word> words = new HashMap<String, Word>();

        // set up XML parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
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
                NodeList childNodes = node.getChildNodes();
                Word word = null;
                for(int j = 0; j < childNodes.getLength(); j++) {
                    Node childNode = childNodes.item(j);
                    if(childNode.getNodeName().equals("em")) {
                        word = new Word(childNode.getTextContent(), state);
                    }
                    else if(word != null && childNode.getNodeName().equals("span")) {
                        word.getSentences().add(childNode.getTextContent());
                    }
                }
                words.put(word.getContent(), word);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }
}
