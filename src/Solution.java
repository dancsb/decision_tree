public class Solution{

	public static double getEntropy(int nCat1, int nCat2){
		if(nCat1 == 0 || nCat2 == 0) return 0.0;
		float p1 = (float) nCat1 / (nCat1 + nCat2);
		float p2 = (float) nCat2 / (nCat1 + nCat2);
		return - p1 * (Math.log(p1) / Math.log(2)) - p2 * (Math.log(p2) / Math.log(2));
	}

	public static int[] getBestSeparation(int[][] features, boolean[] labels){
		int[] answer = {0, 0};
		int P = 0; int N = 0; double E = Double.MAX_VALUE;

		for (boolean label : labels)
			if (label) P++; else N++;

		for(int col = 0; col < features[0].length; col++){
			int p = 0; int n = 0;

			for(int i = 0; i < features.length - 1; i++)
				for (int j = i + 1; j < features.length; j++)
					if (features[j][col] < features[i][col]) {
						for(int k = 0; k < features[0].length; k++)
							features[i][k] = features[i][k] ^ features[j][k] ^ (features[j][k] = features[i][k]);
						labels[i] = labels[i] ^ labels[j] ^ (labels[j] = labels[i]);
					}

			for(int row = 0; row < features.length; row++) {
				if(labels[row]) p++; else n++;

				double rir = (((double) (p + n) / labels.length) * getEntropy(p, n)) + (((double) ((P - p) + (N - n)) / labels.length) * getEntropy(P - p, N - n));
				if(rir < E) {
					E = rir;
					answer[0] = col;
					answer[1] = features[row][col];
				}
			}
		}

		return answer;
	}

	public static void main(String[] args){
		
	}
}
