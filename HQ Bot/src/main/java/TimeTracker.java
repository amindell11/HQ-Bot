import java.text.DecimalFormat;
import java.util.Arrays;

public enum TimeTracker {
	startTime, ocr, queryCondense, search, ansEval, ansCompare, runTime;
	public long timeStamp;

	public static void init() {
		startTime.timeStamp = System.currentTimeMillis();
	}

	public static void storeTime(TimeTracker waypoint) {
		waypoint.timeStamp = getTimestamp();
	}

	public static float getTimeSeconds(long millis) {
		return millis / 1000F;
	}

	public float getTime() {
		return getTimeSeconds(timeStamp);
	}

	public static long getTimestamp() {
		return System.currentTimeMillis() - startTime.timeStamp;
	}

	public float since(TimeTracker val2) {
		return getTime() - val2.getTime();
	}

	public String toString() {
		return new DecimalFormat("##.00").format(getTime());
	}

	public static String getTimeOutputs() {
		return Arrays.stream(TimeTracker.values()).map(v -> v.name() + "\t" + v).reduce("", (a, b) -> a + b);
	}
}
