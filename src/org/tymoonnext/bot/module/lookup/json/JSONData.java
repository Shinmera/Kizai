package org.tymoonnext.bot.module.lookup.json;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses JSON data and provides a simple interface for reading it
 * @author Mithent
 */
public class JSONData{
    private Object content;
    private ArrayList<JSONData> elements;
    private HashMap<String, Integer> namedElementIndexes;
    
    public JSONData(String source) throws InvalidJSONException {
        parseJSONString(source);
    }
    
    public Object getContent()
    {
        return content;
    }
    
    public int elementCount() {
        if (elements == null) return 0;
        
        return elements.size();
    }
    
    public JSONData getIndex(int index) {
        if (elements == null) return null;
        
        if (index < elements.size()) {
            return elements.get(index);
        } else {
            return null;
        }
    }
    
    public JSONData getByName(String name) {
        if (namedElementIndexes == null) return null;
        if (name == null || name.isEmpty()) return null;
        
        Integer index = namedElementIndexes.get(content);
        
        return getIndex(index);
    }
    
    private void parseJSONString(String source) throws InvalidJSONException {
        source = source.trim();
        
        char firstChar = source.charAt(0);
        char lastChar = source.charAt(source.length() - 1);
        
        if (firstChar == '[') {
            // Array
            if (lastChar != ']') throw new InvalidJSONException("Mismatched array");
            parseArray(source.substring(1, source.length() - 1));
        } else if (firstChar == '{') {
            // Object
            if (lastChar != '}') throw new InvalidJSONException("Mismatched object");
            parseObject(source.substring(1, source.length() - 1));
        } else if (firstChar == '"') {
            // String
            if (lastChar != '"') throw new InvalidJSONException("Mismatched string");
            parseString(source.substring(1, source.length() - 1));
        } else if ("true".equals(source.toLowerCase())) {
            content = true;
        } else if ("false".equals(source.toLowerCase())) {
            content = false;
        } else if ("null".equals(source.toLowerCase())) {
            content = null;
        } else {
            // Probably a number
            parseNumber(source);
        }
    }
    
    private void parseArray(String source) throws InvalidJSONException {
        elements = new ArrayList<JSONData>();
        String[] parts = source.split(",");
        
        for(String part : parts) {
            elements.add(new JSONData(part));
        }
    }

    private void parseObject(String source) throws InvalidJSONException {
        elements = new ArrayList<JSONData>();
        namedElementIndexes = new HashMap<String, Integer>();
        String[] fields = source.split(",");
        
        for(String field : fields) {
            String[] parts = field.split(":");
            
            if (parts.length != 2) throw new InvalidJSONException("Object fields must be name/value pairs.");
            
            elements.add(new JSONData(parts[1]));
            namedElementIndexes.put(parts[0], elements.size() - 1);
        }
    }

    private void parseString(String source){
        content = source;
    }

    private void parseNumber(String source) throws InvalidJSONException {
        try {
            Double asDouble = Double.parseDouble(source);
            if (asDouble == Math.floor(asDouble)) {
                content = asDouble.intValue();
            } else {
                content = asDouble.doubleValue();
            } 
        }
        catch (NumberFormatException ex) {
            throw new InvalidJSONException(ex);
        }
    }
    
}
