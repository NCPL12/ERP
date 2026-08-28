package com.ncpl.sales.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

public class InvoiceExcelLogoService {

	public void insertLogoInTemplate(Workbook workbook, Sheet sheet, HttpServletRequest request) throws IOException {
		System.out.println("Inserting logo in the dc template" );
		ServletContext servletContext = request.getSession().getServletContext();
        String imgLoc = servletContext.getRealPath("/WEB-INF/images");
       
		 File file = new File(imgLoc+"/ncpl_logo_new.png");
		 
	        FileInputStream fis = new FileInputStream(file);
	        byte[] imgBytes = IOUtils.toByteArray(fis);
	     	//Set image position
	        CreationHelper helper = workbook.getCreationHelper();
	        ClientAnchor anchor = helper.createClientAnchor();
	        System.out.println(System.getProperty("java.class.path"));

	       // anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
	      
	        
	        anchor.setCol1(0);
	       anchor.setRow1(1);
	        //Draw image
	        Drawing imgDrawing = sheet.createDrawingPatriarch();
	        int imgId = workbook.addPicture(imgBytes, Workbook.PICTURE_TYPE_JPEG);
	        Picture imgPic = imgDrawing.createPicture(anchor, imgId);
	      //  imgPic.resize();
	        imgPic.resize();
	        
	}
	
}
