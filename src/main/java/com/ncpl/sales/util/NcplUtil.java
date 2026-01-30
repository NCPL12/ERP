package com.ncpl.sales.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NcplUtil {

	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper=new ObjectMapper();
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		mapper.setDateFormat(df);
		mapper.setTimeZone(TimeZone.getTimeZone("IST"));
		return mapper;
	}
	
	
	public String DateToString(Date date) {
		String pattern = "yyyy-MM-dd";
		DateFormat df = new SimpleDateFormat(pattern);
		String dateAsString = df.format(date);
		return dateAsString;
	}
}
