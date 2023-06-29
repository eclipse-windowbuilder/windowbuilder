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
package org.eclipse.wb.internal.xwt.model.forms;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Model for {@link ScrolledForm}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.forms
 */
public final class ScrolledFormInfo extends CompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ScrolledFormInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
		setFormText_afterCreate();
		// add "form"
		{
			CompositeInfo form =
					(CompositeInfo) XmlObjectUtils.createObject(
							context,
							Form.class,
							new ExposedPropertyCreationSupport(this, "form"));
			addChild(form);
		}
		// add "body"
		{
			CompositeInfo body =
					(CompositeInfo) XmlObjectUtils.createObject(
							context,
							Composite.class,
							new ExposedPropertyCreationSupport(this, "body"));
			addChild(body);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasts
	//
	////////////////////////////////////////////////////////////////////////////
	private void setFormText_afterCreate() {
		addBroadcastListener(new XmlObjectAdd() {
			@Override
			public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
				if (child == ScrolledFormInfo.this) {
					getChildrenXML().get(0).getPropertyByTitle("text").setValue("New ScrolledForm");
				}
			}
		});
	}
}
