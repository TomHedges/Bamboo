package com.tomhedges.bamboo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class which converts between String and Date formats, which allows
 * for data to be transferred between remote and local sources and model objects.
 * 
 * @see			Game
 * @see			LocalDBDataRetrieval
 * @author      Tom Hedges
 */

public class DateConverter {

	public Date convertStringToDate(String stringDate) {
		//Convert from MySQL date "2014-07-08 19:43:00" to Java Date.
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(stringDate);
			return convertedDate;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Date reduceDateByMinutes(Date dateOrigDate, int minutesToReduceBy) {
		// Reduce date by set number of minutes. Calculation multiplies minutes value to milliseconds
		Date dateReduced = new Date(dateOrigDate.getTime() - (minutesToReduceBy * 60 * 1000));
		return dateReduced;
	}

	public String convertDateToString(Date dateDate) {
		//Convert from Java Date "2014-07-08 19:43:00" to MySQL date.
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(dateDate);
	}
}
