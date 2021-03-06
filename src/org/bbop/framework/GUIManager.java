package org.bbop.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.bbop.framework.event.UserEvent;
import org.bbop.framework.event.UserListener;
import org.bbop.swing.BackgroundEventQueue;
import org.bbop.swing.ComponentPath;
import org.bbop.util.MultiHashMap;
import org.bbop.util.MultiMap;
import org.bbop.util.TaskDelegate;

import org.apache.log4j.*;

public class GUIManager {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(GUIManager.class);

	protected static GUIManager manager;

	protected final static String PREFS_DIR_PROPERTY = "prefsdir";

	protected BackgroundEventQueue screenLockQueue;

	protected BackgroundEventQueue backgroundQueue;

	protected JFrame frame;

	protected boolean started = false;

	protected List<GUITask> activeTasks = new ArrayList<GUITask>();

	protected LinkedList<GUITask> startupTasks = new LinkedList<GUITask>();

	protected static List<Runnable> hooks = new LinkedList<Runnable>();

	protected static List<VetoableShutdownListener> shutdownListeners = new ArrayList<VetoableShutdownListener>();

	protected MultiMap<String, UserListener> userListeners = new MultiHashMap<String, UserListener>();

        protected static boolean confirmOnExit = true; // But note that it now defaults to true in oboedit.Preferences
	
	protected static boolean advxpMatrixEditorCheckBox = false;

	protected static boolean advIntersectionEditorCheckBox = false;

	protected static boolean advSemanticParserCheckBox = false;
	

	protected static File prefsDir;
	
	//Layout lock
	protected boolean lockDoc;

	// OTE View/Scroll Lock
	protected boolean OTElock;
	
	public BackgroundEventQueue getScreenLockQueue() {
		return screenLockQueue;
	}

	public BackgroundEventQueue getBackgroundQueue() {
		return backgroundQueue;
	}

	public static void addVetoableShutdownListener(
			VetoableShutdownListener listener) {
		shutdownListeners.add(listener);
	}

	public static void removeVetoableShutdownListener(
			VetoableShutdownListener listener) {
		shutdownListeners.remove(listener);
	}

	public void runTaskNow(TaskDelegate<?> task, boolean lockScreen) {
		getQueue(lockScreen).runTaskNow(task);
	}

	public void scheduleTask(TaskDelegate<?> task, boolean lockScreen) {
		getQueue(lockScreen).scheduleTask(task);
	}

	protected BackgroundEventQueue getQueue(boolean lockScreen) {
		if (lockScreen)
			return screenLockQueue;
		else
			return backgroundQueue;
	}

	public void scheduleTasks(boolean lockScreen, TaskDelegate<?>... tasks) {
		getQueue(lockScreen).scheduleTasks(tasks);
	}

	public void scheduleDependentTasks(boolean lockScreen,
			TaskDelegate<?>... tasks) {
		getQueue(lockScreen).scheduleDependentTasks(tasks);
	}

	public GUIManager() {
		screenLockQueue = BackgroundEventQueue.getGlobalQueue();
		backgroundQueue = new BackgroundEventQueue();
		lockDoc = false;
		OTElock = false;
	}

	public static GUIManager getManager() {
		if (manager == null) {
			manager = new GUIManager();
		}
		return manager;
	}

	public static void setManager(GUIManager manager) {
		GUIManager.manager = manager;
	}

	public void addUserListener(UserListener listener) {
		userListeners.add(listener.getEventType(), listener);
	}

	public void removeUserListener(UserListener listener) {
		userListeners.remove(listener.getEventType(), listener);
	}

	public void fireUserEvent(UserEvent event) {
		Collection<UserListener> listeners = userListeners.get(event.getType());
		for (UserListener listener : listeners) {
			listener.userEventOccurred(event);
		}
	}

	public void addStartupTask(GUITask task) {
		if (started)
			throw new IllegalStateException(
			"Cannot add a new startup task once the gui manager has started");
		startupTasks.add(task);
	}

	public void removeStartupTask(GUITask task) {
		if (started)
			throw new IllegalStateException(
			"Cannot remove a startup task once the gui manager has started");
		startupTasks.remove(task);
	}

	protected GUITask popTask() {
		return startupTasks.removeFirst();
	}

	public void installTask(GUITask task) {
		activeTasks.add(task);
		task.install();
	}

