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

package org.eclipse.wb.internal.discovery.ui.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.ui.util.BrowserHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A ToolkitControl is a UI wrapper around a WBToolkit.
 * 
 * @see WBToolkit
 */
class ToolkitControl extends Composite {
  private static Font BOLD_FONT;
  
  private boolean selected;
  private WBToolkit wbToolkit;
  
  private List<SelectionListener> listeners = new ArrayList<SelectionListener>();
  
  
  /**
   * Create a new ToolkitControl.
   * 
   * @param parent the SWT parent control
   * @param wbToolkit the WindowBuilder toolkit
   */
  public ToolkitControl(Composite parent, WBToolkit wbToolkit) {
    super(parent, SWT.NONE);
    
    this.wbToolkit = wbToolkit;
    
    setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    setBackground(parent.getBackground());
    
    initControl();
  }
  
  public boolean isSelected() {
    return selected;
  }
  
  public void setSelected(boolean value) {
    this.selected = value;
    
    updateBackground();
  }
  
  public void addSelectionListener(SelectionListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }
  
  public void removeSelectionListener(SelectionListener listener) {
    listeners.remove(listener);
  }
  
  protected void fireSelectionEvent(MouseEvent me) {
    Event e = new Event();
    e.widget = this;
    SelectionEvent event = new SelectionEvent(e);
    event.stateMask = me.stateMask;
    event.widget = this;
    
    for (SelectionListener listener : listeners) {
      listener.widgetSelected(event);
    }
  }
  
  protected WBToolkit getToolkit() {
    return wbToolkit;
  }
  
  private void initControl() {
    GridLayoutFactory.fillDefaults().numColumns(3).spacing(3, 2).applyTo(this);
    
    GridLayout layout = (GridLayout)getLayout();
    layout.marginLeft = 7;
    layout.marginTop = 4;
    layout.marginBottom = 2;
    //layout.horizontalSpacing = 7;
    
    listenTo(this);
    
    Label iconLabel = new Label(this, SWT.NULL);
    iconLabel.setBackground(getBackground());
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).span(1, 2).applyTo(iconLabel);
    listenTo(iconLabel);
    
    if (wbToolkit.getIconURL() != null) {
      ImageDescriptor iconDescriptor = ImageDescriptor.createFromURL(wbToolkit.getIconURL());
      
      if (iconDescriptor != null) {
        iconLabel.setImage(iconDescriptor.createImage());
      }
    }
    
    Label nameLabel = new Label(this, SWT.NONE);
    nameLabel.setText(wbToolkit.getName());
    nameLabel.setBackground(getBackground());
    nameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    nameLabel.setFont(getBoldFont(getFont()));
    listenTo(nameLabel);
    
    Label providerLabel = new Label(this, SWT.NONE);
    providerLabel.setText(wbToolkit.getProviderDescription());
    providerLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    providerLabel.setBackground(getBackground());
    providerLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
    listenTo(providerLabel);
    
    Label summaryLabel = new Label(this, SWT.WRAP);
    String description = wbToolkit.getDescription();
    description = description.replaceAll("(\\r\\n)|\\n|\\r", " ");
    summaryLabel.setText(description);
    summaryLabel.setBackground(getBackground());
    GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(100, SWT.DEFAULT).applyTo(summaryLabel);
    listenTo(summaryLabel);
    
    if (wbToolkit.getMoreInfoURL() != null) {
      // spacer
      Label label = new Label(this, SWT.NONE);
      label.setBackground(getBackground());
      listenTo(label);
      
      // hyperlink
      Link moreInfoLink = new Link(this, SWT.NONE);
      moreInfoLink.setText("<a>more info...</a>");
      moreInfoLink.setBackground(getBackground());
      moreInfoLink.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          BrowserHelper.openUrl(getShell(), wbToolkit.getMoreInfoURL());
        }
      });
      GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(moreInfoLink);
      
      // spacer      
      label = new Label(this, SWT.NONE);
      label.setBackground(getBackground());
      listenTo(label);
    }
    
    if (wbToolkit.isInstalled()) {
      nameLabel.setText(nameLabel.getText() + " (installed)");
      nameLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
      
      summaryLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
      
//      if (iconLabel.getImage() != null) {
//        iconLabel.setImage(
//            new Image(Display.getDefault(), iconLabel.getImage(), SWT.IMAGE_GRAY));
//      }
      
      //setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
    }
  }
  
  private void listenTo(Control control) {
    control.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        fireSelectionEvent(e);
      }
    });
  }
    
  private static Font getBoldFont(Font font) {
    if (BOLD_FONT == null) {
      FontData data = font.getFontData()[0];
      data.setStyle(SWT.BOLD);
      BOLD_FONT = new Font(Display.getDefault(), data);
    }
    
    return BOLD_FONT;
  }
  
  private void updateBackground() {
    Color background;
    
    if (isSelected()) {
      background = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
    } else {
      background = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    }
    
    updateBackground(this, background);
  }
  
  private void updateBackground(Composite composite, Color background) {
    composite.setBackground(background);
    
    for (Control control : composite.getChildren()) {
      if (control instanceof Composite) {
        updateBackground((Composite)control, background);
      } else {
        control.setBackground(background);
      }
    }
  }

}
