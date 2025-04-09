/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
public final class ConfigurableObjectListParameterRule
		implements FailableBiConsumer<AbstractConfigurableDescription, ParameterBaseType.ParameterList, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void accept(AbstractConfigurableDescription editorDescription, ParameterBaseType.ParameterList parameterList)
			throws Exception {
		String name = parameterList.getName();
		String value = parameterList.getValue();
		Assert.isNotNull(value, "Body text for <" + name + "> required.");
		editorDescription.addListParameter(name, value);
	}
}
