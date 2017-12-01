package innards.sound.au;

import innards.iLaunchable;
import innards.debug.ANSIColorUtils;
import innards.util.InnardsDefaults;


/**
 * 
 * lists all the (audio) units
 * @author marc
 * Created on May 23, 2003
 */
public class ListAU implements iLaunchable
{
	public void launch()
	{
//		boolean openAll= InnardsDefaults.getIntProperty("open", 0) == 1;
//		boolean showAll= InnardsDefaults.getIntProperty("all", 0) == 1;
//
////		ComponentDescription cd= new ComponentDescription();
////		AUComponent component= AUComponent.findAU(cd);
//
//		System.out.println(" -- v1 --");
//		while (component != null)
//		{
//			if ((component.getSubType() == AUConstants.kAudioUnitSubType_Effect) || (component.getType() == AUConstants.kAudioUnitSubType_MusicDevice) || showAll)
//			{
//				System.out.println("---- " + ANSIColorUtils.yellow(component.toString()));
//				if (openAll)
//				{
//					try
//					{
//						new AUV2Wrapper(component.getType(), component.getSubType(), component.getManufacturer()).guessParameter("!!!ZZZ", 0);
//					} catch (Exception e)
//					{
//					}
//				}
//			}
//			component= component.findNextAU(cd);
//		}
//
//		System.out.println(" -- v2 --");
//
//		cd= new ComponentDescription();
//		Component allComponent= AUComponent.find(cd);
//		while (allComponent != null)
//		{
//			if ((allComponent.getType() == AUConstants.kAudioUnitType_Effect) || (allComponent.getType() == AUConstants.kAudioUnitType_MusicDevice)  || showAll)
//			{
//				System.out.println("---- " + ANSIColorUtils.yellow(allComponent.toString()));
//				if (openAll)
//				{
//					try
//					{
//						new AUV2Wrapper(allComponent.getType(), allComponent.getSubType(), allComponent.getManufacturer()).guessParameter("!!!ZZZ", 0);
//					} catch (Exception e)
//					{
//					}
//				}
//			}
//			allComponent= allComponent.findNext(cd);
//		}

	}
}
