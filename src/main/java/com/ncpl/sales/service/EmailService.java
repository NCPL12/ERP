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

	private static final String SMTP_HOST_NAME = "smtp.gmail.com";
	private static final int SMTP_HOST_PORT = 465;
	private static String SMTP_AUTH_USER = "teknikainfotech1122@gmail.com";
	private static String SMTP_AUTH_PWD = "hjyqbcvyhhxotpge";

	// Single send path for every report mail (sales order, DC, stock, item added...).
	// to1 is required; to2..to6, cc1..cc6 and "attachment" are optional.
	public Map<String, Object> sendEmailToServer(Map<String, Object> emailContents) throws IOException {
		Properties prop = System.getProperties();
		prop.put("mail.smtp.host", SMTP_HOST_NAME);
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.port", SMTP_HOST_PORT);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		prop.put("mail.smtp.ssl.enable", "true");
		prop.put("mail.smtp.ssl.trust", "*");
		prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getInstance(prop, null);
		Message msg = new MimeMessage(session);
		BodyPart body = new MimeBodyPart();

		try {
			StringWriter writer = new StringWriter();
			Template template = velocityEngine.getTemplate((String) "template/" + emailContents.get("template"), "UTF-8");
			template.merge(new VelocityContext(emailContents), writer);

			msg.setFrom(new InternetAddress(SMTP_AUTH_USER));
			msg.addRecipient(Message.RecipientType.TO,
					new InternetAddress((String) emailContents.get("to1"), false));
			for (int i = 2; i <= 6; i++) {
				addRecipientIfPresent(msg, Message.RecipientType.TO, (String) emailContents.get("to" + i));
			}
			for (int i = 1; i <= 6; i++) {
				addRecipientIfPresent(msg, Message.RecipientType.CC, (String) emailContents.get("cc" + i));
			}

			msg.setSubject((String) emailContents.get("subject"));
			body.setContent(writer.toString(), "text/html;");
			msg.setSentDate(new Date());

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(body);
			String attachment = (String) emailContents.get("attachment");
			if (attachment != null && !attachment.trim().isEmpty()) {
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.attachFile(attachment);
				multipart.addBodyPart(attachPart);
			}
			msg.setContent(multipart);

			SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			t.connect(SMTP_HOST_NAME, SMTP_AUTH_USER, SMTP_AUTH_PWD);
			t.sendMessage(msg, msg.getAllRecipients());
			System.out.println("Sending email: " + t.getLastServerResponse());
			t.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return emailContents;
	}

	private void addRecipientIfPresent(Message msg, Message.RecipientType type, String address) throws MessagingException {
		if (address != null && !address.trim().isEmpty()) {
			msg.addRecipient(type, new InternetAddress(address, false));
		}
	}

	public void sendErrorAlert(String recipients, String subject, String body) throws MessagingException {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", SMTP_HOST_NAME);
		properties.put("mail.smtp.port", String.valueOf(SMTP_HOST_PORT));
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.put("mail.smtp.ssl.checkserveridentity", "true");
		properties.put("mail.smtp.connectiontimeout", "5000");
		properties.put("mail.smtp.timeout", "10000");
		properties.put("mail.smtp.writetimeout", "10000");

		Session session = Session.getInstance(properties);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(SMTP_AUTH_USER));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients, true));
		message.setSubject(subject, "UTF-8");
		message.setText(body, "UTF-8");
		message.setSentDate(new Date());

		SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
		try {
			transport.connect(SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);
			transport.sendMessage(message, message.getAllRecipients());
		} finally {
			transport.close();
		}
	}

	// Old per-report names kept as thin delegates so existing callers/schedulers don't change.
	public Map<String, Object> sendSOWithDesignwherePONotDoneEmailToServer(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendSalesOrderEmailToServer(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendItemAddedEmailToServer(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendSOEmailToServer(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendEmailToServerForStockByItems(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendEmailToServerForStockByCustomerEmail(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendEmailToServerForActiveSO(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendEmailToServerForDCCreated(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
	public Map<String, Object> sendEmailToServerForStockByCustomerEmailSchedular(Map<String, Object> m) throws IOException { return sendEmailToServer(m); }
}
