import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.api.services.customsearch.model.Result;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class AnswerEval implements Comparable<AnswerEval> {
	private String answer;
	private int parsedScore, snippetScore;

	public AnswerEval(String answer) {
		this.answer = answer;
	}

	public static AnswerEval getEvaluate(String answer, List<Result> results, Duration timeout, Executor ex) {
		List<Result> myResults = new ArrayList<>();
		for (Result t : results) {
			Result copy = t.clone();
			myResults.add(copy);
		}
		AnswerEval eval = new AnswerEval(answer);
		CompletableFuture<Void> snippetScore = CompletableFuture
				.runAsync(() -> eval.calcSnippetScore(answer, myResults), ex);
		CompletableFuture<Void> parsedScore = CompletableFuture.runAsync(() -> eval.calcParsedScore(answer, myResults),
				ex);
		CompletableFuture<Void> calcFuture = CompletableFuture.allOf(snippetScore, parsedScore);
		CompletableFuture<AnswerEval> combine = within(calcFuture.thenApply((Void v) -> eval), timeout);
		combine.handle((ans, t) -> {
			if (t != null) {
				System.err.println("failed to complete all calculations in time!");
			}
			return eval;
		});
		try {
			AnswerEval val = combine.get();
			return val;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;

	}

	private Integer calcSnippetScore(String answer, List<Result> results) {
		ForkJoinPool streamOps = new ForkJoinPool(3);
		try {
			streamOps.submit(() -> results.parallelStream()
					.forEach(r -> snippetScore += StringUtils.countMatches(r.getHtmlSnippet(), answer))).get();
			// System.err.println("done checking snippets for \"" + answer + "\" : " +
			// snippetScore);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return snippetScore;
	}

	private Integer calcParsedScore(String answer, List<Result> results) {
		ForkJoinPool streamOps = new ForkJoinPool(3);
		try {
			streamOps.submit(() -> results.parallelStream().forEach(r -> {
				String text = WebUtil.getSiteText(r.getLink());
				text = text.substring(0, text.length() / 2);
				int s = StringUtils.countMatches(text, answer);
				// System.out.println(answer + ": " + s + "\t" + r.getLink());
				parsedScore += s;
			})).get();
			// System.err.println("done parsing for \"" + answer + "\" : " + parsedScore);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return parsedScore;
	}

	private Integer calcParsedScore2(String answer, List<Result> results) {
		ForkJoinPool streamOps = new ForkJoinPool(3);
		try {
			streamOps.submit(() -> results.parallelStream().forEach(r -> {
				String text = WebUtil.getSiteText(r.getLink());
				text = text.substring(0, text.lastIndexOf(answer) + answer.length());
				int s = StringUtils.countMatches(text, answer);
				// System.out.println(answer + ": " + s + "\t" + r.getLink());
				parsedScore += s;
			})).get();
			System.err.println("done parsing for \"" + answer + "\" : " + parsedScore);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return parsedScore;
	}

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
			new ThreadFactoryBuilder().setDaemon(true).setNameFormat("failAfter-%d").build());

	public static <T> CompletableFuture<T> failAfter(Duration duration) {
		final CompletableFuture<T> promise = new CompletableFuture<>();
		scheduler.schedule(() -> {
			return promise.completeExceptionally(new Throwable("timeout reached"));
		}, duration.toMillis(), TimeUnit.MILLISECONDS);
		return promise;
	}

	public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
		final CompletableFuture<T> timeout = failAfter(duration);
		return future.applyToEither(timeout, Function.identity());
	}

	public int getParsedScore() {
		return parsedScore;
	}

	private void setParsedScore(Integer parsedScore) {
		this.parsedScore = parsedScore;
	}

	public int getSnippetScore() {
		return snippetScore;
	}

	private void setSnippetScore(Integer snippetScore) {
		this.snippetScore = snippetScore;
	}

	public String getAnswer() {
		return answer;
	}

	public String toString() {
		return answer + ": " + getParsedScore() + ", " + getSnippetScore();
	}

	public double getOverallScore() {
		return getParsedScore() + getSnippetScore();
	}

	public int compareTo(AnswerEval eval) {
		return (int) (this.getOverallScore() - eval.getOverallScore());
	}
}
