package org.geometerplus.fbreader.library;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.Word;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.ZLTextPlainModel.EntryIteratorImpl;

import com.iwobanas.hunspellchecker.Hunspell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

public class DictionaryParser {
	public static class FileWord {
		public String word;
		public int frequency;
		public boolean isKnown;
		public String description;
		public int paragraphIndex;

		public FileWord(String word) {
			this.word = word;
			this.frequency = 1;
			this.isKnown = false;
		}

		public String toString() {
			String temp = null;
			try {
				temp = getDescription();
			} catch (Exception e) {
			}
			System.out.println(temp);
			return word + "," + frequency + "," + isKnown + "," + temp + ",\n";
		}

		public String getDescription() throws IOException {
			if (this.description != null)
				return this.description;
			URL getUrl = new URL(DICT_URL + word);
			HttpURLConnection connection = (HttpURLConnection) getUrl
					.openConnection();
			connection.connect();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;
			// 取得输入流，并使用Reader读取
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "utf-8"));
			StringBuffer lines = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.append(line);
				lines.append("\n");
			}

			reader.close();
			// 断开连接
			connection.disconnect();
			this.description = lines.toString();
			return this.description;
		}
	}

	static String DICT_URL = "http://dict.qq.com/dict?q=";

	public static String checkEndWithSuffix(String word) {
		if (word.endsWith("'s"))
			return word.substring(0, word.length() - 2);
		if (word.endsWith("'re"))
			return word.substring(0, word.length() - 3);
		if (word.endsWith("'ve"))
			return word.substring(0, word.length() - 3);
		if (word.endsWith("'ll"))
			return word.substring(0, word.length() - 3);
		if (word.endsWith("'d"))
			return word.substring(0, word.length() - 2);
		if (word.endsWith("n't"))
			return word.substring(0, word.length() - 3);
		return word;
	}

	public static LinkedHashMap<String, FileWord> readFromDictionary(
			String fileName) {
		File file = new File(fileName);
		LinkedHashMap<String, FileWord> map = new LinkedHashMap<String, FileWord>();
		if (file.exists()) {
			if (file.isFile()) {
				try {

					BufferedReader input = new BufferedReader(new FileReader(
							file));
					String text;

					while ((text = input.readLine()) != null) {

					}
				} catch (IOException ioException) {
					System.err.println("File Error!");

				}
			}
		}
		return map;
	}

	public static java.util.HashMap<String, String> wordCache = new java.util.HashMap<String, String>();
	public static String getRealWord(String word, String language) {
		
		String realWord = wordCache.get(word);
		if(realWord != null) return realWord;
		String[] results = Hunspell.Instance(language).analyze(word);
		if(results != null) {
			for(String result:results) {
				int index = result.indexOf("st:");
				if(index > -1 ) {
					int endIndex = result.indexOf(" ", index + 3);
					if(endIndex > -1) {
						realWord = result.substring(index+3, endIndex);
					} else {
						realWord = result.substring(index+3);
					}
					break;
				}
			}
		}
		wordCache.put(word, realWord);
		return realWord;
	}
	public static void statisticsString(String text,
			LinkedHashMap<String, FileWord> map, int paragraphIndex, String language) {
		String[] array = text.split(seperate);
		for (String word : array) {
			word = word.trim();
			if (word.trim().length() > 0 && !word.startsWith("-")
					&& !word.startsWith("'")) {
				word = word.toLowerCase();
				word = getRealWord(word, language);
				//word = checkEndWithSuffix(word);
				if (map.containsKey(word)) {
					FileWord temp = map.get(word);
					temp.frequency++;
				} else {
					FileWord temp = new FileWord(word);
					temp.paragraphIndex = paragraphIndex;
					map.put(word, temp);
				}
			}
		}

	}
	
	private static String BAK_FILE_PATH = "/word.bak";
	
	public static void createBak(List<Word> AllKnownWords) {
		File bak = new File(Paths.BookPathOption.getValue()+BAK_FILE_PATH);
		if(bak.exists()) bak.delete();
		
		try {
			bak.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(bak,false));
			for(Word word: AllKnownWords) {
				
					output.write(word.getText()+";"+word.getLanguage()+";"+word.getFrequency());
					output.write("\n");
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void importBak(IBookCollection<Book> collection, List<Word> AllKnownWords) {
		
		try {
			BufferedReader input = new BufferedReader(new FileReader(Paths.BookPathOption.getValue()+BAK_FILE_PATH));
			String line = null;
	        while (( line = input.readLine()) != null){
	          String [] temp = line.split(";");
	          Word word = new Word(-1, temp[1], temp[0],Integer.parseInt(temp[2]),0,0,0);
	        	  
        	  boolean isKnown = false;
        	  for (Word wo : AllKnownWords) {
					if (word.getText().equals(wo.getText())) {
						isKnown = true;
						break;
					}
				}
				if(!isKnown) {
					collection.saveToKnownWords(word);
					AllKnownWords.add(word);
				}
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String seperate = "[0-9~/&%$@#^*‘’“”《》—|_«»\\\n\t\",;=:().{}?　! ]";
	
	public static String readFileToDictionary(IBookCollection<Book> collection, Book book, String fileName, String language) {

		String output = "";
		File file = new File(fileName);

		if (file.exists() && file.isFile()) {
			try {
				List<Word> unknownWords = collection.unknownWords(
						book.getId());
				if (unknownWords.size() > 0) {
					return null;
				}
				LinkedHashMap<String, FileWord> map = new LinkedHashMap<String, FileWord>();
				BufferedReader input = new BufferedReader(new FileReader(file));
				// StringBuffer buffer = new StringBuffer();
				String text;

				int index=0;
				while ((text = input.readLine()) != null) {
					// buffer.append(text +"/n");
					statisticsString(text, map, index, language);
					index++;
				}
				List<Word> knownWords = collection.allKnownWords(book.getLanguage());
				// new File(fileName+".csv").delete();
				for (String word : map.keySet()) {
					FileWord temp = map.get(word);
					// AppendToFile.appendMethodB(fileName+".csv",
					// temp.toString());
					Word w = new Word(book.getId(), book.getLanguage()
							.toLowerCase(), word, temp.frequency, 0, 0, 0);
					if (knownWords.size() == 0) {
						if (temp.frequency >= 15) {
							collection.saveToKnownWords(w);
						} else {
							collection.saveToUnknownWords(w);
						}
					} else {
						boolean isKnown = false;
						for (Word wo : knownWords) {
							if (word.equals(wo.getText())) {
								isKnown = true;
								break;
							}
						}
						if (!isKnown) {
							collection.saveToUnknownWords(w);
						}
					}
				}

				// output = buffer.toString();
			} catch (IOException ioException) {
				System.err.println("File Error!");

			}
		}
		return output;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// readFileToDictionary("d:\\1.Harry Potter and the Sorcerer's Stone.txt");
	}

}
