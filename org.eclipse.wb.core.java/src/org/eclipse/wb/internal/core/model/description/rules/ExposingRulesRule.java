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

import org.eclipse.wb.core.databinding.xsd.component.ExposingRuleType;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ExposingMethodRule;
import org.eclipse.wb.internal.core.model.description.ExposingPackageRule;
import org.eclipse.wb.internal.core.model.description.ExposingRule;

import org.apache.commons.lang3.function.FailableBiConsumer;

import jakarta.xml.bind.JAXBElement;

/**
 * The {@link FailableBiConsumer} that adds include/exclude rules for exposed
 * children.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ExposingRulesRule
		implements FailableBiConsumer<ComponentDescription, JAXBElement<ExposingRuleType>, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, JAXBElement<ExposingRuleType> jaxb) throws Exception {
		// prepare attributes
		ExposingRuleType exposingRule = jaxb.getValue();
		boolean include = "include".equals(jaxb.getName().getLocalPart());
		String packageName = exposingRule.getPackage();
		String methodName = exposingRule.getMethod();
		// add expose rules
		if (packageName != null) {
			ExposingRule rule = new ExposingPackageRule(include, packageName);
			componentDescription.addExposingRule(rule);
		}
		if (methodName != null) {
			ExposingRule rule = new ExposingMethodRule(include, methodName);
			componentDescription.addExposingRule(rule);
		}
	}
}
