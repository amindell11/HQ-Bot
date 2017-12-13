import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
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
	public static final WebUtil INSTANCE = new WebUtil();

	public static WebUtil getInstance() {
		return INSTANCE;
	}

	private HttpTransport httpTransport;
	private JsonFactory jsonFactory;
	private Customsearch customsearch;
	private org.jsoup.nodes.Document doc;
	private LanguageServiceClient language;
	private Document.Builder nlDocBuilder;
	private AnalyzeEntitiesRequest.Builder request;

	private WebUtil() {
		httpTransport = new NetHttpTransport();
		jsonFactory = new JacksonFactory();
		customsearch = new Customsearch.Builder(httpTransport, jsonFactory, null).setApplicationName(APP_NAME).build();
		request = AnalyzeEntitiesRequest.newBuilder().setEncodingType(EncodingType.UTF16);
		nlDocBuilder = Document.newBuilder().setType(Type.PLAIN_TEXT);
		try {
			language = LanguageServiceClient.create();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Result[][] runAnswerSearch(Question question) {
		Result[][] results = new Result[3][1];
		for (int x = 0; x < results.length; x++) {
			results[x] = runSearch(question.getAnswers()[x], 3).getItems().toArray(new Result[0]);
		}
		return results;
	}

	/**
	 * uses the question as the search query
	 */
	public List<Result> runQuestionSearch(Question question) {
		String query = simplifyQuestion(question);
		return runSearch(query, 9).getItems();
	}

	public Search runSearch(String query, long numResults) {
		try {
			Customsearch.Cse.List list = customsearch.cse().list(query);
			list.setKey(API_KEY);
			list.setCx(CX);
			list.setFilter("1");
			list.setNum(numResults);
			Search results = list.execute();
			return results;
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

	public String getSiteText(String url) {
		try {
			doc = Jsoup.connect(url).get();
			String textContents = doc.body().text();
			return textContents;
		} catch (IOException e) {
		}
		return "";
	}

	private static String simplifyQuestion(Question q) {
		String qe = q.getQuestion();
		String delim = String.join("\" OR \"", q.getAnswers());
		qe = qe.replaceAll("(?i)not\\s", "") + delim;
		return qe;
	}

	/**
	 * Identifies entities in the string {@code text}.
	 */
	public Double getSalience(String text) throws Exception {
		Document doc = nlDocBuilder.setContent(text).build();
		AnalyzeEntitiesRequest req = request.setDocument(doc).build();
		AnalyzeEntitiesResponse response = language.analyzeEntities(req);
		return response.getEntitiesList().stream().map(e -> e.getSalience())
				.collect(Collectors.averagingDouble(f -> (double) f));
	}

}