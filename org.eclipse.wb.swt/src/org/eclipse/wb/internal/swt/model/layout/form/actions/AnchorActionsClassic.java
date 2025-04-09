/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swt.model.layout.form.actions;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutUtils;
import org.eclipse.wb.internal.swt.model.layout.form.IFormAttachmentInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Support for actions changing the alignment of the control.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public class AnchorActionsClassic<C extends IControlInfo> {
	private static final String IMAGE_PREFIX = "info/layout/FormLayoutClassic/";
	private final FormLayoutInfoImplClassic<C> m_layoutImpl;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AnchorActionsClassic(FormLayoutInfoImplClassic<C> impl) {
		m_layoutImpl = impl;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////
	public void fillMenuHorizontal(final C widget, final int side, IMenuManager manager) {
		ObjectInfo widgetModel = widget.getUnderlyingModel();
		if (side == PositionConstants.LEFT) {
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToLeftAsOffset, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/left_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.LEFT);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToRightAsOffset, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/right_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.RIGHT);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToLeftAsPercent, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/left_percent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToRightOfControl,
					Activator.getImageDescriptor(IMAGE_PREFIX + "h/menu/left_control.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToControl(widget, side, PositionConstants.RIGHT);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_deleteAttachment, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/left_free.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_delete(widget, side);
				}
			});
		} else if (side == PositionConstants.RIGHT) {
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToRightAsOffset, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/right_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.RIGHT);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToLeftAsOffset, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/left_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.LEFT);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToLeftAsPercent, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/left_percent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToLeftOfControl, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/right_control.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToControl(widget, side, PositionConstants.LEFT);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_deleteAttachment, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "h/menu/right_free.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_delete(widget, side);
				}
			});
		}
	}

	public void fillMenuVertical(final C widget, final int side, IMenuManager manager) {
		ObjectInfo widgetModel = widget.getUnderlyingModel();
		if (side == PositionConstants.TOP) {
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToTopAsOffset, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/top_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.TOP);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToBottomAsOffset,
					Activator.getImageDescriptor(IMAGE_PREFIX + "v/menu/bottom_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.BOTTOM);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToTopAsPercent, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/top_percent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToBottomOfControl,
					Activator.getImageDescriptor(IMAGE_PREFIX + "v/menu/top_control.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToControl(widget, side, PositionConstants.BOTTOM);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_deleteAttachment, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/top_free.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_delete(widget, side);
				}
			});
		} else if (side == PositionConstants.BOTTOM) {
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToBottomAsOffset,
					Activator.getImageDescriptor(IMAGE_PREFIX + "v/menu/bottom_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.BOTTOM);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToTopAsOffset, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/top_parent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParent(widget, side, PositionConstants.TOP);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToTopAsPercent, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/top_percent.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_attachToTopOfControl, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/bottom_control.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_bindToControl(widget, side, PositionConstants.TOP);
				}
			});
			manager.add(new ObjectInfoAction(widgetModel,
					ModelMessages.AnchorActionsClassic_deleteAttachment, Activator.getImageDescriptor(IMAGE_PREFIX
							+ "v/menu/bottom_free.png")) {
				@Override
				protected void runEx() throws Exception {
					m_layoutImpl.anchor_delete(widget, side);
				}
			});
		}
	}

	public ImageDescriptor getImageHorizontal(final C control, final int side) {
		return ExecutionUtils.runObjectLog(() -> getImageHorizontal0(control, side), null);
	}

	private ImageDescriptor getImageHorizontal0(C control, int side) throws Exception {
		IFormAttachmentInfo<C> attachment = m_layoutImpl.getAttachment(control, side);
		String imageName = side == PositionConstants.LEFT ? "left_" : "right_";
		if (attachment == null) {
			imageName += "free";
		} else if (m_layoutImpl.isControlAttachment(attachment)) {
			imageName += "control";
			int targetSide = FormLayoutUtils.convertSwtAlignment(attachment.getAlignment());
			if (targetSide == side) {
				imageName += targetSide == PositionConstants.LEFT ? "_left" : "_right";
			}
		} else if (attachment.getNumerator() == 100) {
			imageName = "right_parent";
		} else if (attachment.getNumerator() == 0) {
			imageName = "left_parent";
		} else {
			imageName = "left_percent";
		}
		return Activator.getImageDescriptor(IMAGE_PREFIX + "/h/" + imageName + ".png");
	}

	public ImageDescriptor getImageVertical(final C control, final int side) {
		return ExecutionUtils.runObjectLog(() -> getImageVertical0(control, side), null);
	}

	private ImageDescriptor getImageVertical0(C control, int side) throws Exception {
		IFormAttachmentInfo<C> attachment = m_layoutImpl.getAttachment(control, side);
		String imageName = side == PositionConstants.TOP ? "top_" : "bottom_";
		if (attachment == null) {
			imageName += "free";
		} else if (m_layoutImpl.isControlAttachment(attachment)) {
			imageName += "control";
			int targetSide = FormLayoutUtils.convertSwtAlignment(attachment.getAlignment());
			if (targetSide == side) {
				imageName += targetSide == PositionConstants.TOP ? "_top" : "_bottom";
			}
		} else if (attachment.getNumerator() == 100) {
			imageName = "bottom_parent";
		} else if (attachment.getNumerator() == 0) {
			imageName = "top_parent";
		} else {
			imageName = "top_percent";
		}
		return Activator.getImageDescriptor(IMAGE_PREFIX + "/v/" + imageName + ".png");
	}
}
