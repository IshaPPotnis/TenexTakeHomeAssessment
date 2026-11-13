package com.tenextakehome.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.model.ParserType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class XmlParser implements Parser {

    private final ObjectMapper objectMapper;

    public XmlParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ParserType getParserType() {
        return ParserType.XML;
    }

    @Override
    public String parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("XML log must not be null or blank");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            Document document = builder.parse(new InputSource(new StringReader(input)));

            Map<String, Object> json = new LinkedHashMap<>();
            Element root = document.getDocumentElement();
            if ("Event".equals(root.getTagName())) {
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        addChild(json, child.getNodeName(), convertElement((Element) child));
                    }
                }
            } else {
                json.put(root.getTagName(), convertElement(root));
            }
            return objectMapper.writeValueAsString(json);
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new IllegalArgumentException("Unable to parse XML log", exception);
        }
    }

    private Object convertElement(Element element) {
        Map<String, Object> value = new LinkedHashMap<>();

        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attribute = element.getAttributes().item(i);
            value.put("@" + attribute.getNodeName(), attribute.getNodeValue());
        }

        StringBuilder text = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addChild(value, child.getNodeName(), convertElement((Element) child));
            } else if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(child.getNodeValue());
            }
        }

        String textValue = text.toString().trim();
        if (value.isEmpty()) {
            return textValue;
        }
        if (!textValue.isEmpty()) {
            value.put("#text", textValue);
        }
        return value;
    }

    private void addChild(Map<String, Object> parent, String name, Object child) {
        Object existing = parent.get(name);
        if (existing == null) {
            parent.put(name, child);
        } else if (existing instanceof List<?> existingList) {
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) existingList;
            children.add(child);
        } else {
            List<Object> children = new ArrayList<>();
            children.add(existing);
            children.add(child);
            parent.put(name, children);
        }
    }
}
