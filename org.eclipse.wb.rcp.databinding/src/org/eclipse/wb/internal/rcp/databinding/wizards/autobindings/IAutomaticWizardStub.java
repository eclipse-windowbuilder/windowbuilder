/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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