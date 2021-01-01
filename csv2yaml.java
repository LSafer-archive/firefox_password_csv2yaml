import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class csv2yaml {
	public static void main(String[] args) throws IOException {
		if (args.length < 1)
			throw new IllegalArgumentException("You must provide an argument");

		File input = new File(args[0]);

		if (!input.exists() || input.isDirectory())
			throw new IllegalArgumentException(args[0] + " is not a file!");

		List<String> unexpected = new ArrayList<>();
		List<Map<String, String>> parsed = parseCSV(readFile(input), unexpected::add);

		{//parsed
			File output = new File(args[0].replaceFirst("(.csv$)|($)", ".yaml"));

			String parsedYaml = formatYaml(parsed, csv2yaml::unitLabel);
			writeFile(output, parsedYaml);
			System.out.println("Generated " + parsed.size() + " elements to " + output);
		}
		{//unexpected
			File output = new File(args[0].replaceFirst("(.csv$)|($)", " malformed.yaml"));
			String malformedList = unexpected.stream()
					.collect(Collectors.joining(
							"\n"
					));
			writeFile(output, malformedList);
			System.out.println("Generated " + unexpected.size() + " elements to " + output);
		}
		if (args.length >= 2) {//filtered
			File output = new File(args[0].replaceFirst("(.csv$)|$", " (" + args[1].replaceAll(
					"[^\\w\\d]",
					"-"
			) + ").yaml"));

			Pattern pattern = Pattern.compile(args[1]);
			List<Map<String, String>> filtered = parsed.stream()
					.filter(m ->
							m.entrySet()
									.stream()
									.anyMatch(e -> pattern.matcher(e.getValue()).find())
					)
					.collect(Collectors.toList());
			String filteredYaml = formatYaml(filtered, csv2yaml::unitLabel);
			writeFile(output, filteredYaml);
			System.out.println("Generated " + filtered.size() + " elements to " + output);
		}
	}

	public static final String readFile(File file) throws IOException {
		StringBuilder builder = new StringBuilder();
		FileReader reader = new FileReader(file);
		char[] buffer = new char[1024];
		while (true) {
			int length = reader.read(buffer);

			if (length == -1)
				break;
			if (length > 0)
				builder.append(buffer, 0, length);
		}

		reader.close();
		return builder.toString();
	}

	public static final void writeFile(File file, String s) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(s);
		writer.close();
	}

	private static final String formatYaml(List<Map<String, String>> list, Function<Map<String, String>, String> label) {
		return list.stream()
				.sorted(Comparator.comparing(label::apply))
				.map(m ->
						m.entrySet()
								.stream()
								.sorted(Comparator.comparing(Map.Entry::getKey))
								.map(entry -> {
									return "\n\t" + entry.getKey() + ": " + entry.getValue();
								})
								.collect(Collectors.joining(
										"",
										label.apply(m) + ":",
										"\n"
								))
				)
				.collect(Collectors.joining(
						"\n"
				));
	}

	private static final List<Map<String, String>> parseCSV(String string, Consumer<String> unexpected) {
		String[][] headder = {null};
		return Arrays.stream(string.split("\r?\n\r?"))
				.map(line -> {
					return new String[][]{
							Arrays.stream(line.split(","))
									.map(s ->
											s.matches("\\s*\"[^\"]*\"\\s*") ?
											s.replaceAll("(^\\s*\")|(\\s*\"$)", "") :
											s
									)
									.toArray(String[]::new),
							{line}
					};
				})
				.peek(array -> {
					if (headder[0] == null)
						headder[0] = array[0];
				})
				.skip(1)
				.filter(array -> {
					if (array[0].length == headder[0].length)
						return true;

					unexpected.accept(array[1][0]);
					return false;
				})
				.map(array ->
						IntStream.range(0, array[0].length)
								.boxed()
								.collect(Collectors.toMap(
										i -> headder[0][i],
										i -> array[0][i]
								))
				)
				.collect(Collectors.toList());
	}

	private static final String unitLabel(Map<String, String> map) {
		return map.getOrDefault("username", "") + "@" +
			   map.getOrDefault("url", "")
					   .replaceAll("^https?:\\/\\/([^/]*)\\/?.*$", "$1");
	}
}
