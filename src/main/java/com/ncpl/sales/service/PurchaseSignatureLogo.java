package com.ncpl.sales.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.util.IOUtils;

import com.ncpl.sales.security.User;

public class PurchaseSignatureLogo {
	public void insertLogoInTemplate(Workbook workbook, Sheet sheet, HttpServletRequest request,int rowCount, User user) throws IOException {
		System.out.println("Inserting logo in the Purchase template" );
		ServletContext servletContext = request.getSession().getServletContext();
        String imgLoc = servletContext.getRealPath("/WEB-INF/images");
       
        String img;
        if(user.getUsername().equalsIgnoreCase("vighneshwar")) {
        	img="/vigneshwar_sign.png";
        	
        }else {
        	//img="/abhilashSign2.png";
        	img="/sumathySign2.png";
        }
		 File file = new File(imgLoc+img);
		 
	        FileInputStream fis = new FileInputStream(file);
	        byte[] imgBytes = IOUtils.toByteArray(fis);
	     	//Set image position
	        CreationHelper helper = workbook.getCreationHelper();
	        ClientAnchor anchor = helper.createClientAnchor();
	       // anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
	      
	        
	        anchor.setCol1(10);
	       anchor.setRow1(rowCount+3);
	        //Draw image
	        Drawing imgDrawing = sheet.createDrawingPatriarch();
	        int imgId = workbook.addPicture(imgBytes, Workbook.PICTURE_TYPE_JPEG);
	        Picture imgPic = imgDrawing.createPicture(anchor, imgId);
	      //  imgPic.resize();
	        imgPic.resize();
	        
	}
}
