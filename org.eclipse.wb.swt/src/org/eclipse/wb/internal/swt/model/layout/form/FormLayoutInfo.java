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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormUtils;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormAttachmentInfo.FormAttachmentClipboardInfo;
import org.eclipse.wb.internal.swt.model.layout.form.actions.PredefinedAnchorsActions;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import java.util.List;

/**
 * SWT {@link FormLayout} model.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class FormLayoutInfo extends LayoutInfo implements IFormLayoutInfo<ControlInfo> {
	private final FormLayoutInfoImpl<ControlInfo> impl;
	private final FormLayoutPreferences<ControlInfo> preferences;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		preferences = new FormLayoutPreferences<>(this, description.getToolkit());
		addBroadcastListeners();
		impl =
				getPreferences().useClassic()
				? new FormLayoutInfoImplClassic<>(this)
						: new FormLayoutInfoImplAutomatic<>(this);
	}

	private void addBroadcastListeners() {
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addContextMenu(List<? extends ObjectInfo> objects,
					ObjectInfo object,
					IMenuManager manager) throws Exception {
				if (isManagedObject(object)) {
					ControlInfo control = (ControlInfo) object;
					contributeControlContextMenu(manager, control);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link FormDataInfo} associated with given {@link ControlInfo}.
	 */
	public static FormDataInfo getFormData(ControlInfo control) {
		return (FormDataInfo) getLayoutData(control);
	}

	@Override
	protected FormData getDefaultVirtualDataObject() throws Exception {
		return new FormData();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	private void contributeControlContextMenu(IMenuManager manager, final ControlInfo control) {
		// order
		{
			List<ControlInfo> controls = getComposite().getChildrenControls();
			new OrderingSupport(controls, control).contributeActions(manager);
		}
		// auto-size
		{
			IAction action =
					new ObjectInfoAction(control, ModelMessages.FormLayoutInfo_autoSize,
							CoreImages.LAYOUT_FIT_TO_SIZE) {
				@Override
				protected void runEx() throws Exception {
					doAutoSize(control);
				}
			};
			manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, action);
		}
		// pre-defined anchors
		{
			IMenuManager predefinedMenuManager =
					new MenuManager(ModelMessages.FormLayoutInfo_quickConstraints);
			manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, predefinedMenuManager);
			new PredefinedAnchorsActions<>(this).contributeActions(
					control,
					predefinedMenuManager);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Autosize
	//
	////////////////////////////////////////////////////////////////////////////
	private void doAutoSize(ControlInfo control) throws Exception {
		// horizontal dimension
		doAutoSize(
				getAttachment(control, PositionConstants.LEFT),
				getAttachment(control, PositionConstants.RIGHT));
		// vertical dimension
		doAutoSize(
				getAttachment(control, PositionConstants.TOP),
				getAttachment(control, PositionConstants.BOTTOM));
		// clear FormData properties
		FormDataInfo formData = getFormData(control);
		formData.getPropertyByTitle("width").setValue(Property.UNKNOWN_VALUE);
		formData.getPropertyByTitle("height").setValue(Property.UNKNOWN_VALUE);
	}

	private void doAutoSize(FormAttachmentInfo leading, FormAttachmentInfo trailing) throws Exception {
		// if either leading or trailing side is not attached, don't need to do anything
		if (!(leading.isVirtual() || trailing.isVirtual())) {
			// if attached to trailing, clear leading attachment
			if (leading.isParentTrailing() || trailing.getControl() != null && trailing.getOffset() <= 0) {
				leading.delete();
			} else {
				trailing.delete();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void commandMove(ControlInfo control, ControlInfo nextControl) throws Exception {
		command_MOVE(control, nextControl);
	}

	@Override
	public void commandCreate(ControlInfo control, ControlInfo nextControl) throws Exception {
		command_CREATE(control, nextControl);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy/Paste
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addCompositeCommands(List<ClipboardCommand> commands)
			throws Exception {
		// first of all, add all control commands
		for (ControlInfo control : getComposite().getChildrenControls()) {
			if (!JavaInfoUtils.isImplicitlyCreated(control)) {
				commands.add(new LayoutClipboardCommand<FormLayoutInfo>(control) {
					private static final long serialVersionUID = 0L;

					@Override
					protected void add(FormLayoutInfo layout, ControlInfo control) throws Exception {
						layout.command_CREATE(control, null);
					}
				});
			}
		}
		// add apply attachments info commands
		List<ControlInfo> childrenControls = getComposite().getChildrenControls();
		for (int i = 0; i < childrenControls.size(); i++) {
			ControlInfo control = childrenControls.get(i);
			if (!JavaInfoUtils.isImplicitlyCreated(control)) {
				FormDataInfo formData = (FormDataInfo) getLayoutData(control);
				final FormAttachmentClipboardInfo left =
						formData.getAttachment(PositionConstants.LEFT).getClipboardInfo();
				final FormAttachmentClipboardInfo right =
						formData.getAttachment(PositionConstants.RIGHT).getClipboardInfo();
				final FormAttachmentClipboardInfo top =
						formData.getAttachment(PositionConstants.TOP).getClipboardInfo();
				final FormAttachmentClipboardInfo bottom =
						formData.getAttachment(PositionConstants.BOTTOM).getClipboardInfo();
				final int index = i;
				commands.add(new ComponentClipboardCommand<CompositeInfo>() {
					private static final long serialVersionUID = 0L;

					@Override
					public void execute(CompositeInfo _thisComposite) throws Exception {
						ControlInfo control = _thisComposite.getChildrenControls().get(index);
						FormDataInfo _thisFormData = (FormDataInfo) getLayoutData(control);
						_thisFormData.getAttachment(PositionConstants.LEFT).applyClipboardInfo(control, left);
						_thisFormData.getAttachment(PositionConstants.RIGHT).applyClipboardInfo(control, right);
						_thisFormData.getAttachment(PositionConstants.TOP).applyClipboardInfo(control, top);
						_thisFormData.getAttachment(PositionConstants.BOTTOM).applyClipboardInfo(
								control,
								bottom);
					}
				});
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Setting new layout
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void onSet() throws Exception {
		List<ControlInfo> controls = getControls();
		Rectangle parentArea = getComposite().getClientArea();
		for (ControlInfo control : controls) {
			Rectangle controlBounds = control.getModelBounds();
			Dimension preferredSize = control.getPreferredSize();
			int x = controlBounds.x - parentArea.x;
			int y = controlBounds.y - parentArea.y;
			int width = controlBounds.width;
			int height = controlBounds.height;
			setAttachmentOffset(control, PositionConstants.LEFT, x);
			setAttachmentOffset(control, PositionConstants.TOP, y);
			if (width != preferredSize.width) {
				setAttachmentOffset(control, PositionConstants.RIGHT, x + width);
			}
			if (height != preferredSize.height) {
				setAttachmentOffset(control, PositionConstants.BOTTOM, y + height);
			}
		}
		// install preference change listener to be able to re-parse on layout mode change (classic or auto)
		preferences.addPropertyChangeListener();
	}

	@Override
	public void setAttachmentOffset(ControlInfo control, int side, int offset) throws Exception {
		FormDataInfo dataInfo = (FormDataInfo) getLayoutData(control);
		FormAttachmentInfo attachment = dataInfo.getAttachment(side);
		attachment.setNumerator(0);
		attachment.setDenominator(100);
		attachment.setOffset(offset);
		attachment.write();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public FormLayoutInfoImpl<ControlInfo> getImpl() {
		return impl;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc/Helpers
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link FormAttachment} instance for given <code>side</code> of widget.
	 */
	private FormAttachmentInfo getAttachment(ControlInfo widget, int side) throws Exception {
		FormDataInfo layoutData = getFormData(widget);
		return layoutData.getAttachment(side);
	}

	/**
	 * @return the size of the container excluding and client area insets.
	 */
	@Override
	public final Dimension getContainerSize() {
		AbstractComponentInfo composite = getComposite();
		Rectangle compositeBounds = composite.getModelBounds().getCopy();
		Insets clientAreaInsets = composite.getClientAreaInsets();
		compositeBounds.shrink(clientAreaInsets);
		Insets marginInsets = FormUtils.getLayoutMargins(this);
		return compositeBounds.shrink(marginInsets).getSize();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Quick Anchors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Anchors control at current place to parent with given sides. If side omitted and if relative,
	 * then anchors the missing side to percent, otherwise deletes attachment.
	 */
	@Override
	public void setQuickAnchors(ControlInfo widget, int sides, boolean relative) throws Exception {
		setQuickAnchor(
				widget,
				PositionConstants.LEFT,
				relative,
				(sides & PositionConstants.LEFT) != 0);
		setQuickAnchor(
				widget,
				PositionConstants.RIGHT,
				relative,
				(sides & PositionConstants.RIGHT) != 0);
		setQuickAnchor(widget, PositionConstants.TOP, relative, (sides & PositionConstants.TOP) != 0);
		setQuickAnchor(
				widget,
				PositionConstants.BOTTOM,
				relative,
				(sides & PositionConstants.BOTTOM) != 0);
	}

	private void setQuickAnchor(ControlInfo widget, int side, boolean relative, boolean hasSide)
			throws Exception {
		if (hasSide) {
			anchorToParent(widget, side, side);
		} else {
			if (relative) {
				anchorToParentAsPercent(widget, side);
			} else {
				FormAttachmentInfo attachment = getAttachment(widget, side);
				if (!attachment.isVirtual()) {
					attachment.delete();
				}
			}
		}
	}

	@Override
	public void anchorToParent(ControlInfo control, int controlSide, int parentSide) throws Exception {
		FormAttachmentInfo attachment = getAttachment(control, controlSide);
		attachment.setControl(null);
		attachment.setDenominator(100);
		boolean isHorizontal = PlacementUtils.isHorizontalSide(controlSide);
		Transposer t = new Transposer(!isHorizontal);
		int margin =
				isHorizontal ? FormUtils.getLayoutMarginLeft(this) : FormUtils.getLayoutMarginTop(this);
		org.eclipse.draw2d.geometry.Rectangle controlBounds = t.t(control.getModelBounds());
		Dimension containerSize = t.t(getContainerSize());
		int offset = 0;
		if (!PlacementUtils.isTrailingSide(controlSide)) {
			if (PlacementUtils.isTrailingSide(parentSide)) {
				attachment.setNumerator(100);
				offset = -(containerSize.width - controlBounds.x) - margin;
			} else {
				attachment.setNumerator(0);
				offset = controlBounds.x - margin;
			}
		} else {
			if (PlacementUtils.isTrailingSide(parentSide)) {
				attachment.setNumerator(100);
				offset = -(containerSize.width - controlBounds.right()) - margin;
			} else {
				attachment.setNumerator(0);
				offset = controlBounds.right() - margin;
			}
		}
		attachment.setOffset(offset);
		attachment.write();
	}

	@Override
	public void anchorToParentAsPercent(ControlInfo control, int controlSide) throws Exception {
		FormAttachmentInfo attachment = getAttachment(control, controlSide);
		attachment.setControl(null);
		attachment.setDenominator(100);
		attachment.setOffset(0);
		boolean isHorizontal = PlacementUtils.isHorizontalSide(controlSide);
		Transposer t = new Transposer(!isHorizontal);
		int margin =
				isHorizontal ? FormUtils.getLayoutMarginLeft(this) : FormUtils.getLayoutMarginTop(this);
		org.eclipse.draw2d.geometry.Rectangle controlBounds = t.t(control.getModelBounds());
		Dimension containerSize = t.t(getContainerSize());
		if (!PlacementUtils.isTrailingSide(controlSide)) {
			attachment.setNumerator((int) (100.0 * (controlBounds.x - margin) / containerSize.width));
		} else {
			attachment.setNumerator((int) (100.0 * (controlBounds.right() - margin) / containerSize.width));
		}
		attachment.write();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public FormLayoutPreferences<ControlInfo> getPreferences() {
		return preferences;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IResource getUnderlyingResource() throws Exception {
		return getEditor().getModelUnit().getUnderlyingResource();
	}
}