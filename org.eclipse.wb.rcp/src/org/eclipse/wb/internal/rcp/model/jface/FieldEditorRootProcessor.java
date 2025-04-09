/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Support that configures palette for using {@link FieldEditorInfo} & binds {@link FieldEditorInfo}
 * to {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.model.jface
 */
public final class FieldEditorRootProcessor implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new FieldEditorRootProcessor();

	private FieldEditorRootProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		// configure palette for using {@link FieldEditor_Info}
		if (root.getDescription().getToolkit() == ToolkitProvider.DESCRIPTION) {
			installPaletteConfigurator(root);
		}
		// bind {@link FieldEditor_Info}'s into hierarchy.
		if (root instanceof FieldLayoutPreferencePageInfo) {
			bindFieldEditors(root, components);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String ID_SYSTEM = "org.eclipse.wb.rcp.system";
	private static final String ID_FIELD_EDITORS = "org.eclipse.wb.rcp.fieldEditors";

	/**
	 * Install listener for configuring palette.
	 */
	private void installPaletteConfigurator(final JavaInfo rootJavaInfo) {
		rootJavaInfo.addBroadcastListener(new PaletteEventListener() {
			@Override
			public void categories(List<CategoryInfo> categories) throws Exception {
				configureFieldEditorElements(rootJavaInfo, categories);
			}
		});
	}

	/**
	 * Configures {@link FieldEditorInfo}'s category and entries.
	 */
	private static void configureFieldEditorElements(final JavaInfo root,
			final List<CategoryInfo> categories) throws Exception {
		if (root instanceof FieldEditorPreferencePageInfo) {
			for (CategoryInfo category : categories) {
				String id = category.getId();
				if (!(id.equals(ID_SYSTEM) || id.equals(ID_FIELD_EDITORS))) {
					category.setVisible(false);
				}
			}
		} else if (root instanceof FieldLayoutPreferencePageInfo) {
			// do nothing
		} else {
			for (CategoryInfo category : categories) {
				if (category.getId().equals(ID_FIELD_EDITORS)) {
					category.setVisible(false);
				}
			}
		}
	}

	/**
	 * On {@link FieldLayoutPreferencePageInfo} binds {@link FieldEditorInfo}'s to their container
	 * {@link CompositeInfo}'s.
	 */
	private void bindFieldEditors(final JavaInfo root, final List<JavaInfo> components)
			throws Exception {
		for (JavaInfo component : components) {
			if (component instanceof FieldEditorInfo fieldEditor
					&& component.getCreationSupport() instanceof ConstructorCreationSupport) {
				ConstructorCreationSupport creationSupport =
						(ConstructorCreationSupport) component.getCreationSupport();
				for (ParameterDescription parameter : creationSupport.getDescription().getParameters()) {
					if (parameter.getType() == Composite.class) {
						// prepare CompositeInfo used in FieldEditor creation
						CompositeInfo compositeInfo;
						{
							ClassInstanceCreation creation = creationSupport.getCreation();
							Expression compositeExpression =
									DomGenerics.arguments(creation).get(parameter.getIndex());
							compositeInfo = (CompositeInfo) root.getChildRepresentedBy(compositeExpression);
						}
						// CompositeInfo that is used as container for FieldEditor - has no layout
						compositeInfo.markNoLayout();
						// bind FieldEditor to CompositeInfo
						compositeInfo.addChild(fieldEditor);
						fieldEditor.setAssociation(new EmptyAssociation());
					}
				}
			}
		}
	}
}
