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
package org.eclipse.wb.internal.core.model.layout;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;

import java.util.List;

/**
 * Support for removing {@link GeneralLayoutData} when it is not needed anymore.
 *
 * @author scheglov_ke
 * @coverage core.model.layout
 */
public final class GeneralLayoutDataRemover implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new GeneralLayoutDataRemover();

	private GeneralLayoutDataRemover() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
		root.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				clearHierarachy(root);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static void clearHierarachy(JavaInfo root) throws Exception {
		root.accept0(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				GeneralLayoutData.clearForInfo(objectInfo);
			}
		});
	}
}
