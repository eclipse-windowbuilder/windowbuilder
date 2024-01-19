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

import org.eclipse.wb.core.databinding.xsd.component.TagType;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that adds some tag for
 * {@link ParameterDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ParameterTagRule implements FailableBiConsumer<ParameterDescription, TagType, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void accept(ParameterDescription parameterDescription, TagType tag) throws Exception {
		String name = tag.getName();
		String value = tag.getValue();
		parameterDescription.putTag(name, value);
	}
}
