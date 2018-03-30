package ua.rsa.gd.external.javadbf;

import java.io.*;

import java.util.Calendar;




// Referenced classes of package com.hexiong.jdbf:
//            JDBFException, JDBField
public class DBFWriter
{
  //~ Instance fields ----------------------------------------------------------

  private BufferedOutputStream stream;
  private int recCount;
  private JDBField[] fields;
  private String fileName;
 // private String dbfEncoding;

  //~ Constructors -------------------------------------------------------------

  public DBFWriter(String s, JDBField[] ajdbfield) throws JDBFException
  {
    stream = null;
    recCount = 0;
    fields = null;
    fileName = null;
  //  dbfEncoding = null;
    fileName = s;

    try
    {
      init(new FileOutputStream(s), ajdbfield);
    }
    catch (FileNotFoundException filenotfoundexception)
    {
      throw new JDBFException(filenotfoundexception);
    }
  }

  public DBFWriter(OutputStream outputstream, JDBField[] ajdbfield, String s1)
    throws JDBFException
  {
    stream = null;
    recCount = 0;
    fields = null;
    fileName = null;
  //  dbfEncoding = s1;
    init(outputstream, ajdbfield);
  }

  public DBFWriter(
    OutputStream outputstream, JDBField[] ajdbfield, String s1, int rCount
  ) throws JDBFException
  {
    stream = null;
    recCount = 0;
    fields = null;
    fileName = null;
 //   dbfEncoding = s1;
    init(outputstream, ajdbfield, rCount);
  }

  public DBFWriter(String s, JDBField[] ajdbfield, String s1)
    throws JDBFException
  {
    stream = null;
    recCount = 0;
    fields = null;
    fileName = s;
  //  dbfEncoding = s1;

    try
    {
      init(new FileOutputStream(s), ajdbfield);
    }
    catch (FileNotFoundException filenotfoundexception)
    {
      throw new JDBFException(filenotfoundexception);
    }
  }

  //~ Methods ------------------------------------------------------------------

  private void init(OutputStream outputstream, JDBField[] ajdbfield)
    throws JDBFException
  {
    fields = ajdbfield;

    try
    {
      // Bug
      // stream = new BufferedOutputStream(outputstream);
      stream = new BufferedOutputStream(outputstream, 1024);
      writeHeader();

      for (int i = 0; i < ajdbfield.length; i++)
        writeFieldHeader(ajdbfield[i]);

      stream.write(13);
      stream.flush();
    }
    catch (Exception exception)
    {
      throw new JDBFException(exception);
    }
  }

  private void init(
    OutputStream outputstream, JDBField[] ajdbfield, int rCount
  ) throws JDBFException
  {
    fields = ajdbfield;

    try
    {
      // Bug
      // stream = new BufferedOutputStream(outputstream);
      stream = new BufferedOutputStream(outputstream, 1024);
      writeHeader(rCount);

      for (int i = 0; i < ajdbfield.length; i++)
        writeFieldHeader(ajdbfield[i]);

      stream.write(13);
      stream.flush();
    }
    catch (Exception exception)
    {
      throw new JDBFException(exception);
    }
  }

  private void writeHeader() throws IOException
  {
    byte[] abyte0 = new byte[16];
    abyte0[0] = 3;

    Calendar calendar = Calendar.getInstance();
    abyte0[1] = (byte) (calendar.get(1) - 1900);
    abyte0[2] = (byte) calendar.get(2);
    abyte0[3] = (byte) calendar.get(5);
    abyte0[4] = 0;
    abyte0[5] = 0;
    abyte0[6] = 0;
    abyte0[7] = 0;

    int i = ((fields.length + 1) * 32) + 1;
    abyte0[8] = (byte) (i % 256);
    abyte0[9] = (byte) (i / 256);

    int j = 1;

    for (int k = 0; k < fields.length; k++)
      j += fields[k].getLength();

    abyte0[10] = (byte) (j % 256);
    abyte0[11] = (byte) (j / 256);
    abyte0[12] = 0;
    abyte0[13] = 0;
    abyte0[14] = 0;
    abyte0[15] = 0;
    stream.write(abyte0, 0, abyte0.length);

    for (int l = 0; l < 16; l++)
      abyte0[l] = 0;
    abyte0[13] = 0x65; // Romka Added 22 may 2012 OEM RUS Codepage

    stream.write(abyte0, 0, abyte0.length);
  }

