package com.ncpl.sales.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NcplUtil {
	public static String formatCompanyAddress(String[] fields) {
		if (fields.length != 8) {
			throw new IllegalArgumentException("Company address must have eight $$-separated fields");
		}
		return fields[1].trim() + "\n" + fields[2].trim() + "\n" + fields[3].trim()
				+ "\n" + fields[4].trim() + "\n" + fields[5].trim() + " " + fields[7].trim();
	}

	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper=new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
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
