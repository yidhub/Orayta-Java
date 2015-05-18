package tester.classTester;

import java.util.ArrayList;

import tester.ITest;
import userSettings.bookSettings.SingleBookSettings;
import userSettings.bookSettings.BookSettingsManager;
import userSettings.bookSettings.BookSettingsMapper;

public class BookSettingsTester implements ITest 
{

	public void Run() 
	{
		BookSettingsManager bsmn = new BookSettingsManager();
		BookSettingsMapper bsmp = bsmn.getSettingsMapper();
		
		for (int i=0; i<4; i++)
		{
			SingleBookSettings bs = new SingleBookSettings();
			
			bs.setShowNikud(i < 2);
			bs.setShowTeamim(i < 2);
			
			ArrayList<Integer> l = new ArrayList<Integer>();
			ArrayList<String> s = new ArrayList<String>();
			for (int j = 0; j<i; j++)
			{
				l.add(j);
				s.add("S" + j);
			}
			
			bs.setWeavedDisplayIDs(l);
			bs.setWeavedDisplayTitles(s);
			
			bsmp.put(i, bs);
		}
		bsmp.put(4, new SingleBookSettings());
		
		bsmn.saveToFile();

		BookSettingsManager bsmn2 = new BookSettingsManager();
		bsmn2.readSettingsFromFile();
		BookSettingsMapper bsm2 = bsmn2.getSettingsMapper();
		
		System.out.println(bsm2.get(2).getWeavedDisplayIDs());
		System.out.println(bsm2.get(2).getWeavedDisplayTitles());
		
	}

}
