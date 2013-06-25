package edu.drexel.psal.anonymouth.gooie;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;


/**
 * @author sadiaafroz
 * @author Marc Barrowclift
 *
 */
public class Translation {
	
	private final static String NAME = "( Translation ) - ";
	private static final String[] RESTART_OPTIONS = {"Cancel", "Restart"};
	private final static int MAXNUMOFTRIES = 5;
	
	private static ArrayList<String> secrets;
	private static ArrayList<String> clients;
	private static ArrayList<String> availability;
	private static int current = 0;
	private static int numAccounts;
	private static int tries = MAXNUMOFTRIES;
	private static int currentMonth;
	private static int currentDay;
	
	private static Language allLangs[] = {Language.ARABIC, Language.BULGARIAN, Language.CATALAN,
			Language.CHINESE_SIMPLIFIED, Language.CHINESE_TRADITIONAL,Language.CZECH,
			Language.DANISH,Language.DUTCH,Language.ESTONIAN,Language.FINNISH,
			Language.FRENCH,Language.GERMAN,Language.GREEK,Language.HAITIAN_CREOLE,
			Language.HEBREW,Language.HINDI,Language.HMONG_DAW,Language.HUNGARIAN,
			Language.INDONESIAN,Language.ITALIAN,Language.JAPANESE,
			Language.KOREAN,Language.LATVIAN,Language.LITHUANIAN,
			Language.NORWEGIAN,Language.POLISH,Language.PORTUGUESE,
			Language.ROMANIAN,Language.RUSSIAN,Language.SLOVAK,
			Language.SLOVENIAN,Language.SPANISH, Language.SWEDISH, 
			Language.THAI, Language.TURKISH, Language.UKRAINIAN, Language.VIETNAMESE};
	
	private static Language usedLangs[] = {Language.ARABIC, Language.CZECH, Language.DANISH,Language.DUTCH,
			Language.FRENCH,Language.GERMAN,Language.GREEK, Language.HUNGARIAN,
			Language.ITALIAN,Language.JAPANESE, Language.KOREAN, Language.POLISH, Language.RUSSIAN,
			Language.SPANISH, Language.VIETNAMESE};
	
	private static HashMap<Language, String> names = new HashMap<Language, String>();
	
	public Translation()
	{
		names.put(allLangs[0], "Arabic");
		names.put(allLangs[1], "Bulgarian");
		names.put(allLangs[2], "Catalan");
		names.put(allLangs[3], "Chinese_Simplified");
		names.put(allLangs[4], "Chinese_Traditional");
		names.put(allLangs[5], "Czech");
		names.put(allLangs[6], "Danish");
		names.put(allLangs[7], "Dutch");
		names.put(allLangs[8], "Estonian");
		names.put(allLangs[9], "Finnish");
		names.put(allLangs[10], "French");
		names.put(allLangs[11], "German");
		names.put(allLangs[12], "Greek");
		names.put(allLangs[13], "Haitian_Creole");
		names.put(allLangs[14], "Hebrew");
		names.put(allLangs[15], "Hindi");
		names.put(allLangs[16], "Hmong_Daw");
		names.put(allLangs[17], "Hungarian");
		names.put(allLangs[18], "Indonesian");
		names.put(allLangs[19], "Italian");
		names.put(allLangs[20], "Japanese");
		names.put(allLangs[21], "Korean");
		names.put(allLangs[22], "Latvian");
		names.put(allLangs[23], "Lithuanian");
		names.put(allLangs[24], "Norwegian");
		names.put(allLangs[25], "Polish");
		names.put(allLangs[26], "Portugese");
		names.put(allLangs[27], "Romanian");
		names.put(allLangs[28], "Russian");
		names.put(allLangs[29], "Slovak");
		names.put(allLangs[30], "Slovenian");
		names.put(allLangs[31], "Spanish");
		names.put(allLangs[32], "Swedish");
		names.put(allLangs[33], "Thai");
		names.put(allLangs[34], "Turkish");
		names.put(allLangs[35], "Ukrainian");
		names.put(allLangs[36], "Vietnamese");
		
		readyAccountsAndSecrets();
	}

	private void readyAccountsAndSecrets() {
		clients = new ArrayList<String>(10);
		clients.add("fyberoptikz");
		clients.add("weegeemounty");
		clients.add("drexel1");
		clients.add("drexel4");
		clients.add("sheetal57");
		clients.add("drexel2");
		clients.add("ozoxdxie");
		clients.add("fiskarkwix");
		clients.add("ewambybambi");
		clients.add("zarcosmarkos");
		
		numAccounts = clients.size();
		
		secrets = new ArrayList<String>(10);
		secrets.add("fAjWBltN4QV+0BKqqqg9nmXVMlo5ffa90gxU6wOW55Q=");
		secrets.add("UR0YCU0x20oOSzqt+xtHkT2lhk6RjcKvqEqd/3Hsdvs=");
		secrets.add("+L2MqaOGTDs4NpMTZyJ5IdBWD6CLFi9iV51NJTXLiYE=");
		secrets.add("F5Hw32MSQoTygwLu6YMpHYx9zV3TQVQxqsIIybVCI1Y=");
		secrets.add("+L2MqaOGTDs4NpMTZyJ5IdBWD6CLFi9iV51NJTXLiYE=");
		secrets.add("KKQWCR7tBFZWA5P6VZzWRWg+5yJ+s1d5+RhcLW6+w3g=");
		secrets.add("wU9ROglnO5qzntfRsxkq7WWGp7LAMrz0jdxPEd0t1u8=");
		secrets.add("tz1OrF0BdiMdowk7CC3ZpkLA0y23baO1EBWphT+GPL0=");
		secrets.add("THQLVzCfATeZmhiA6UOPXc4ml7FaxcBoP3NJIgCgoRs=");
		secrets.add("Xs7OIXhpL/bxr++EUguRAcD8tsuW3wwThas9gHwCa0o=");
		
		Calendar today = Calendar.getInstance();
		currentMonth = today.get(Calendar.MONTH)+1;
		currentDay = today.get(Calendar.DAY_OF_MONTH);
		
		availability = PropertiesUtil.getClientAvailability();
		int availSize = availability.size();
		
		if (availSize == 0 || availability == null || availability.isEmpty())
			return;
		
		for (int i = 0; i < availSize; i++) {
			String account = availability.get(i);
			
			if (!account.equals("ready")) {
				String[] date = account.split("/");
				int month = Integer.parseInt(date[0]);
				int day = Integer.parseInt(date[1]);
				
				if (month - currentMonth != 0 && currentDay > day) {
					availability.set(i, "ready");
				} else {
					clients.remove(i);
					secrets.remove(i);
					numAccounts--;
				}
			}
		}
		
		current = PropertiesUtil.getCurrentClient();
		
		Logger.logln(NAME + availability.toString());
		Logger.logln(NAME + "Current client = " + current);
	}
	
