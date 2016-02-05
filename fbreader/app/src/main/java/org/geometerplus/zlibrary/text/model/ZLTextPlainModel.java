/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.model;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jopencc.util.ChineseConvertor;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.Word;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.library.DictionaryParser;
import org.geometerplus.fbreader.library.DictionaryParser.FileWord;
import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.*;

import com.iwobanas.hunspellchecker.Hunspell;

import android.text.TextUtils;

public final class ZLTextPlainModel implements ZLTextModel, ZLTextStyleEntry.Feature {
	private final String myId;
	private final String myLanguage;

	private int[] myStartEntryIndices;
	private int[] myStartEntryOffsets;
	private int[] myParagraphLengths;
	private int[] myTextSizes;
	private byte[] myParagraphKinds;

	private int myParagraphsNumber;

	private final CachedCharStorage myStorage;
	private final Map<String,ZLImage> myImageMap;

	private ArrayList<ZLTextMark> myMarks;

	private final FontManager myFontManager;

	public final class EntryIteratorImpl implements ZLTextParagraph.EntryIterator {
		private int myCounter;
		private int myLength;
		private byte myType;

		int myDataIndex;
		int myDataOffset;

		// TextEntry data
		private char[] myTextData;
		private int myTextOffset;
		private int myTextLength;

		// ControlEntry data
		private byte myControlKind;
		private boolean myControlIsStart;
		// HyperlinkControlEntry data
		private byte myHyperlinkType;
		private String myHyperlinkId;

		// ImageEntry
		private ZLImageEntry myImageEntry;

		// VideoEntry
		private ZLVideoEntry myVideoEntry;

		// ExtensionEntry
		private ExtensionEntry myExtensionEntry;

		// StyleEntry
		private ZLTextStyleEntry myStyleEntry;

		// FixedHSpaceEntry data
		private short myFixedHSpaceLength;

		public EntryIteratorImpl(int index) {
			reset(index);
		}

		public void reset(int index) {
			myCounter = 0;
			myLength = myParagraphLengths[index];
			myDataIndex = myStartEntryIndices[index];
			myDataOffset = myStartEntryOffsets[index];
		}

		public byte getType() {
			return myType;
		}

		public char[] getTextData() {
			return myTextData;
		}
		public int getTextOffset() {
			return myTextOffset;
		}
		public int getTextLength() {
			return myTextLength;
		}

		public byte getControlKind() {
			return myControlKind;
		}
		public boolean getControlIsStart() {
			return myControlIsStart;
		}
		public byte getHyperlinkType() {
			return myHyperlinkType;
		}
		public String getHyperlinkId() {
			return myHyperlinkId;
		}

		public ZLImageEntry getImageEntry() {
			return myImageEntry;
		}

		public ZLVideoEntry getVideoEntry() {
			return myVideoEntry;
		}

		public ExtensionEntry getExtensionEntry() {
			return myExtensionEntry;
		}

		public ZLTextStyleEntry getStyleEntry() {
			return myStyleEntry;
		}

		public short getFixedHSpaceLength() {
			return myFixedHSpaceLength;
		}

