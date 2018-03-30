package ru.by.rsa.external.javadbf;

import java.io.*;

// Referenced classes of package com.hexiong.jdbf:
//            JDBFException, JDBField
public class DBFReader
{
  //~ Instance fields ----------------------------------------------------------

  private DataInputStream stream;
  private JDBField[] fields;
  private byte[] nextRecord;
  private long fileSize;
  private int recordSize;

  //~ Constructors -------------------------------------------------------------

  public DBFReader(String s) throws JDBFException
  {
    stream = null;
    fields = null;
    nextRecord = null;
    
    File file = new File(s);

    // Get the number of bytes in the file
    fileSize = file.length();

    try
    {
      init(new FileInputStream(s));
    }
    catch (FileNotFoundException filenotfoundexception)
    {
      throw new JDBFException(filenotfoundexception);
    }
  }

  public DBFReader(InputStream inputstream) throws JDBFException
  {
    stream = null;
    fields = null;
    nextRecord = null;
    init(inputstream);
  }

  //~ Methods ------------------------------------------------------------------

  private void init(InputStream inputstream) throws JDBFException
  {
    try
    {
      stream = new DataInputStream(new BufferedInputStream(inputstream, 256));

      int i = readHeader();
      fields = new JDBField[i];

      int j = 1;

      for (int k = 0; k < i; k++)
      {
        fields[k] = readFieldHeader();
        j += fields[k].getLength();
      }

      if (stream.read() < 1)
        throw new JDBFException("Unexpected end of file reached.");

      recordSize = j;
      nextRecord = new byte[j];

      try
      {
        stream.readFully(nextRecord);
      }
      catch (EOFException eofexception)
      {
        nextRecord = null;
        stream.close();
      }
    }
    catch (IOException ioexception)
    {
      throw new JDBFException(ioexception);
    }
  }
  
  public long getFileSize()
  {
	  return fileSize;
  }
  
  public int getRecordSize()
  {
	  return recordSize;
  }
  
  private int readHeader() throws IOException, JDBFException
  {
    byte[] abyte0 = new byte[16];

    try
    {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception)
    {
      throw new JDBFException("Unexpected end of file reached.");
    }

    int i = abyte0[8];

    if (i < 0)
      i += 256;

    i += (256 * abyte0[9]);
    i = --i / 32;
    i--;

    try
    {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception1)
    {
      throw new JDBFException("Unexpected end of file reached.");
    }

    return i;
  }

  private JDBField readFieldHeader() throws IOException, JDBFException
  {
    byte[] abyte0 = new byte[16];

    try
    {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception)
    {
      throw new JDBFException("Unexpected end of file reached.");
    }

    StringBuffer stringbuffer = new StringBuffer(10);

    for (int i = 0; i < 10; i++)
    {
      if (abyte0[i] == 0)
        break;

      stringbuffer.append((char) abyte0[i]);
    }

    char c = (char) abyte0[11];

    try
    {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception1)
    {
      throw new JDBFException("Unexpected end of file reached.");
    }

    int j = abyte0[0];
    int k = abyte0[1];

    if (j < 0)
      j += 256;

    if (k < 0)
      k += 256;

    return new JDBField(stringbuffer.toString(), c, j, k);
  }

  public int getFieldCount()
  {
    return fields.length;
  }

  public JDBField getField(int i)
  {
    return fields[i];
  }

  public boolean hasNextRecord()
  {
    return nextRecord != null;
  }

  public Object[] nextRecord() throws JDBFException
  {
    if (!hasNextRecord()) return null;
      //throw new JDBFException("No more records available.");

    Object[] aobj = new Object[fields.length];
    int i = 1;

    for (int j = 0; j < aobj.length; j++)
    {
      int k = fields[j].getLength();
      StringBuffer stringbuffer = new StringBuffer(k);
      
      stringbuffer.append( fieldFrom866(nextRecord, i, k) );
      
      aobj[j] = fields[j].parse(stringbuffer.toString());
      
      i += fields[j].getLength();
    }

    try
    {
      stream.readFully(nextRecord);
    }
    catch (EOFException eofexception)
    {
      nextRecord = null;
    }
    catch (IOException ioexception)
    {
      throw new JDBFException(ioexception);
    }

    return aobj;
  }
  
  public String fieldFrom866(byte[] btArray, int i, int k)
  {
	  StringBuffer mStr = new StringBuffer(k);
	  String let866 = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмноп"
			  		  +"________________________________________________" 
			  		  +"рстуфхцчшщъыьэюяЁёЕеIiYy____#___";
	  byte[] b = new byte[1];
	  
	  for (int j=0;j<k;j++)
	  {
		  b[0] = btArray[j+i];
		  if (b[0]>0)
		  {
			  if (b[0]!=0x3F)
			  {
				  mStr.append(new String(b, 0, 1));
			  }
			  else
			  {
				  mStr.append("i");
			  }
		  }
		  else
		  {
			  mStr.append(let866.charAt(b[0]+128));
		  }
	  }
	  
	  return mStr.toString().trim();
  }

  public void close() throws JDBFException
  {
    nextRecord = null;

    try
    {
      stream.close();
    }
    catch (IOException ioexception)
    {
      throw new JDBFException(ioexception);
    }
  }
}
