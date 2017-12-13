import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.api.services.customsearch.model.Result;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class AnswerEval implements Comparable<AnswerEval> {
	private String answer;
	private int parsedScore;

	public AnswerEval(String answer) {
		this.answer = answer;
	}

	public AnswerEval getEvaluate(Question q, Result[][] allResults) {
		new AnswerEval(q.getAnswers()[0]).byAnswerSearch(getKeyWords(q), q.getAnswerString(),
				allResults[ArrayUtils.indexOf(q.getAnswers(), answer)], Executors.newCachedThreadPool());
		System.out.println(parsedScore);
		return null;

		/*
		 * 11 List<Result> myResults = new ArrayList<>(); for (Result t : results) {
		 * Result copy = t.clone(); myResults.add(copy); } AnswerEval eval = new
		 * AnswerEval(answer); CompletableFuture<Void> parsedScore =
		 * CompletableFuture.runAsync(() -> eval.calcParsedScore(answer, myResults),
		 * ex); CompletableFuture<AnswerEval> combine =
		 * within(parsedScore.thenApply((Void v) -> eval), timeout);
		 * combine.handle((ans, t) -> { if (t != null) {
		 * System.err.println("failed to complete all calculations in time!"); } return
		 * eval; }); try { AnswerEval val = combine.get(); return val; } catch
		 * (InterruptedException e) { e.printStackTrace(); } catch (ExecutionException
		 * e) { e.printStackTrace(); } return null;
		 */
	}

	public String[] getKeyWords(Question q) {
		String qe = q.getQuestion();
		qe = qe.replaceAll("(?i)not\\s", "");
		qe = qe.replaceAll("\\b[\\w']{1,4}\\b", "");
		qe = qe.replaceAll("\\s{2,}", " ");
		qe = qe.replaceAll("\\.", "");
		qe = qe.trim();
		String[] split = qe.split(" ");
		Arrays.stream(split).forEach(System.out::println);
		return split;
	}

	public CompletableFuture<Void> byAnswerSearch(String[] searches, String ans, Result[] results, Executor ex) {
		return CompletableFuture.allOf(Arrays.stream(results).map((Result r) -> {
			return CompletableFuture.runAsync(
					() -> parsedScore += StringSearch.match(WebUtil.getInstance().getSiteText(r.getLink()), searches),
					ex);
		}).toArray(CompletableFuture[]::new));
	}

	private Integer calcParsedScore(String answer, List<Result> results) {
		ForkJoinPool streamOps = new ForkJoinPool(3);
		try {
			streamOps.submit(() -> results.parallelStream().forEach(r -> {
				String text = WebUtil.getInstance().getSiteText(r.getLink());
				int s = StringUtils.countMatches(text, answer);
				if (answer.equalsIgnoreCase("Harbor Wave")) {
					System.out.println(answer + ": " + s + "\t" + r.getLink());
				}
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

	public String getAnswer() {
		return answer;
	}

	public String toString() {
		return answer + ": " + getParsedScore();
	}

	public int compareTo(AnswerEval eval) {
		return (int) (this.getParsedScore() - eval.getParsedScore());
	}
}
