package uk.ac.open.kmi.iserve.discovery.engine.rest;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 23/07/2014.
 */

public class DiscoveryResult {
    private String label;
    private String description;

    private Set<URI> modelReferences;
    private Map<URI, Object> rankPropertyValues;
    private Double rankingScore;
    private MatchResult matchResult;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRankingScore() {
        return rankingScore;
    }

    public void setRankingScore(Double rankingScore) {
        this.rankingScore = rankingScore;
    }


    public Map<URI, Object> getRankPropertyValues() {
        return rankPropertyValues;
    }

    public void setRankPropertyValues(Map<URI, Object> rankPropertyValues) {
        this.rankPropertyValues = rankPropertyValues;
    }

    public void addRankPropertyValue(URI property, Object value) {
        if (rankPropertyValues == null) {
            rankPropertyValues = Maps.newTreeMap();
        }
        rankPropertyValues.put(property, value);
    }

    public Object removeRankProperty(URI property) {
        if (rankPropertyValues != null) {
            return rankPropertyValues.remove(property);
        }
        return null;
    }

    public void addModelReference(URI modelReference) {
        if (modelReferences == null) {
            modelReferences = Sets.newHashSet();
        }
        modelReferences.add(modelReference);
    }

    public Object removeModelReference(URI property) {
        if (rankPropertyValues != null) {
            return rankPropertyValues.remove(property);
        }
        return null;
    }


    public Set<URI> getModelReferences() {
        return modelReferences;
    }

    public void setModelReferences(Set<URI> modelReferences) {
        this.modelReferences = modelReferences;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }

    public String toXML() {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("discoveryResult");
            doc.appendChild(rootElement);

            if (label != null) {
                Element labelEl = doc.createElement("label");
                labelEl.appendChild(doc.createTextNode(label));
                rootElement.appendChild(labelEl);
            }

            if (description != null) {
                Element descriptionEl = doc.createElement("description");
                descriptionEl.appendChild(doc.createTextNode(description));
                rootElement.appendChild(descriptionEl);
            }

            if (rankPropertyValues != null) {
                Element propertiesEl = doc.createElement("properties");
                for (URI property : rankPropertyValues.keySet()) {
                    if (rankPropertyValues.get(property) != null) {
                        Element propertyEl = doc.createElement("property");
                        String value = (String) rankPropertyValues.get(property);
                        propertyEl.setAttribute("name", property.toASCIIString());
                        propertyEl.setAttribute("value", value);
                        propertiesEl.appendChild(propertyEl);
                    }

                }
                rootElement.appendChild(propertiesEl);
            }

            if (modelReferences != null) {
                Element propertiesEl = doc.createElement("modelReferences");
                for (URI modelReference : modelReferences) {
                    Element propertyEl = doc.createElement("modelReference");
                    propertyEl.setAttribute("uri", modelReference.toString());
                    propertiesEl.appendChild(propertyEl);
                }
                rootElement.appendChild(propertiesEl);
            }

            if (rankingScore != null) {
                Element scoreEl = doc.createElement("rankingScore");
                scoreEl.appendChild(doc.createTextNode(rankingScore.toString()));
                rootElement.appendChild(scoreEl);
            }

            if (matchResult != null) {
                Element matchResultEl = doc.createElement("matchResult");
                matchResultEl.setAttribute("matchedResource", matchResult.getMatchedResource().toASCIIString());
                matchResultEl.setAttribute("resourceToMatch", matchResult.getResourceToMatch().toASCIIString());
                matchResultEl.setAttribute("matchType", matchResult.getMatchType().toString());
                matchResultEl.setAttribute("explanation", matchResult.getExplanation());
                rootElement.appendChild(matchResultEl);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            StreamResult result = new StreamResult(os);

            transformer.transform(source, result);

            return new String(os.toByteArray(), "UTF-8");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
