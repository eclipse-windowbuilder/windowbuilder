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

import org.eclipse.wb.core.databinding.xsd.component.MorphingType.MorphTarget;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that adds {@link MorphingTargetDescription} to
 * {@link ComponentDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MorphingTargetRule implements FailableBiConsumer<ComponentDescription, MorphTarget, Exception> {
	private final EditorState m_state;
	private final ClassLoader m_classLoader;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public MorphingTargetRule(EditorState state) {
		m_state = state;
		m_classLoader = m_state.getEditorLoader();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, MorphTarget morphTarget) throws Exception {
		try {
			addTarget(componentDescription, morphTarget);
		} catch (ClassNotFoundException e) {
		}
	}

	private void addTarget(ComponentDescription componentDescription, MorphTarget morphTarget)
			throws ClassNotFoundException {
		String creationId = morphTarget.getCreationId();
		// prepare class
		Class<?> clazz;
		{
			String className = morphTarget.getClazz();
			Assert.isNotNull(className);
			clazz = m_classLoader.loadClass(className);
		}
		// add morphing target
		componentDescription.addMorphingTarget(new MorphingTargetDescription(clazz, creationId));
	}
}