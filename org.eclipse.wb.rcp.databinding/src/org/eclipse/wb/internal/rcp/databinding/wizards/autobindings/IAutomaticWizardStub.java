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
package org.eclipse.wb.internal.rcp.databinding.wizards.autobindings;

import java.util.Collection;

/**
 * @author lobas_av
 * @coverage bindings.rcp.wizard.auto
 */
public interface IAutomaticWizardStub {
	/**
	 * Add bindings additional imports.
	 */
	void addImports(Collection<String> importList);

	/**
	 * @return the source code create observable for given {@code fieldName} and {@code propertyName}.
	 */
	String createSourceCode(String fieldName, String propertyName);
}