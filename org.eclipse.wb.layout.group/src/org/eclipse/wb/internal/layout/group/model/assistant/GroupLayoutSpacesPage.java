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
package org.eclipse.wb.internal.layout.group.model.assistant;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.core.controls.CSpinnerDeferredNotifier;
import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.layout.absolute.IImageProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.layout.group.Messages;
import org.eclipse.wb.internal.layout.group.model.AnchorsSupport;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.netbeans.modules.form.layoutdesign.LayoutConstants;

/**
 * Layout assistant page for managing component and space around it.
 * 
 * @author mitin_aa
 */
public final class GroupLayoutSpacesPage extends Composite
    implements
      ILayoutAssistantPage,
      LayoutConstants {
  private final IGroupLayoutInfo m_layout;
  private final AbstractComponentInfo m_javaInfo;
  private final AnchorsSupport m_anchorsSupport;
  private final SpaceComposite[] m_spaceComposites = new SpaceComposite[4];
  private final ComponentComposite m_componentComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutSpacesPage(Composite parent, IGroupLayoutInfo layout, ObjectInfo javaInfo) {
    super(parent, SWT.NONE);
    // fields
    m_layout = layout;
    m_javaInfo = (AbstractComponentInfo) javaInfo;
    m_anchorsSupport = new AnchorsSupport(m_layout);
    // UI
    GridLayoutFactory.create(this).noMargins().noSpacing();
    // put everything into a top group
    Group topGroup = new Group(this, SWT.NONE);
    GridDataFactory.create(topGroup).grab().fill();
    GridLayoutFactory.create(topGroup).columns(3);
    topGroup.setText(Messages.GroupLayoutSpacesPage_spaceGroup);
    {
      new Label(topGroup, SWT.NONE);
      m_spaceComposites[0] = new SpaceComposite(topGroup, LEADING, VERTICAL);
      new Label(topGroup, SWT.NONE);
    }
    {
      m_spaceComposites[1] = new SpaceComposite(topGroup, LEADING, HORIZONTAL);
      {
        Group group = new Group(topGroup, SWT.NONE);
        GridDataFactory.create(group).grab().fill();
        GridLayoutFactory.create(group);
        group.setText(Messages.GroupLayoutSpacesPage_componentGroup);
        m_componentComposite = new ComponentComposite(group);
      }
      m_spaceComposites[2] = new SpaceComposite(topGroup, TRAILING, HORIZONTAL);
    }
    {
      new Label(topGroup, SWT.NONE);
      m_spaceComposites[3] = new SpaceComposite(topGroup, TRAILING, VERTICAL);
      new Label(topGroup, SWT.NONE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutAssistantPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updatePage() {
    for (int i = 0; i < m_spaceComposites.length; i++) {
      m_spaceComposites[i].updateUI();
    }
    m_componentComposite.updateUI();
  }

  public boolean isPageValid() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private CSpinner createSpinner(Composite componentComposite) {
    CSpinner spinner = new CSpinner(componentComposite, SWT.BORDER);
    GridDataFactory.create(spinner).hintHC(7);
    spinner.setMaximum(Short.MAX_VALUE);
    return spinner;
  }

  private void createSpinnerNotifier(CSpinner spinner, Listener listener) {
    new CSpinnerDeferredNotifier(spinner, 500, listener);
  }

  /**
   * Creates button control to set default width/height and adds given {@link SelectionListener}.
   */
  private Control createClearButton(Composite parent, SelectionListener selectionListener) {
    ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
    ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
    toolItem.setImage(m_layout.getAdapter(IImageProvider.class).getImage(
        "info/layout/groupLayout/clear.gif"));
    toolItem.setToolTipText(Messages.GroupLayoutSpacesPage_setDefaultSize);
    toolItem.addSelectionListener(selectionListener);
    return toolBar;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setComponentSize(final int dimension, final int value) {
    ExecutionUtils.run(m_layout.getAdapter(JavaInfo.class), new RunnableEx() {
      public void run() throws Exception {
        m_anchorsSupport.action_setComponentSize(m_javaInfo, dimension, value);
      }
    });
  }

  private void updateAnchors(ToolBarManager manager, boolean isHorizontal) {
    manager.removeAll();
    m_anchorsSupport.fillContributionManager(m_javaInfo, manager, isHorizontal);
    manager.update(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Component composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class ComponentComposite extends Composite {
    private CSpinner m_widthSpinner;
    private CSpinner m_heightSpinner;
    private ToolBarManager m_verticalAnchorsManager;
    private ToolBarManager m_horizontalAnchorsManager;

    public ComponentComposite(Composite parent) {
      super(parent, SWT.NONE);
      GridLayoutFactory.create(this).columns(2).noMargins().noSpacing();
      {
        m_horizontalAnchorsManager = new ToolBarManager(SWT.FLAT);
        m_anchorsSupport.fillContributionManager(m_javaInfo, m_horizontalAnchorsManager, true);
        ToolBar toolBar = m_horizontalAnchorsManager.createControl(this);
        GridDataFactory.create(toolBar).alignHC();
      }
      {
        new Label(this, SWT.NONE);
      }
      {
        Group sizeGroup = new Group(this, SWT.NONE);
        GridDataFactory.create(sizeGroup).alignVM().grab();
        GridLayoutFactory.create(sizeGroup).columns(3);
        sizeGroup.setText(Messages.GroupLayoutSpacesPage_sizeGroup);
        {
          new Label(sizeGroup, SWT.NONE).setText(Messages.GroupLayoutSpacesPage_sizeWidth);
          m_widthSpinner = createSpinner(sizeGroup);
          createSpinnerNotifier(m_widthSpinner, new ComponentSizeSetListener(HORIZONTAL));
          createClearButton(sizeGroup, new ComponentSizeSetDefaultListener(HORIZONTAL));
        }
        {
          new Label(sizeGroup, SWT.NONE).setText(Messages.GroupLayoutSpacesPage_sizeHeight);
          m_heightSpinner = createSpinner(sizeGroup);
          createSpinnerNotifier(m_heightSpinner, new ComponentSizeSetListener(VERTICAL));
          createClearButton(sizeGroup, new ComponentSizeSetDefaultListener(VERTICAL));
        }
      }
      {
        m_verticalAnchorsManager = new ToolBarManager(SWT.FLAT | SWT.VERTICAL);
        new AnchorsSupport(m_layout).fillContributionManager(
            m_javaInfo,
            m_verticalAnchorsManager,
            false);
        ToolBar toolBar = m_verticalAnchorsManager.createControl(this);
        GridDataFactory.create(toolBar).grabV().alignVM();
      }
    }

    public void updateUI() {
      Rectangle bounds = m_javaInfo.getBounds();
      m_widthSpinner.setSelection(bounds.width);
      m_heightSpinner.setSelection(bounds.height);
      updateAnchors(m_horizontalAnchorsManager, true);
      updateAnchors(m_verticalAnchorsManager, false);
    }
  }
  private final class ComponentSizeSetDefaultListener extends SelectionAdapter {
    private final int m_dimension;

    public ComponentSizeSetDefaultListener(int dimension) {
      m_dimension = dimension;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      setComponentSize(m_dimension, NOT_EXPLICITLY_DEFINED);
    }
  }
  private final class ComponentSizeSetListener implements Listener {
    private final int m_dimension;

    public ComponentSizeSetListener(int dimension) {
      m_dimension = dimension;
    }

    public void handleEvent(Event event) {
      if (event.type == SWT.Selection && event.doit) {
        CSpinner spinner = (CSpinner) event.widget;
        final int value = spinner.getSelection();
        if (value > 0) {
          setComponentSize(m_dimension, value);
        }
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Space composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class SpaceComposite extends Composite {
    private final CSpinner m_spinner;
    private final Button m_button;
    private final int m_direction;
    private final int m_dimension;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SpaceComposite(Composite parent, final int direction, final int dimension) {
      super(parent, SWT.NONE);
      m_direction = direction;
      m_dimension = dimension;
      // UI
      GridDataFactory.create(this).alignHC();
      GridLayoutFactory.create(this).noMargins().noSpacing();
      if (dimension == VERTICAL) {
        GridLayoutFactory.modify(this).columns(2);
      }
      m_spinner = createSpinner(this);
      m_button = new Button(this, SWT.CHECK);
      m_button.setText(Messages.GroupLayoutSpacesPage_defaultSpace);
      // install listeners
      m_button.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          int gapValue;
          if (m_button.getSelection()) {
            gapValue = NOT_EXPLICITLY_DEFINED;
          } else {
            gapValue = m_spinner.getSelection();
          }
          setGapValue(gapValue);
        }
      });
      createSpinnerNotifier(m_spinner, new Listener() {
        public void handleEvent(Event event) {
          if (event.type == SWT.Selection && event.doit) {
            int gapValue = m_spinner.getSelection();
            int defaultGapSize =
                m_anchorsSupport.getDefaultGapSize(m_javaInfo, m_dimension, m_direction).intValue();
            if (gapValue < 1 || gapValue == defaultGapSize) {
              gapValue = NOT_EXPLICITLY_DEFINED;
            }
            setGapValue(gapValue);
          }
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Update
    //
    ////////////////////////////////////////////////////////////////////////////
    private void updateUI() {
      Integer emptySpaceValue =
          m_anchorsSupport.getEmptySpaceValue(m_javaInfo, m_dimension, m_direction);
      if (emptySpaceValue != null) {
        int gapSize = emptySpaceValue.intValue();
        final boolean isDefault = gapSize == NOT_EXPLICITLY_DEFINED;
        if (isDefault) {
          gapSize =
              m_anchorsSupport.getDefaultGapSize(m_javaInfo, m_dimension, m_direction).intValue();
        }
        m_spinner.setSelection(gapSize);
        m_button.setSelection(isDefault);
      } else {
        setEnabled(false);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Helper
    //
    ////////////////////////////////////////////////////////////////////////////
    private void setGapValue(final int gapValue) {
      ExecutionUtils.run(m_layout.getAdapter(JavaInfo.class), new RunnableEx() {
        public void run() throws Exception {
          m_anchorsSupport.action_setEmptySpaceProperties(
              m_javaInfo,
              m_dimension,
              m_direction,
              gapValue);
        }
      });
    }
  }
}
