/*
Copyright (c) 2011, Carlos Tse <copperoxide@gmail.com>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package jopencc.util;

import static jopencc.util.Util.isMissing;
import static jopencc.util.Util.log;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Dict {

	public static final String	ZHT_TO_ZHS		= "zht2zhs",
								ZHS_TO_ZHT		= "zhs2zht";

	private static final String ZHS_TO_ZHT_PHRASE="data/dictionary/STPhrases.txt";
	private static final String ZHS_TO_ZHT_CHARACTER="data/dictionary/STCharacters.txt";
	private static final String ZHT_TO_ZHS_PHRASE="data/dictionary/TSPhrases.txt";
    private static final String ZHT_TO_ZHS_CHARACTER="data/dictionary/TSCharacters.txt";
    private Context mContext;
	
	public Dict(String config, Context context) {
		super();
		this.config = config;
		init();
		mContext = context;
	}
	
	public Dict(StringBuffer src, String config) {
		super();
		setSrc(src);
		this.config = config;
		init();
	}
	
	public Dict(String src, String config) {
		super();
		setSrc(src);
		this.config = config;
		init();
	}

	private void init() {
		dictPhrase = new LinkedHashMap<String, String>();
		dictChar = new LinkedHashMap<String, String>();
	}
	
	private boolean init;
	private Map<String, String> dictPhrase, dictChar; // static dictionary map
	private StringBuffer src;
	private String config;
	
	/**
	 * initialize dictionary
	 */
	private void initDict() {
		if (init)
			return;
		

		InputStream is = null;
		String p0 = config.equals(ZHT_TO_ZHS)? ZHT_TO_ZHS_PHRASE:ZHS_TO_ZHT_PHRASE;
		String p1 = config.equals(ZHT_TO_ZHS)? ZHT_TO_ZHS_CHARACTER:ZHS_TO_ZHT_CHARACTER;
		try {
            dictPhrase = FileUtil.readDict(mContext.getResources().getAssets().open(p0));
            dictChar = FileUtil.readDict(mContext.getResources().getAssets().open(p1));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		
		if (isMissing(dictPhrase) || isMissing(dictChar)){
			return;
		}
		
		init = true;
	}
	
	/**
	 * Convert the source
	 */
	public void convert() {
		// initialize the map once
		initDict();
		
		// map the phrases
//		log("map phrases...");
//		if (!isMissing(dictPhrase))
//			map(src, dictPhrase, true);
		
		
		// map the characters
//		log("map characters...");
		if (!isMissing(dictChar))
			map(src, dictChar, false);
	}

	/**
	 * Map the source with dictionary
	 * @param src
	 * @param dict
	 */
	private static void map(StringBuffer src, Map<String, String> dict, boolean isPhrase) {
		if(isPhrase) {
			if(src.length() < 10) return;
			String key, value;
			int idx, pos, len;
			Iterator<String> it = dict.keySet().iterator();
			while (it.hasNext()){
				key = it.next();
				pos = 0;
				while ((idx = src.indexOf(key, pos)) > -1){
					value = dict.get(key);
					len = value.length();
					src.replace(idx, idx + len, value);
					pos = idx + len;
				}
			}
			it = null;
		} else {
			for(int i= 0; i < src.length(); i++) {
				String value = dict.get(""+ src.charAt(i));
				if(value != null) {
					src.replace(i, i+1, value);
				}
			}
		}
	}
	
	public void clear() {
		src = null;
	}
	
	public String getResult() {
		return src.toString();
	}

	public void setSrc(StringBuffer src) {
		this.src = src;
	}
	
	public void setSrc(String src) {
		this.src = new StringBuffer(src);
	}
}
