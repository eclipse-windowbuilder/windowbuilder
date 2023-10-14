/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.core.model.order.MethodOrder;

import java.util.List;

/**
 * Processor class responsible for binding the {@link ResourceManagerInfo} to
 * the root {@link JavaInfo}.
 */
public final class ResourceManagerRootProcessor implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		// bind {@link ResourceManagerInfo}'s into hierarchy.
		for (JavaInfo javaInfo : components) {
			if (javaInfo instanceof ResourceManagerInfo resourceManagerInfo) {
				javaInfo.setAssociation(new EmptyAssociation());
				ManagerContainerInfo.get(root).addChild(resourceManagerInfo);
				MethodOrder methodOrder = new LocalResourceManagerInfo.MethodOrderAfterResourceManager();
				root.getDescription().setDefaultMethodOrder(methodOrder);
			}
		}
	}
}
