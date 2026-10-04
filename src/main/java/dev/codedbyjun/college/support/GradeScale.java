package dev.codedbyjun.college.support;

/** 100점 만점 점수 → 등급 / 4.5 평점 */
public final class GradeScale {

	private static final int[] CUTS = {95, 90, 85, 80, 75, 70, 65, 60};
	private static final String[] LETTERS = {"A+", "A0", "B+", "B0", "C+", "C0", "D+", "D0"};
	private static final double[] POINTS = {4.5, 4.0, 3.5, 3.0, 2.5, 2.0, 1.5, 1.0};

	private GradeScale() {
	}

	public static String letter(Integer score) {
		if (score == null) {
			return "-";
		}
		for (int i = 0; i < CUTS.length; i++) {
			if (score >= CUTS[i]) {
				return LETTERS[i];
			}
		}
		return "F";
	}

	public static double point(int score) {
		for (int i = 0; i < CUTS.length; i++) {
			if (score >= CUTS[i]) {
				return POINTS[i];
			}
		}
		return 0.0;
	}
}
