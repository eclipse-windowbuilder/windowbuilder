/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.support.AbstractSupport;
import org.eclipse.wb.internal.swt.support.DisplaySupport;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ResourceRegistry;
import org.eclipse.swt.widgets.Display;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * When we create instances of various {@link ResourceRegistry}'s, for example {@link ColorRegistry}
 * , they usually want to be disposed with {@link Display}, so they use
 * {@link Display#disposeExec(Runnable)}. But our {@link Display} instance lives all time while
 * Eclipse lives. So, practically we have memory leak: we keep in memory instance of
 * {@link ResourceRegistry}, its allocated resources, and what is much worse - instance of
 * {@link ClassLoader} that loaded this {@link ResourceRegistry}.
 * <p>
 * So, we need some trick to dispose {@link ResourceRegistry}'s with editor and remove its dispose
 * {@link Runnable} from {@link Display}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swt.model.jface
 */
public final class ResourceRegistryRootProcessor implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		processRoot(root);
		processComponents(root, components);
	}

	private void processRoot(final JavaInfo root) {
		root.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshDispose() throws Exception {
				disposeResourceRegistries(root);
			}

			@Override
			public void dispose() throws Exception {
				disposeResourceRegistries(root);
			}
		});
	}

	private void processComponents(final JavaInfo root, final List<JavaInfo> components)
			throws Exception {
		// bind {@link ResourceRegistryInfo}'s into hierarchy.
		for (JavaInfo javaInfo : components) {
			if (javaInfo instanceof ResourceRegistryInfo resourceRegistryInfo) {
				RegistryContainerInfo.get(root).addChild(resourceRegistryInfo);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Disposes all {@link ResourceRegistry}'s, loaded for given hierarchy.
	 *
	 * @param javaInfo
	 *          the root {@link JavaInfo} of hierarchy.
	 */
	private static void disposeResourceRegistries(JavaInfo javaInfo) throws Exception {
		// SWT utilities require "activeJavaInfo"
		ObjectInfo activeObject = GlobalState.getActiveObject();
		if (!(activeObject instanceof JavaInfo activeJavaInfo)) {
			return;
		}
		try {
			GlobalStateJava.activate(javaInfo);
			if (AbstractSupport.is_SWT()) {
				Object display = DisplaySupport.getDefault();
				Runnable[] disposeList = (Runnable[]) ReflectionUtils.getFieldObject(display, "disposeList");
				if (disposeList != null) {
					// Step 1: Get all listeners called once the display is disposed
					Set<Runnable> disposeRunnables = new HashSet<>();
					for (ResourceRegistryInfo registryInfo : RegistryContainerInfo.getRegistries(javaInfo, ResourceRegistryInfo.class)) {
						Runnable disposeRunnable = registryInfo.getDisposeRunnable();
						if (disposeRunnable != null) {
							// get listener
							disposeRunnables.add(disposeRunnable);
						}
					}
					// Step 2: Manually call the selected listeners and remove them from the display
					for (int i = 0; i < disposeList.length; ++i) {
						Runnable disposeRunnable = disposeList[i];
						if (disposeRunnable != null) {
							if (disposeRunnables.contains(disposeRunnable)) {
								// remove listener
								disposeList[i] = null;
								// clear resources
								disposeRunnable.run();
							}
						}
					}
				}
			}
		} finally {
			GlobalStateJava.activate(activeJavaInfo);
		}
	}
}
