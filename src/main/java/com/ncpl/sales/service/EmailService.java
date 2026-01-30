package com.ncpl.sales.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.mail.smtp.SMTPTransport;

@Service
public class EmailService {

	@Autowired
	private VelocityEngine velocityEngine;
		
	/*private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private static final String EMAIL_FROM = "sunil@tek-nika.com";*/

	//Using below host name for sending mail through godaddy server
    private static final String SMTP_HOST_NAME = "smtp.gmail.com"; //smtp URL
    private static final int SMTP_HOST_PORT = 465; //port number
    private static String SMTP_AUTH_USER = "teknikainfotech1122@gmail.com"; //email_id of sender
    private static String SMTP_AUTH_PWD = "hjyqbcvyhhxotpge"; //password of sender email_id
   // private static String SMTP_AUTH_USER = "anitha.poojary6666@gmail.com"; //email_id of sender
  //  private static String SMTP_AUTH_PWD = "blupiewengglcmzz"; //password of sender email_id
//    private static String SMTP_AUTH_USER = "sunilsgs891@gmail.com"; //email_id of sender
//    private static String SMTP_AUTH_PWD = "suniL58891"; //password of sender email_id
   // private static String SMTP_BOUCE_BACK_USER = "anitha.poojary6666@gmail.com"; 
  
    
    
