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

import org.xml.sax.Attributes;

/**
 * The {@link FailableBiConsumer} that adds some tag for
 * {@link ParameterDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ParameterTagRule extends AbstractDesignerRule
		implements FailableBiConsumer<ParameterDescription, TagType, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void begin(String namespace, String name, Attributes attributes) throws Exception {
		String tag = getRequiredAttribute(name, attributes, "name");
		String value = getRequiredAttribute(name, attributes, "value");
		ParameterDescription parameterDescription = (ParameterDescription) getDigester().peek();
		parameterDescription.putTag(tag, value);
	}

	@Override
	public void accept(ParameterDescription parameterDescription, TagType tag) throws Exception {
		String name = tag.getName();
		String value = tag.getValue();
		parameterDescription.putTag(name, value);
	}
}
