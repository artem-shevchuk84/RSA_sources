package ru.by.rsa;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


/**
 * Class that stores NMEA data, for $GPRMC messages only
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class RsaNMEA
{
	/** Curent stored NMEA message */
	private String strNMEA;
	/** Array of 13 entries of Current stored NMEA message */
	private static String[] tags;
	/** Flag of start. Default = "0", if start of movement then = "1" */
	private static boolean startFlag;
	/** Constant value of correct NMEA message */
	public static final String MHEADER	= "$GPRMC";
	/** Constant value of KEY when message contains gps FIX */
	public static final String ACTIVE	= "A";
	
	/** Constant for get-function of wthis class, meens NMEA Header */
	public static final int HEADER 		= 0; 		//0			"$GPRMC"
	/** Constant for get-function of wthis class, meens UTC Time hhmmss*/
	public static final int UTCTIME 	= 1;		//1			hhmmss
	/** Constant for get-function of wthis class, meens FIX or NOT (A|V) */
	public static final int STATUS		= 2;		//2			"A" or "V"
	/** Constant for get-function of wthis class, meens Geo Latitude ddmm.mmmm */
	public static final int LATITUDE	= 3;		//3			ddmm.mmmm
	/** Constant for get-function of wthis class, meens North|South indicator (N|S) */
	public static final int NINDICATOR	= 4;		//4			"N" or "S"
	/** Constant for get-function of wthis class, meens Geo Longtitude dddmm.mmmm */
	public static final int LONGTITUDE	= 5;		//5			dddmm.mmmm
	/** Constant for get-function of wthis class, meens East|West indicator (E|W) */
	public static final int WINDICATOR	= 6;		//6			"W" or "E"
	/** Constant for get-function of wthis class, meens Speed x.x */
	public static final int SPEED		= 7;		//7			x.x
	/** Constant for get-function of wthis class, meens Course x.x */
	public static final int COURSE		= 8;		//8			x.x
	/** Constant for get-function of wthis class, meens UTC Date ddmmyy*/
	public static final int DATE		= 9;		//9			ddmmyy
	/** Constant for get-function of wthis class, meens Magnitude, not used */
	public static final int MAGNITUDE	= 10;		//10		not used
	/** Constant for get-function of wthis class, meens ? not used */
	public static final int A			= 11;		//11		not used
	/** Constant for get-function of wthis class, meens CRC, not used */
	public static final int CRC			= 12;		//12		CheckSum
	
	/**
	 * Constructor set empty nmea message
	 */
	public RsaNMEA()
	{
		this.strNMEA = MHEADER + ",000000.000,V,0000.000000,N,00000.000000,E,0.0,0.0,000000,,,A*79";
		tags = strNMEA.split(",");
		startFlag = true;
	}

	/**
	 * Sets Current NMEA if it match
	 * @param newNMEA string used to set as current
	 */
	public boolean setNMEA(String newNMEA)
	{
		//Log.v("RRRR: setNMEA(): ",newNMEA);
		String[] temptags = newNMEA.split(",");
		
		if (!temptags[0].equals(MHEADER))
			return false;

		//Toast.makeText(cont, Integer.toString(temptags.length) + " -> " + newNMEA,Toast.LENGTH_SHORT).show(); // to remove

		//Log.v("RRRR: setNMEA(): ",Integer.toString(temptags.length));
		
		if (temptags.length == 13)
		{
			// this.strNMEA = newNMEA;
			// tags = temptags;
			// return true;
			this.strNMEA = nmeaNormalize(temptags, 13);
			tags = this.strNMEA.split(",");
			return true;
		}
		else if (temptags.length == 17)
		{
			this.strNMEA = nmeaNormalize(temptags, 17);
			tags = this.strNMEA.split(",");
			return true;
		}
		else if (temptags.length == 12)
		{
			this.strNMEA = nmeaNormalize(temptags, 12);
			tags = this.strNMEA.split(",");
			return true;
		}
		return false;
	}
	
	/**
	 * Get full nmea message
	 * @return string of entire nmea message
	 */
	public String getNMEA()
	{
		return strNMEA;
	}
	
	/**
	 * Get one of entries values from Current nmea string
	 * @param value used to know wich entire it must return
	 * @return entrie on nmea message 
	 */
	public String get(int value)
	{
		switch (value)
		{
			case HEADER: 		return MHEADER;
			case UTCTIME:		return tags[1].substring(0, 6);
			case STATUS: 		return tags[2];
			case LATITUDE: 		return tags[3];
			case NINDICATOR: 	return tags[4];
			case LONGTITUDE: 	return tags[5];
			case WINDICATOR: 	return tags[6];
			case SPEED: 		return tags[7];
			case COURSE: 		return tags[8];
			case DATE: 			return tags[9];
			case MAGNITUDE: 	return tags[10];
			case A: 			return tags[11];
			case CRC: 			return tags[12];
			
			default:
				break;
		}
		
		return null;
	}
	
	/**
	 * Get one of entries values from nmea string
	 * @param nmea NMEA string message
	 * @param value used to know wich entire it must return
	 * @return entrie on nmea message 
	 */
	public static String get(String nmea, int value)
	{
		String[] tags = nmea.split(",");
		
		if (!tags[0].equals(MHEADER))
			return null;
		
		switch (value)
		{
			case HEADER: 		return MHEADER;
			case UTCTIME: 		return tags[1].substring(0, 6);
			case STATUS: 		return tags[2];
			case LATITUDE: 		return tags[3];
			case NINDICATOR: 	return tags[4];
			case LONGTITUDE: 	return tags[5];
			case WINDICATOR: 	return tags[6];
			case SPEED: 		return tags[7];
			case COURSE: 		return tags[8];
			case DATE: 			return tags[9];
			case MAGNITUDE: 	return tags[10];
			case A: 			return tags[11];
			case CRC: 			return tags[12];
			
			default:
			{
				return null;
			}
		}
	}

	/** 
	 * Calculate CRC of message. Simple XOR of all elements between $ and * symbols
	 * @param nmea message that we need to calculate CRC
	 * @return string with 2 symbols - hex value
	 */
	public static String calcCRC(String nmea)
	{
		byte[] bNMEA;
		byte res;
		
		String sub = nmea.substring(nmea.indexOf("$")+1, nmea.indexOf("*"));
		try
		{
			bNMEA = sub.getBytes("ASCII");
		} 
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
		
		res = 0;
		for (int i=0;i<bNMEA.length;i++)
		{
			res ^= bNMEA[i]; 
		}
		
		return String.format("%02X", res);
	}
	
	/** 
	 * If accepted nmea message has 17 entries then normalize it to 13 like
	 * @param temptags Array 17 of entries
	 * @return normalized nmea string with 13 entries 
	 */
	public static String nmeaNormalize(String[] temptags, int value)
	{
		StringBuilder res = null;
		
		switch (value)
		{
		case 17:
		{
						  res = new StringBuilder(  temptags[0] + "," );
									   res.append(  temptags[1] + "," );
									   res.append(  temptags[2] + "," );
									   res.append(  temptags[3] + "." );
									   res.append(  temptags[4] + "," );
									   res.append(  temptags[5] + "," );
									  
									   if (temptags[6].length()==4)
										   res.append(  "0" + temptags[6] + "." );
									   else
										   res.append(  temptags[6] + "." );
									   
									   res.append(  temptags[7] + "," );
									   res.append(  temptags[8] + "," );//E
									   res.append(  normalizeSpeed(temptags[9]) + "," );//speed
									   res.append(  normalizeCourse(temptags[10]) + "," );//Course
									   res.append(  temptags[13] + "," );
									   res.append(  temptags[14] + "," );
									   res.append(  temptags[15] + "," );
									   res.append(  temptags[16] );
			break;
		}
		case 13:
		{
									res = new StringBuilder(  temptags[0] + "," );
									   res.append(  temptags[1] + "," );
									   res.append(  temptags[2] + "," );
									   res.append(  normalizeCoord(temptags[3]) + "," );
									   res.append(  temptags[4] + "," );
									   res.append(  normalizeCoord(temptags[5]) + "," );
									   res.append(  temptags[6] + "," );
									   res.append(  normalizeSpeed(temptags[7]) + "," );
									   res.append(  normalizeCourse(temptags[8]) + "," );
									   res.append(  temptags[9] + "," );
									   res.append(  temptags[10] + "," );
									   res.append(  temptags[11] + "," );
									   res.append(  temptags[12] );
			break;
		}
		case 12:
		{
									res = new StringBuilder(  temptags[0] + "," );
									   res.append(  temptags[1] + "," );
									   res.append(  temptags[2] + "," );
									   res.append(  normalizeCoord(temptags[3]) + "," );
									   res.append(  temptags[4] + "," );
									   res.append(  normalizeCoord(temptags[5]) + "," );
									   res.append(  temptags[6] + "," );
									   res.append(  normalizeSpeed(temptags[7]) + "," );
									   res.append(  normalizeCourse(temptags[8]) + "," );
									   res.append(  temptags[9] + "," );
									   res.append(  temptags[10] + ",,A" );
									   res.append(  temptags[11] );
			break;
		}
		default:
			break;
		}

		// System.out.println("RRR>" + res.toString());
		return res.toString();
	}

	public static String normalizeCourse(String s)
	{
		if ((s == null)||(s.length()==0))
			return "0.0";
		
		if (s.equalsIgnoreCase("nan"))
			return "0.0";
		
		try 
		{
			Float.parseFloat(s);
		}
		catch (Exception e) 
		{
			return "0.0";
		}
		
		return s;
	}

	public static String normalizeSpeed(String s)
	{
		if ((s == null)||(s.length()==0))
			return "0.0";
		
		return s;
	}
	
	public static String normalizeCoord(String s)
	{
		if ((s.length()==8)||((s.length()==9)))
			return (s + "000");
		
		return s;
	}
	/**
	 * @return startFlag in String format: "1"-if set or "0"-if not
	 */
	public String getStartFlag()
	{
		return startFlag?"1":"0";
	}
	
	/**
	 * @param flag set startFlag with this value
	 */
	public void setStartFlag(boolean flag)
	{
		startFlag = flag;
	}
}
