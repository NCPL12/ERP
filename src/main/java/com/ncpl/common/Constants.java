package com.ncpl.common;

import java.io.File;

import org.apache.commons.lang.SystemUtils;

public class Constants {
	public static String  FILE_LOCATION = SystemUtils.getUserHome()+File.separator+"NCPL_FILES";
	//public static String location = "E:\\files";
	//public static String  FILE_LOCATION = new File(location) +File.separator+"NCPL_FILES";
	//public static String  PDF_LOCATION = new File(location) +File.separator+"PDF_FILES";
	public static long currentDate() {
		return System.currentTimeMillis();
	}
	

	public static final String PO_ACTIVITY_REASON = "STOCK IMPORTED";
	public static final String SO_ACTIVITY_REASON = "STOCK EXPORTED";
	public static final String ASSIGNMENT_REASON = "STOCK ASSIGNED";
}
