package ru.assisttech.assistsdk;

public class Utils {

	static public String processAmount(String amt) {
		if(amt.length() != 0) {
			int pos = amt.indexOf('.');
			if(pos == -1) {
				amt += ".00";
			}
			while(pos > amt.length() - 3) {
				amt += "0";
			}
		}
		return amt;
	}
}

