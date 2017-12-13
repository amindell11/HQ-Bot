public final class Question {
	private final String[] answers;
	private final String question;
	private final boolean oddOneOut;

	public Question(String question, String[] answers) {
		this.question = question;
		this.answers = answers;
		this.oddOneOut = question.contains("not");
	}

	public String[] getAnswers() {
		return answers;
	}

	public String getQuestion() {
		return question;
	}

	public boolean isOddOneOut() {
		return oddOneOut;
	}

	public String toString() {
		return question + "\t[" + String.join(", ", answers) + "]";
	}
}
