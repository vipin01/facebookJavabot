package com.barclays.facebook.bot.factory;

import java.util.HashMap;
import java.util.Map;

public interface MessengerFactory {
	public Map<String,String> getAgentRequestParametersHashMap();
	public Map<String,String> getFacebookParameters();
	public String getVerifyToken();
	public String getPageName();
	public String getBotName();
	
	

}
