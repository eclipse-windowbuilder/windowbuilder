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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Support for bind all non-visual beans ({@link JavaInfo}'s without parent) to container.
 *
 * @author lobas_av
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public final class NonVisualBeanRootProcessor implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new NonVisualBeanRootProcessor();

	private NonVisualBeanRootProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
		// check initial root state
		Assert.isNull(NonVisualBeanContainerInfo.find(root));
		// prepare all non-visual beans
		List<JavaInfo> nonVisualBeans = new ArrayList<>();
		for (JavaInfo component : components) {
			if (component.getParent() == null && NonVisualBeanInfo.getNonVisualInfo(component) != null) {
				nonVisualBeans.add(component);
			}
		}
		// check create container
		if (!nonVisualBeans.isEmpty()) {
			NonVisualBeanContainerInfo container = new NonVisualBeanContainerInfo();
			root.addChild(container);
			for (JavaInfo component : nonVisualBeans) {
				component.setAssociation(new NonVisualAssociation());
				container.addChild(component);
			}
		}
	}
}
