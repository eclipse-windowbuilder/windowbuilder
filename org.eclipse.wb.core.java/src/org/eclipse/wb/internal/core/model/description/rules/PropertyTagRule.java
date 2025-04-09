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

import org.eclipse.wb.core.databinding.xsd.component.PropertyConfiguration;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets a tag for current
 * {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyTagRule
		implements FailableBiConsumer<GenericPropertyDescription, PropertyConfiguration.Tag, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(GenericPropertyDescription propertyDescription, PropertyConfiguration.Tag tag) throws Exception {
		String tagName = tag.getName();
		String tagValue = tag.getValue();
		propertyDescription.putTag(tagName, tagValue);
	}
}
