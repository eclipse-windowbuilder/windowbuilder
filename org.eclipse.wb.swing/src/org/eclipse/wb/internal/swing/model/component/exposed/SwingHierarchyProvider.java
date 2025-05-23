/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.component.exposed;

import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;

import org.apache.commons.lang3.ArrayUtils;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Implementation of {@link HierarchyProvider} for Swing components.
 *
 * @author mitin_aa
 * @coverage swing.model
 */
public final class SwingHierarchyProvider extends HierarchyProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// HierarchyProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getParentObject(Object object) throws Exception {
		if (object instanceof JMenuItem) {
			return getJMenu((JMenuItem) object);
		}
		if (object instanceof Component component) {
			return component.getParent();
		}
		return null;
	}

	@Override
	public Object[] getChildrenObjects(Object object) throws Exception {
		// javax.swing.JMenu
		if (object instanceof JMenu menu) {
			int componentCount = menu.getMenuComponentCount();
			Component[] menuComponents = new Component[componentCount];
			for (int i = 0; i < componentCount; i++) {
				menuComponents[i] = menu.getMenuComponent(i);
			}
			return menuComponents;
		}
		// generic java.awt.Container
		if (object instanceof Container container) {
			return container.getComponents();
		}
		// unknown
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * There are no good way to find {@link JMenu} parent of {@link JMenuItem}, see
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4103931
	 */
	private static Component getJMenu(JMenuItem item) {
		if (item.getParent() instanceof JPopupMenu) {
			JPopupMenu popup = (JPopupMenu) item.getParent();
			return popup.getInvoker();
		}
		return item.getParent();
	}
}