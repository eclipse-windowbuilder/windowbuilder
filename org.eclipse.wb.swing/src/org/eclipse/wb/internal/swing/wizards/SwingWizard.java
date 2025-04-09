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
package org.eclipse.wb.internal.swing.wizards;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizard;

import org.eclipse.jface.wizard.Wizard;

import java.util.Set;

/**
 * Abstract {@link Wizard} for Swing toolkit.
 *
 * @author scheglov_ke
 * @coverage swing.wizards.ui
 */
public abstract class SwingWizard extends AbstractDesignWizard {
	@Override
	protected final Set<String> getRequiredModuleNames() {
		return Set.of("java.desktop");
	}
}