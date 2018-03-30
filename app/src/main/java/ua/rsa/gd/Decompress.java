package ua.rsa.gd;

import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.util.zip.ZipEntry; 
import java.util.zip.ZipInputStream; 
 
/** 
 * 
 * @author Komarev 
 */ 
public class Decompress 
{ 
  private String _zipFile; 
  private String _location; 
 
  public Decompress(String zipFile, String location) 
  { 
    _zipFile = zipFile; 
    _location = location; 
 
    _dirChecker(""); 
  } 
 
  public void unzip() 
  { 
    try  
    { 
      FileInputStream fin = new FileInputStream(_zipFile); 
      ZipInputStream zin = new ZipInputStream(fin); 
      ZipEntry ze = null; 
      byte[] buffer = new byte[1024];
      int length;
      
      while ((ze = zin.getNextEntry()) != null) 
      { 
 
        if(ze.isDirectory()) 
        { 
          _dirChecker(ze.getName()); 
        } 
        else 
        { 
          FileOutputStream fout = new FileOutputStream(_location + ze.getName()); 
          
          while ((length = zin.read(buffer))>0) 
          {
        	  fout.write(buffer, 0, length);
          }
 
          zin.closeEntry(); 
          fout.close(); 
        } 
         
      } 
      zin.close(); 
    } 
    catch(Exception e) 
    { 
    } 
 
  } 
 
  private void _dirChecker(String dir) { 
    File f = new File(_location + dir); 
 
    if(!f.isDirectory()) { 
      f.mkdirs(); 
    } 
  } 
} 