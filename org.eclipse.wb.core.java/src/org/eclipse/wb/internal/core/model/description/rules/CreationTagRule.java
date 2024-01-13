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
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that adds some tag for
 * {@link CreationDescription}.
 *
 * @author sablin_aa
 * @coverage core.model.description
 */
public final class CreationTagRule implements FailableBiConsumer<CreationDescription, TagType, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(CreationDescription creationDescription, TagType tagType) throws Exception {
		String tag = tagType.getName();
		String value = tagType.getValue();
		creationDescription.putTag(tag, value);
	}
}