	public static String getTranslation(String original, Language other) {   
		Translate.setClientId(clients.get(current));
		Translate.setClientSecret(secrets.get(current));

		while (tries > 0) {
			try {
				String translatedText = Translate.execute(original, Language.ENGLISH,other);
				String backToEnglish = Translate.execute(translatedText,other,Language.ENGLISH);

				if (backToEnglish.contains("TranslateApiException: The Azure Market Place Translator Subscription associated with the request credentials has zero balance.")) {
					Logger.logln(NAME+"Translations could not be obtained, current account all used. Will now stop.", LogOut.STDERR);

					//Setting the availability to make sure we don't use this client until it's renewed.
					if (availability.size() == 0) {
						for (int i = 0; i < numAccounts; i++) {
							if (i == current)
								availability.add(Integer.toString(currentMonth) + "/" + Integer.toString(currentDay));
							else
								availability.add("ready");
						}
					} else {
						for (int i = 0; i < numAccounts; i++) {
							if (i == current)
								availability.set(i, Integer.toString(currentMonth) + "/" + Integer.toString(currentDay));
							else
								availability.set(i, "ready");
						}
					}
					
					PropertiesUtil.setClientAvailability(availability);

					//Updating the current client so we pick a good one next time
					if (current >= numAccounts)
						current = 0;
					else
						current++;

					PropertiesUtil.setCurrentClient(current);

					//Set the appropriate text in the translations panel as a reminder why there aren't translations there.
					GUIMain.inst.notTranslated.setText("The account used for translations has expired.\n\n" +
							"In order to continue recieving translations, you must restart in order for the " +
							"account change to be reflected.");
					GUIMain.inst.translationsHolderPanel.add(GUIMain.inst.notTranslated, "");
					Translator.accountsUsed = true;

					//Alert the user about what happened and how to handle it
					int answer = JOptionPane.showOptionDialog(null,
							"The account currently being used for translations has now expired.\n" +
									"A new account will now be used in it's place, but Anonymouth must be\n" +
									"restarted for this change to take effect. Restart now?",
									"Translations Account Alert",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE,
									UIManager.getIcon("OptionPane.warningIcon"), RESTART_OPTIONS, 0);

					if (answer == 1) {
						if (GUIMain.saved) {
							System.exit(0);
						}

						if (PropertiesUtil.getAutoSave()) {
							DriverEditor.save(GUIMain.inst);
							System.exit(0);
						}

						if (PropertiesUtil.getWarnQuit()) {
							int restart = JOptionPane.showOptionDialog(null,
									"Are You Sure to Close Application?\nYou will lose all unsaved changes.",
									"Unsaved Changes Warning",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									UIManager.getIcon("OptionPane.warningIcon"), null, null);
							if (restart == JOptionPane.YES_OPTION)
								System.exit(0);
						}
					}

					return "account";
				}

				return backToEnglish; 
			} catch (Exception e) {
				e.printStackTrace();
				Logger.logln(NAME+"Could not load translations (may not be connected to the internet. Will try again.", LogOut.STDOUT);
				tries--;
			}
		}
		
		if (tries <= 0) {
			Logger.logln(NAME+"Translations could not be obtained, no internet connection. Will now stop.", LogOut.STDERR);
			return "internet";
		} else {
			return null;
		}
	}
	
	/**
	 * 2-way translates the given sentence and returns an ArrayList of them
	 * @param original String you want translated
	 * @return 2-way translated sentences for every language available
	 * @author julman
	 */
	public ArrayList<String> getAllTranslations(String original) {
		Translate.setClientId(clients.get(current));
		Translate.setClientSecret(secrets.get(current));

		ArrayList<String> translations = new ArrayList<String>();
		try {  		
			for (Language other:allLangs) {
				String translatedText = Translate.execute(original, Language.ENGLISH,other);
				String backToEnglish = Translate.execute(translatedText,other,Language.ENGLISH);

				translations.add(backToEnglish);
			}

			return translations;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static String getName(Language lang) {
		return names.get(lang);
	}
	
	public static Language[] getAllLangs() {
		return allLangs;
	}
	
	public static Language[] getUsedLangs() {
		return usedLangs;
	}
}