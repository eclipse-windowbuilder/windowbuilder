/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.wizards.actions;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link Action} for displaying all WindowBuilder wizards in single drop-down menu.
 *
 * @author scheglov_ke
 * @coverage core.wizards.ui
 */
public class NewDesignerTypeDropDownAction extends Action
implements
IWorkbenchWindowPulldownDelegate,
IActionDelegate2 {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private Menu m_menu;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NewDesignerTypeDropDownAction() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuCreator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispose() {
		if (m_menu != null) {
			m_menu.dispose();
			m_menu = null;
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		m_menu = null;
		if (m_menu == null) {
			m_menu = new Menu(parent);
			createCategoryItems(m_menu, "org.eclipse.wb");
			disposeExtraSeparators(m_menu);
		}
		return m_menu;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static void createCategoryItems(Menu parent, String parentCategoryId) {
		// process wizards
		getWizardWizards(parentCategoryId).forEach(wizard -> {
			if (isWizardVisibleInMenu(wizard)) {
				IAction action = new OpenTypeWizardAction(wizard);
				ActionContributionItem item = new ActionContributionItem(action);
				item.fill(parent, -1);
			}
		});
		// process sub-categories
		getWizardCategories(parentCategoryId).forEach(category -> {
			boolean isInline = "true".equals(category.getAttribute("wbp-menu-inline"));
			// prepare Menu for category items
			Menu categoryMenu;
			if (isInline) {
				addSeparator(parent);
				categoryMenu = parent;
			} else {
				MenuItem categoryItem = new MenuItem(parent, SWT.CASCADE);
				categoryItem.setText(getCategoryName(category));
				categoryMenu = new Menu(categoryItem);
				categoryItem.setMenu(categoryMenu);
			}
			// process children
			createCategoryItems(categoryMenu, parentCategoryId + "/" + category.getAttribute("id"));
			// add separator after inlined category
			if (isInline) {
				addSeparator(parent);
			}
		});
	}

	private static void addSeparator(Menu parent) {
		new MenuItem(parent, SWT.SEPARATOR);
	}

	private static void disposeExtraSeparators(Menu menu) {
		if (menu == null) {
			return;
		}
		// dispose adjacent separators
		boolean separator = false;
		for (MenuItem item : menu.getItems()) {
			if (isSeparator(item)) {
				if (separator) {
					item.dispose();
				}
				separator = true;
			} else {
				separator = false;
				disposeExtraSeparators(item.getMenu());
			}
		}
		// dispose first separator
		{
			MenuItem[] items = menu.getItems();
			if (items.length != 0) {
				MenuItem firstItem = items[0];
				if (isSeparator(firstItem)) {
					firstItem.dispose();
				}
			}
		}
		// dispose last separator
		{
			MenuItem[] items = menu.getItems();
			if (items.length != 0) {
				MenuItem lastItem = items[items.length - 1];
				if (isSeparator(lastItem)) {
					lastItem.dispose();
				}
			}
		}
	}

	private static boolean isSeparator(MenuItem item) {
		return (item.getStyle() & SWT.SEPARATOR) != 0;
	}

	/**
	 * @return the name of category to display in menu.
	 */
	private static String getCategoryName(IConfigurationElement category) {
		{
			String attribute = category.getAttribute("wbp-menu-name");
			if (attribute != null) {
				return attribute;
			}
		}
		return category.getAttribute("name");
	}

	private static boolean isWizardVisibleInMenu(IConfigurationElement wizard) {
		String visible = wizard.getAttribute("wbp-menu-visible");
		return visible != null ? "true".equals(visible) : true;
	}

	private static Stream<IConfigurationElement> getWizardWizards(String parentCategoryId) {
		return getWizardElements("wizard", "category", parentCategoryId);
	}

	private static Stream<IConfigurationElement> getWizardCategories(String parentCategoryId) {
		return getWizardElements("category", "parentCategory", parentCategoryId);
	}

	private static Stream<IConfigurationElement> getWizardElements(String elementName,
			final String attributeName,
			final String parentCategoryId) {
		List<IConfigurationElement> allCategories =
				ExternalFactoriesHelper.getElements("org.eclipse.ui.newWizards", elementName);
		return allCategories.stream().filter(t -> parentCategoryId.equals(t.getAttribute(attributeName)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IActionDelegate2
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void init(IAction action) {
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		// this action is represented as ToolItem
		Widget widget = event.widget;
		if (widget instanceof ToolItem toolItem) {
			Listener[] listeners = toolItem.getListeners(SWT.Selection);
			if (listeners.length > 0) {
				Listener listener = listeners[0];
				// prepare DropDown click event
				Event e = new Event();
				e.type = SWT.Selection;
				e.widget = toolItem;
				e.detail = SWT.DROP_DOWN;
				e.x = toolItem.getBounds().x;
				e.y = toolItem.getBounds().height;
				// send event to the widget
				listener.handleEvent(e);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IWorkbenchWindowActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void init(IWorkbenchWindow window) {
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}