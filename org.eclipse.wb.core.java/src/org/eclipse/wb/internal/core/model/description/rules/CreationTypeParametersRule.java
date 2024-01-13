/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.core.databinding.xsd.component.TypeParameterType;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that adds type parameter (generic)
 * descriptions for {@link CreationDescription}.
 *
 * @author sablin_aa
 * @coverage core.model.description
 */
public final class CreationTypeParametersRule
		implements FailableBiConsumer<CreationDescription, TypeParameterType, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(CreationDescription creationDescription, TypeParameterType parameterType) throws Exception {
		String name = parameterType.getName();
		String type = parameterType.getType();
		String description = parameterType.getTitle();
		creationDescription.setTypeParameter(name, type, description);
	}
}