	    public Map<String, Object> sendEmailToServer(Map<String, Object> emailContents) throws IOException {
	        Properties prop = System.getProperties();
	        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
	        prop.put("mail.smtp.starttls.enable","true");
	       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
	        prop.put("mail.smtp.socketFactory.class",    
                    "javax.net.ssl.SSLSocketFactory");   
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.ssl.enable", "true");
	        prop.put("mail.smtp.ssl.trust", "*");
	       
	        
	        Session session = Session.getInstance(prop, null);
	        Message msg = new MimeMessage(session);
	        BodyPart body = new MimeBodyPart();

	        try {
	        	StringWriter writer = new StringWriter();
	        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
				template.merge(new VelocityContext(emailContents), writer);
			
				// from
	            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
	           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

				// to 
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to1"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to2"), false));

				// cc
	            msg.addRecipient(Message.RecipientType.CC,
	                    new InternetAddress((String) emailContents.get("cc1"), false));
	            msg.addRecipient(Message.RecipientType.CC,
	                    new InternetAddress((String) emailContents.get("cc2"), false));
	            msg.addRecipient(Message.RecipientType.CC,
	                    new InternetAddress((String) emailContents.get("cc3"), false));

				// subject
	            msg.setSubject((String) emailContents.get("subject"));
				
				// content 
	            body.setContent(writer.toString(),"text/html;");
	            
	            msg.setSentDate(new Date());
	            
	            //attachment
	            Multipart multipart = new MimeMultipart();
	            multipart.addBodyPart(body);
	            System.out.println((String)emailContents.get("attachment"));
	            
	            MimeBodyPart attachPart = new MimeBodyPart();

	            attachPart.attachFile((String) emailContents.get("attachment"));
	            multipart.addBodyPart(attachPart);
	            msg.setContent(multipart);
	            
				// Get SMTPTransport
	            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
				
				// connect
	            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
				
				// send
	            t.sendMessage(msg, msg.getAllRecipients());

	            System.out.println("Sending email: " + t.getLastServerResponse());

	            t.close();

	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
			return emailContents;


	    }
	    public Map<String, Object> sendSOWithDesignwherePONotDoneEmailToServer(Map<String, Object> emailContents) throws IOException {
	        Properties prop = System.getProperties();
	        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
	        prop.put("mail.smtp.starttls.enable","true");
	       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
	        prop.put("mail.smtp.socketFactory.class",    
                    "javax.net.ssl.SSLSocketFactory");   
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.ssl.enable", "true");
	        prop.put("mail.smtp.ssl.trust", "*");
	       
	        
	        Session session = Session.getInstance(prop, null);
	        Message msg = new MimeMessage(session);
	        BodyPart body = new MimeBodyPart();

	        try {
	        	StringWriter writer = new StringWriter();
	        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
				template.merge(new VelocityContext(emailContents), writer);
			
				// from
	            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
	           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

				// to 
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to1"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to2"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to3"), false));

				// cc
	            msg.addRecipient(Message.RecipientType.CC,
	                    new InternetAddress((String) emailContents.get("cc1"), false));
	            msg.addRecipient(Message.RecipientType.CC,
	                    new InternetAddress((String) emailContents.get("cc2"), false));
	            msg.addRecipient(Message.RecipientType.CC,
	                    new InternetAddress((String) emailContents.get("cc3"), false));

				// subject
	            msg.setSubject((String) emailContents.get("subject"));
				
				// content 
	            body.setContent(writer.toString(),"text/html;");
	            
	            msg.setSentDate(new Date());
	            
	            //attachment
	            Multipart multipart = new MimeMultipart();
	            multipart.addBodyPart(body);
	            System.out.println((String)emailContents.get("attachment"));
	            
	            MimeBodyPart attachPart = new MimeBodyPart();

	            attachPart.attachFile((String) emailContents.get("attachment"));
	            multipart.addBodyPart(attachPart);
	            msg.setContent(multipart);
	            
				// Get SMTPTransport
	            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
				
				// connect
	            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
				
				// send
	            t.sendMessage(msg, msg.getAllRecipients());

	            System.out.println("Sending email: " + t.getLastServerResponse());

	            t.close();

	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
			return emailContents;


	    }




		public Map<String, Object> sendSalesOrderEmailToServer(Map<String, Object> emailContents) throws IOException {
	        Properties prop = System.getProperties();
	        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
	        prop.put("mail.smtp.starttls.enable","true");

	        prop.put("mail.smtp.socketFactory.class",    
                    "javax.net.ssl.SSLSocketFactory");   
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.ssl.enable", "true");
	        prop.put("mail.smtp.ssl.trust", "*");
	        
	        Session session = Session.getInstance(prop, null);
	        Message msg = new MimeMessage(session);
	        BodyPart body = new MimeBodyPart();

	        try {
	        	StringWriter writer = new StringWriter();
	        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
				template.merge(new VelocityContext(emailContents), writer);
			
				// from
	            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));

				// to 
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to1"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to2"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to3"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to4"), false));
	            msg.addRecipient(Message.RecipientType.TO,
	                    new InternetAddress((String) emailContents.get("to5"), false));


				// subject
	            msg.setSubject((String) emailContents.get("subject"));
				
				// content 
	            body.setContent(writer.toString(),"text/html;");
	            
	            msg.setSentDate(new Date());
	            
	            //attachment
	            Multipart multipart = new MimeMultipart();
	            multipart.addBodyPart(body);
	       
	            
	            msg.setContent(multipart);
	            
				// Get SMTPTransport
	            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
				
				// connect
	            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
				
				// send
	            t.sendMessage(msg, msg.getAllRecipients());

	            System.out.println("Sending email: " + t.getLastServerResponse());

	            t.close();

	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
			return emailContents;


	    }




		 public Map<String, Object> sendItemAddedEmailToServer(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to2"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to3"), false));
		            

					// cc
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc2"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc3"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc4"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc5"), false));

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }
		 
		 public Map<String, Object> sendSOEmailToServer(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to2"), false));

		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to3"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to4"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to5"), false));

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }
		 
		 public Map<String, Object> sendEmailToServerForStockByItems(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		            

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    } 

		 public Map<String, Object> sendEmailToServerForStockByCustomerEmail(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to2"), false));

					// cc
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc2"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc3"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc4"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc5"), false));

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }
		 
		 public Map<String, Object> sendEmailToServerForActiveSO(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to2"), false));

					// cc
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc2"), false));
		           

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }
		 
		 public Map<String, Object> sendSOWithDesignwherePONotDoneEmailToServerNew(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to2"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to3"), false));

					// cc
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc2"), false));
		            

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }
		 
		 public Map<String, Object> sendEmailToServerForDCCreated(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));

					// cc
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		           

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }
		 public Map<String, Object> sendEmailToServerForStockByCustomerEmailSchedular(Map<String, Object> emailContents) throws IOException {
		        Properties prop = System.getProperties();
		        prop.put("mail.smtp.host", SMTP_HOST_NAME); //optional, defined in SMTPTransport
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.port", SMTP_HOST_PORT); // default port 25
		        prop.put("mail.smtp.starttls.enable","true");
		       // prop.put("mail.smtp.from",SMTP_BOUCE_BACK_USER);
		        prop.put("mail.smtp.socketFactory.class",    
	                    "javax.net.ssl.SSLSocketFactory");   
		        prop.put("mail.smtp.auth", "true");
		        prop.put("mail.smtp.ssl.enable", "true");
		        prop.put("mail.smtp.ssl.trust", "*");
		       
		        
		        Session session = Session.getInstance(prop, null);
		        Message msg = new MimeMessage(session);
		        BodyPart body = new MimeBodyPart();

		        try {
		        	StringWriter writer = new StringWriter();
		        	Template template = velocityEngine.getTemplate((String) "template/"+emailContents.get("template"), "UTF-8");
					template.merge(new VelocityContext(emailContents), writer);
				
					// from
		            msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
		           // msg.addFrom(InternetAddress.parse(SMTP_AUTH_USER));

					// to 
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to1"), false));
		            msg.addRecipient(Message.RecipientType.TO,
		                    new InternetAddress((String) emailContents.get("to2"), false));

					// cc
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc1"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc2"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc3"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc4"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc5"), false));
		            msg.addRecipient(Message.RecipientType.CC,
		                    new InternetAddress((String) emailContents.get("cc6"), false));

					// subject
		            msg.setSubject((String) emailContents.get("subject"));
					
					// content 
		            body.setContent(writer.toString(),"text/html;");
		            
		            msg.setSentDate(new Date());
		            
		            //attachment
		            Multipart multipart = new MimeMultipart();
		            multipart.addBodyPart(body);
		            System.out.println((String)emailContents.get("attachment"));
		            
		            MimeBodyPart attachPart = new MimeBodyPart();

		            attachPart.attachFile((String) emailContents.get("attachment"));
		            multipart.addBodyPart(attachPart);
		            msg.setContent(multipart);
		            
					// Get SMTPTransport
		            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
					
					// connect
		            t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
					
					// send
		            t.sendMessage(msg, msg.getAllRecipients());

		            System.out.println("Sending email: " + t.getLastServerResponse());

		            t.close();

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
				return emailContents;


		    }


}
