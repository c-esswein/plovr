package org.plovr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LanguageConfig {

	private List<Language> languageSettings = new ArrayList<Language>();
	
	public static class Language{
		public String langKey;
		public File translationFile;
		
		public Language(String langKey, File translationFile){
			this.langKey = langKey;
			this.translationFile = translationFile;
		}
	}
	
	public void addLanguage(Language lang){
		languageSettings.add(lang);
	}
	
	public List<Language> getLanguages(){
		return languageSettings;
	}
}
