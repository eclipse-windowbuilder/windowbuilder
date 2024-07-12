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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantListener;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.ComplexAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;
import org.eclipse.wb.internal.swt.gef.policy.layout.AbsoluteBasedLayoutEditPolicySWT;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.LayoutAssistantPage;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.TabFolder;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for edit containers with {@link FormLayout} installed.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy.form
 */
public final class FormLayoutEditPolicy<C extends IControlInfo>
extends
AbsoluteBasedLayoutEditPolicySWT<C> {
	private final IFormLayoutInfo<C> layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutEditPolicy(IFormLayoutInfo<C> layout_) {
		super(layout_);
		this.layout = layout_;
		createPlacementsSupport((IAbsoluteLayoutCommands) layout.getImpl());
		new BroadcastListenerHelper(layout.getUnderlyingModel(), this, new LayoutAssistantListener() {
			@Override
			public void createAssistantPages(List<ObjectInfo> objects,
					TabFolder folder,
					List<ILayoutAssistantPage> pages) throws Exception {
				if (!objects.isEmpty()) {
					for (ObjectInfo object : objects) {
						if (object.getParent() != layout.getComposite()) {
							return;
						}
					}
					LayoutAssistantPage<C> page =
							new LayoutAssistantPage<>(layout, placementsSupport, folder, objects);
					TabFactory.item(folder).text("FormLayout").control(page);
					pages.add(page);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Decorate Child
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(org.eclipse.wb.gef.core.EditPart child) {
		if (layout.getControls().contains(child.getModel())) {
			child.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new FormSelectionEditPolicy<>(layout));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Coordinates
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Dimension getContainerSize() {
		return layout.getContainerSize();
	}

	@Override
	public Point getClientAreaOffset() {
		Point offset = layout.getComposite().getClientArea().getLocation();
		return offset.getTranslated(
				FormUtils.getLayoutMarginLeft(layout),
				FormUtils.getLayoutMarginTop(layout));
	}

	@Override
	protected void translateAbsoluteToModel(Translatable t) {
		super.translateAbsoluteToModel(t);
		t.performTranslate(getClientAreaOffset().getNegated());
	}

	@Override
	protected void translateModelToFeedback(Translatable t) {
		super.translateModelToFeedback(t);
		t.performTranslate(getClientAreaOffset());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void eraseSelectionFeedbacks() {
		super.eraseSelectionFeedbacks();
		for (EditPart child : getHost().getChildren()) {
			if (layout.getControls().contains(child.getModel())) {
				@SuppressWarnings("unchecked")
				FormSelectionEditPolicy<C> editPolicy =
				(FormSelectionEditPolicy<C>) child.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
				editPolicy.hideSelection();
			}
		}
	}

	@Override
	protected void showSelectionFeedbacks() {
		super.showSelectionFeedbacks();
		for (EditPart child : getHost().getChildren()) {
			if (layout.getControls().contains(child.getModel())
					&& child.getSelected() != EditPart.SELECTED_NONE) {
				@SuppressWarnings("unchecked")
				FormSelectionEditPolicy<C> editPolicy =
				(FormSelectionEditPolicy<C>) child.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
				editPolicy.showSelection();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(final CreateRequest request) {
		return new EditCommand(layout) {
			@SuppressWarnings("unchecked")
			@Override
			protected void executeEdit() throws Exception {
				layout.commandCreate((C) request.getNewObject(), null);
				placementsSupport.commitAdd();
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getMoveCommand(final ChangeBoundsRequest request) {
		return new EditCommand(layout) {
			@Override
			protected void executeEdit() throws Exception {
				placementsSupport.commit();
			}
		};
	}

	@Override
	protected Command getAddCommand(ChangeBoundsRequest request) {
		final List<? extends EditPart> editParts = request.getEditParts();
		//
		return new EditCommand(layout) {
			@SuppressWarnings("unchecked")
			@Override
			protected void executeEdit() throws Exception {
				for (EditPart editPart : editParts) {
					layout.commandMove((C) editPart.getModel(), null);
				}
				placementsSupport.commitAdd();
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paste
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
			throws Exception {
		@SuppressWarnings("unchecked")
		C control = (C) pastedWidget.getComponent();
		layout.commandCreate(control, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection Actions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractAlignmentActionsSupport<C> getAlignmentActionsSupport() {
		return new FormLayoutAlignmentActionsSupport<>(layout, placementsSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ToolkitDescription getToolkit() {
		return GlobalState.getToolkit();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Inner classes
	//
	////////////////////////////////////////////////////////////////////////////
	public static final class FormLayoutAlignmentActionsSupport<C extends IControlInfo>
	extends
	ComplexAlignmentActionsSupport<C> {
		private final IFormLayoutInfo<C> m_layout;

		public FormLayoutAlignmentActionsSupport(IFormLayoutInfo<C> layout,
				PlacementsSupport placementsSupport) {
			super(placementsSupport);
			m_layout = layout;
		}

		@Override
		protected boolean isComponentInfo(ObjectInfo object) {
			return object instanceof IControlInfo && m_layout.isManagedObject(object);
		}

		@Override
		protected IAbstractComponentInfo getLayoutContainer() {
			return m_layout.getComposite();
		}
	}
}
