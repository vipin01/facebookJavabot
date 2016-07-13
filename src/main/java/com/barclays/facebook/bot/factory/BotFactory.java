package com.barclays.facebook.bot.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class BotFactory {
	
	public static MessengerFactory getMessengerFactory(String messengerName){
		String country = messengerName.substring(messengerName.indexOf("/")+1, messengerName.lastIndexOf("/"));
		String className=getProperty(country);
		MessengerFactory  factory=null;
		try {
			if(className!=null){
				factory=(MessengerFactory) Class.forName(className).newInstance();
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*if(messengerName.contains("uk")){
			return new FacebookUKMessengerFactory();
		}else if(messengerName.contains("us")){
			return new FacebookUSMessengerFactory();
		}*/
		return factory;
	}
	
	public static String getProperty(String propertyName){
		Properties prop=loadProperties();
		String value=null;
		if(prop!=null)
			value=prop.getProperty(propertyName);
		return value;
	}
	
	private static Properties loadProperties(){
		Properties prop = new Properties();
		InputStream input = null;
		try {

			input = BotFactory.class.getClassLoader().getResourceAsStream("countryconfig.properties");
			// load a properties file
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}
	
	public static ResourceBundle getBotPropertiesPerCountry(String path){
		String country = path.substring(path.indexOf("/")+1, path.lastIndexOf("/"));
		String lanuguageAndCntryCode=getProperty(country);
		String[] lanuguageAndCntryCodeArray=lanuguageAndCntryCode.split(",");
		Locale locale=new Locale(lanuguageAndCntryCodeArray[0],lanuguageAndCntryCodeArray[1]);
		ResourceBundle facebookProps = ResourceBundle.getBundle("facebookprops", locale);
		return facebookProps;
	}

}
