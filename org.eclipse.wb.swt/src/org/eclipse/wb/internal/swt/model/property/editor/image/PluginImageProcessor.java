/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.image;

import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.IImageInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

/**
 * Default implementation for handling SWT {@link Image}s via the OSGi bundles.
 */
public class PluginImageProcessor extends AbstractPluginImageProcessor {

	@Override
	public boolean postOpen(IGenericProperty property, IImageInfo imageInfo, String[] value) {
		if (getPageId().equals(imageInfo.getPageId())) {
			JavaInfo javaInfo = property.getJavaInfo();
			ExecutionUtils.runRethrow(() -> ManagerUtils.ensure_ResourceManager(javaInfo));
			//
			String[] data = (String[]) imageInfo.getData();
			String symbolicName = StringConverter.INSTANCE.toJavaSource(javaInfo, data[0]);
			String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, data[1]);
			//
			value[0] = "org.eclipse.wb.swt.ResourceManager.getPluginImage(" + symbolicName + ", " + pathSource + ")";
			return true;
		}
		return false;
	}

}
