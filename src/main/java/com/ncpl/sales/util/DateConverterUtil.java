package com.ncpl.sales.util;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

@Service
public class DateConverterUtil {
	public Timestamp convertJavaDateToSqlDate(java.util.Date date)
	{
		Timestamp ts = new Timestamp(date.getTime());
		return  ts;
	}
	
	
}
