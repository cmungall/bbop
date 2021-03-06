package org.bbop.framework;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.SecondaryWindow;

import org.apache.log4j.*;

public class HelpManager {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(HelpManager.class);

	protected static HelpManager manager;
	protected HelpBroker helpBroker;
	protected File helpSetFile;

	protected HelpManager() {

	}

	public boolean isEnabled() {
		return helpBroker != null;
	}

	public static HelpManager getManager() {
		if (manager == null) {
			manager = new HelpManager();
		}
		return manager;
	}

	public void setHelpSetFile(File helpSetFile) {
		this.helpSetFile = helpSetFile;
		logger.debug("HelpManager.setHelpSetFile " + helpSetFile); // DEL
		helpBroker = createHelpBroker(helpSetFile);
	}

	public void displayHelp() {
		if (GUIManager.getManager() != null)
			(new CSH.DisplayHelpFromSource(helpBroker))
				.actionPerformed(new ActionEvent(GUIManager.getManager()
								 .getFrame(), 0, "help"));
		else
			new CSH.DisplayHelpFromSource(helpBroker);
	}

	public void displayHelp(String topicID) {
		displayHelp(GUIManager.getManager().getFrame(), topicID);
	}

	public void displayHelp(Component invoker, String topicID) {
		try {
//			Popup popup = (Popup) Popup.getPresentation(
//					helpBroker.getHelpSet(), null);
			SecondaryWindow popup = (SecondaryWindow) SecondaryWindow.getPresentation(
				helpBroker.getHelpSet(), null);
			if (topicID != null)
				popup.setCurrentID(topicID);
//			popup.setInvoker(invoker);
			popup.setDisplayed(true);
		} catch (Exception ee) {
			logger.info("trouble with visiting id; " + ee);
		}
	}

	public HelpBroker getBroker() {
		return helpBroker;
	}

	protected static HelpBroker createHelpBroker(File docsDir) {
		HelpSet hs;
		try {
			hs = new HelpSet(null, docsDir.toURL());
		} catch (Exception ee) {
			return null;
		}
		return hs.createHelpBroker();
	}
}
