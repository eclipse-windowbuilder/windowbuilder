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
package org.eclipse.wb.internal.rcp.gef.policy.jface;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.RequestProcessor;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorInfo;
import org.eclipse.wb.internal.rcp.model.jface.FieldLayoutPreferencePageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.gef.Request;

/**
 * Implementation of {@link RequestProcessor} for dropping {@link FieldEditorInfo} on
 * {@link CompositeInfo}, inside {@link FieldLayoutPreferencePageInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class FieldEditorDropRequestProcessor extends RequestProcessor {
	public static final RequestProcessor INSTANCE = new FieldEditorDropRequestProcessor();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private FieldEditorDropRequestProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// RequestProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Request process(EditPart editPart, Request request) throws Exception {
		FieldLayoutPreferencePageInfo page = getFieldLayoutPreferencePage(editPart);
		if (page != null && request instanceof CreateRequest editorCreateRequest) {
			if (editorCreateRequest.getNewObject() instanceof FieldEditorInfo) {
				final FieldEditorInfo editor = (FieldEditorInfo) editorCreateRequest.getNewObject();
				final CompositeInfo composite = page.schedule_CREATE(editor);
				// after CREATE select "composite"
				editorCreateRequest.setSelectObject(composite);
				// prepare CreateRequest, that creates our ActionInfo
				CreateRequest createRequest = new CreateRequest(new ICreationFactory() {
					@Override
					public void activate() throws Exception {
					}

					@Override
					public Object getNewObject() {
						return composite;
					}
				});
				createRequest.copyStateFrom(editorCreateRequest);
				return createRequest;
			}
		}
		// no, we don't know this request
		return request;
	}

	/**
	 * @return the root {@link FieldLayoutPreferencePageInfo}, or <code>null</code>.
	 */
	private static FieldLayoutPreferencePageInfo getFieldLayoutPreferencePage(EditPart editPart) {
		if (editPart.getModel() instanceof ObjectInfo) {
			ObjectInfo editPartModel = (ObjectInfo) editPart.getModel();
			if (editPartModel.getRoot() instanceof FieldLayoutPreferencePageInfo) {
				return (FieldLayoutPreferencePageInfo) editPartModel.getRoot();
			}
		}
		return null;
	}
}
