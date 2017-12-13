import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class QuestionEval {
	public static final double EVAL_TIME_LIMIT = 5;
	private static QuestionEval INSTANCE = new QuestionEval();

	private QuestionEval() {
	};

	public static QuestionEval getInstance() {
		return INSTANCE;
	}

	public String getAnswer(Question q) {
		System.out.println("starting search");
		String searchQuery = simplifyQuestion(q);
		Search results = runSearch(searchQuery);
		System.out.println(searchQuery);
		List<AnswerEval> answerEvals = asyncEvaluateAll(q.getAnswers(), results.getItems());
		String bestAnswer = chooseBestAnswer(answerEvals, q.isOddOneOut());
		return bestAnswer;
	}

	private String chooseBestAnswer(List<AnswerEval> answerEvals, boolean isOddOneOut) {
		return (isOddOneOut ? Collections.min(answerEvals) : Collections.max(answerEvals)).getAnswer();
	}

	private String simplifyQuestion(Question q) {
		return q.getQuestion().replace(" not", " ");
	}

	private Search runSearch(String searchQuery) {
		return WebUtil.runSearch(searchQuery);
	}

	private List<AnswerEval> asyncEvaluateAll(String[] answers, List<Result> items) {
		Executor ex = Executors.newCachedThreadPool(
				new ThreadFactoryBuilder().setNameFormat("scoreCalcPool-%d").setPriority(Thread.MAX_PRIORITY).build());
		try {
			List<AnswerEval> evals = new ArrayList<>();
			ForkJoinPool parallelAnswerEvals = new ForkJoinPool(3);
			parallelAnswerEvals
					.submit(() -> Arrays.stream(answers).parallel().forEach(
							(String a) -> evals.add(AnswerEval.getEvaluate(a, items, Duration.ofMillis(500000), ex))))
					.get();
			System.out.println("evaluations: " + evals);
			return evals;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
