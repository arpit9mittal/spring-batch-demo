package a9m.demo.batch.config.readers.multiline;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "batch.file.two")
public class MultiLineFileProperties {

	private Map<String,Map<String,List<String>>> tokenizer;

	/**
	 * @return the tokenizer
	 */
	public Map<String, Map<String, List<String>>> getTokenizer() {
		return tokenizer;
	}

	/**
	 * @param tokenizer the tokenizer to set
	 */
	public void setTokenizer(Map<String, Map<String, List<String>>> tokenizer) {
		this.tokenizer = tokenizer;
	}

	
}
