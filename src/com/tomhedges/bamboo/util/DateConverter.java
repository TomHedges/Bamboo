package com.tomhedges.bamboo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {

	public Date convertStringToDate(String stringDate) {
		//Convert from MySQL date "2014-07-08 19:43:00" to Java Date.
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(stringDate);
			return convertedDate;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public String convertDateToString(Date dateDate) {
		//Convert from Java Date "2014-07-08 19:43:00" to MySQL date.
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(dateDate);
	}
}