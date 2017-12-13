import java.io.IOException;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Search;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;

public class WebUtil {
	public static final String API_KEY = "AIzaSyBZVyVt1dxr0BvRp8AIHYK5H6WV_Dk4pSk";
	public static final String CX = "002984914276044763183:lholwuagtcq";
	public static final String APP_NAME = "HQBot";

	/**
	 * uses the question as the search query
	 */
	public static Search runQuestionSearch(Question question) {
		String query = simplifyQuestion(question);
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		Customsearch customsearch = new Customsearch.Builder(httpTransport, jsonFactory, null)
				.setApplicationName(APP_NAME).build();
		try {
			System.out.println(query);
			Customsearch.Cse.List list = customsearch.cse().list(query);
			list.setKey(API_KEY);
			list.setCx(CX);
			list.setFilter("1");
			Search results = list.execute();
			System.out.println(results.getQueries());
			return results;
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

	public static String getSiteText(String url) {
		org.jsoup.nodes.Document doc;
		try {
			doc = Jsoup.connect(url).get();
			String textContents = doc.body().text();
			return textContents;
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return "";
	}

	private static String simplifyQuestion(Question q) {
		String qe = q.getQuestion();
		String delim = String.join("\" OR \"", q.getAnswers());
		qe = qe.replaceAll("(?i)not\\s", "") + delim;
		return null;
	}

	/**
	 * Identifies entities in the string {@code text}.
	 */
	public static Double getSalience(String text) throws Exception {
		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
			AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();
			AnalyzeEntitiesResponse response = language.analyzeEntities(request);
			return response.getEntitiesList().stream().map(e -> e.getSalience())
					.collect(Collectors.averagingDouble(f -> (double) f));
		}
	}
}