	public void notifyComplete(GUITask task) {
		task.shutdown();
		activeTasks.remove(task);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public boolean installMenuItem(String path, JMenuItem item) {
		return ComponentPath.addComponent(path, getFrame().getJMenuBar(), item);
	}

	public boolean uninstallMenuItem(String path, JMenuItem item) {
		return ComponentPath.removeComponent(path, getFrame().getJMenuBar(),
				item);
	}

	public void setEnabledMenuItem(String path, boolean enabled) {
		if (path!= null){
			ComponentPath.getComponent(path, getFrame().getJMenuBar()).setEnabled(enabled);
		}
		else
			logger.error("GUIManager.setEnabledMenuItem failed - null path");
	}

	public boolean installToolBar(JToolBar toolbar) {
		return ComponentPath.addComponent(null, getFrame(), toolbar);
	}

	public void setDocLockStatus(boolean lock) {
		lockDoc = lock;
		if (lockDoc)
			ComponentManager.getManager().getDriver().lockDockingPanels();
		else
			ComponentManager.getManager().getDriver().unlockDockingPanels();
		return;
	}

	public boolean getDocLockStatus() {
		return lockDoc;
	}
	
	public void setOTELockStatus(boolean lock) {
		OTElock = lock;
		if (OTElock)
			ComponentManager.getManager().getDriver().lockOTE();
		else
			ComponentManager.getManager().getDriver().unlockOTE();
		return;
	}

	public boolean getOTELockStatus(){
		return OTElock;
	}

	public void start() {
		addShutdownHook(new Runnable() {

			public void run() {
				shutdown();
			}
		});

		while (startupTasks.size() > 0) {
			GUITask startupTask = popTask();
			installTask(startupTask);
		}
		started = true;
	}

	public void shutdown() {
		List<GUITask> taskList = new LinkedList<GUITask>(activeTasks);
		for (GUITask task : taskList) {
			task.shutdown();
		}
		started = false;
	}

	public static final File readPrefsDir() {
		File prefsDir = null;
		if (System.getProperty(PREFS_DIR_PROPERTY) != null) {
			prefsDir = new File(System.getProperty(PREFS_DIR_PROPERTY));
			boolean worked = true;
			if (!prefsDir.exists())
				worked = prefsDir.mkdirs();
			if (!worked)
				prefsDir = null;
		}
		if (prefsDir == null) {
			prefsDir = new File(System.getProperty("user.home")
					+ "/.bbopframework");
			if (!prefsDir.exists())
				prefsDir.mkdirs();
		}

		return prefsDir;
	}

	public static File getPrefsDir() {
		if (prefsDir == null) {
			readPrefsDir();
		}
		return prefsDir;
	}

	public static void setPrefsDir(File dir) {
		prefsDir = dir;
		boolean worked = true;
		if (!prefsDir.exists())
			worked = prefsDir.mkdirs();
		if (!worked)
			prefsDir = null;
	}

	public static void addShutdownHook(Runnable r) {
		hooks.add(r);
	}

	public static void removeShutdownHook(Runnable r) {
		hooks.remove(r);
	}

	public static void exit(final int status) {

		for (VetoableShutdownListener listener : shutdownListeners) {
			if (!listener.willShutdown())
				return;
		}

		for (Runnable r : hooks) {
			SwingUtilities.invokeLater(r);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				logger.info("Exiting.\n");
				System.exit(status);
			}
		});

	}

	public static boolean isConfirmOnExit() {
		return confirmOnExit;
	}

	public static void setConfirmOnExit(boolean confirmOnExit) {
		GUIManager.confirmOnExit = confirmOnExit;
	}
	
	public static boolean advxpMatrixEditorCheckBox() {
		return advxpMatrixEditorCheckBox;
	}

	public static void setadvxpMatrixEditorCheckBox(boolean advxpMatrixEditorCheckBox) {
		GUIManager.advxpMatrixEditorCheckBox = advxpMatrixEditorCheckBox;
	}

	public static boolean advIntersectionEditorCheckBox() {
		return advIntersectionEditorCheckBox;
	}

	public static void setadvIntersectionEditorCheckBox(boolean advIntersectionEditorCheckBox) {
		GUIManager.advIntersectionEditorCheckBox =  advIntersectionEditorCheckBox;
	}
	
	public static boolean  advSemanticParserCheckBox() {
		return advSemanticParserCheckBox;
	}

	public static void setadvSemanticParserCheckBox (boolean advSemanticParserCheckBox) {
		GUIManager.advSemanticParserCheckBox = advSemanticParserCheckBox ;
	}

}
