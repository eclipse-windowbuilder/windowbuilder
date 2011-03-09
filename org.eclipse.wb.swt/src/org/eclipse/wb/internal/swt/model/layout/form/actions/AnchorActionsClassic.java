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
package org.eclipse.wb.internal.swt.model.layout.form.actions;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutUtils;
import org.eclipse.wb.internal.swt.model.layout.form.IFormAttachmentInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

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
    if (side == IPositionConstants.LEFT) {
      manager.add(new ObjectInfoAction(widgetModel, "Attach to left as offset",
          Activator.getImage(IMAGE_PREFIX + "h/menu/left_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.LEFT);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to right as offset",
          Activator.getImage(IMAGE_PREFIX + "h/menu/right_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.RIGHT);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to left as percentage offset",
          Activator.getImage(IMAGE_PREFIX + "h/menu/left_percent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to right of the control",
          Activator.getImage(IMAGE_PREFIX + "h/menu/left_control.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToControl(widget, side, IPositionConstants.RIGHT);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Delete attachment",
          Activator.getImage(IMAGE_PREFIX + "h/menu/left_free.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_delete(widget, side);
        }
      });
    } else if (side == IPositionConstants.RIGHT) {
      manager.add(new ObjectInfoAction(widgetModel, "Attach to right as offset",
          Activator.getImage(IMAGE_PREFIX + "h/menu/right_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.RIGHT);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to left as offset",
          Activator.getImage(IMAGE_PREFIX + "h/menu/left_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.LEFT);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to left as percentage offset",
          Activator.getImage(IMAGE_PREFIX + "h/menu/left_percent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to left of the control",
          Activator.getImage(IMAGE_PREFIX + "h/menu/right_control.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToControl(widget, side, IPositionConstants.LEFT);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Delete attachment",
          Activator.getImage(IMAGE_PREFIX + "h/menu/right_free.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_delete(widget, side);
        }
      });
    }
  }

  public void fillMenuVertical(final C widget, final int side, IMenuManager manager) {
    ObjectInfo widgetModel = widget.getUnderlyingModel();
    if (side == IPositionConstants.TOP) {
      manager.add(new ObjectInfoAction(widgetModel, "Attach to top as offset",
          Activator.getImage(IMAGE_PREFIX + "v/menu/top_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.TOP);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to bottom as offset",
          Activator.getImage(IMAGE_PREFIX + "v/menu/bottom_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.BOTTOM);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to top as percentage offset",
          Activator.getImage(IMAGE_PREFIX + "v/menu/top_percent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to bottom of the control",
          Activator.getImage(IMAGE_PREFIX + "v/menu/top_control.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToControl(widget, side, IPositionConstants.BOTTOM);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Delete attachment",
          Activator.getImage(IMAGE_PREFIX + "v/menu/top_free.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_delete(widget, side);
        }
      });
    } else if (side == IPositionConstants.BOTTOM) {
      manager.add(new ObjectInfoAction(widgetModel, "Attach to bottom as offset",
          Activator.getImage(IMAGE_PREFIX + "v/menu/bottom_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.BOTTOM);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to top as offset",
          Activator.getImage(IMAGE_PREFIX + "v/menu/top_parent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParent(widget, side, IPositionConstants.TOP);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to top as percentage offset",
          Activator.getImage(IMAGE_PREFIX + "v/menu/top_percent.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToParentAsPercent(widget, side);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Attach to top of the control",
          Activator.getImage(IMAGE_PREFIX + "v/menu/bottom_control.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_bindToControl(widget, side, IPositionConstants.TOP);
        }
      });
      manager.add(new ObjectInfoAction(widgetModel, "Delete attachment",
          Activator.getImage(IMAGE_PREFIX + "v/menu/bottom_free.png")) {
        @Override
        protected void runEx() throws Exception {
          m_layoutImpl.anchor_delete(widget, side);
        }
      });
    }
  }

  public Image getImageHorizontal(final C control, final int side) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        return getImageHorizontal0(control, side);
      }
    }, null);
  }

  private Image getImageHorizontal0(C control, int side) throws Exception {
    IFormAttachmentInfo<C> attachment = m_layoutImpl.getAttachment(control, side);
    String imageName = side == IPositionConstants.LEFT ? "left_" : "right_";
    if (attachment == null) {
      imageName += "free";
    } else if (m_layoutImpl.isControlAttachment(attachment)) {
      imageName += "control";
      int targetSide = FormLayoutUtils.convertSwtAlignment(attachment.getAlignment());
      if (targetSide == side) {
        imageName += targetSide == IPositionConstants.LEFT ? "_left" : "_right";
      }
    } else if (attachment.getNumerator() == 100) {
      imageName = "right_parent";
    } else if (attachment.getNumerator() == 0) {
      imageName = "left_parent";
    } else {
      imageName = "left_percent";
    }
    return Activator.getImage(IMAGE_PREFIX + "/h/" + imageName + ".png");
  }

  public Image getImageVertical(final C control, final int side) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        return getImageVertical0(control, side);
      }
    }, null);
  }

  private Image getImageVertical0(C control, int side) throws Exception {
    IFormAttachmentInfo<C> attachment = m_layoutImpl.getAttachment(control, side);
    String imageName = side == IPositionConstants.TOP ? "top_" : "bottom_";
    if (attachment == null) {
      imageName += "free";
    } else if (m_layoutImpl.isControlAttachment(attachment)) {
      imageName += "control";
      int targetSide = FormLayoutUtils.convertSwtAlignment(attachment.getAlignment());
      if (targetSide == side) {
        imageName += targetSide == IPositionConstants.TOP ? "_top" : "_bottom";
      }
    } else if (attachment.getNumerator() == 100) {
      imageName = "bottom_parent";
    } else if (attachment.getNumerator() == 0) {
      imageName = "top_parent";
    } else {
      imageName = "top_percent";
    }
    return Activator.getImage(IMAGE_PREFIX + "/v/" + imageName + ".png");
  }
}
