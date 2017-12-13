import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Search;

public class WebUtil {
	public static final String API_KEY = "AIzaSyBZVyVt1dxr0BvRp8AIHYK5H6WV_Dk4pSk";
	public static final String CX = "002984914276044763183:lholwuagtcq";
	public static final String APP_NAME = "HQBot";

	public static Search runSearch(String query) {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		Customsearch customsearch = new Customsearch.Builder(httpTransport, jsonFactory, null)
				.setApplicationName(APP_NAME).build();
		try {
			Customsearch.Cse.List list = customsearch.cse().list(query);
			list.setKey(API_KEY);
			list.setCx(CX);
			Search results = list.execute();
			return results;
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

	public static String getSiteText(String url) {
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			String textContents = doc.body().text();
			return textContents;
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return "";
	}
}
