
package com.barclays.facebook.bot;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import com.barclays.facebook.bot.pojo.AIResponse;
import com.barclays.facebook.bot.utility.GsonFactory;
import com.google.gson.Gson;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.GraphResponse;

import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.Message;
import com.restfb.types.webhook.WebhookEntry;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@WebServlet(name = "/webhook", urlPatterns = { "/webhook" })
public class JavaAPIBotServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("inside doGet() method");
       // response.getWriter().println("hello");

       if (request.getParameter("hub.verify_token").equalsIgnoreCase("verify_token")) {
            response.getWriter().append(request.getParameter("hub.challenge"));
        } else {
            response.getWriter().append("Served at: ").append(request.getContextPath());
        }
    }
	
	@Override
	  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
	      throws ServletException, IOException {

	    // retrieve POST Body
	    String body = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

	    // map body json to WebhookObject
	    DefaultJsonMapper mapper = new DefaultJsonMapper();
	    WebhookObject webhookObject = mapper.toJavaObject(body, WebhookObject.class);

	    for (WebhookEntry entry : webhookObject.getEntryList()) {
	      if (!entry.getMessaging().isEmpty()) {
	        for (MessagingItem item : entry.getMessaging()) {
	          String senderId = item.getSender().getId();

	          // create recipient
	          IdMessageRecipient recipient = new IdMessageRecipient(senderId);
	         if( item.getMessage()!=null){
	          String message=fetchQueryResponse(item.getMessage().getText());

	          final Gson gson = GsonFactory.getGson();
	          final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);
	          // check message
	          if (item.getMessage() != null && item.getMessage().getText() != null) {
	            // create simple text message
	            Message simpleTextMessage = new Message(aiResponse.getResult().getFulfillment().getSpeech());

	            // build send client and send message
	            FacebookClient sendClient = new DefaultFacebookClient("EAAOZB9RlT1RsBAA8UakMpEDlT5mwHWHOBZC1qBiq5tSLu9bouLZCsmwPOZBxW2CakWlFXz19rCTqXp1VeUpi4uPwWkCfr9LeX8bFr2uYXsec1mVLmeaEsZB2sWfUP1aZCDrWkfjuqNQayoNy5nvOq1zderQYu1OsI1PMcYDwfhRQZDZD", Version.VERSION_2_6);
	            sendClient.publish("me/messages", GraphResponse.class, Parameter.with("recipient", recipient),
	              Parameter.with("message", simpleTextMessage));
	          }

	          if (item.getPostback() != null) {
	            //LOG.debug("run postback");
	          }
	         }
	        }
	      }
	    }
	  }
	
	public String fetchQueryResponse(String query){

        String url="https://api.api.ai/v1/query";

        Client client = Client.create(new DefaultClientConfig());

        MultivaluedMap queryParams = new MultivaluedMapImpl();

        queryParams.add("query", query);
        queryParams.add("v", "20160621");
        queryParams.add("timezone", "Europe/London");
        queryParams.add("lang","en");
        queryParams.add("sessionId","1234567890");

        WebResource webResource = client.resource(url);         

        WebResource.Builder builder = webResource.queryParams(queryParams).accept("*/*");

        builder.header(HttpHeaders.AUTHORIZATION, "Bearer "+"44bb669ab90341e39af262754f7d5368");

        

        ClientResponse response=builder.get(ClientResponse.class);

        System.out.println("Response is "+response.toString());
      
        String entity = response.getEntity(String.class);

        System.out.println("Response data is : " + entity);


        return entity;                              

        

}


}
