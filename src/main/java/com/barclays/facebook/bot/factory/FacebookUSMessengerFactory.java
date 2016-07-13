package com.barclays.facebook.bot.factory;

import java.util.HashMap;
import java.util.Map;

public class FacebookUSMessengerFactory implements MessengerFactory {

	@Override
	public Map<String, String> getAgentRequestParametersHashMap() {
		Map<String,String> parametersMap=new HashMap();
		parametersMap.put("v", "20160621");
		parametersMap.put("timezone", "US/Newyork");
		parametersMap.put("lang", "en");
		parametersMap.put("sessionId", "1234567891");
		parametersMap.put("Authorization", "Bearer " + "3872af97acda4598b0557174c14c241c");
		return parametersMap;
	}

	@Override
	public Map<String, String> getFacebookParameters() {
		Map<String,String> parametersMap=new HashMap();
		parametersMap.put("token", "EAAOlIyo3LTMBAApctladjzZAoZCsInwCe2QI7IbeFGFtWSYe3dWAAogZBYjdRLmjsQGfPAMLZB9FBCJSJK2VqdkKkZAkivlplQLeU3d98xstN84VIEpJ4rP5KmnC9IKUwR7oSY83knB0LYMZBOQ87R9GkWMDZBeRJzZApbfqjBb4YQZDZD");
		return parametersMap;
		
	}
	
	public String getVerifyToken(){
		return "verify_token";
	}

	@Override
	public String getPageName() {
		// TODO Auto-generated method stub
		return "Testbot";
	}

	@Override
	public String getBotName() {
		// TODO Auto-generated method stub
		return "testbot";
	}
	
	

}
