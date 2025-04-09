/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;

import java.util.List;

/**
 * Support for binding {@link ButtonGroupInfo}'s to hierarchy.
 *
 * @author sablin_aa
 * @coverage swing.model
 */
public final class ButtonGroupRootProcessor implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new ButtonGroupRootProcessor();

	private ButtonGroupRootProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
		for (JavaInfo javaInfo : components) {
			if (javaInfo instanceof ButtonGroupInfo buttonGroupInfo) {
				buttonGroupInfo.setAssociation(new EmptyAssociation());
				ButtonGroupContainerInfo.get(root).addChild(buttonGroupInfo);
			}
		}
	}
}
