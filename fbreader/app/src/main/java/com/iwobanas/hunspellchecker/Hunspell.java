package com.iwobanas.hunspellchecker;

public class Hunspell {
	private static Hunspell mHunspell = new Hunspell();
	public static String Language;
	public native void create(String aff, String dic, Hunspell mutex);
	
	// 0 means no such word.
	public native int spell(String word);
	
	public native String[] getSuggestions(String word);
	public native String[] analyze(String word);
	
	static {
        System.loadLibrary("hunspell-jni");
    }
	public static Hunspell Instance(String language) {
		if(language == null) language= Language;
		language = language.toLowerCase();
        String fileBase = "/sdcard/Dictdata/hunspell/" + language;
    	synchronized (mHunspell) {
    		if(Language == null || !Language.equals(language)) {
    			mHunspell.create( fileBase + ".aff", fileBase + ".dic", mHunspell);
    			Language = language;
    		}
		}
        return mHunspell;
	}
}
