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

import org.eclipse.wb.core.databinding.xsd.component.PropertyConfiguration;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;

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
