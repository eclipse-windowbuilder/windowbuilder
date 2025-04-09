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
package org.eclipse.wb.internal.rcp.databinding;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.core.editor.IEditorPageFactory;
import org.eclipse.wb.internal.core.databinding.ui.BindingDesignPage;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Bindings design page factory.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class DesignPageFactory implements IEditorPageFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// IDesignPageFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createPages(IDesignerEditor editor, List<IEditorPage> pages) {
		if (isRCP(editor)) {
			BindingDesignPage.addPage(pages);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isRCP(IDesignerEditor editor) {
		try {
			ICompilationUnit compilationUnit = editor.getCompilationUnit();
			IJavaProject project = compilationUnit.getJavaProject();
			// check "big" SWT
			if (project.findType("org.eclipse.swt.custom.SashForm") != null) {
				IImportDeclaration[] imports = compilationUnit.getImports();
				//
				for (IType type : compilationUnit.getTypes()) {
					String superclassName = type.getSuperclassName();
					// ignore ActionBarAdvisor
					if ("org.eclipse.ui.application.ActionBarAdvisor".equals(superclassName)
							|| "ActionBarAdvisor".equals(superclassName)
							&& isImport(imports, "org.eclipse.ui.application.ActionBarAdvisor")) {
						return false;
					}
					String[] superInterfaceNames = type.getSuperInterfaceNames();
					// ignore perspectives
					if (ArrayUtils.contains(superInterfaceNames, "org.eclipse.ui.IPerspectiveFactory")
							|| ArrayUtils.contains(superInterfaceNames, "IPerspectiveFactory")
							&& isImport(imports, "org.eclipse.ui.IPerspectiveFactory")) {
						return false;
					}
				}
				// check SWT, JFace or Eclipse UI imports
				for (IImportDeclaration importDeclaration : imports) {
					String elementName = importDeclaration.getElementName();
					if (elementName.startsWith("org.eclipse.swt")
							|| elementName.startsWith("org.eclipse.jface")
							|| elementName.startsWith("org.eclipse.ui")) {
						return true;
					}
				}
			}
		} catch (Throwable e) {
		}
		return false;
	}

	private static boolean isImport(IImportDeclaration[] imports, String name) {
		for (IImportDeclaration importDeclaration : imports) {
			String elementName = importDeclaration.getElementName();
			if (name.equals(elementName)) {
				return true;
			}
		}
		return false;
	}
}