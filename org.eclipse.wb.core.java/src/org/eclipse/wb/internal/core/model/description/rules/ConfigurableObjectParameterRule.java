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
import org.eclipse.wb.internal.core.model.description.internal.AbstractConfigurableDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets value of
 * {@link AbstractConfigurableDescription} parameter.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ConfigurableObjectParameterRule
		implements FailableBiConsumer<AbstractConfigurableDescription, ParameterBaseType.Parameter, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AbstractConfigurableDescription editorDescription, ParameterBaseType.Parameter parameter)
			throws Exception {
		String name = parameter.getName();
		String value = parameter.getValue();
		Assert.isNotNull(value, "Body text for <" + name + "> required.");
		editorDescription.addParameter(name, value);
	}
}
