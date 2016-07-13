
package com.barclays.facebook.bot;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import com.barclays.facebook.bot.factory.BotFactory;
import com.barclays.facebook.bot.factory.MessengerFactory;
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
import com.restfb.types.send.Bubble;
import com.restfb.types.send.ButtonTemplatePayload;
import com.restfb.types.send.GenericTemplatePayload;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.Message;
import com.restfb.types.send.PostbackButton;
import com.restfb.types.send.TemplateAttachment;
import com.restfb.types.webhook.WebhookEntry;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

//@WebServlet(name = "/webhook", urlPatterns = { "/webhook" })
public class JavaAPIBotServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("inside doGet() method");
		System.out.println("ServletPath " + request.getServletPath());

		/*MessengerFactory factory = BotFactory.getMessengerFactory(request.getServletPath());
		String verifyToken = factory.getVerifyToken();
		*/
		ResourceBundle bundle=BotFactory.getBotPropertiesPerCountry(request.getServletPath());
		String verifyToken =bundle.getString("verify_token");
		if (request.getParameter("hub.verify_token").equalsIgnoreCase(verifyToken)) {
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
		System.out.println("In doPost ServletPath "+request.getServletPath());
		//MessengerFactory factory=BotFactory.getMessengerFactory(request.getServletPath());
		ResourceBundle bundle=BotFactory.getBotPropertiesPerCountry(request.getServletPath());
		// map body json to WebhookObject
		DefaultJsonMapper mapper = new DefaultJsonMapper();
		WebhookObject webhookObject = mapper.toJavaObject(body, WebhookObject.class);

		for (WebhookEntry entry : webhookObject.getEntryList()) {
			if (!entry.getMessaging().isEmpty()) {
				for (MessagingItem item : entry.getMessaging()) {
					String senderId = item.getSender().getId();

					// create recipient
					IdMessageRecipient recipient = new IdMessageRecipient(senderId);
					
						// check message
						if (item.getMessage() != null && item.getMessage().getText() != null) {
							String message = fetchQueryResponse(item.getMessage().getText(),bundle);

							final Gson gson = GsonFactory.getGson();
							final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);
							String temp =null;
							String link=null;
							if(aiResponse.getResult()!=null){
								temp=aiResponse.getResult().getFulfillment().getSpeech();
								link = aiResponse.getResult().getAction();
							}
							System.out.printf("Temp %s", temp);
							if (temp == null || temp.isEmpty()) {
								System.out.println("Temp is empty");
								String message2 = fetchQueryResponse("none",bundle);
								final AIResponse aiResponse2 = gson.fromJson(message2, AIResponse.class);
								temp = aiResponse2.getResult().getFulfillment().getSpeech();
							}
							Message templateMessage = getTemplateMessage(temp,link);
							// build send client and send message
							//Testbot
							/*FacebookClient sendClient = new DefaultFacebookClient(
									"EAAOlIyo3LTMBAApctladjzZAoZCsInwCe2QI7IbeFGFtWSYe3dWAAogZBYjdRLmjsQGfPAMLZB9FBCJSJK2VqdkKkZAkivlplQLeU3d98xstN84VIEpJ4rP5KmnC9IKUwR7oSY83knB0LYMZBOQ87R9GkWMDZBeRJzZApbfqjBb4YQZDZD",
									Version.VERSION_2_6);*/
							//vibhor bot
							/*FacebookClient sendClient = new DefaultFacebookClient(
									"EAAG3bIDZBeuEBAEP2qUYiYFPAlCfPusWu0knMXuFi2EZB0nFvdvIW3sunQMiNf2Vnm0sHgo2ZARqy0ht4lBy0lFMtH5ANk6I5oi4vkecI9gtODoJHY8E27OKpsdLJdcPux5xYRZAcUJnSvHq0sLdFiDYBoZAfD5mINbRVNtcLkAZDZD",
									Version.VERSION_2_6);*/
							//loveoflearningbot
							/*FacebookClient sendClient = new DefaultFacebookClient(
									"EAARItObxyDwBAKakZCeWys3NPbBxUTCMUFGwjwM1NUXS9fBPmnTin3EHJBHHesh6KEf4A691w8NFizCfD6fj8T72yFUcamNtPDOwSyReRmriBoL2A4Yw6sA7vu220sXh8CZARtrOq2nvF0yCfe4Riw7lTi8WPNX7KaIAF62AZDZD",
									Version.VERSION_2_6);*/
							FacebookClient sendClient = new DefaultFacebookClient(bundle.getString("token"),Version.VERSION_2_6);
							try{
								sendClient.publish("me/messages", GraphResponse.class,
									Parameter.with("recipient", recipient), Parameter.with("message", templateMessage));
							}catch(Throwable th){
								th.printStackTrace();
							}
						}

					
					if (item.getPostback() != null) {
						String message = fetchQueryResponse(item.getPostback().getPayload(),bundle);

						final Gson gson = GsonFactory.getGson();
						final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);
						String temp = aiResponse.getResult().getFulfillment().getSpeech();
						String link = aiResponse.getResult().getAction();
						System.out.printf("Temp %s", temp);
						if (temp == null || temp.isEmpty()) {
							System.out.println("Temp is empty");
							String message2 = fetchQueryResponse("none",bundle);
							final AIResponse aiResponse2 = gson.fromJson(message2, AIResponse.class);
							temp = aiResponse2.getResult().getFulfillment().getSpeech();
							link = aiResponse2.getResult().getAction();
						}
						Message templateMessage = getTemplateMessage(temp,link);

						// build send client and send message
						//Testbot
						/*FacebookClient sendClient = new DefaultFacebookClient(
								"EAAOlIyo3LTMBAApctladjzZAoZCsInwCe2QI7IbeFGFtWSYe3dWAAogZBYjdRLmjsQGfPAMLZB9FBCJSJK2VqdkKkZAkivlplQLeU3d98xstN84VIEpJ4rP5KmnC9IKUwR7oSY83knB0LYMZBOQ87R9GkWMDZBeRJzZApbfqjBb4YQZDZD",
								Version.VERSION_2_6);*/
						//vibhor bot
						/*FacebookClient sendClient = new DefaultFacebookClient(
								"EAAG3bIDZBeuEBAEP2qUYiYFPAlCfPusWu0knMXuFi2EZB0nFvdvIW3sunQMiNf2Vnm0sHgo2ZARqy0ht4lBy0lFMtH5ANk6I5oi4vkecI9gtODoJHY8E27OKpsdLJdcPux5xYRZAcUJnSvHq0sLdFiDYBoZAfD5mINbRVNtcLkAZDZD",
								Version.VERSION_2_6);*/
						//loveoflearningbot
						/*FacebookClient sendClient = new DefaultFacebookClient(
								"EAARItObxyDwBAKakZCeWys3NPbBxUTCMUFGwjwM1NUXS9fBPmnTin3EHJBHHesh6KEf4A691w8NFizCfD6fj8T72yFUcamNtPDOwSyReRmriBoL2A4Yw6sA7vu220sXh8CZARtrOq2nvF0yCfe4Riw7lTi8WPNX7KaIAF62AZDZD",
								Version.VERSION_2_6);*/
						FacebookClient sendClient = new DefaultFacebookClient(bundle.getString("token"),Version.VERSION_2_6);
						try{
							sendClient.publish("me/messages", GraphResponse.class, Parameter.with("recipient", recipient),
								Parameter.with("message", templateMessage));
						}catch(Throwable th){
							th.printStackTrace();
						}
					}
				}
			}
		}
	}

	public String fetchQueryResponse(String query, ResourceBundle factory) {

		String url = "https://api.api.ai/v1/query";

		Client client = Client.create(new DefaultClientConfig());

		MultivaluedMap queryParams = new MultivaluedMapImpl();

		queryParams.add("query", query);
		queryParams.add("v", factory.getString("v"));
		queryParams.add("timezone", factory.getString("timezone"));
		queryParams.add("lang", factory.getString("lang"));
		queryParams.add("sessionId", factory.getString("sessionId"));

		WebResource webResource = client.resource(url);

		WebResource.Builder builder = webResource.queryParams(queryParams).accept("*/*");

		// builder.header(HttpHeaders.AUTHORIZATION, "Bearer " +
		// "615edc6548374cc9a9a0672223be7fe4");
		// builder.header(HttpHeaders.AUTHORIZATION, "Bearer " +
		// "cfbaf6d6ce674487ba6d7844d73fadca");
		builder.header(HttpHeaders.AUTHORIZATION, factory.getString("Authorization"));
		ClientResponse response = builder.get(ClientResponse.class);

		System.out.println("Response is " + response.toString());

		String entity = response.getEntity(String.class);

		System.out.println("Response data is : " + entity);

		return entity;

	}

	public Message getTemplateMessage(String textMessage, String link) {
		GenericTemplatePayload genericPayload = null;
		ButtonTemplatePayload genericButtonPayload = null;
		TemplateAttachment template = null;
		Message message = null;
		String[] splitMessage = textMessage.split(":");
		if (splitMessage.length > 1) {
			if (splitMessage[0].equals("$")) {
				ButtonTemplatePayload payload = new ButtonTemplatePayload();
				payload.setText("Hi, How may I help you today?");
				int length = splitMessage.length;
				System.out.println(length);
				int a;
				for (a = 1; a < length; ++a) {
					System.out.println(splitMessage[a]);
					PostbackButton postbackButton = new PostbackButton(splitMessage[a], splitMessage[a]);
					payload.addButton(postbackButton);

				}

				template = new TemplateAttachment(payload);
				message = new Message(template);

			} else {
				genericPayload = new GenericTemplatePayload();
				for (String msg : splitMessage) {
					PostbackButton postback = new PostbackButton("Answer", msg);
					Bubble bubble = new Bubble("Welcome to Barclaycard FAQs");
					bubble.setImageUrl("https://i.imgsafe.org/df4be6fe74.png");
					// bubble.setImageUrl("http://53bde1b4.ngrok.io/facebookJavabot-0.0.1-SNAPSHOT/images/barclaycard_logo.png");
					bubble.setItemUrl(link);
					bubble.setSubtitle(msg);
					bubble.addButton(postback);
					genericPayload.addBubble(bubble);

				}
				template = new TemplateAttachment(genericPayload);
				message = new Message(template);
			}
		} else {
			if (textMessage.length() > 320)
				message = new Message(textMessage.substring(0, 320));
			else
				message = new Message(textMessage);
		}
		return message;

	}

}
