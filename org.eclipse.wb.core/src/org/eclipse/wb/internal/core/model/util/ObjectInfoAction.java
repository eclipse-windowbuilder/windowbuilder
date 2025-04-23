/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract JFace {@link Action} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class ObjectInfoAction extends Action {
	private final ObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObjectInfoAction(ObjectInfo object) {
		this(object, null);
	}

	public ObjectInfoAction(ObjectInfo object, String text) {
		this(object, text, (ImageDescriptor) null);
	}

	public ObjectInfoAction(ObjectInfo object, String text, ImageDescriptor image) {
		this(object, text, image, AS_PUSH_BUTTON);
	}

	public ObjectInfoAction(ObjectInfo object, String text, int style) {
		this(object, text, null, style);
	}

	public ObjectInfoAction(ObjectInfo object, String text, ImageDescriptor image, int style) {
		super(text, style);
		setImageDescriptor(image);
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void run() {
		if (shouldRun()) {
			ExecutionUtils.run(m_object, this::runEx);
		}
	}

	/**
	 * @return <code>true</code> if {@link #runEx()} should be run inside of edit operation.
	 */
	protected boolean shouldRun() {
		if (getStyle() == AS_RADIO_BUTTON) {
			return isChecked();
		}
		return true;
	}

	/**
	 * Executes some {@link ObjectInfo} editing operation.
	 */
	protected abstract void runEx() throws Exception;
}
