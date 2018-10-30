package com.sheepapps.bookreader.book.bookRenderer;

import com.sheepapps.bookreader.book.common.FixedCharSequence;
import com.sheepapps.bookreader.book.common.FixedStringBuilder;

public class HypenateManager {

	private final static class HypenatePattern {
		public final CharSequence Pattern;
		public final int PatternLength;
		public final int Position;
		
		public HypenatePattern(CharSequence pattern, int position) {
			Pattern = FixedCharSequence.toFixedCharSequence(pattern);
			PatternLength = Pattern.length(); 
			Position = position;
		}
	}
	
	private static final char[] s_charTypes;
	private static final HypenatePattern[] s_patterns = new HypenatePattern[]{
		new HypenatePattern("xgg", 1),
		new HypenatePattern("xgs", 1),
		new HypenatePattern("xsg", 1),
		new HypenatePattern("xss", 1),
		new HypenatePattern("gssssg", 3),
		new HypenatePattern("gsssg", 3),
		new HypenatePattern("sgsg", 2),
		new HypenatePattern("gssg", 2),
		new HypenatePattern("sggg", 2),
		new HypenatePattern("sggs", 2)				
     };
	
	static {
		s_charTypes = new char[100];

		// special letters
		s_charTypes['й' - 0x0410] = 'x';
		s_charTypes['ь' - 0x0410] = 'x';
		s_charTypes['ъ' - 0x0410] = 'x';

		// vowels
		s_charTypes['а' - 0x0410] = 'g';
		s_charTypes['е' - 0x0410] = 'g';
		// Ukraininan
		s_charTypes['и' - 0x0410] = 'g';
		s_charTypes['о' - 0x0410] = 'g';
		s_charTypes['у' - 0x0410] = 'g';
		s_charTypes['ы' - 0x0410] = 'g';
		s_charTypes['э' - 0x0410] = 'g';
		s_charTypes['ю' - 0x0410] = 'g';
		// consonants
		s_charTypes['б' - 0x0410] = 's';
		s_charTypes['в' - 0x0410] = 's';
		s_charTypes['г' - 0x0410] = 's';
		s_charTypes['д' - 0x0410] = 's';
		s_charTypes['ж' - 0x0410] = 's';
		s_charTypes['з' - 0x0410] = 's';
		s_charTypes['к' - 0x0410] = 's';
		s_charTypes['л' - 0x0410] = 's';
		s_charTypes['м' - 0x0410] = 's';
		s_charTypes['н' - 0x0410] = 's';
		s_charTypes['п' - 0x0410] = 's';
		s_charTypes['р' - 0x0410] = 's';
		s_charTypes['с' - 0x0410] = 's';
		s_charTypes['т' - 0x0410] = 's';
		s_charTypes['ф' - 0x0410] = 's';
		s_charTypes['х' - 0x0410] = 's';
		s_charTypes['ц' - 0x0410] = 's';
		s_charTypes['ч' - 0x0410] = 's';
		s_charTypes['ш' - 0x0410] = 's';
		s_charTypes['щ' - 0x0410] = 's';
	}
	
	private static boolean isCyrillic(char c) {
		return c >= 0x0410 && c <= 0x045F;
	}
	
	public static boolean canHypenate(FixedCharSequence word, float maxWidth, float [] widths, float dashWidth, Object [] hypened) {
		int wordLength = word.length();
		
		if (wordLength <= 4)
			return false;
		
		if (!Character.isLetter(word.charAt(0)) || word.charAt(wordLength - 1) == '-')
			return false;		

		float newWidth = dashWidth;
		
		int length = wordLength;
		
		for(int i = 0; i < wordLength; i++) {
			char c = word.charAt(i);
			
			float cWidth = widths[i];
			
			if (!isCyrillic(c)) {
				length = i;
				break;
			}
			
			if (i > 0 && Character.isUpperCase(c)) {
				return false;
			}
			
			if (newWidth + cWidth > maxWidth) {
				length = i;
				break;
			} else
				newWidth += cWidth;
		}
		
		if (length < 2) {
			return false;
		}
		
		length += 2;
		if (length > wordLength)
			length = wordLength;
		
		FixedStringBuilder buffer = new FixedStringBuilder(length);
		
		char first = Character.toLowerCase(word.charAt(0));
		
		if (!isCyrillic(first))
			return false;		
		
		first = s_charTypes[first - 0x0410];
		
		if (first == 0) {
			return false;
		}
		
		buffer.append(first);
		for(int i=1;i<length;i++) {
			char c = word.charAt(i);
			
			if (!isCyrillic(c))
				break;
						
			c = s_charTypes[Character.toLowerCase(word.charAt(i)) - 0x0410];
			if (c == 0)
				return false;
			
			buffer.append(c);			
		}
		
		boolean found = false;
		
		int len = buffer.length();
		
		for(int i = len; i >= 0;i--) {
			for(int j = 0; j < s_patterns.length; j++) {
                if (len - i >= s_patterns[j].PatternLength && len - i - s_patterns[j].Position > 2 && i + s_patterns[j].Position > 2) {
                    if (buffer.equalsPart(i, i + s_patterns[j].PatternLength, s_patterns[j].Pattern, s_patterns[j].PatternLength)) {
                        found = true;
                        length = i + s_patterns[j].Position;
                        break;
                    }
                }
                if (found)
                    break;
            }
		}
		
		if (!found) {
			return false;
		}
		
		FixedStringBuilder builder = new FixedStringBuilder(length + 1);
		builder.append(word.subSequence(0, length));
		builder.append('-');
		
		CharSequence newWord = builder.toCharSequence();
		
		CharSequence restWord = word.subSequence(length, wordLength);
		
		hypened[0] = newWord;
		hypened[1] = restWord;
		return true;
	}
}
