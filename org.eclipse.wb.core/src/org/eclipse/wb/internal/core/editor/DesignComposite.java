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
package org.eclipse.wb.internal.core.editor;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.core.controls.LineControl;
import org.eclipse.wb.core.controls.flyout.FlyoutControlComposite;
import org.eclipse.wb.core.controls.flyout.IFlyoutPreferences;
import org.eclipse.wb.core.controls.flyout.PluginFlyoutPreferences;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.structure.DesignComponentsComposite;
import org.eclipse.wb.internal.core.gef.EditPartFactory;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.views.PaletteView;
import org.eclipse.wb.internal.core.views.StructureView;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorPart;

/**
 * {@link Composite} with GUI for visual design, i.e. properties table, palette, GEF.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public abstract class DesignComposite extends Composite {
  protected final IEditorPart m_editorPart;
  private final ICommandExceptionHandler m_exceptionHandler;
  protected DesignComponentsComposite m_componentsComposite;
  protected ToolBar m_toolBar;
  protected ViewersComposite m_viewersComposite;
  protected GraphicalViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignComposite(Composite parent,
      int style,
      IEditorPart editorPart,
      ICommandExceptionHandler exceptionHandler) {
    super(parent, style);
    m_editorPart = editorPart;
    m_exceptionHandler = exceptionHandler;
    // create GUI
    setLayout(new FillLayout());
    createMainComposite();
    // fill toolbar
    createDesignActions();
    createDesignToolbarHelper();
  }

  private void createMainComposite() {
    // create mainComposite
    FlyoutControlComposite mainComposite;
    {
      PluginFlyoutPreferences preferences =
          new PluginFlyoutPreferences(DesignerPlugin.getPreferences(), "design.structure");
      preferences.initializeDefaults(
          IFlyoutPreferences.DOCK_WEST,
          IFlyoutPreferences.STATE_OPEN,
          300);
      mainComposite = new FlyoutControlComposite(this, SWT.NONE, preferences);
      mainComposite.setTitleText("Structure");
      mainComposite.setMinWidth(200);
      mainComposite.addMenuContributor(new DesignerFlyoutMenuContributor(StructureView.ID));
    }
    // create components composite
    m_componentsComposite =
        new DesignComponentsComposite(mainComposite.getFlyoutParent(), SWT.NONE);
    // create editor composite
    createEditorComposite(mainComposite.getClientParent());
  }

  private void createEditorComposite(Composite parent) {
    Composite editorComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(editorComposite).noMargins().spacingV(0);
    // toolbar
    {
      m_toolBar = new ToolBar(editorComposite, SWT.FLAT | SWT.RIGHT);
      GridDataFactory.create(m_toolBar).grabH().fill();
    }
    // separator to highlight toolbar
    {
      LineControl separator = new LineControl(editorComposite, SWT.HORIZONTAL);
      separator.setBackground(IColorConstants.buttonDarker);
      GridDataFactory.create(separator).grabH().fill();
    }
    // create gefComposite - palette and design canvas (viewer)
    createGEFComposite(editorComposite);
  }

  protected void createGEFComposite(Composite parent) {
    PluginFlyoutPreferences preferences =
        new PluginFlyoutPreferences(DesignerPlugin.getPreferences(), "design.palette");
    preferences.initializeDefaults(IFlyoutPreferences.DOCK_WEST, IFlyoutPreferences.STATE_OPEN, 210);
    FlyoutControlComposite gefComposite = new FlyoutControlComposite(parent, SWT.NONE, preferences);
    GridDataFactory.create(gefComposite).grab().fill();
    gefComposite.setTitleText("Palette");
    gefComposite.setMinWidth(150);
    gefComposite.setValidDockLocations(IFlyoutPreferences.DOCK_WEST | IFlyoutPreferences.DOCK_EAST);
    gefComposite.addMenuContributor(new DesignerFlyoutMenuContributor(PaletteView.ID));
    // create palette
    createPalette(gefComposite);
    // GEF viewers composite
    createViewersComposite(gefComposite.getClientParent());
  }

  private void createViewersComposite(Composite parent) {
    m_viewersComposite = new ViewersComposite(parent, SWT.NONE);
    // prepare domain
    EditDomain domain;
    {
      domain = new EditDomain();
      domain.setExceptionHandler(m_exceptionHandler);
    }
    // configure main GEF viewer
    {
      m_viewer = m_viewersComposite.getViewer();
      m_viewer.getRootFigure().setBackground(IColorConstants.listBackground);
      m_viewer.setEditDomain(domain);
      m_viewer.setEditPartFactory(EditPartFactory.INSTANCE);
    }
    // bind viewers
    m_viewersComposite.bindViewers();
    // add product layer
    m_viewer.getRootFigure().addLayer(new Layer("product") {
      @Override
      protected void paintClientArea(Graphics graphics) {
        BrandingUtils.getBranding().paintBrandingOnCanvas(getClientArea(), graphics);
      }
    });
  }

  protected abstract void createDesignActions();

  protected abstract void createDesignToolbarHelper();

  protected abstract void createPalette(FlyoutControlComposite gefComposite);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFocus() {
    return m_viewersComposite.setFocus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Design access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * New model was parsed. We should display it.
   */
  public abstract void refresh(ObjectInfo rootObject, IProgressMonitor monitor);

  /**
   * Model is going to be disposed. We should clean up visual presentation.
   */
  public void disposeDesign() {
    // clear GEF
    m_viewer.setInput(null);
    // clear properties
    if (!m_componentsComposite.isDisposed()) {
      m_componentsComposite.setInput(m_viewer, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Structure/Palette reparenting access
  //
  ////////////////////////////////////////////////////////////////////////////
  private IExtractableControl m_extractableComponents;

  /**
   * This interface allows move {@link Control} onto new parent and later restore on old parent.
   */
  public static interface IExtractableControl {
    Control getControl();

    void extract(Composite newParent);

    void restore();
  }
  /**
   * Internal implementation of {@link IExtractableControl}.
   */
  protected static final class ExtractableControl implements IExtractableControl {
    private final Control m_control;
    private final Composite m_terminator;
    private final Composite m_oldParent;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ExtractableControl(Control control, Composite terminator) {
      m_control = control;
      m_terminator = terminator;
      m_oldParent = m_control.getParent();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IExtractableControl
    //
    ////////////////////////////////////////////////////////////////////////////
    public Control getControl() {
      return m_control;
    }

    public void extract(Composite newParent) {
      m_control.setParent(newParent);
      doLayout(m_oldParent);
      newParent.layout();
    }

    public void restore() {
      m_control.setParent(m_oldParent);
      m_control.setVisible(true);
      doLayout(m_oldParent);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    private void doLayout(Composite composite) {
      if (composite != m_terminator) {
        composite.layout();
        doLayout(composite.getParent());
      }
    }
  }

  /**
   * @return the {@link IExtractableControl} for accessing "Structure" {@link Control}.
   */
  public IExtractableControl getExtractableComponents() {
    if (m_extractableComponents == null) {
      m_extractableComponents = new ExtractableControl(m_componentsComposite, this);
    }
    return m_extractableComponents;
  }

  /**
   * @return the {@link IExtractableControl} for accessing "Structure" {@link Control}.
   */
  public abstract IExtractableControl getExtractablePalette();
}
