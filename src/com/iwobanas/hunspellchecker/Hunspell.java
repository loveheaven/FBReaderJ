package com.iwobanas.hunspellchecker;

import java.util.HashMap;

public class Hunspell {
	private static Hunspell mHunspell;
	private static HashMap<String, Hunspell> ourCache = new HashMap<String, Hunspell>();
	public native void create(String aff, String dic);
	
	// 0 means no such word.
	public native int spell(String word);
	
	public native String[] getSuggestions(String word);
	public native String[] analyze(String word);
	
	static {
        System.loadLibrary("hunspell-jni");
    }
	public static Hunspell Instance(String language) {
        String fileBase = "/sdcard/Dictdata/hunspell/" + language;
        Hunspell hunspell = ourCache.get(language);
        if(hunspell == null) {
        	hunspell = new Hunspell();
        	hunspell.create( fileBase + ".aff", fileBase + ".dic");
        }
        return hunspell;
	}
}
