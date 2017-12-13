import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.services.customsearch.model.Result;

public class QuestionEval {
	public static final double EVAL_TIME_LIMIT = 5;
	private static QuestionEval INSTANCE = new QuestionEval();

	private QuestionEval() {
	};

	public static QuestionEval getInstance() {
		return INSTANCE;
	}

	public String getAnswer(Question q) {
		TimeTracker.storeTime(TimeTracker.queryCondense);
		Result[][] results = WebUtil.getInstance().runAnswerSearch(q);
		TimeTracker.storeTime(TimeTracker.search);
		List<AnswerEval> answerEvals = Arrays.stream(q.getAnswers()).map(a -> new AnswerEval(a)).collect(toList());
		answerEvals.parallelStream().forEach(a -> a.getEvaluate(q, results));
		TimeTracker.storeTime(TimeTracker.ansEval);
		String bestAnswer = chooseBestAnswer(answerEvals, q.isOddOneOut());
		System.out.println(bestAnswer);
		TimeTracker.storeTime(TimeTracker.ansCompare);
		return bestAnswer;
	}

	private String chooseBestAnswer(List<AnswerEval> answerEvals, boolean isOddOneOut) {
		return (isOddOneOut ? Collections.min(answerEvals) : Collections.max(answerEvals)).getAnswer();
	}
}
