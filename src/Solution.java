import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Data {
	int[][] features;
	boolean[] labels;

	public Data(int[][] features, boolean[] labels) {
		this.features = features;
		this.labels = labels;
	}
}

class Node {
	Node[] nextNodes = {null, null}; Data data;
	int separationAttribute, separationThreshold, decision = -1;

	public Node(Data data) {
		this.data = data;
	}
}

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

		for(boolean label : labels)
			if (label) P++; else N++;

		for(int col = 0; col < features[0].length; col++){
			int p = 0; int n = 0;

			sortByAttribute(new Data(features, labels), col);

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

	private static void sortByAttribute(Data data, int attribute) {
		for(int i = 0; i < data.features.length - 1; i++)
			for(int j = i + 1; j < data.features.length; j++)
				if(data.features[j][attribute] < data.features[i][attribute]) {
					for(int k = 0; k < data.features[0].length; k++)
						data.features[i][k] = data.features[i][k] ^ data.features[j][k] ^ (data.features[j][k] = data.features[i][k]);
					data.labels[i] = data.labels[i] ^ data.labels[j] ^ (data.labels[j] = data.labels[i]);
				}
	}

	private static Data fileRead(String filename, boolean train) throws FileNotFoundException {
		ArrayList<ArrayList<Integer>> features = new ArrayList<>();
		ArrayList<Boolean> labels = new ArrayList<>();
		File file = new File(filename);
		Scanner sc = new Scanner(file);

		while(sc.hasNextLine()) {
			String s = sc.nextLine();
			ArrayList<String> t = new ArrayList<>(Arrays.asList(s.split(",")));

			if(train) {
				if(t.get(t.size() - 1).equals("1")) labels.add(true); else labels.add(false);
				t.remove(t.size() - 1);
			}

			features.add(new ArrayList<>());
			t.forEach(a -> features.get(features.size() - 1).add(Integer.parseInt(a)));
		}

		sc.close();
		return new Data(features.stream().map(f -> f.stream().mapToInt(Integer::intValue).toArray()).toArray(int[][]::new), Booleans2booleans(labels));
	}

	private static boolean[] Booleans2booleans(ArrayList<Boolean> Booleans) {
		boolean[] booleans = new boolean[Booleans.size()];
		for(int i = 0; i < Booleans.size(); i++)
			booleans[i] = Booleans.get(i);
		return booleans;
	}

	private static Data[] separate(Data data, int separationAttribute, int separationThreshold) {
		Data[] separated = {null, null};
		ArrayList<ArrayList<Integer>> features1 = new ArrayList<>();
		ArrayList<ArrayList<Integer>> features2 = new ArrayList<>();
		ArrayList<Boolean> labels1 = new ArrayList<>();
		ArrayList<Boolean> labels2 = new ArrayList<>();

		for(int i = 0; i < data.features.length; i++)
			if(data.features[i][separationAttribute] <= separationThreshold) {
				features1.add(IntStream.of(data.features[i]).boxed().collect(Collectors.toCollection(ArrayList::new)));
				labels1.add(data.labels[i]);
			}
			else {
				features2.add(IntStream.of(data.features[i]).boxed().collect(Collectors.toCollection(ArrayList::new)));
				labels2.add(data.labels[i]);
			}

		separated[0] = new Data(features1.stream().map(f -> f.stream().mapToInt(Integer::intValue).toArray()).toArray(int[][]::new), Booleans2booleans(labels1));
		separated[1] = new Data(features2.stream().map(f -> f.stream().mapToInt(Integer::intValue).toArray()).toArray(int[][]::new), Booleans2booleans(labels2));
		return separated;
	}

	private static int getDecision(Data data) {
		for(int i = 1; i < data.labels.length; i++)
			if((data.labels[i] != data.labels[0]))
				return -1;

		if(data.labels[0]) return 1; else return 0;
	}

	private static Node buildTree(Data data) {
		Node node = new Node(data);

		int[] answer = getBestSeparation(node.data.features, node.data.labels);
		node.separationAttribute = answer[0];
		node.separationThreshold = answer[1];
		node.decision = getDecision(data);

		if(node.decision == -1) {
			sortByAttribute(node.data, node.separationAttribute);
			Data[] separated = separate(node.data, node.separationAttribute, node.separationThreshold);
			node.nextNodes[0] = buildTree(separated[0]);
			node.nextNodes[1] = buildTree(separated[1]);
		}

		return node;
	}

	public static void main(String[] args) throws IOException {
		Data train = fileRead("train.csv", true);
		Data test = fileRead("test.csv", false);
		FileWriter fw = new FileWriter("results.csv");

		Node tree = buildTree(train);

		for(int i = 0; i < test.features.length; i++) {
			Node currentNode = tree;
			while (currentNode.decision == -1)
				if(test.features[i][currentNode.separationAttribute] < currentNode.separationThreshold)
					currentNode = currentNode.nextNodes[0];
				else
					currentNode = currentNode.nextNodes[1];

			fw.write(currentNode.decision + "\n");
		}

		fw.close();
	}
}
