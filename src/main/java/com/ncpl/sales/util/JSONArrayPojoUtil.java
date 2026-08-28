package com.ncpl.sales.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class JSONArrayPojoUtil {
ObjectMapper mapper = new ObjectMapper();
	public List<?> jsonArrayToPojo(String jsonArr){
		  Gson gson = new Gson(); 
		  Type listType = new TypeToken<ArrayList<?>>(){}.getType();
		  ArrayList<?> pojoList = gson.fromJson(jsonArr, listType);  
		  
		  
		return pojoList;
	}
}
