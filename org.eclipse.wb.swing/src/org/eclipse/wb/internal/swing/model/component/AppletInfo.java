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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.osgi.util.NLS;

/**
 * Model for {@link java.applet.Applet Applet} and {@link javax.swing.JApplet
 * JApplet}.
 *
 * @author scheglov_ke
 * @coverage swing.model
 * @deprecated Applets have been removed with Java 26
 *             (https://bugs.openjdk.org/browse/JDK-8359053) This class and
 *             general support for applets will be removed after the 2027-12
 *             release.
 */
@Deprecated(since = "2025-12", forRemoval = true)
public final class AppletInfo extends ContainerInfo implements IJavaInfoRendering {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AppletInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		JavaInfoUtils.scheduleSpecialRendering(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IJavaInfoRendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void render() throws Exception {
		Object applet = getObject();
		ReflectionUtils.invokeMethod(applet, "init()");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public String getText() {
			return NLS.bind(ModelMessages.AppletInfo_deprecated, super.getText());
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return presentation;
	}
}
