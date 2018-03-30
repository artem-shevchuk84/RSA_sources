package ru.by.rsa;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

import ru.by.rsa.adapter.item.MerchImagesItem;

public class MerchImages {
	File[] fList;
	ArrayList<MerchImagesItem> merchList;
	
	public MerchImages(File[] files) {
		merchList = new ArrayList<MerchImagesItem>(); 
		fList = files;
		
		for(int i=0;i<fList.length;i++) {
			merchList.add(new MerchImagesItem(fList[i]));
		}
	}
	
	public int getCount(Calendar c, String custID, String shopID) {
		String matchString = custID + "@" + shopID;
		String timeStamp = 
	            new SimpleDateFormat("yyyyMMdd").format(new Date());
		int count = 0;
		
		for(int i=0;i<merchList.size();i++) {
			Log.i("RRR",timeStamp + "=" + merchList.get(i).getDate());
			Log.i("RRR",matchString + "=" + merchList.get(i).getID());
			if (timeStamp.equals(merchList.get(i).getDate())
					&&(matchString.equals(merchList.get(i).getID()))) {
				count++;
			}
		}
		
		return count;
	}

}
