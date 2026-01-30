package com.ncpl.sales.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.json.JSONArray;
import org.json.JSONException;

@Converter
public class JSONObjectConverter implements AttributeConverter<JSONArray, String> {
    @Override
    public String convertToDatabaseColumn(JSONArray jsonData) {
        String json;
        try{
            json = jsonData.toString();
        }
        catch (NullPointerException ex)
        {
            //extend error handling here if you want
            json = "";
        }
        return json;
    }

    @Override
    public JSONArray convertToEntityAttribute(String jsonDataAsJson) {
    	JSONArray jsonData;
        try {
            jsonData = new JSONArray(jsonDataAsJson);
        } catch (JSONException ex) {
            //extend error handling here if you want
            jsonData = null;
        }
        return jsonData;
    }
}
