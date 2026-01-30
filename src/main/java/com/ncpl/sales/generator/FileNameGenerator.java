package com.ncpl.sales.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FileNameGenerator {
	public String generateFileNameAsDate() {
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss") ;
		Date date = new Date();
		String currentDateTime = dateFormat.format(date);
		return currentDateTime;
	}
}