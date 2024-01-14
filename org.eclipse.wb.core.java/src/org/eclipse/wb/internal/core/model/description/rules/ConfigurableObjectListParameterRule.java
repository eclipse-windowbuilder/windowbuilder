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

import org.eclipse.wb.core.databinding.xsd.component.ParameterBaseType;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;
import org.eclipse.wb.internal.core.model.description.internal.AbstractConfigurableDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.xml.sax.Attributes;

/**
 * The {@link FailableBiConsumer} that sets value of
 * {@link AbstractConfigurableDescription} parameter.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ConfigurableObjectListParameterRule extends AbstractDesignerRule
		implements FailableBiConsumer<AbstractConfigurableDescription, ParameterBaseType.ParameterList, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_name;
	private String m_value;

	@Override
	public void begin(String namespace, String name, Attributes attributes) throws Exception {
		m_name = getRequiredAttribute(name, attributes, "name");
	}

	@Override
	public void body(String namespace, String name, String text) throws Exception {
		m_value = text;
		Assert.isNotNull(m_value, "Body text for <" + name + "> required.");
	}

	@Override
	public void end(String namespace, String name) throws Exception {
		AbstractConfigurableDescription description = (AbstractConfigurableDescription) getDigester().peek();
		description.addListParameter(m_name, m_value);
	}

	@Override
	public void accept(AbstractConfigurableDescription editorDescription, ParameterBaseType.ParameterList parameterList)
			throws Exception {
		String name = parameterList.getName();
		String value = parameterList.getValue();
		Assert.isNotNull(value, "Body text for <" + name + "> required.");
		editorDescription.addListParameter(name, value);
	}
}
