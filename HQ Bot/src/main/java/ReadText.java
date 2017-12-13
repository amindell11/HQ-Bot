
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

public class ReadText {
	public static Question detectText(String filePath, PrintStream out) throws Exception, IOException {
		List<AnnotateImageRequest> requests = new ArrayList<>();

		ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

		Image img = Image.newBuilder().setContent(imgBytes).build();
		Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);
		System.out.println("connectiong to GCP...");
		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			System.out.println("processing image...");
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();
			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					out.printf("Error: %s\n", res.getError().getMessage());
					return null;
				}
				String[] fieldList = res.getTextAnnotations(0).getDescription().split("\n");
				String[] answers = Arrays.copyOfRange(fieldList, fieldList.length - 3, fieldList.length);
				String question = String.join(" ", Arrays.copyOfRange(fieldList, 0, fieldList.length - 3));
				Question quest = new Question(question, answers);
				System.out.println(quest);
				return quest;
			}
		}
		return null;
	}
}