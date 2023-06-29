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
package org.eclipse.wb.internal.core.xml.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Broadcasts based functionality for {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public final class XmlObjectRootProcessor implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new XmlObjectRootProcessor();

	private XmlObjectRootProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final XmlObjectInfo root) throws Exception {
		root.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void dispose() throws Exception {
				root.onHierarchyDispose();
			}
		});
		// visibility in tree/GEF
		root.addBroadcastListener(new ObjectInfoChildTree() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object instanceof XmlObjectInfo) {
					XmlObjectInfo xObject = (XmlObjectInfo) object;
					String visibilityTreeString = XmlObjectUtils.getParameter(xObject, "visible.inTree");
					if (visibilityTreeString != null) {
						visible[0] = Boolean.parseBoolean(visibilityTreeString);
					} else {
						String visibilityString = XmlObjectUtils.getParameter(xObject, "visible");
						if (visibilityString != null) {
							visible[0] = Boolean.parseBoolean(visibilityString);
						}
					}
				}
			}
		});
		root.addBroadcastListener(new ObjectInfoChildGraphical() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object instanceof XmlObjectInfo) {
					XmlObjectInfo xObject = (XmlObjectInfo) object;
					String visibilityGraphString =
							XmlObjectUtils.getParameter(xObject, "visible.inGraphical");
					if (visibilityGraphString != null) {
						visible[0] = Boolean.parseBoolean(visibilityGraphString);
					} else {
						String visibilityString = XmlObjectUtils.getParameter(xObject, "visible");
						if (visibilityString != null) {
							visible[0] = Boolean.parseBoolean(visibilityString);
						}
					}
				}
			}
		});
		// text decoration
		root.addBroadcastListener(new ObjectInfoPresentationDecorateText() {
			@Override
			public void invoke(ObjectInfo object, String[] text) throws Exception {
				if (object instanceof XmlObjectInfo) {
					XmlObjectInfo xObject = (XmlObjectInfo) object;
					IPreferenceStore preferences = xObject.getDescription().getToolkit().getPreferences();
					if (preferences.getBoolean(IPreferenceConstants.P_GENERAL_TEXT_SUFFIX)) {
						broadcast_presentation_decorateText(xObject, text);
					}
				}
			}
		});
	}

	/**
	 * Adds "text" property prefix to the given presentation text of this {@link ObjectInfo}.
	 */
	private static void broadcast_presentation_decorateText(XmlObjectInfo object, String[] text)
			throws Exception {
		for (Property property : object.getProperties()) {
			if (property instanceof GenericPropertyImpl) {
				GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
				if (genericProperty.hasTrueTag("isText")
						&& genericProperty.getObject() == object
						&& genericProperty.isModified()) {
					String suffix = (String) genericProperty.getValue();
					text[0] = text[0] + " - \"" + suffix + "\"";
					break;
				}
			}
		}
	}
}