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
package org.eclipse.wb.internal.xwt.model.layout.form;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormUtils;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImpl;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutPreferences;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.actions.PredefinedAnchorsActions;
import org.eclipse.wb.internal.xwt.model.layout.CompositeClipboardCommand;
import org.eclipse.wb.internal.xwt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.form.FormAttachmentInfo.FormAttachmentClipboardInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;

import java.util.List;

/**
 * SWT {@link FormLayout} model.
 * 
 * @author mitin_aa
 * @coverage XWT.model.layout
 */
public final class FormLayoutInfo extends LayoutInfo implements IFormLayoutInfo<ControlInfo> {
  private final FormLayoutInfoImpl<ControlInfo> impl;
  private final FormLayoutPreferences<ControlInfo> preferences;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormLayoutInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    preferences = new FormLayoutPreferences<ControlInfo>(this, description.getToolkit());
    addBroadcastListeners();
    impl =
        getPreferences().useClassic()
            ? new FormLayoutInfoImplClassic<ControlInfo>(this)
            : new FormLayoutInfoImplAutomatic<ControlInfo>(this);
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
          new ObjectInfoAction(control, "Autosize control",
              Activator.getImage("info/layout/FormLayout/fit_to_size.png")) {
            @Override
            protected void runEx() throws Exception {
              doAutoSize(control);
            }
          };
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, action);
    }
    // pre-defined anchors
    {
      IMenuManager predefinedMenuManager = new MenuManager("Quick constraints");
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, predefinedMenuManager);
      new PredefinedAnchorsActions<ControlInfo>(this).contributeActions(
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
        getAttachment(control, IPositionConstants.LEFT),
        getAttachment(control, IPositionConstants.RIGHT));
    // vertical dimension
    doAutoSize(
        getAttachment(control, IPositionConstants.TOP),
        getAttachment(control, IPositionConstants.BOTTOM));
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
  public void commandMove(ControlInfo control, ControlInfo nextControl) throws Exception {
    command_MOVE(control, nextControl);
  }

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
      if (!isImplicitlyCreated(control)) {
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
      if (!isImplicitlyCreated(control)) {
        FormDataInfo formData = (FormDataInfo) getLayoutData(control);
        final FormAttachmentClipboardInfo left =
            formData.getAttachment(IPositionConstants.LEFT).getClipboardInfo();
        final FormAttachmentClipboardInfo right =
            formData.getAttachment(IPositionConstants.RIGHT).getClipboardInfo();
        final FormAttachmentClipboardInfo top =
            formData.getAttachment(IPositionConstants.TOP).getClipboardInfo();
        final FormAttachmentClipboardInfo bottom =
            formData.getAttachment(IPositionConstants.BOTTOM).getClipboardInfo();
        final int index = i;
        commands.add(new CompositeClipboardCommand() {
          private static final long serialVersionUID = 0L;

          @Override
          protected void execute(CompositeInfo composite) throws Exception {
            ControlInfo control = composite.getChildrenControls().get(index);
            FormDataInfo _thisFormData = (FormDataInfo) getLayoutData(control);
            _thisFormData.getAttachment(IPositionConstants.LEFT).applyClipboardInfo(control, left);
            _thisFormData.getAttachment(IPositionConstants.RIGHT).applyClipboardInfo(control, right);
            _thisFormData.getAttachment(IPositionConstants.TOP).applyClipboardInfo(control, top);
            _thisFormData.getAttachment(IPositionConstants.BOTTOM).applyClipboardInfo(
                control,
                bottom);
          }
        });
      }
    }
  }

  private static boolean isImplicitlyCreated(ControlInfo control) {
    return control.getCreationSupport() instanceof IImplicitCreationSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setting new layout
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    super.onSet();
    List<ControlInfo> controls = getControls();
    Rectangle parentArea = getComposite().getClientArea();
    for (ControlInfo control : controls) {
      Rectangle controlBounds = control.getModelBounds();
      Dimension preferredSize = control.getPreferredSize();
      int x = controlBounds.x - parentArea.x;
      int y = controlBounds.y - parentArea.y;
      int width = controlBounds.width;
      int height = controlBounds.height;
      setAttachmentOffset(control, IPositionConstants.LEFT, x);
      setAttachmentOffset(control, IPositionConstants.TOP, y);
      if (width != preferredSize.width) {
        setAttachmentOffset(control, IPositionConstants.RIGHT, x + width);
      }
      if (height != preferredSize.height) {
        setAttachmentOffset(control, IPositionConstants.BOTTOM, y + height);
      }
    }
    // install preference change listener to be able to re-parse on layout mode change (classic or auto)
    preferences.addPropertyChangeListener();
  }

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
  public final Dimension getContainerSize() {
    AbstractComponentInfo composite = getComposite();
    Rectangle compositeBounds = composite.getModelBounds().getCopy();
    Insets clientAreaInsets = composite.getClientAreaInsets();
    return compositeBounds.crop(clientAreaInsets).getSize();
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
  public void setQuickAnchors(ControlInfo widget, int sides, boolean relative) throws Exception {
    setQuickAnchor(
        widget,
        IPositionConstants.LEFT,
        relative,
        (sides & IPositionConstants.LEFT) != 0);
    setQuickAnchor(
        widget,
        IPositionConstants.RIGHT,
        relative,
        (sides & IPositionConstants.RIGHT) != 0);
    setQuickAnchor(widget, IPositionConstants.TOP, relative, (sides & IPositionConstants.TOP) != 0);
    setQuickAnchor(
        widget,
        IPositionConstants.BOTTOM,
        relative,
        (sides & IPositionConstants.BOTTOM) != 0);
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

  public void anchorToParent(ControlInfo control, int controlSide, int parentSide) throws Exception {
    FormAttachmentInfo attachment = getAttachment(control, controlSide);
    attachment.setControl(null);
    attachment.setDenominator(100);
    boolean isHorizontal = PlacementUtils.isHorizontalSide(controlSide);
    Transposer t = new Transposer(!isHorizontal);
    int margin =
        isHorizontal ? FormUtils.getLayoutMarginLeft(this) : FormUtils.getLayoutMarginTop(this);
    org.eclipse.wb.draw2d.geometry.Rectangle controlBounds = t.t(control.getModelBounds());
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

  public void anchorToParentAsPercent(ControlInfo control, int controlSide) throws Exception {
    FormAttachmentInfo attachment = getAttachment(control, controlSide);
    attachment.setControl(null);
    attachment.setDenominator(100);
    attachment.setOffset(0);
    boolean isHorizontal = PlacementUtils.isHorizontalSide(controlSide);
    Transposer t = new Transposer(!isHorizontal);
    int margin =
        isHorizontal ? FormUtils.getLayoutMarginLeft(this) : FormUtils.getLayoutMarginTop(this);
    org.eclipse.wb.draw2d.geometry.Rectangle controlBounds = t.t(control.getModelBounds());
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
  public FormLayoutPreferences<ControlInfo> getPreferences() {
    return preferences;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  public IResource getUnderlyingResource() throws Exception {
    return getContext().getFile();
  }
}