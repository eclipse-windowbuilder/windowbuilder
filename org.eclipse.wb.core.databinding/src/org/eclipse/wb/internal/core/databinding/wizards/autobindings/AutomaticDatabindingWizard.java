/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.wizards.AbstractDesignWizard;
import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Automatic bindings wizard.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public abstract class AutomaticDatabindingWizard extends AbstractDesignWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public abstract void addPages();

	@Override
	protected final AbstractDesignWizardPage createMainPage() {
		return null;
	}

	@Override
	protected final Set<String> getRequiredModuleNames() {
		return Collections.emptySet();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the fully qualified name of class if given {@link IStructuredSelection} contains .java
	 *         file.
	 */
	protected static String getSelectionBeanClass(IStructuredSelection selection) {
		try {
			// check no selection
			if (UiUtils.isEmpty(selection)) {
				return null;
			}
			// prepare selection object
			Object object = selection.getFirstElement();
			// check java selection
			if (object instanceof IJavaElement element) {
				// find compilation unit
				while (element != null) {
					if (element instanceof ICompilationUnit compilationUnit) {
						IType[] types = compilationUnit.getTypes();
						// find main type
						if (!ArrayUtils.isEmpty(types)) {
							return StringUtils.defaultIfEmpty(types[0].getFullyQualifiedName(), null);
						}
						// wrong selection
						return null;
					}
					// lookup to parent
					element = element.getParent();
				}
			}
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
		// wrong selection
		return null;
	}
}