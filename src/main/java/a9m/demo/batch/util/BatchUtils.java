/**
 * 
 */
package a9m.demo.batch.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

/**
 * @author s2565087
 *
 */
public final class BatchUtils {

	/**
	 * @param names e.g. ISIN,Quantity,Price,Customer
	 * @param range e.g. 1-12,13-15,16-20,21-29
	 * @return FixedLengthTokenizer
	 */
	public static FixedLengthTokenizer getTokenizer(String names, String range) {
		FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
		
		if(Strings.isNotBlank(range)) {
			List<Range> ranges =
			 Stream.of(range.split(",")).map(str -> {
				String[] arr = str.split("-");
				return new Range(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
			}).collect(Collectors.toList());
			tokenizer.setColumns(ranges.toArray(new Range[0]));
		}
		
		if(Strings.isNotBlank(names))
			tokenizer.setNames(names.split(","));
		
		return tokenizer;
	}
	
	/**
	 * @param names
	 * @param range
	 * @return
	 */
	public static FixedLengthTokenizer getTokenizer(List<String> names, List<String> range) {
		FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();

		if (null != names)
			tokenizer.setNames(names.toArray(new String[0]));

		if (null != range) {
			List<Range> ranges = 
					range.stream().map(str -> {
						String[] arr = str.split("-");
						return new Range(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
					}).collect(Collectors.toList());
			tokenizer.setColumns(ranges.toArray(new Range[0]));
		}

		return tokenizer;
	}
	
}
