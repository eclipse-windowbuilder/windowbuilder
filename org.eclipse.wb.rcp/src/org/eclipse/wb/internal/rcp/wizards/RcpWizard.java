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
package org.eclipse.wb.internal.rcp.wizards;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.AbstractDesignWizard;

import org.eclipse.jface.wizard.Wizard;

import java.util.Set;

/**
 * Abstract {@link Wizard} for RCP toolkit.
 *
 * @author scheglov_ke
 * @coverage rcp.wizards.ui
 */
public abstract class RcpWizard extends AbstractDesignWizard {
	@Override
	protected final Set<String> getRequiredModuleNames() {
		return Set.copyOf(ProjectUtils.getAllPluginLibraries());
	}
}