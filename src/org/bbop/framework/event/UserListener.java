package org.bbop.framework.event;

import java.util.EventListener;

public interface UserListener extends EventListener {
	public void userEventOccurred(UserEvent e);
	public String getEventType();
}
