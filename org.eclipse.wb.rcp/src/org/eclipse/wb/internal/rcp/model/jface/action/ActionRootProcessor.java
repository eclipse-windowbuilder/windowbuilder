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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.palette.ActionExternalEntryInfo;
import org.eclipse.wb.internal.rcp.palette.ActionNewEntryInfo;

import org.eclipse.jface.action.Action;

import java.util.List;
import java.util.ListIterator;

/**
 * Support that contributes {@link ActionInfo} elements on palette.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.model.jface
 */
public final class ActionRootProcessor implements IRootProcessor {
	/**
	 * Id of palette {@link CategoryInfo} with {@link Action} related entries.
	 */
	public static final String ACTIONS_CATEGORY_ID = "org.eclipse.wb.rcp.jface.actions";
	/**
	 * Id of palette {@link CategoryInfo} with "Menu" related entries.
	 */
	public static final String MENU_CATEGORY_ID = "org.eclipse.wb.rcp.menu";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new ActionRootProcessor();

	private ActionRootProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		processRoot(root);
		processComponents(root, components);
	}

	private void processRoot(final JavaInfo root) {
		if (root.getDescription().getToolkit() == ToolkitProvider.DESCRIPTION) {
			root.addBroadcastListener(new PaletteEventListener() {
				@Override
				public void categories(List<CategoryInfo> categories) throws Exception {
					addActionElements(root, categories);
				}
			});
		}
	}

	private void processComponents(final JavaInfo root, final List<JavaInfo> components)
			throws Exception {
		// bind {@link Action_Info}'s into hierarchy.
		for (JavaInfo javaInfo : components) {
			if (javaInfo instanceof ActionInfo actionInfo) {
				actionInfo.setAssociation(new EmptyAssociation());
				ActionContainerInfo.get(root).addChild(actionInfo);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds {@link ActionInfo} category and entries.
	 */
	private static void addActionElements(JavaInfo root, List<CategoryInfo> categories)
			throws Exception {
		CategoryInfo category = new CategoryInfo();
		category.setId(ACTIONS_CATEGORY_ID);
		category.setName("JFace Actions");
		category.setOpen(true);
		// add standard entries
		if (hasChildOfType(root, ContributionManagerInfo.class)) {
			category.addEntry(new ActionNewEntryInfo(category.getId() + ".newAction"));
			category.addEntry(new ActionExternalEntryInfo(category.getId() + ".externalAction"));
			{
				ComponentEntryInfo separatorEntry = new ComponentEntryInfo();
				separatorEntry.setId(category.getId() + ".separator");
				separatorEntry.setComponentClassName("org.eclipse.jface.action.Separator");
				category.addEntry(separatorEntry);
			}
		}
		// need MenuManager
		if (hasChildOfType(root, MenuManagerInfo.class)) {
			ComponentEntryInfo menuManagerEntry = new ComponentEntryInfo();
			menuManagerEntry.setId(category.getId() + ".menuManager");
			menuManagerEntry.setComponentClassName("org.eclipse.jface.action.MenuManager");
			category.addEntry(menuManagerEntry);
		}
		// need CoolBarManager
		if (hasChildOfType(root, CoolBarManagerInfo.class)) {
			ComponentEntryInfo toolBarManagerEntry = new ComponentEntryInfo();
			toolBarManagerEntry.setId(category.getId() + ".toolBarManager");
			toolBarManagerEntry.setComponentClassName("org.eclipse.jface.action.ToolBarManager");
			category.addEntry(toolBarManagerEntry);
		}
		// add if there are entries
		if (!category.getEntries().isEmpty()) {
			addActionsCategory(categories, category);
		}
	}

	/**
	 * Adds given "JFace Actions" category, if possible - before SWT "Menu" category, if not - at the
	 * end.
	 */
	private static void addActionsCategory(List<CategoryInfo> categories, CategoryInfo category) {
		for (ListIterator<CategoryInfo> I = categories.listIterator(0); I.hasNext();) {
			CategoryInfo existingCategory = I.next();
			if (existingCategory.getId().equals(MENU_CATEGORY_ID)) {
				categories.add(I.previousIndex(), category);
				return;
			}
		}
		categories.add(category);
	}

	/**
	 * @return <code>true</code> if given {@link JavaInfo} has direct/indirect child with given
	 *         {@link Class} of model.
	 */
	private static boolean hasChildOfType(JavaInfo root, final Class<?> clazz) {
		final boolean[] result = new boolean[1];
		root.accept(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				result[0] |= clazz.isAssignableFrom(objectInfo.getClass());
			}
		});
		return result[0];
	}
}
