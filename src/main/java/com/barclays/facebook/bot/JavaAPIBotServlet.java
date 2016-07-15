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

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

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

	private static final String QUESTION_SPLITTER = ":";

	private static final String BUTTON_TMP_IDENTIFIER = "$";

	private static final String SESSION_ID = "sessionId";

	private static final String LANG = "lang";

	private static final String TIMEZONE = "timezone";

	private static final String VERSION = "v";

	private static final String QUERY = "query";

	private static final String RECIPIENT = "recipient";

	private static final String MESSAGE = "message";

	private static final String ME_MESSAGES = "me/messages";

	private static final String TOKEN = "token";

	private static final String NONE = "none";

	private static final String HUB_CHALLENGE = "hub.challenge";

	private static final String HUB_VERIFY_TOKEN = "hub.verify_token";

	private static final String VERIFY_TOKEN = "verify_token";

	private static final String HELP_QUERY = "help_query";

	private static final String IMG_URL = "https://i.imgsafe.org/df4be6fe74.png";

	private static final String WELCOME_TXT = "welcome";

	private static final String ANSWER_BUTTON = "answer_button";

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JavaAPIBotServlet.class);

	@Override

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)

			throws ServletException, IOException {

		logger.debug("inside doGet() method");

		logger.debug("ServletPath " + request.getServletPath());

		/*
		 * 
		 * MessengerFactory factory =
		 * 
		 * BotFactory.getMessengerFactory(request.getServletPath()); String
		 * 
		 * verifyToken = factory.getVerifyToken();
		 * 
		 */

		ResourceBundle bundle = BotFactory.getBotPropertiesPerCountry(request.getServletPath());

		String verifyToken = bundle.getString(VERIFY_TOKEN);

		if (request.getParameter(HUB_VERIFY_TOKEN).equalsIgnoreCase(verifyToken)) {

			response.getWriter().append(request.getParameter(HUB_CHALLENGE));

		} else {

			response.getWriter().append("Served at: ").append(request.getContextPath());

		}

	}

	@Override

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)

			throws ServletException, IOException {

		// retrieve POST Body

		String body = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

		logger.debug("In doPost ServletPath " + request.getServletPath());

		// MessengerFactory

		// factory=BotFactory.getMessengerFactory(request.getServletPath());

		ResourceBundle bundle = BotFactory.getBotPropertiesPerCountry(request.getServletPath());

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

						String message = fetchQueryResponse(item.getMessage().getText(), bundle);

						final Gson gson = GsonFactory.getGson();

						final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);

						String temp = null;

						String link = null;

						if (aiResponse.getResult() != null) {

							temp = aiResponse.getResult().getFulfillment().getSpeech();

							link = aiResponse.getResult().getAction();

						}

						if (temp == null || temp.isEmpty()) {

							logger.debug("Temp is empty");

							String message2 = fetchQueryResponse(NONE, bundle);

							final AIResponse aiResponse2 = gson.fromJson(message2, AIResponse.class);

							temp = aiResponse2.getResult().getFulfillment().getSpeech();

						}

						Message templateMessage = getTemplateMessage(temp, link, bundle);

						FacebookClient sendClient = new DefaultFacebookClient(bundle.getString(TOKEN),

								Version.VERSION_2_6);

						try {

							sendClient.publish(ME_MESSAGES, GraphResponse.class, Parameter.with(RECIPIENT, recipient),

									Parameter.with(MESSAGE, templateMessage));

						} catch (Throwable th) {

							th.printStackTrace();

						}

					}

					if (item.getPostback() != null) {

						String message = fetchQueryResponse(item.getPostback().getPayload(), bundle);

						final Gson gson = GsonFactory.getGson();

						final AIResponse aiResponse = gson.fromJson(message, AIResponse.class);

						String temp = aiResponse.getResult().getFulfillment().getSpeech();

						String link = aiResponse.getResult().getAction();

						
						if (temp == null || temp.isEmpty()) {

							logger.debug("Temp is empty");

							String message2 = fetchQueryResponse(NONE, bundle);

							final AIResponse aiResponse2 = gson.fromJson(message2, AIResponse.class);

							temp = aiResponse2.getResult().getFulfillment().getSpeech();

							link = aiResponse2.getResult().getAction();

						}

						Message templateMessage = getTemplateMessage(temp, link, bundle);

						FacebookClient sendClient = new DefaultFacebookClient(bundle.getString(TOKEN),

								Version.VERSION_2_6);

						try {

							sendClient.publish(ME_MESSAGES, GraphResponse.class, Parameter.with(RECIPIENT, recipient),

									Parameter.with(MESSAGE, templateMessage));

						} catch (Throwable th) {

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

		queryParams.add(QUERY, query);

		queryParams.add(VERSION, factory.getString(VERSION));

		queryParams.add(TIMEZONE, factory.getString(TIMEZONE));

		queryParams.add(LANG, factory.getString(LANG));

		queryParams.add(SESSION_ID, factory.getString(SESSION_ID));

		WebResource webResource = client.resource(url);

		WebResource.Builder builder = webResource.queryParams(queryParams).accept("*/*");

		builder.header(HttpHeaders.AUTHORIZATION, factory.getString("Authorization"));

		ClientResponse response = builder.get(ClientResponse.class);

		logger.debug("Response is " + response.toString());

		String entity = response.getEntity(String.class);

		logger.debug("Response data is : " + entity);

		return entity;

	}

	public Message getTemplateMessage(String textMessage, String link, ResourceBundle factory) {

		GenericTemplatePayload genericPayload = null;

		ButtonTemplatePayload genericButtonPayload = null;

		TemplateAttachment template = null;

		Message message = null;

		String[] splitMessage = textMessage.split(QUESTION_SPLITTER);

		if (splitMessage.length > 1) {

			if (splitMessage[0].equals(BUTTON_TMP_IDENTIFIER)) {

				ButtonTemplatePayload payload = new ButtonTemplatePayload();

				payload.setText(factory.getString(HELP_QUERY));

				int length = splitMessage.length;

				logger.debug("Split message length " + length);

				int a;

				for (a = 1; a < length; ++a) {

					logger.debug(splitMessage[a]);

					PostbackButton postbackButton = new PostbackButton(splitMessage[a], splitMessage[a]);

					payload.addButton(postbackButton);

				}

				template = new TemplateAttachment(payload);

				message = new Message(template);

			} else {

				genericPayload = new GenericTemplatePayload();

				for (String msg : splitMessage) {

					PostbackButton postback = new PostbackButton(factory.getString(ANSWER_BUTTON), msg);

					Bubble bubble = new Bubble(factory.getString(WELCOME_TXT));

					bubble.setImageUrl(IMG_URL);

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
