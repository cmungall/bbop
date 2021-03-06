package org.bbop.framework;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.apache.log4j.*;

public class ViewMenus {
//	initialize logger
	protected final static Logger logger = Logger.getLogger(ViewMenus.class);

	public ViewMenus() {
	}

	/** Returns a list of submenus named for FactoryCategories. */
	public List<JMenu> getMenus() {
		Map<GUIComponentFactory.FactoryCategory, List<GUIComponentFactory>> factoryMap = 
			new HashMap<GUIComponentFactory.FactoryCategory, List<GUIComponentFactory>>();
		Collection<GUIComponentFactory<?>> f = ComponentManager
		.getManager().getFactories();
		// Figure out the menu category for each component factory
		for (final GUIComponentFactory factory : f) {
			if (!factory.showInMenus())
				continue;
			List<GUIComponentFactory> factories = factoryMap.get(factory.getCategory());
			if (factories == null) {
				factories = new ArrayList<GUIComponentFactory>();
				factoryMap.put(factory.getCategory(), factories);
			}
			factories.add(factory);
		}
		Comparator<GUIComponentFactory> factoryComparator = new Comparator<GUIComponentFactory>() {
			public int compare(GUIComponentFactory o1,
					GUIComponentFactory o2) {
//				return o1.toString().compareTo(o2.toString());
				return o1.getName().compareTo(o2.getName());
			}
		};

		// Construct submenu for each factory type
		List<JMenu> out = new LinkedList<JMenu>();
		for (GUIComponentFactory.FactoryCategory category : GUIComponentFactory.FactoryCategory.values()) {
			List<GUIComponentFactory> factories = factoryMap.get(category);
			if (factories != null && factories.size() > 0) {
				// Within each submenu, items are sorted alphabetically
				Collections.sort(factories, factoryComparator);
				JMenu subMenu = new JMenu(category.toString());
				subMenu.setName(category.toString());
				for (final GUIComponentFactory factory : factories) {
					JMenuItem item = new JMenuItem(new AbstractAction(factory.getName()) {
						public void actionPerformed(ActionEvent e) {
							ComponentManager.getManager()
							.showComponent(factory, null);
						}
					});
					item.setName(item.getText());
					//logger.debug(category.toString() + " menu: " + item.getText().toString() + " (factory = " + factory.toString() + ")"); // DEL
					subMenu.add(item);
				}
				out.add(subMenu);

			}
		}
		return out;
	}
}
