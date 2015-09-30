package org.geometerplus.fbreader.book;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import android.os.Parcel;
import android.os.Parcelable;

public class Word extends ZLTextFixedPosition implements Parcelable {
	private long myBookId;
	private String myText;
	private long myFrequency;
	private String myLanguage;
	
	public String getText() {
		return myText;
	}
	
	public String getLanguage() {
		return myLanguage;
	}
	
	public long getFrequency() {
		return myFrequency;
	}
	
	public void modifyText(String newText) {
		myText = newText;
	}
	public long getBookId() {
		return myBookId;
	}
	public void setBookId(long bookid) {
		myBookId = bookid;
	}
	public Word(long bookId, String language, String text, long frequency, long paragraph, long wordIndex, long charIndex) {
		super((int)paragraph, (int)wordIndex, (int)charIndex);
		this.myBookId = bookId;
		this.myText = text;
		this.myFrequency = frequency;
		this.myLanguage = language;
	}
	public Word(Book book, ZLTextPosition position, String text, long frequency) {
		super(position);
		if(book != null) {
			this.myBookId = book.getId();
			this.myLanguage = book.getLanguage().toLowerCase();
		}
		this.myText = text;
		this.myFrequency = frequency;
	}
	
	/*public void saveToKnownWords() {
	BooksDatabase.Instance().insertKnownWord(myText, myLanguage, myFrequency);
}
public void deleteKnownWord() {
	BooksDatabase.Instance().deleteKnownWord(myText, myLanguage);
}

public void updateKnownWord(String newText) {
	BooksDatabase.Instance().updateKnownWord(myText, myLanguage, newText);
}

public void updateUnknownWord(String newText) {
	BooksDatabase.Instance().updateUnknownWord(myBookId, myText, newText);
}

public void deleteUnknownWord() {
	BooksDatabase.Instance().deleteUnknownWord(myBookId, myText);
}
public void saveToUnknownWords() {
	BooksDatabase.Instance().insertUnknownWord(myBookId, myText, myFrequency, myParagraph, myWordIndex, myCharIndex);
}*/

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(myBookId);
		dest.writeString(myLanguage);
		dest.writeString(myText);
		dest.writeLong(myFrequency);
		dest.writeLong(this.getParagraphIndex());
		dest.writeLong(this.getElementIndex());
		dest.writeLong(this.getCharIndex());
		
	}
	
	public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() { 

        @Override 
        public Word createFromParcel(Parcel source) { 
                return new Word(source.readLong(), source.readString(), source.readString(), source.readLong(),
                		source.readLong(), source.readLong(), source.readLong()); 
        } 

        @Override 
        public Word[] newArray(int size) { 
                return new Word[size]; 
        } 

	}; 
}