		public boolean next() {
			if (myCounter >= myLength) {
				return false;
			}

			int dataOffset = myDataOffset;
			char[] data = myStorage.block(myDataIndex);
			if (data == null) {
				return false;
			}
			if (dataOffset >= data.length) {
				data = myStorage.block(++myDataIndex);
				if (data == null) {
					return false;
				}
				dataOffset = 0;
			}
			short first = (short)data[dataOffset];
			byte type = (byte)first;
			if (type == 0) {
				data = myStorage.block(++myDataIndex);
				if (data == null) {
					return false;
				}
				dataOffset = 0;
				first = (short)data[0];
				type = (byte)first;
			}
			myType = type;
			++dataOffset;
			switch (type) {
				case ZLTextParagraph.Entry.TEXT:
				{
					int textLength = (int)data[dataOffset++];
					textLength += (((int)data[dataOffset++]) << 16);
					textLength = Math.min(textLength, data.length - dataOffset);
					myTextLength = textLength;
					myTextData = data;
					myTextOffset = dataOffset;
					dataOffset += textLength;
					break;
				}
				case ZLTextParagraph.Entry.CONTROL:
				{
					short kind = (short)data[dataOffset++];
					myControlKind = (byte)kind;
					myControlIsStart = (kind & 0x0100) == 0x0100;
					myHyperlinkType = 0;
					break;
				}
				case ZLTextParagraph.Entry.HYPERLINK_CONTROL:
				{
					final short kind = (short)data[dataOffset++];
					myControlKind = (byte)kind;
					myControlIsStart = true;
					myHyperlinkType = (byte)(kind >> 8);
					final short labelLength = (short)data[dataOffset++];
					myHyperlinkId = new String(data, dataOffset, labelLength);
					dataOffset += labelLength;
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
				{
					final short vOffset = (short)data[dataOffset++];
					final short len = (short)data[dataOffset++];
					final String id = new String(data, dataOffset, len);
					dataOffset += len;
					final boolean isCover = data[dataOffset++] != 0;
					myImageEntry = new ZLImageEntry(myImageMap, id, vOffset, isCover);
					break;
				}
				case ZLTextParagraph.Entry.FIXED_HSPACE:
					myFixedHSpaceLength = (short)data[dataOffset++];
					break;
				case ZLTextParagraph.Entry.STYLE_CSS:
				case ZLTextParagraph.Entry.STYLE_OTHER:
				{
					final short depth = (short)((first >> 8) & 0xFF);
					final ZLTextStyleEntry entry =
						type == ZLTextParagraph.Entry.STYLE_CSS
							? new ZLTextCSSStyleEntry(depth)
							: new ZLTextOtherStyleEntry();

					final short mask = (short)data[dataOffset++];
					for (int i = 0; i < NUMBER_OF_LENGTHS; ++i) {
						if (ZLTextStyleEntry.isFeatureSupported(mask, i)) {
							final short size = (short)data[dataOffset++];
							final byte unit = (byte)data[dataOffset++];
							entry.setLength(i, size, unit);
						}
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, ALIGNMENT_TYPE) ||
						ZLTextStyleEntry.isFeatureSupported(mask, NON_LENGTH_VERTICAL_ALIGN)) {
						final short value = (short)data[dataOffset++];
						if (ZLTextStyleEntry.isFeatureSupported(mask, ALIGNMENT_TYPE)) {
							entry.setAlignmentType((byte)(value & 0xFF));
						}
						if (ZLTextStyleEntry.isFeatureSupported(mask, NON_LENGTH_VERTICAL_ALIGN)) {
							entry.setVerticalAlignCode((byte)((value >> 8) & 0xFF));
						}
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, FONT_FAMILY)) {
						entry.setFontFamilies(myFontManager, (short)data[dataOffset++]);
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, FONT_STYLE_MODIFIER)) {
						final short value = (short)data[dataOffset++];
						entry.setFontModifiers((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF));
					}

					myStyleEntry = entry;
				}
				case ZLTextParagraph.Entry.STYLE_CLOSE:
					// No data
					break;
				case ZLTextParagraph.Entry.RESET_BIDI:
					// No data
					break;
				case ZLTextParagraph.Entry.AUDIO:
					// No data
					break;
				case ZLTextParagraph.Entry.VIDEO:
				{
					myVideoEntry = new ZLVideoEntry();
					final short mapSize = (short)data[dataOffset++];
					for (short i = 0; i < mapSize; ++i) {
						short len = (short)data[dataOffset++];
						final String mime = new String(data, dataOffset, len);
						dataOffset += len;
						len = (short)data[dataOffset++];
						final String src = new String(data, dataOffset, len);
						dataOffset += len;
						myVideoEntry.addSource(mime, src);
					}
					break;
				}
				case ZLTextParagraph.Entry.EXTENSION:
				{
					short kindLength = (short)data[dataOffset++];
					if((kindLength + dataOffset) > data.length) {
						kindLength = (short)(data.length - dataOffset);
					}
					if(kindLength < 0 || dataOffset < 0) {
						break;
					}
					final String kind = new String(data, dataOffset, kindLength);
					dataOffset += kindLength;

					final Map<String,String> map = new HashMap<String,String>();
					final short dataSize = (short)((first >> 8) & 0xFF);
					for (short i = 0; i < dataSize; ++i) {
						short keyLength = (short)data[dataOffset++];
						if((keyLength + dataOffset) > data.length) {
							keyLength = (short)(data.length - dataOffset);
						}
						if(keyLength < 0 || dataOffset < 0) {
							break;
						}
						final String key = new String(data, dataOffset, keyLength);
						dataOffset += keyLength;
						final short valueLength = (short)data[dataOffset++];
						map.put(key, new String(data, dataOffset, valueLength));
						dataOffset += valueLength;
					}
					myExtensionEntry = new ExtensionEntry(kind, map);
					break;
				}
			}
			++myCounter;
			myDataOffset = dataOffset;
			return true;
		}
	}

	public ZLTextPlainModel(
		String id,
		String language,
		int paragraphsNumber,
		int[] entryIndices,
		int[] entryOffsets,
		int[] paragraphLengths,
		int[] textSizes,
		byte[] paragraphKinds,
		String directoryName,
		String fileExtension,
		int blocksNumber,
		Map<String,ZLImage> imageMap,
		FontManager fontManager
	) {
		myId = id;
		myLanguage = language;
		myParagraphsNumber = paragraphsNumber;
		myStartEntryIndices = entryIndices;
		myStartEntryOffsets = entryOffsets;
		myParagraphLengths = paragraphLengths;
		myTextSizes = textSizes;
		myParagraphKinds = paragraphKinds;
		myStorage = new CachedCharStorage(directoryName, fileExtension, blocksNumber);
		myImageMap = imageMap;
		myFontManager = fontManager;
	}

	public final String getId() {
		return myId;
	}

	public final String getLanguage() {
		return myLanguage;
	}

	public final ZLTextMark getFirstMark() {
		return (myMarks == null || myMarks.isEmpty()) ? null : myMarks.get(0);
	}

	public final ZLTextMark getLastMark() {
		return (myMarks == null || myMarks.isEmpty()) ? null : myMarks.get(myMarks.size() - 1);
	}

	public final ZLTextMark getNextMark(ZLTextMark position) {
		if (position == null || myMarks == null) {
			return null;
		}

		ZLTextMark mark = null;
		for (ZLTextMark current : myMarks) {
			if (current.compareTo(position) >= 0) {
				if ((mark == null) || (mark.compareTo(current) > 0)) {
					mark = current;
				}
			}
		}
		return mark;
	}

	public final ZLTextMark getPreviousMark(ZLTextMark position) {
		if ((position == null) || (myMarks == null)) {
			return null;
		}

		ZLTextMark mark = null;
		for (ZLTextMark current : myMarks) {
			if (current.compareTo(position) < 0) {
				if ((mark == null) || (mark.compareTo(current) < 0)) {
					mark = current;
				}
			}
		}
		return mark;
	}
	public void makeDictionary(IBookCollection<Book> collection, Book book) {
		int	startIndex = 0;
		int	endIndex = myParagraphsNumber;
		int index = startIndex;
		
//		List<Word> unknownWords = Library.Instance().unknownWords(book.getId());
//    	if(unknownWords.size() >0) {
//    		return;
//    	}
    	
		final EntryIteratorImpl it = new EntryIteratorImpl(index);
		LinkedHashMap<String, FileWord> map = new LinkedHashMap<String, FileWord>();
		while (true) {
			int offset = 0;
			while (it.next()) {
				//it.next();
				if (it.getType() == ZLTextParagraph.Entry.TEXT) {
					char[] textData = it.getTextData();
					int textOffset = it.getTextOffset();
					int textLength = it.getTextLength();
					char[] textDataDst = new char[textLength + 1];
					
					System.arraycopy(textData, textOffset, textDataDst, 0, textLength);
					
					DictionaryParser.statisticsString(new String(textDataDst), map, index, book.getLanguage());
					
					offset += textLength;
				}
			}
			if (++index >= endIndex) {
				break;
			}
			it.reset(index);
		}
		
		List<Word> knownWords = collection.allKnownWords(book.getLanguage());
        //new File(fileName+".csv").delete();
        for(String word:map.keySet()) {
        	FileWord temp = map.get(word);
        	//AppendToFile.appendMethodB(fileName+".csv", temp.toString());
        	Word w = new Word(book.getId(), book.getLanguage().toLowerCase(), word, temp.frequency, temp.paragraphIndex, 0, 0);
        	if(knownWords.size() == 0) {
            	if(temp.frequency >= 15) {
            		collection.saveToKnownWords(w);
            	} else {
            		collection.saveToUnknownWords(w);
            	}
        	} else {
        		boolean isKnown = false;
        		if(!book.getLanguage().equalsIgnoreCase(Hunspell.Language)) return;
        		if(Hunspell.Instance(book.getLanguage()).spell(word) != 0) {
	        		for(Word wo:knownWords) {
	        			word = DictionaryParser.getRealWord(word, book.getLanguage());
	        			if(word.equals(wo.getText().toLowerCase())) {
	        				isKnown = true;
	        				break;
	        			}
	        		}
        		}
        		if(!isKnown) {
        			collection.saveToUnknownWords(w);
        		}
        	}
        }
	}

	public static String[] splitGujiYuan(String strYuan) {
    	if(strYuan == null || strYuan.length() == 0) return null;
    	int lastPos = 0;
    	boolean shouldSplit = true;
    	ArrayList<String> list = new ArrayList<String>();
    	for(int i = 0; i< strYuan.length(); i++) {
    		char c = strYuan.charAt(i);
    		if(c == '{') {
    			shouldSplit = false;
    		} else if(c == '}') {
    			shouldSplit = true;
    		} else if(c == '。') {
    			if(shouldSplit) {
    				list.add(strYuan.substring(lastPos, i));
    				lastPos = i+1;
    			}
    		}
    	}
    	if(lastPos <strYuan.length()) {
    		list.add(strYuan.substring(lastPos));
    	}
    	String[] ret = new String[list.size()];
    	list.toArray(ret);
    	return ret;
    }
	public String recompositeGuji(String guji) {
		List<GujiStructure> text = extractComSecFromString(guji);
		String ret = "";
		for(int i = 0; i< text.size(); i++) {
			GujiStructure s = text.get(i);
			if(!TextUtils.isEmpty(s.text)) {
				if(TextUtils.isEmpty(s.tag)) {
					String yuan = extractGujiFromString(s.text);
					String yi = extractTranslationFromString(s.text);
					ret += combineGujiYuanAndTranslation(yuan, yi);
				} else {
					String yuan = extractGujiFromString(s.text);
					String yi = extractTranslationFromString(s.text);
					ret += s.tag + "{" + combineGujiYuanAndTranslation(yuan, yi) + "}";
				}
			}
		}
		return ret + "\n";
	}

	public String combineGujiYuanAndTranslation(String tempStringYuan, String tempStringYi) {
		if(TextUtils.isEmpty(tempStringYuan) && TextUtils.isEmpty(tempStringYi)) {
			return "";
		} else if(TextUtils.isEmpty(tempStringYuan)) {
			return tempStringYi;
		} else if(TextUtils.isEmpty(tempStringYi)) {
			return tempStringYuan;
		}
		if(tempStringYuan.endsWith("\n"))tempStringYuan =tempStringYuan.substring(0, tempStringYuan.length()-1);
		if(tempStringYi.endsWith("\n"))tempStringYi =tempStringYi.substring(0, tempStringYi.length()-1);
		String[] yuan=splitGujiYuan(
				tempStringYuan.replaceAll("<[^>]*>", "").replaceAll("〔[一二三四五六七八九十０]*〕", "")
				.replace("!", "！").replace(",", "，").replace(":", "：")
        		.replace("“", "「").replace("”", "」").replace("‘", "『").replace("’", "』")
				);
        String[] yi=tempStringYi.replaceAll("<[^>]*>", "")
        		.replace("!", "！").replace(",", "，").replace(":", "：")
        		.replace("“", "「").replace("”", "」").replace("‘", "『").replace("’", "』")
        		.split("。");
        int i=0, j=0;
        String result = "";
        for(i=0, j =0; i< yuan.length && j < yi.length; i++,j++) {
        	if((i+1)==yuan.length && !tempStringYuan.endsWith("。")) {
        		result+= yuan[i];
        		if(yuan[i].length() > 0 && !yuan[i].endsWith("？")&&!yuan[i].endsWith("！")&&!yuan[i].endsWith("：")) {
    				result+="。";
    			}
            	if(yi[j].length() > 0) {
            		if(yi[j].startsWith("」")||yi[j].startsWith("』")) {
                		yi[j]=yi[j].substring(1);
                	}
            		result+=  "|{"+yi[j];
            		if(!yi[j].endsWith("？")&&!yi[j].endsWith("！")&&!yi[j].endsWith("：")) {
            			result+="。";
            		}
            		result+="}";
            	}
        	} else if((i+1)<yuan.length && (yuan[i+1].startsWith("」")||yuan[i+1].startsWith("』"))) {
        		if(yuan[i+1].startsWith("」")) {
        			result+=  yuan[i]+"。」";
        			result+=  "|{"+yi[j]+"。」}";
        		} else {
        			result+=  yuan[i]+"。』";
        			result+=  "|{"+yi[j]+"。』}";
        		}
            	
            	if((j+1)<yi.length && (yi[j+1].startsWith("」")||yi[j+1].startsWith("』"))) {
            		yi[j+1]=yi[j+1].substring(1);
            	}
            	yuan[i+1]=yuan[i+1].substring(1);
        	} else {
        		result+=  yuan[i];
        		if(yuan[i].length() > 0 && !yuan[i].endsWith("？")&&!yuan[i].endsWith("！")&&!yuan[i].endsWith("：")) {
    				result+="。";
    			}
        		if(yi[j].startsWith("」")||yi[j].startsWith("』")) {
            		yi[j]=yi[j].substring(1);
            	}
        		result+=  "|{"+yi[j];
        		if(!yi[j].endsWith("？")&&!yi[j].endsWith("！")&&!yi[j].endsWith("：")) {
        			result+="。";
        		}
        		result+="}";
        	}
        }
        
        if(i<yuan.length) {
        	for(;i<yuan.length; i++) {
        			result+=  yuan[i];
        			if(yuan[i].length() > 0 && !yuan[i].endsWith("？")&&!yuan[i].endsWith("！")&&!yuan[i].endsWith("：")) {
        				result+="。";
        			}
        	}
        }
        if(j<yi.length) {
        	for(;j<yi.length; j++) {
        		if(yi[j].startsWith("」")||yi[j].startsWith("』")) {
            		yi[j]=yi[j].substring(1);
            	}
        		result+=  "|{"+yi[j];
        		if(!yi[j].endsWith("？")&&!yi[j].endsWith("！")&&!yi[j].endsWith("：")) {
        			result+="。";
        		}
        		result+="}";
        	}
        }
        
        return result;
	}
	
	class GujiStructure {
		String text;
		String tag;
		GujiStructure(String t, String b) {
			text = t;
			tag = b;
		}
	}
	
	//抽出最外层com, subt里的文本与对应的tag，文本包含原文和注释
	public List<GujiStructure> extractComSecFromString(String text) {
		text = text.trim();
		List<GujiStructure> ret = new ArrayList<GujiStructure>();
		if(text.startsWith("\\section")) {
			ret.add(new GujiStructure(text, ""));
			return ret;
		}
		int pos = text.indexOf("\\com");
		if(pos == -1) {
			pos = text.indexOf("\\sec");
			if(pos == -1) {
				pos = text.indexOf("\\subt");
				if(pos == -1) {
					ret.add(new GujiStructure(text, ""));
					return ret;
				}
			}
		}
		if(pos > 0) {
			ret.add(new GujiStructure(text.substring(0, pos), ""));
		}
		int index = 0;
		String tag="";
		for(int i = pos; i < text.length(); i++) {
			if(text.charAt(i) == '{') {
				if(index == 0) {
					tag = text.substring(pos, i);
					pos = i+1;
				}
				index++;
			} else if(text.charAt(i) == '}') {
				index--;
				if(index == 0) {
					if(pos < i) {
						ret.add(new GujiStructure(text.substring(pos, i), tag));
					}
					pos = i + 1;
					break;
				}
			}
		}
		if(pos < text.length())
		ret.add(new GujiStructure(text.substring(pos), ""));
		return ret;
	}

	public String extractGujiFromString(String text) {
		if(text == null || text.length() == 0) return text;
		return text.replaceAll("\\|\\{[^}]*\\}", "");
	}
	public String extractTranslationFromString(String text) {
		if(text == null || text.length() == 0) return text;
		Pattern p = Pattern.compile("\\|\\{[^}]*\\}");
	    Matcher m = p.matcher(text);
	    String result ="";
	    while(m.find()) {
	    	result+=m.group();
	    }
	    return result.replaceAll("\\|\\{", "").replaceAll("\\}", "");
	}
	public String extractAnnoFromString(String text) {
		if(text == null || text.length() == 0) return text;
		Pattern p = Pattern.compile("\\\\anno\\{[^}]*\\}");
	    Matcher m = p.matcher(text);
	    String result ="";
	    while(m.find()) {
	    	result+=m.group();
	    }
	    return result.replaceAll("\\\\anno\\{", "").replaceAll("\\}", "");
	}
	public final void saveGuji(Book book, int indexToModify, String newString) {
		int	startIndex = 0;
		int	endIndex = myParagraphsNumber;
		int index = startIndex;
		
		try {
			FileOutputStream file = new FileOutputStream(book.getPath(), false);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(file,"utf-8"));
//			out.write("\\title{");
//			out.write(book.getTitle());
//			out.write("}\n");
			final EntryIteratorImpl it = new EntryIteratorImpl(index);
			boolean isChapterEnd = false;
			boolean isTitleEnd = false;
			ArrayList<String> extraLines = new ArrayList<String>();
			String lastString = "";
			String lastYiString = "";
			String replacePre = "";
			String replacePost = "";
			while (true) {
				int offset = 0;
				if(indexToModify == index) {
					if(isTitleEnd) {
						isTitleEnd = false;
						out.write("}\n");
					}
					if(isChapterEnd) {
						isChapterEnd = false;
						out.write("}\n");
					}
					extraLines.clear();
					if(newString.indexOf("|a") > -1) {
						String[] lines = newString.split("\\|a");//将一行截断，并把截断的放入新的一行
						newString = lines[0];
						newString = recompositeGuji(newString);
						if(lines.length > 1)
							for(int i = 1; i < lines.length; i++) {
								newString +=  lines[i];
							}
						if(!newString.endsWith("\n")) {
							newString +="\n";
						}
						out.write(newString);
					} else if(newString.startsWith("|c")) {//保留译文，将原文放后段
						newString = newString.substring(2);
						String yuan = extractGujiFromString(newString);
						String yi = extractTranslationFromString(newString);
						if(yi.length()>0) {
							out.write(yi+"\n");
						}
						lastString = yuan;
					} else if(newString.startsWith("|t")) {//保留原文，将译文放后段
						newString = newString.substring(2);
						String yuan = extractGujiFromString(newString);
						String yi = extractTranslationFromString(newString);
						if(yuan.trim().length()>0) {
							out.write(yuan.trim()+"\n");
						}
						lastYiString = yi;
					} else if(newString.startsWith("|r")) {
						newString = newString.substring(2);
						int pos = newString.indexOf(";;;");
						replacePre = newString.substring(0, pos);
						newString = newString.substring(pos + 3);
						pos = newString.indexOf(";;;");
						replacePost = newString.substring(0, pos);
						newString = newString.substring(pos + 3);
						newString = newString.replaceAll(replacePre, replacePost);
						if(!newString.endsWith("\n")) {
							newString +="\n";
						}
						out.write(newString);
					} else if(newString.startsWith("|f")) {//简体转换成繁体
						newString = newString.substring(2);
						StringBuilder str = new StringBuilder();
						for(int i =0; i< newString.length(); i++) {
							if(newString.charAt(i) == '|' && (i+1) < newString.length() && newString.charAt(i+1) == '{') {
								int pos = newString.indexOf("}", i + 1);
								if(pos > -1) {
									str.append(ChineseConvertor.convertToZht(newString.substring(i, pos + 1)));
									i = pos;
								} else {
									str.append(newString.charAt(i));
								}
							} else if(newString.charAt(i) == '\\' && (i+5) < newString.length() 
									&& newString.substring(i, i+6).equals("\\anno{")){
								int pos = newString.indexOf("}", i + 1);
								if(pos > -1) {
									str.append(ChineseConvertor.convertToZht(newString.substring(i, pos + 1)));
									i=pos;
								} else {
									str.append(newString.charAt(i));
								}
							} else {
								str.append(newString.charAt(i));
							}
						}
//						Pattern p = Pattern.compile("\\\\sub\\{[^}]*\\}");
//					    Matcher m = p.matcher(newString);
//					    int startSubIndex = 0;
//					    int endSubIndex = -1;
//					    StringBuilder str = new StringBuilder();
//					    while(m.find()) {
//					    	String group = m.group();
//					    	endSubIndex = newString.indexOf(group);
//					    	if(startSubIndex != endSubIndex)
//					    	str.append(newString.substring(startSubIndex, endSubIndex));
//					    	str.append(ChineseConvertor.convertToZht(group));
//					    	startSubIndex = endSubIndex + group.length();
//					    	endSubIndex = startSubIndex;
//					    }
//					    if(endSubIndex != -1) {
//					    	str.append(newString.substring(endSubIndex));
//					    	newString = str.toString();
//					    }
						newString = str.toString();
						if(!newString.endsWith("\n")) {
							newString +="\n";
						}
						out.write(newString);
					} else {
						String[] lines = newString.split("\\|n");//从所在处断行，并将所断文本放入下一行并重新composite
						newString = lines[0];
						newString = recompositeGuji(newString);
						
						if(lines.length > 1)
						for(int i = 1; i < lines.length; i++) {
							if(lines[i].endsWith("\n")) lines[i] = lines[i].substring(0, lines[i].length()-1);
							extraLines.add(lines[i]);
						}
						out.write(newString);
					}
				} else { //else for if(indexToModify == index) {
					String extraLine = "";
					if(extraLines.size() > 0) {
						extraLine = extraLines.get(0);
						extraLines.remove(0);
					}
					
					String result="";
					if(myParagraphKinds[index] == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH) {
						if(!TextUtils.isEmpty(lastString)) {
							out.write(lastString+"\n");
							lastString = "";
						}
						if(!TextUtils.isEmpty(lastYiString)) {
							out.write("|{"+lastYiString+"}\n");
							lastYiString = "";
						}
						result += "\\endSection\n";
					} else {
						while (it.next()) {
							if (it.getType() == ZLTextParagraph.Entry.TEXT) {
								{
									char[] textData = it.getTextData();
									int textOffset = it.getTextOffset();
									int textLength = it.getTextLength();
									char[] textDataDst = new char[textLength];
									
									System.arraycopy(textData, textOffset, textDataDst, 0, textLength);
									result+=new String(textDataDst);
									offset += textLength;
								}
							} else if (it.getType() == ZLTextParagraph.Entry.CONTROL) {
								if(it.myControlIsStart) {
									switch(it.myControlKind) {
									case FBTextKind.TITLE:
										result+="\\title{";
										isTitleEnd = true;
										break;
									case FBTextKind.CONTENTS_TABLE_ENTRY:
										if(!TextUtils.isEmpty(lastString)) {
											out.write(lastString+"\n");
											lastString = "";
										}
										if(!TextUtils.isEmpty(lastYiString)) {
											out.write("|{"+lastYiString+"}\n");
											lastYiString = "";
										}
										result += "\\endSection\n";
										break;
									case FBTextKind.GUJI_TRANSLATION:
										result+="|{";
										break;
									case FBTextKind.GUJI_ANNOTATION:
										result+="\\anno{";
										break;
									case FBTextKind.GUJI_COMMENT:
										result+="\\com{";
										break;
									case FBTextKind.GUJI_SUBSCRIPT:
										result+="\\sub{";
										break;
									case FBTextKind.GUJI_SUBTITLE:
										result+="\\subt{";
										break;
									case FBTextKind.GUJI_CR:
										result+="\\cr{";
										break;
									case FBTextKind.GUJI_PARAGRAPHSTART:
										result+="\\ps{";
										break;
									case FBTextKind.GUJI_AUTHOR:
										result+="\\author{";
										break;
									case FBTextKind.GUJI_TITLEANNOTATION:
										result+="\\tanno{";
										break;
									case FBTextKind.GUJI_SECTIONTITLE1:
										result+="\\sec1{";
										break;
									case FBTextKind.GUJI_SECTIONTITLE2:
										result+="\\sec2{";
										break;
									case FBTextKind.GUJI_SECTIONTITLE3:
										result+="\\sec3{";
										break;
									case FBTextKind.GUJI_SECTIONTITLE4:
										result+="\\sec4{";
										break;
									case FBTextKind.GUJI_SUPERSCRIPT:
										result+="\\sup{";
										break;
									case FBTextKind.H1:
										result+="\\h1{";
										break;
									case FBTextKind.H3:
										result+="\\h3{";
										break;
									case FBTextKind.H2:
										result+="\\h2{";
										break;
									case FBTextKind.H4:
										result+="\\h4{";
										break;
									case FBTextKind.GUJI_SECTIONTITLE:
										if(!TextUtils.isEmpty(lastString)) {
											out.write(lastString+"\n");
											lastString = "";
										}
										if(!TextUtils.isEmpty(lastYiString)) {
											out.write("|{"+lastYiString+"}\n");
											lastYiString = "";
										}
										result+="\\section{";
										isChapterEnd = true;
										break;
									}
								} else {
									switch(it.myControlKind) {
									case FBTextKind.GUJI_TRANSLATION:
										result+="}";
										break;
									case FBTextKind.GUJI_ANNOTATION:
										result+="}";
										break;
									case FBTextKind.GUJI_COMMENT:
										result+="}";
										break;
									case FBTextKind.GUJI_SUBSCRIPT:
										result+="}";
										break;
									case FBTextKind.GUJI_SUBTITLE:
										result+="}";
										break;
									case FBTextKind.GUJI_CR:
										result+="}";
										break;
									case FBTextKind.GUJI_PARAGRAPHSTART:
										result+="}";
										break;
									case FBTextKind.GUJI_AUTHOR:
										result+="}";
										break;
									case FBTextKind.GUJI_TITLEANNOTATION:
										result+="}";
										break;
									case FBTextKind.GUJI_SECTIONTITLE1:
										result+="}";
										break;
									case FBTextKind.GUJI_SECTIONTITLE2:
										result+="}";
										break;
									case FBTextKind.GUJI_SECTIONTITLE3:
										result+="}";
										break;
									case FBTextKind.GUJI_SECTIONTITLE4:
										result+="}";
										break;
									case FBTextKind.GUJI_SUPERSCRIPT:
										result+="}";
										break;
									case FBTextKind.H3:
										result+="}";
										break;
									case FBTextKind.H2:
										result+="}";
										break;
									case FBTextKind.H1:
										result+="}";
										break;
									case FBTextKind.H4:
										result+="}";
										break;
									case FBTextKind.GUJI_SECTIONTITLE:
										result+="}\n";
										break;
									case FBTextKind.TITLE:
										result+="}\n";
										break;
									}
									
								}
							}
						}//end while(it.hasnext())
					}
					if(isTitleEnd) {
						isTitleEnd = false;
						if(!result.endsWith("}\n")) {
							result+="}\n";
						}
					}
					if(isChapterEnd) {
						isChapterEnd = false;
						if(!result.endsWith("}\n")) {
							result+="}\n";
						}
					}
					if(!TextUtils.isEmpty(replacePre)) {
						result = result.replaceAll(replacePre, replacePost);
						if(!result.endsWith("\n")) {
							result +="\n";
						}
					} else if(!TextUtils.isEmpty(lastYiString)) {
						String yuan = extractGujiFromString(result);
						String yi = extractTranslationFromString(result);
						result = combineGujiYuanAndTranslation(yuan.trim(), lastYiString) +"\n";
						lastYiString = yi.trim();
					} else if(!TextUtils.isEmpty(lastString)) {
						String yuan = extractGujiFromString(result);
						String yi = extractTranslationFromString(result);
						result = combineGujiYuanAndTranslation(lastString, yi) +"\n";
						lastString = yuan.trim();
					} else {
						result=extraLine+result;
						if(extraLine.length() > 0) {
							String yuan = extractGujiFromString(result);
							String yi = extractTranslationFromString(result);
							result = combineGujiYuanAndTranslation(yuan, yi) + "\n";
						}
					}
					extraLine="";
					
					out.write(result);
					
				}//end if
				if (++index >= endIndex) {
					break;
				}
				it.reset(index);
			}
			out.flush();
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public final int search(final String text, int startIndex, int endIndex, boolean ignoreCase) {
		int count = 0;
		ZLSearchPattern pattern = new ZLSearchPattern(text, ignoreCase);
		myMarks = new ArrayList<ZLTextMark>();
		if (startIndex > myParagraphsNumber) {
			startIndex = myParagraphsNumber;
		}
		if (endIndex > myParagraphsNumber) {
			endIndex = myParagraphsNumber;
		}
		int index = startIndex;
		final EntryIteratorImpl it = new EntryIteratorImpl(index);
		while (true) {
			int offset = 0;
			while (it.next()) {
				if (it.getType() == ZLTextParagraph.Entry.TEXT) {
					char[] textData = it.getTextData();
					int textOffset = it.getTextOffset();
					int textLength = it.getTextLength();
					for (ZLSearchUtil.Result res = ZLSearchUtil.find(textData, textOffset, textLength, pattern); res != null;
						res = ZLSearchUtil.find(textData, textOffset, textLength, pattern, res.Start + 1)) {
						myMarks.add(new ZLTextMark(index, offset + res.Start, res.Length));
						++count;
					}
					offset += textLength;
				}
			}
			if (++index >= endIndex) {
				break;
			}
			it.reset(index);
		}
		return count;
	}

	public final List<ZLTextMark> getMarks() {
		return myMarks != null ? myMarks : Collections.<ZLTextMark>emptyList();
	}

	public final void removeAllMarks() {
		myMarks = null;
	}

	public final int getParagraphsNumber() {
		return myParagraphsNumber;
	}

	public final ZLTextParagraph getParagraph(int index) {
		final byte kind = myParagraphKinds[index];
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index) :
			new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	public final int getTextLength(int index) {
		if (myTextSizes.length == 0) {
			return 0;
		}
		return myTextSizes[Math.max(Math.min(index, myParagraphsNumber - 1), 0)];
	}

	private static int binarySearch(int[] array, int length, int value) {
		int lowIndex = 0;
		int highIndex = length - 1;

		while (lowIndex <= highIndex) {
			int midIndex = (lowIndex + highIndex) >>> 1;
			int midValue = array[midIndex];
			if (midValue > value) {
				highIndex = midIndex - 1;
			} else if (midValue < value) {
				lowIndex = midIndex + 1;
			} else {
				return midIndex;
			}
		}
		return -lowIndex - 1;
	}

	public final int findParagraphByTextLength(int length) {
		int index = binarySearch(myTextSizes, myParagraphsNumber, length);
		if (index >= 0) {
			return index;
		}
		return Math.min(-index - 1, myParagraphsNumber - 1);
	}
}
