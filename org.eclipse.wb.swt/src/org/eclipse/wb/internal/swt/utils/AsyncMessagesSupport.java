/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.utils;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import java.util.List;

/**
 * Support for optional running SWT "async" runnables.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swt.utils
 */
public final class AsyncMessagesSupport implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new AsyncMessagesSupport();

	private AsyncMessagesSupport() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		processRoot(root);
	}

	private void processRoot(final JavaInfo rootJavaInfo) {
		rootJavaInfo.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshAfterCreate() throws Exception {
				runAsyncMessagesIfNeeded(rootJavaInfo);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Async messages
	//
	////////////////////////////////////////////////////////////////////////////
	private void runAsyncMessagesIfNeeded(JavaInfo rootJavaInfo) throws Exception {
		if (requiresAsyncMessages(rootJavaInfo)) {
			EditorState editorState = JavaInfoUtils.getState(rootJavaInfo);
			editorState.getBroadcast().getListener(DisplayEventListener.class).beforeMessagesLoop();
			try {
				runAllAsyncMessages(editorState);
			} finally {
				editorState.getBroadcast().getListener(DisplayEventListener.class).afterMessagesLoop();
			}
		}
	}

	private void runAllAsyncMessages(EditorState editorState) throws ClassNotFoundException,
	Exception {
		ClassLoader classLoader = editorState.getEditorLoader();
		Class<?> displayClass = classLoader.loadClass("org.eclipse.swt.widgets.Display");
		Object display = ReflectionUtils.invokeMethod(displayClass, "getDefault()");
		Object synchronizer = ReflectionUtils.getFieldObject(display, "synchronizer");
		ReflectionUtils.invokeMethod(synchronizer, "runAsyncMessages(boolean)", true);
	}

	private static boolean requiresAsyncMessages(JavaInfo javaInfo) {
		for (JavaInfo child : javaInfo.getChildrenJava()) {
			if (requiresAsyncMessages(child)) {
				return true;
			}
		}
		return JavaInfoUtils.hasTrueParameter(javaInfo, "SWT.runAsyncMessages");
	}
}