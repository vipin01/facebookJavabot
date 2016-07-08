
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
					if (item.getMessage() != null) {
						String message = fetchQueryResponse(item.getMessage().getText());

						final Gson gson = GsonFactory.getGson();
						final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);
						// check message
						if (item.getMessage() != null && item.getMessage().getText() != null) {
							String temp = aiResponse.getResult().getFulfillment().getSpeech();
							String link = aiResponse.getResult().getAction();
							System.out.printf("Temp %s", temp);
							if (temp == null || temp.isEmpty()) {
								System.out.println("Temp is empty");
								String message2 = fetchQueryResponse("none");
								final AIResponse aiResponse2 = gson.fromJson(message2, AIResponse.class);
								temp = aiResponse2.getResult().getFulfillment().getSpeech();
							}
							Message templateMessage = getTemplateMessage(temp,link);
							// build send client and send message
							FacebookClient sendClient = new DefaultFacebookClient(
									"EAAOlIyo3LTMBAApctladjzZAoZCsInwCe2QI7IbeFGFtWSYe3dWAAogZBYjdRLmjsQGfPAMLZB9FBCJSJK2VqdkKkZAkivlplQLeU3d98xstN84VIEpJ4rP5KmnC9IKUwR7oSY83knB0LYMZBOQ87R9GkWMDZBeRJzZApbfqjBb4YQZDZD",
									Version.VERSION_2_6);
						
							sendClient.publish("me/messages", GraphResponse.class,
									Parameter.with("recipient", recipient), Parameter.with("message", templateMessage));
						}

					}
					if (item.getPostback() != null) {
						String message = fetchQueryResponse(item.getPostback().getPayload());

						final Gson gson = GsonFactory.getGson();
						final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);
						String temp = aiResponse.getResult().getFulfillment().getSpeech();
						String link = aiResponse.getResult().getAction();
						System.out.printf("Temp %s", temp);
						if (temp == null || temp.isEmpty()) {
							System.out.println("Temp is empty");
							String message2 = fetchQueryResponse("none");
							final AIResponse aiResponse2 = gson.fromJson(message2, AIResponse.class);
							temp = aiResponse2.getResult().getFulfillment().getSpeech();
							link = aiResponse2.getResult().getAction();
						}
						Message templateMessage = getTemplateMessage(temp,link);

						// build send client and send message
						FacebookClient sendClient = new DefaultFacebookClient(
								"EAAOlIyo3LTMBAApctladjzZAoZCsInwCe2QI7IbeFGFtWSYe3dWAAogZBYjdRLmjsQGfPAMLZB9FBCJSJK2VqdkKkZAkivlplQLeU3d98xstN84VIEpJ4rP5KmnC9IKUwR7oSY83knB0LYMZBOQ87R9GkWMDZBeRJzZApbfqjBb4YQZDZD",
								Version.VERSION_2_6);
					
						sendClient.publish("me/messages", GraphResponse.class, Parameter.with("recipient", recipient),
								Parameter.with("message", templateMessage));
						
					}
				}
			}
		}
	}

	public String fetchQueryResponse(String query) {

		String url = "https://api.api.ai/v1/query";

		Client client = Client.create(new DefaultClientConfig());

		MultivaluedMap queryParams = new MultivaluedMapImpl();

		queryParams.add("query", query);
		queryParams.add("v", "20160621");
		queryParams.add("timezone", "Europe/London");
		queryParams.add("lang", "en");
		queryParams.add("sessionId", "1234567890");

		WebResource webResource = client.resource(url);

		WebResource.Builder builder = webResource.queryParams(queryParams).accept("*/*");

		builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + "615edc6548374cc9a9a0672223be7fe4");
	
		ClientResponse response = builder.get(ClientResponse.class);

		System.out.println("Response is " + response.toString());

		String entity = response.getEntity(String.class);

		System.out.println("Response data is : " + entity);

		return entity;

	}

	public Message getTemplateMessage(String textMessage,String link) {
		GenericTemplatePayload genericPayload = null;
		ButtonTemplatePayload genericButtonPayload = null;
		TemplateAttachment template = null;
		Message message = null;
		String[] splitMessage = textMessage.split(":");
		if (splitMessage.length > 1) {
			if(splitMessage[0].equals("$")){
				ButtonTemplatePayload payload = new ButtonTemplatePayload();
				payload.setText("Hi, How may I help you today?");
				int length=splitMessage.length;
				System.out.println(length);
				int a;
				for(a=1;a<length;++a){
					System.out.println(splitMessage[a]);
					PostbackButton postbackButton = new PostbackButton(splitMessage[a],splitMessage[a]);
					payload.addButton(postbackButton);

				}
				
				template = new TemplateAttachment(payload);
				message = new Message(template);
				
			}
			else {
			genericPayload = new GenericTemplatePayload();
			for (String msg : splitMessage) {
				PostbackButton postback = new PostbackButton("Answer", msg);
				Bubble bubble = new Bubble("Welcome to Barclaycard FAQs");
				bubble.setImageUrl("https://i.imgsafe.org/df4be6fe74.png");
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