  private void writeHeader(int rCount) throws IOException
  {
    byte[] abyte0 = new byte[16];
    abyte0[0] = 3;

    Calendar calendar = Calendar.getInstance();
    abyte0[1] = (byte) (calendar.get(1) - 1900);
    abyte0[2] = (byte) calendar.get(2);
    abyte0[3] = (byte) calendar.get(5);
    abyte0[4] = (byte) (rCount % 256);
    abyte0[5] = (byte) ((rCount / 256) % 256);
    abyte0[6] = (byte) ((rCount / 0x10000) % 256);
    abyte0[7] = (byte) ((rCount / 0x1000000) % 256);

    int i = ((fields.length + 1) * 32) + 1;
    abyte0[8] = (byte) (i % 256);
    abyte0[9] = (byte) (i / 256);

    int j = 1;

    for (int k = 0; k < fields.length; k++)
      j += fields[k].getLength();

    abyte0[10] = (byte) (j % 256);
    abyte0[11] = (byte) (j / 256);
    abyte0[12] = 0;
    abyte0[13] = 0;
    abyte0[14] = 0;
    abyte0[15] = 0;
    stream.write(abyte0, 0, abyte0.length);

    for (int l = 0; l < 16; l++)
      abyte0[l] = 0;
    abyte0[13] = 0x65; // Romka Added 22 may 2012 OEM RUS Codepage

    stream.write(abyte0, 0, abyte0.length);
  }

  private void writeFieldHeader(JDBField jdbfield) throws IOException
  {
    byte[] abyte0 = new byte[16];
    String s = jdbfield.getName();
    int i = s.length();

    if (i > 10)
      i = 10;

    for (int j = 0; j < i; j++)
      abyte0[j] = (byte) s.charAt(j);

    for (int k = i; k <= 10; k++)
      abyte0[k] = 0;

    abyte0[11] = (byte) jdbfield.getType();
    abyte0[12] = 0;
    abyte0[13] = 0;
    abyte0[14] = 0;
    abyte0[15] = 0;
    stream.write(abyte0, 0, abyte0.length);

    for (int l = 0; l < 16; l++)
      abyte0[l] = 0;

    abyte0[0] = (byte) jdbfield.getLength();
    abyte0[1] = (byte) jdbfield.getDecimalCount();
    stream.write(abyte0, 0, abyte0.length);
  }

  public synchronized void addRecord(Object[] aobj) throws JDBFException
  {
    if (aobj.length != fields.length)
    	throw new JDBFException(
    			"Error adding record: Wrong number of values. Expected " +
    			fields.length + ", got " + aobj.length + "."
    			);
    			
    int i = 0;

    for (int j = 0; j < fields.length; j++)
      i += fields[j].getLength();

    byte[] abyte0 = new byte[i];
    int k = 0;

    for (int l = 0; l < fields.length; l++)
    {
      String s = fields[l].format(aobj[l]);
      byte[] abyte1;

   //   try
    //  {
     //   if (dbfEncoding != null)
          abyte1 = fieldTo866(s);
   //     else
    //      abyte1 = s.getBytes();
     // catch (UnsupportedEncodingException unsupportedencodingexception)
      //{
       // throw new JDBFException(unsupportedencodingexception);
     // }

      for (int i1 = 0; i1 < fields[l].getLength(); i1++)
        abyte0[k + i1] = abyte1[i1];

      k += fields[l].getLength();
    }

    
    int tries = 0;

    do {
	    	try
	    	{
	    		stream.write(32);
	    		stream.write(abyte0, 0, abyte0.length);
	    		tries = 3;
	    		// Bug
	    		// stream.flush();
	    	}
	    	catch (IOException ioexception)
	    	{
	    		if (tries >= 3)
	    			throw new JDBFException(ioexception);
	    		else
	    			tries++;
	    	}
    } while (tries < 3);

    recCount++;
  }
  
  public byte[] fieldTo866(String s)
  {
	  byte[] b = new byte[s.length()];
	  
	  for (int j=0;j<s.length();j++)
	  {
		  int ch = (int) s.charAt(j);
		  if (ch < 0x0080)
		  {
			  b[j] = (byte) ch;
		  }
		  else if (ch<0x0440)
		  {
			  b[j] = (byte) (ch-912);
		  }
		  else
		  {
			  b[j] = (byte) (ch-864);
		  }
	  }
	  
	  return b;
  }

  public void close() throws JDBFException
  {
    try
    {
      stream.write(26);
      stream.close();

      if (fileName != null)
      {
        RandomAccessFile randomaccessfile =
          new RandomAccessFile(fileName, "rw");
        randomaccessfile.seek(4L);

        byte[] abyte0 = new byte[4];
        abyte0[0] = (byte) (recCount % 256);
        abyte0[1] = (byte) ((recCount / 256) % 256);
        abyte0[2] = (byte) ((recCount / 0x10000) % 256);
        abyte0[3] = (byte) ((recCount / 0x1000000) % 256);
        randomaccessfile.write(abyte0, 0, abyte0.length);
        randomaccessfile.close();
      }
    }
    catch (IOException ioexception)
    {
      throw new JDBFException(ioexception);
    }
  }
}
