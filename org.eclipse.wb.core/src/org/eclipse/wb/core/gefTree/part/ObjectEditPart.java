/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *     Marcel du Preez - Preference check added to alter the root object name in the TreeView
 *******************************************************************************/
package org.eclipse.wb.core.gefTree.part;

import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.tree.DesignTreeEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.gef.tree.TreeViewer;
import org.eclipse.wb.internal.gef.tree.policies.AutoExpandEditPolicy;
import org.eclipse.wb.internal.gef.tree.policies.SelectionEditPolicy;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link TreeEditPart} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gefTree
 */
public class ObjectEditPart extends DesignTreeEditPart {
	private final ObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObjectEditPart(ObjectInfo object) {
		m_object = object;
		setModel(m_object);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void activate() {
		super.activate();
		if (m_object.isRoot()) {
			final TreeViewer viewer = (TreeViewer) getViewer();
			final Tree tree = viewer.getControl();
			// refresh hierarchy
			m_object.addBroadcastListener(new ObjectEventListener() {
				private List<ObjectInfo> m_delayedSelectionObjects;

				@Override
				public void refreshed() throws Exception {
					// do in setRedraw(false) to avoid flashing after component moving
					tree.setRedraw(false);
					try {
						refresh(ObjectEditPart.this);
						{
							setSelectionIfAllEditParts(m_delayedSelectionObjects);
							m_delayedSelectionObjects = null;
						}
						viewer.setSelectionToTreeWidget();
					} finally {
						tree.setRedraw(true);
					}
				}

				private void refresh(EditPart editPart) {
					editPart.refresh();
					for (EditPart child : editPart.getChildren()) {
						refresh(child);
					}
				}

				@Override
				public void select(List<? extends ObjectInfo> objects) throws Exception {
					m_delayedSelectionObjects = null;
					// set selection now, or delay
					if (!setSelectionIfAllEditParts(objects)) {
						m_delayedSelectionObjects = new ArrayList<>(objects);
					}
				}

				private boolean setSelectionIfAllEditParts(List<? extends ObjectInfo> objects) {
					List<EditPart> editParts = getAllEditParts(objects);
					if (editParts == null) {
						return false;
					}
					viewer.setSelection(new StructuredSelection(editParts));
					return true;
				}

				/**
				 * @return {@link EditPart} for each given model, or <code>null</code> if some model has no
				 *         {@link EditPart}.
				 */
				private List<EditPart> getAllEditParts(List<? extends ObjectInfo> objects) {
					if (objects == null) {
						return null;
					}
					List<EditPart> editParts = new ArrayList<>();
					for (ObjectInfo object : objects) {
						EditPart editPart = viewer.getEditPartRegistry().get(object);
						if (editPart == null) {
							return null;
						}
						editParts.add(editPart);
					}
					return editParts;
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visual
	//
	////////////////////////////////////////////////////////////////////////////
	private ImageDescriptor m_imageDescriptor;

	@Override
	protected final String getText() {
		//Obtain the preference specifying the root object name. If no name is specified then the default is used
		String rootObjectName = InstanceScope.INSTANCE.getNode(IEditorPreferenceConstants.WB_BASIC_UI_PREFERENCE_NODE) //
				.get(IEditorPreferenceConstants.WB_ROOT_OBJ_NAME, null);

		TreeItem treeItem = (TreeItem) getWidget();
		if (treeItem.getParentItem() == null && rootObjectName != null) {
			return rootObjectName;
		}

		return ObjectInfo.getText(m_object);
	}

	@Override
	protected final Image getImage() {
		unregisterVisuals();

		m_imageDescriptor = ObjectInfo.getImageDescriptor(m_object);
		if (m_imageDescriptor != null) {
			return getViewer().getResourceManager().create(m_imageDescriptor);
		}

		return null;
	}

	@Override
	protected void unregisterVisuals() {
		if (m_imageDescriptor != null) {
			getViewer().getResourceManager().destroy(m_imageDescriptor);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new SelectionEditPolicy());
		installEditPolicy(AutoExpandEditPolicy.class, new AutoExpandEditPolicy());
		refreshEditPolicies();
	}

	/**
	 * Installs {@link EditPolicy}'s after model refresh.
	 */
	protected void refreshEditPolicies() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<?> getModelChildren() {
		return ExecutionUtils.runObjectLog(() -> m_object.getPresentation().getChildrenTree(), Collections.emptyList());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh() {
		super.refresh();
		refreshEditPolicies();
	}
}
