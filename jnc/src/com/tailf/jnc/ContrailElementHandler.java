package com.tailf.jnc;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

public class ContrailElementHandler extends ElementHandler {

    private Map<String, Tagpath> library = new HashMap<>();

    public ContrailElementHandler(String namespace,String name) {
        initLibrary(namespace, name);
    }

    private void initLibrary(String namespace,String tagpath){
        SchemaNode node= SchemaTree.lookup(namespace, new Tagpath(tagpath));
        if(node!=null){
            if(node.mapping_path!=null&&!node.mapping_path.trim().equals("")){
                this.library.put(node.mapping_path.trim().replaceAll("-","_"),new Tagpath(tagpath));
            }
            for(String childPath:node.yang_children){
                if(childPath!=null&&!childPath.trim().equals("")){
                    initLibrary(namespace,tagpath+"/"+childPath);}
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        String convertName = elementConverter(localName);
        if (unknownLevel > 0) {
            unknownStartElement(uri, convertName, attributes);
            return;
        }
        final Element parent = current;
        Element child;

        try {
            child = YangElement.createInstance(this, parent, uri, convertName);
        } catch (final JNCException e) {
            e.printStackTrace();
            throw new SAXException(e.toString());
        }

        if (top == null) {
            top = child;
        }

        if (child == null && unknownLevel == 1) {
            // we're entering XML data that's not in the schema
            unknownStartElement(uri, convertName, attributes);
            return;
        }

        if (child == null) {
            // it's a known leaf
            // it'll be handled in the endElement method
            leaf = true;
            leafNs = uri;
            leafName = convertName;
            leafValue = "";
            return;
        }
        child.prefixes = prefixes;
        prefixes = null;
        addOtherAttributes(attributes, child);
        current = child; // step down
    }

    private void unknownStartElement(String uri, String localName, Attributes attributes) throws SAXException {
        String convertName = elementConverter(localName);
        final Element child = new Element(uri, convertName);
        child.prefixes = prefixes;
        prefixes = null;
        addOtherAttributes(attributes, child);
        if (current == null) {
            top = child;
        } else {
            current.addChild(child);
        }
        current = child; // step down
    }

    private void addOtherAttributes(Attributes attributes, Element child) {
        // add other attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            final String attrName = attributes.getLocalName(i);
            final String attrUri = attributes.getURI(i);
            final String attrValue = attributes.getValue(i);
            final Attribute attr = new Attribute(attrUri, attrName, attrValue);
            child.addAttr(attr);
        }
    }

    private void unknownEndElement() {
        // check that we don't have mixed content
        if (current.hasChildren() && current.value != null) {
            // MIXED content not allowed
            current.value = null;
        }
        // step up
        current = current.getParent();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (unknownLevel > 0) {
            unknownEndElement();
            unknownLevel--;
            return;
        }

        if (leaf) {
            // If it's a Leaf - we need to set value properly using
            // the setLeafValue method which will check restrictions
            try {
                ((YangElement) current).setLeafValue(leafNs, leafName, leafValue);
            } catch (final JNCException e) {
                e.printStackTrace();
                throw new SAXException(e.toString());
            }
        } else {
            // check that we don't have mixed content
            if (current.hasChildren() && current.value != null) {
                // MIXED content not allowed
                current.value = null;
            }
        }

        // step up
        if (!leaf) {
            current = current.getParent();
        } else {
            leaf = false;
        }
    }

    private void unknownCharacters(char[] ch, int start, int length) {
        if (current.value == null) {
            current.value = "";
        }
        current.value = current.value + new String(ch, start, length);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (unknownLevel > 0) {
            unknownCharacters(ch, start, length);
            return;
        }

        if (leaf) {
            leafValue = leafValue + new String(ch, start, length);
        } else {
            if (current.value == null) {
                current.value = "";
            }
            current.value = current.value + new String(ch, start, length);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        if (prefixes == null) {
            prefixes = new PrefixMap();
        }
        prefixes.add(new Prefix(prefix, uri));
    }

    public String elementConverter(String localName)  {
        String key = null;
        if (library.containsKey(localName)) {
            String tagpath = library.get(localName).toString();
            key = tagpath.substring(tagpath.lastIndexOf("/")+1);
        }
        if (key == null) {
            System.out.println("can't find the tagpath for : " + localName);
            key=localName;
        }
        return key;
    }

    protected Boolean evaluateTagpath(String namespace, String name){
        Tagpath tagpath=null;
        if(current==null){
            tagpath=new Tagpath(elementConverter(name));
        }else{
            tagpath=new Tagpath(current.tagpath()+"/"+elementConverter(name));
        }
        SchemaNode node= SchemaTree.lookup(namespace, tagpath);
        if(node!=null)
            return true;
        else
            return false;
    }
}
