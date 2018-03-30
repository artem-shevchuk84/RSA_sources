package ua.rsa.gd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties; 
import javax.activation.CommandMap; 
import javax.activation.DataHandler; 
import javax.activation.DataSource; 
import javax.activation.FileDataSource; 
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.pop3.POP3SSLStore;
import com.sun.mail.pop3.POP3Store; 
 
/**
 * Class helper for using Java Mail
 * one instance can be used just for send or for receive email
 * 
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class Mail extends javax.mail.Authenticator 
{ 
	/** Variables for POP3 session */
	private Session session 	= null;
	private Store 	store 		= null;
	private Folder 	folder		= null;
	
	/** Variables for SMTP session */
	private String _user; 
	private String _pass;
	private String[] _to; 
	private String _from; 
	private String _port; 
	private String _host; 
	private String _subject; 
	private String _body; 
	private boolean _auth;
	private boolean _useSSL;
	private Multipart _multipart; 
	
	
	
	/**
	 * Constructor of class with main variables initialization 
	 * @param user Emails username (exp.: neo@mail.ru)
	 * @param pass Emails password (exp.: fjgjgkd)
	 * @param host if instance used for send message then smtp-host, if receive - pop-host (exp.: smtp.mail.ru)
	 * @param port Service port (usually, for SMTP = 25 And for POP = 110) 
	 */
	public Mail(String user, String pass, String host, String port, boolean auth, boolean useSSL) 
	{ 
		// Init class fields
		_user 		= user; 
		_pass 		= pass;
		_host 		= host;
		_port 		= port;
		_from 		= user; 
		_subject 	= ""; 
		_body 		= "";
		_useSSL		= useSSL;
	  
		// SMTP auntentification, by default must be TRUE
		_auth 		= auth; 	
    
		_multipart = new MimeMultipart();
    
		/** 
		 * There is something wrong with MailCap, javamail can not find a handler for the 
		 * multipart/mixed part, so this bit needs to be added.
		 */ 
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
		CommandMap.setDefaultCommandMap(mc);
	} 

	/**
	 * Set session properties for SMTP
	 * this used if you want to send email
	 * @return set of selected properties
	 */
	private Properties _setPropertiesSMTP() 
	{ 
		Properties props = new Properties(); 
	  
		if (_useSSL == false) {
			if(_auth) 
				props.put("mail.smtp.auth", "true"); 
			props.put("mail.smtp.localhost", "localhost");
			props.put("mail.smtp.host", _host);
			props.put("mail.smtp.port", _port); 
			props.put("mail.smtp.socketFactory.port", _port); 
			props.put("mail.smtp.socketFactory.fallback", "false"); 
		} else {
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", _host);  
			props.put("mail.smtp.port", _port); 
			props.put("mail.smtp.ssl.enable", true);
			props.put("mail.smtp.socketFactory.port", _port);
			props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false"); 
			props.put("mail.smtp.quitwait", "false"); 
			props.put("mail.smtp.auth", "true"); 
		}
		return props; 
	}  
  
	/**
	 * Try to send message 
	 * @return ture when sended and false if some parameters are empty
	 * @throws Exception if some thing wrong 
	 */
	public boolean send() throws Exception 
	{ 
		Log.d("RRR mMessage.send()", "Creating properties");
		// Set session properties for sending message via SMTP
		Properties props = _setPropertiesSMTP(); 
	 
		// if all parameters is OK then ...
		if ( 	!_user.equals("") 
				&& !_pass.equals("") 
				&& (_to.length > 0) 
				&& !_from.equals("") 
				//&& !_subject.equals("") 
				//&& !_body.equals("")    
				) 
		{ 
			Log.d("RRR mMessage.send()", "parameters is OK. Trying to create session");
			// Create session by selected properties
			Session session = Session.getInstance(props, this); 
	 
			Log.d("RRR mMessage.send()", "Trying create new blank message");
			// Create message
			MimeMessage msg = new MimeMessage(session);
			// Set From field in message
			Log.d("RRR mMessage.send()", "Trying set FROM");
			msg.setFrom(new InternetAddress(_from)); 
	       
			/**
			 * Converting _to adreses in correct format and 
			 * set To field in message (it can be more then one recepient)
			 */
			Log.d("RRR mMessage.send()", "Trying set TO");
			InternetAddress[] addressTo = new InternetAddress[_to.length];
			for (int i = 0; i < _to.length; i++) 
			{ 
	    		addressTo[i] = new InternetAddress(_to[i]); 
			} 			
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);
			
			Log.d("RRR mMessage.send()", "Trying to set SUBJECT");
			// Set Subject of message
			msg.setSubject(_subject);
			
			Log.d("RRR mMessage.send()", "Trying to set Date");
			// Set date of message creation (current date)
			msg.setSentDate(new Date()); 
	 
			Log.d("RRR mMessage.send()", "Trying to create attachement");
			// Set body of message with attachment if added
			BodyPart messageBodyPart = new MimeBodyPart(); 
			
			Log.d("RRR mMessage.send()", "Trying to set TEXT");
			messageBodyPart.setText(_body); 
			
			Log.d("RRR mMessage.send()", "Trying to ATTACH");
			_multipart.addBodyPart(messageBodyPart);
			
			Log.d("RRR mMessage.send()", "Trying to set content multipart");
			msg.setContent(_multipart); 

			Log.d("RRR mMessage.send()2", "Trying to send");
			//Log.d("RRRRR mMessage.send() ", InetAddress.getLocalHost().getHostName());
			
			// Perform sending
			Transport.send(msg); 
	 
			return true; 
		} 
		else 
			Log.d("RRR mMessage.send()", "Parameters is NOT OK STOP!!");
		{	// if some of parameters is NOT OK then do anything and return false
			return false; 
		} 
  	} 
	 
	/**
	 * Add some files to Attachment of message to send
	 * @param filename Path to file with filesname  (ex.: "/data/ddd/fgg.dbf")
	 * @param attachname Name of file in attachment (ex.: "fgg.dbf")
	 * @throws Exception
	 */
  	public void addAttachment(String filename, String attachname) throws Exception 
  	{ 
  		BodyPart messageBodyPart = new MimeBodyPart(); 
  		DataSource source = new FileDataSource(filename);
  		
	    messageBodyPart.setDataHandler(new DataHandler(source));
	    messageBodyPart.setFileName(MimeUtility.encodeText(attachname)); 
	    
	    _multipart.addBodyPart(messageBodyPart); 
	} 
  
  /////////////// Methods for POP3 //////////////////////////	
  	
    private Properties _setPropertiesPOP() 
    { 
  	  Properties props = new Properties(); 
  	  
  	  if (_useSSL==false) {
	      props.setProperty("mail.pop3.socketFactory.fallback", "false");
	      props.setProperty("mail.pop3.port",  _port);
	      props.setProperty("mail.pop3.socketFactory.port", _port);
  	  } else {
		  props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		  props.setProperty("mail.pop3.socketFactory.fallback", "false");
		  props.setProperty("mail.pop3.port",  _port);
		  props.setProperty("mail.pop3.socketFactory.port", _port);		
  	  }
  	  return props; 
    }  	
  	
  // Method for POP3 connection
  public void connect() throws Exception 
  {
      Properties pop3Props = _setPropertiesPOP();
       
      URLName url = new URLName("pop3", _host, Integer.parseInt(_port), "", _user, _pass);
       
      session = Session.getInstance(pop3Props, null);
      if (_useSSL==false) {
    	  store = new POP3Store(session, url);
      } else {
    	  store = new POP3SSLStore(session, url);
      }
      store.connect();       
  }
  
  // Opens POP3 folder
  public void openFolder(String folderName) throws Exception 
  {
      folder = store.getDefaultFolder();
       
      folder = folder.getFolder(folderName);
       
      if (folder == null) 
      {
    	  throw new Exception("Invalid POP folder");
      }
      // try to open read/write and if that fails try read-only
      try 
      {
          folder.open(Folder.READ_WRITE);
      } 
      catch (MessagingException ex) 
      {
          folder.open(Folder.READ_ONLY);
      }
  }
  
  // Password auth
  @Override 
  public PasswordAuthentication getPasswordAuthentication() 
  { 
	  return new PasswordAuthentication(_user, _pass); 
  } 
  
  // Close FOLDER for POP3 function
  public void closeFolder() throws Exception 
  {
	  folder.close(false);
  }
  
  // Get Messages count for POP3 function
  public int getMessageCount() throws Exception 
  {
	  return folder.getMessageCount();
  }
  
  // Get new Messages count for POP3 function
  public int getNewMessageCount() throws Exception 
  {
	  return folder.getNewMessageCount();
  }
  
  // Get all messages from inbox
  public Message[] getMessages() throws Exception
  {
	  return folder.getMessages();
  }
  
  // if attachment exists
  public boolean hasAttachment(Message mMess) throws Exception
  {
	  Multipart multipart = (Multipart) mMess.getContent();
	  
	 // Log.v("Mail.java", Integer.toString(multipart.getCount()));
	  
	  for (int i=0; i<multipart.getCount(); i++) 
	  {
	        BodyPart bodyPart = multipart.getBodyPart(i);
	        
	        if ((bodyPart.getDisposition()==null)&&(multipart.getCount()>3)) {
	        	return true;
	        }
	        
	        if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) 
	        {
	        	continue; // dealing with attachments only
	        } 
	        return true;
	  }
	  return false;
  }
  
  // if from equals 
  public boolean isFrom(Message mMess, String mFrom) throws Exception
  {
	  // Plutenkos ticket .... 
	  return true;
	  /*
	  Address[] adr = mMess.getFrom();
	  
	  for (int i=0; i<adr.length; i++) 
	  {
	        if(adr[i].toString().toLowerCase().contains(mFrom.toLowerCase())) 
	        {
	        	return true;
	        } 
	  }
	  return false;*/
  }
  
  // if attachment has 5 main lzma files
  public int hasMainAttachment(Message mMess, SharedPreferences mPref) throws Exception
  {
	  Multipart multipart = (Multipart) mMess.getContent();
	  int bMain = 0;
	  String fileName = null;
	  
	  if (mPref.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
	  {
		  for (int i=0; i<multipart.getCount(); i++) 
		  {
	        BodyPart bodyPart = multipart.getBodyPart(i);
	        
	        if (bodyPart.getFileName()==null) {
	        	continue;
	        }
	        if (!((bodyPart.getDisposition()==null)&&(multipart.getCount()>3))) {
		        if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
		        	continue; // dealing with attachments only
		        } 
	        }
	        
	        if (bodyPart.getContentType().toLowerCase().contains("octet"))
			{
	        	fileName = MimeUtility.decodeText(bodyPart.getFileName());
			}
	        else
	        {
	        	fileName = bodyPart.getFileName();
	        }
	        
	        if (fileName.toLowerCase().equals("goods.dbf.lzma")) 		// 0000 0001
	        {
	        	bMain |= 1; 
	        }
	        else if (fileName.toLowerCase().equals("cust.dbf.lzma"))  // 0000 0010
	        {
	        	bMain |= 2;
	        }
	        else if (fileName.toLowerCase().equals("char.dbf.lzma"))  // 0000 0100
	        {
	        	bMain |= 4;
	        }
	        else if (fileName.toLowerCase().equals("debit.dbf.lzma"))	// 0000 1000
	        {
	        	bMain |= 8;
	        }
	        else if (fileName.toLowerCase().equals("shop.dbf.lzma"))  // 0001 0000
	        {
	        	bMain |= 16;
	        }	        
	        else if (fileName.toLowerCase().equals("sklad.dbf.lzma"))	// 0010 0000
	        {
	        	bMain |= 32;
	        }
	        else if (fileName.toLowerCase().equals("group.dbf.lzma")) // 0100 0000
	        {
	        	bMain |= 64;
	        }
	        else if (fileName.toLowerCase().equals("brand.dbf.lzma")) // 1000 0000
	        {
	        	bMain |= 128;
	        }
	        else if (fileName.toLowerCase().equals("workinf.dbf.lzma")) // 10000 0000
	        {
	        	bMain |= 256;
	        }
	        else if (fileName.toLowerCase().equals("plan.dbf.lzma")) // 1 0000 0000 0
	        {
	        	bMain |= 512;
	        }
	        else if (fileName.toLowerCase().equals("sold.dbf.lzma")) // 10 0000 0000 0
	        {
	        	bMain |= 1024;
	        }
	        else if (fileName.toLowerCase().equals("matrix.dbf.lzma")) // 100 0000 0000 0
	        {
	        	bMain |= 2048;
	        }
	        else if (fileName.toLowerCase().equals("prodlock.dbf.lzma")) // 1000 0000 0000 0
	        {
	        	bMain |= 4096;
	        }
	        else if (fileName.toLowerCase().equals("skladdet.dbf.lzma")) // 100 0000 0000 0000 
	        {
	        	bMain |= 16384;
	        }
		  }
	  }
	  else if (mPref.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
	  {
		  for (int i=0; i<multipart.getCount(); i++) 
		  {
	        BodyPart bodyPart = multipart.getBodyPart(i);
	        
	        if (bodyPart.getFileName()==null) {
	        	continue;
	        }
	        if (!((bodyPart.getDisposition()==null)&&(multipart.getCount()>3))) {
		        if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
		        	continue; // dealing with attachments only
		        } 
	        }
	        
	        if (bodyPart.getContentType().toLowerCase().contains("octet"))
			{
	        	fileName = MimeUtility.decodeText(bodyPart.getFileName());
			}
	        else
	        {
	        	fileName = bodyPart.getFileName();
	        }
	        
	        if (fileName.toLowerCase().equals("goods.xml.zip")) 		// 0000 0001
	        {
	        	bMain |= 1; 
	        }
	        else if (fileName.toLowerCase().equals("cust.xml.zip"))  // 0000 0010
	        {
	        	bMain |= 2;
	        }
	        else if (fileName.toLowerCase().equals("char.xml.zip"))  // 0000 0100
	        {
	        	bMain |= 4;
	        }
	        else if (fileName.toLowerCase().equals("debit.xml.zip"))	// 0000 1000
	        {
	        	bMain |= 8;
	        }
	        else if (fileName.toLowerCase().equals("shop.xml.zip"))  // 0001 0000
	        {
	        	bMain |= 16;
	        }	        
	        else if (fileName.toLowerCase().equals("sklad.xml.zip"))	// 0010 0000
	        {
	        	bMain |= 32;
	        }
	        else if (fileName.toLowerCase().equals("group.xml.zip")) // 0100 0000
	        {
	        	bMain |= 64;
	        }
	        else if (fileName.toLowerCase().equals("brand.xml.zip")) // 1000 0000
	        {
	        	bMain |= 128;
	        }
	        else if (fileName.toLowerCase().equals("workinf.xml.zip")) // 10000 0000
	        {
	        	bMain |= 256;
	        }
	        else if (fileName.toLowerCase().equals("plan.xml.zip")) // 10000 0000 0
	        {
	        	bMain |= 512;
	        }
	        else if (fileName.toLowerCase().equals("sold.xml.zip")) // 10 0000 0000 0
	        {
	        	bMain |= 1024;
	        }
	        else if (fileName.toLowerCase().equals("matrix.xml.zip")) // 100 0000 0000 0
	        {
	        	bMain |= 2048;
	        }
	        else if (fileName.toLowerCase().equals("prodlock.xml.zip")) // 1000 0000 0000 0
	        {
	        	bMain |= 4096;
	        }
		  }
	  }
	  else if (mPref.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
	  {
		  for (int i=0; i<multipart.getCount(); i++) 
		  {
	        BodyPart bodyPart = multipart.getBodyPart(i);
	        
	        if (bodyPart.getFileName()==null) {
	        	continue;
	        }
	        if (!((bodyPart.getDisposition()==null)&&(multipart.getCount()>3))) {
		        if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
		        	continue; // dealing with attachments only
		        } 
	        }
	        
	        if (bodyPart.getContentType().toLowerCase().contains("octet"))
			{
	        	fileName = MimeUtility.decodeText(bodyPart.getFileName());
			}
	        else
	        {
	        	fileName = bodyPart.getFileName();
	        }
	        
	        if (fileName.toLowerCase().equals("goods.csv.zip")) 		// 0000 0001
	        {
	        	bMain |= 1; 
	        }
	        else if (fileName.toLowerCase().equals("cust.csv.zip"))  // 0000 0010
	        {
	        	bMain |= 2;
	        }
	        else if (fileName.toLowerCase().equals("char.csv.zip"))  // 0000 0100
	        {
	        	bMain |= 4;
	        }
	        else if (fileName.toLowerCase().equals("debit.csv.zip"))	// 0000 1000
	        {
	        	bMain |= 8;
	        }
	        else if (fileName.toLowerCase().equals("shop.csv.zip"))  // 0001 0000
	        {
	        	bMain |= 16;
	        }	        
	        else if (fileName.toLowerCase().equals("sklad.csv.zip"))	// 0010 0000
	        {
	        	bMain |= 32;
	        }
	        else if (fileName.toLowerCase().equals("group.csv.zip")) // 0100 0000
	        {
	        	bMain |= 64;
	        }
	        else if (fileName.toLowerCase().equals("brand.csv.zip")) // 1000 0000
	        {
	        	bMain |= 128;
	        }
	        else if (fileName.toLowerCase().equals("workinf.csv.zip")) // 10000 0000
	        {
	        	bMain |= 256;
	        }
	        else if (fileName.toLowerCase().equals("plan.csv.zip")) // 10000 0000 0
	        {
	        	bMain |= 512;
	        }
		  }
	  }
	  
	  return bMain;
  }
  
  // get all attached lzma-files to specified directory
  public void getArchives(Message mMess, String strPath, Context context) throws Exception
  {
	  String appPath = context.getFilesDir().getAbsolutePath();
	  String fileName = null;
	  
    	  Multipart multipart = (Multipart) mMess.getContent();
    	  
    	  for (int i=0; i<multipart.getCount(); i++) 
    	  {
    	        BodyPart bodyPart = multipart.getBodyPart(i);
    	        
    	        if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) 
    	        {
    	        	continue; // dealing with attachments only
    	        } 
    	        
    	        if (bodyPart.getContentType().toLowerCase().contains("octet"))
    			{
    	        	fileName = MimeUtility.decodeText(bodyPart.getFileName());
    			}
    	        else
    	        {
    	        	fileName = bodyPart.getFileName();
    	        }
    	        
    	        if (fileName.contains(".lzma") || fileName.contains(".zip"))
    	        {	
    	        	InputStream is = bodyPart.getInputStream();
    	     //   	                /data/app + /inbox  +   /            + goods.dbf.lzma
    	        	File f = new File(appPath + strPath + File.separator + fileName);
    	        	FileOutputStream fos = new FileOutputStream(f);
    	        	byte[] buf = new byte[4096];
    	        	int bytesRead;
    	        	while((bytesRead = is.read(buf))!=-1) 
    	        	{
    	        		fos.write(buf, 0, bytesRead);
    	        	}
    	        	fos.close();
    	        }
    	  }
  }
  
  // Delete all pop3 messages
  public void deleteAllMessages(Message[] mMess) throws MessagingException
  {
	  for (int i = 0; i < mMess.length; i++) 
	  {
		  mMess[i].setFlag(FLAGS.Flag.DELETED, true);
      } 
  }
  
  // for pop
  public void disconnect() throws Exception 
  {
      store.close();
  }
  
  // Setters for SMTP function
  public void setTo(String[] strTo)
  {
	  this._to = strTo;
  }
  
  public void setFrom(String strFrom)
  {
	  this._from = strFrom;
  }
  
  public void setSubject(String strSub)
  {
	  this._subject = strSub;
  }
  
  public void setBody(String _body) 
  { 
	  this._body = _body; 
  }
 
} 