package com.barclays.facebook.bot.factory;

import java.util.HashMap;
import java.util.Map;

public class FacebookUKMessengerFactory implements MessengerFactory {

	@Override
	public Map<String, String> getAgentRequestParametersHashMap() {
		Map<String,String> parametersMap=new HashMap();
		parametersMap.put("v", "20160621");
		parametersMap.put("timezone", "Europe/London");
		parametersMap.put("lang", "en");
		parametersMap.put("sessionId", "1234567890");
		parametersMap.put("Authorization", "Bearer " + "615edc6548374cc9a9a0672223be7fe4");
		return parametersMap;
	}

	@Override
	public Map<String, String> getFacebookParameters() {
		Map<String,String> parametersMap=new HashMap();
		parametersMap.put("token", "EAARItObxyDwBAHSxSZBGQAY6JxtNSD7xGeZBsTf67Pi67exZBatI0agX2qHND6kQ71my1fKdvUDtD893eYbaYRuT05bWeshF8zZBq7wK8WjNbdCJ95uJ2WSADzl3ni2cf349mbblyecRliboETJTVJMNZBUIu86oy5LTonbfczwZDZD");
		return parametersMap;
		
	}
	
	public String getVerifyToken(){
		return "verify_uk";
	}

	@Override
	public String getPageName() {
		// TODO Auto-generated method stub
		return "Love of Learning";
	}

	@Override
	public String getBotName() {
		// TODO Auto-generated method stub
		return "consoleapibot";
	}
	
	

}
