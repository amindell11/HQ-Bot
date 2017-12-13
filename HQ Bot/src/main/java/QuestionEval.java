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

	public String getAnswer(Question q) {TimeTracker.storeTime(TimeTracker.queryCondense);
		Search results = WebUtil.runQuestionSearch(q);
		TimeTracker.storeTime(TimeTracker.search);
		List<AnswerEval> answerEvals = asyncEvaluateAll(q.getAnswers(), results.getItems());
		TimeTracker.storeTime(TimeTracker.ansEval);
		String bestAnswer = chooseBestAnswer(answerEvals, q.isOddOneOut());
		System.out.println(bestAnswer);
		TimeTracker.storeTime(TimeTracker.ansCompare);
		return bestAnswer;
	}

	private String chooseBestAnswer(List<AnswerEval> answerEvals, boolean isOddOneOut) {
		return (isOddOneOut ? Collections.min(answerEvals) : Collections.max(answerEvals)).getAnswer();
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
