import java.util.Arrays;

public enum TimeTracker {
	startTime, ocr, queryCondense, search, ansEval, ansCompare, runTime;
	public double timeStamp;

	public static void init() {
		startTime.timeStamp = getTimeSeconds();
	}

	public static void storeTime(TimeTracker waypoint) {
		waypoint.timeStamp = getTimeSeconds() - startTime.timeStamp;
	}

	private static double getTimeSeconds() {
		return .01d * Math.round(System.currentTimeMillis() / 10d);
	}

	public double getTimestamp() {
		return timeStamp - startTime.timeStamp;
	}

	public static double getTimeDiff(TimeTracker val1, TimeTracker val2) {
		return val2.timeStamp - val1.timeStamp;
	}

	public static String getTimeOutputs() {
		return Arrays.stream(TimeTracker.values())
				.map(v -> v.name() + "\t" + v.timeStamp + "\t" + v.getTimestamp() + "\n").reduce("", (a, b) -> a + b);
	}
}
