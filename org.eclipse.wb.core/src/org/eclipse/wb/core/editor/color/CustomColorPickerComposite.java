/*******************************************************************************
 * Copyright (c) 2021, 2021 DSA Daten- und Systemtechnik GmbH. (https://www.dsa.de)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Stahlmann - Initial implementation
 *    Marcel du Preez   - Model messages updates
 *******************************************************************************/
package org.eclipse.wb.core.editor.color;

import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorsComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CustomColorPickerComposite extends AbstractColorsComposite {
  private Label colorLabelBlue;
  private Label colorLabelRed;
  private Label colorLabelGreen;
  private Label colorLabelHue;
  private Label colorLabelSaturation;
  private Label colorLabelbrightness;
  private Text colorTextBlue;
  private Text colorTextRed;
  private Text colorTextGreen;
  private Text colorTextHue;
  private Text colorTextSaturation;
  private Text colorTextbrightness;
  private Text previewText;
  private Composite previewImage;
  private Text previewTextBackgroundBlack;
  private Text previewTextBackgroundWhite;
  ColorSelector colorSelector;

  public CustomColorPickerComposite(Composite parent, int style, AbstractColorDialog colorDialog) {
    super(parent, style, colorDialog);
    setLayout(new GridLayout());
    createContent(this);
  }

  private void createContent(Composite parent) {
    Composite color = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().applyTo(color);
    GridDataFactory.swtDefaults().applyTo(color);
    Composite colorSelectorComp = new Composite(color, SWT.NONE);
    GridLayoutFactory.swtDefaults().applyTo(colorSelectorComp);
    GridDataFactory.swtDefaults().applyTo(colorSelectorComp);
    Label buttonLabel = new Label(colorSelectorComp, SWT.NONE);
    buttonLabel.setText(ModelMessages.CustomColorPicker_btnSelectColor);
    colorSelector = new ColorSelector(colorSelectorComp);
    createInfoComposite(color);
    previewComposite(color);
    colorSelector.getButton().addSelectionListener(new SelectionListener() {
      @Override
	public void widgetSelected(SelectionEvent e) {
        if (colorSelector.getColorValue() == null) {
          return;
        }
        RGB currentColor = colorSelector.getColorValue();
        m_colorDialog.setResultColor(
            new ColorInfo(currentColor.red, currentColor.green, currentColor.blue));
        colorSelector.setColorValue(currentColor);
        updatePreview(currentColor);
        colorTextRed.setText("" + currentColor.red);
        colorTextGreen.setText("" + currentColor.green);
        colorTextBlue.setText("" + currentColor.blue);
        colorTextHue.setText("" + String.format("%.2f", currentColor.getHSB()[0]));
        colorTextSaturation.setText("" + String.format("%.2f", currentColor.getHSB()[1]));
        colorTextbrightness.setText("" + String.format("%.2f", currentColor.getHSB()[2]));
      }

      @Override
	public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
      }
    });
  }

  private void previewComposite(Composite parent) {
    Composite previewComposite = new Composite(parent, SWT.BORDER);
    GridLayoutFactory.swtDefaults().numColumns(1).applyTo(previewComposite);
    GridDataFactory.swtDefaults().applyTo(previewComposite);
    Font mono = new Font(parent.getDisplay(), "Monospaced", 15, SWT.BOLD);
    previewText = new Text(previewComposite, SWT.READ_ONLY);
    previewText.setFont(mono);
    previewText.setText(ModelMessages.CustomColorPicker_previewTextExample);
    previewImage = new Composite(previewComposite, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(1).applyTo(previewImage);
    GridDataFactory.swtDefaults().hint(106, 26).indent(0, 0).applyTo(previewImage);
    previewTextBackgroundBlack = new Text(previewComposite, SWT.READ_ONLY);
    previewTextBackgroundBlack.setFont(mono);
    previewTextBackgroundBlack.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    previewTextBackgroundBlack.setText(ModelMessages.CustomColorPicker_previewTextExample);
    previewTextBackgroundWhite = new Text(previewComposite, SWT.READ_ONLY);
    previewTextBackgroundWhite.setFont(mono);
    previewTextBackgroundWhite.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    previewTextBackgroundWhite.setText(ModelMessages.CustomColorPicker_previewTextExample);
  }

  private void updatePreview(RGB rgb) {
    Color color = new Color(Display.getCurrent(), rgb);
    previewText.setForeground(color);
    previewTextBackgroundBlack.setBackground(color);
    previewTextBackgroundWhite.setBackground(color);
    previewImage.setBackground(color);
  }

  public void updateColorBeforeOpen() {
    if (m_colorDialog.getColorInfo() == null) {
      m_colorDialog.setColorInfo(new ColorInfo(0, 0, 0));
    }
    updatePreview(m_colorDialog.getColorInfo().getRGB());
    colorSelector.setColorValue(m_colorDialog.getColorInfo().getRGB());
  }

  private void createInfoComposite(Composite parent) {
    Composite infoComposite = new Composite(parent, SWT.BORDER);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(infoComposite);
    GridDataFactory.swtDefaults().applyTo(infoComposite);
    Composite rgbComposite = new Composite(infoComposite, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(rgbComposite);
    GridDataFactory.swtDefaults().applyTo(rgbComposite);
    Composite hsbComposite = new Composite(infoComposite, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(hsbComposite);
    GridDataFactory.swtDefaults().applyTo(hsbComposite);
    colorLabelBlue = new Label(rgbComposite, SWT.NONE);
    colorLabelBlue.setText(ModelMessages.CustomColorPicker_lblBlue);
    colorTextBlue = new Text(rgbComposite, SWT.READ_ONLY);
    colorLabelRed = new Label(rgbComposite, SWT.NONE);
    colorLabelRed.setText(ModelMessages.CustomColorPicker_lblRed);
    colorTextRed = new Text(rgbComposite, SWT.READ_ONLY);
    colorLabelGreen = new Label(rgbComposite, SWT.NONE);
    colorLabelGreen.setText(ModelMessages.CustomColorPicker_lblGreen);
    colorTextGreen = new Text(rgbComposite, SWT.READ_ONLY);
    colorLabelHue = new Label(hsbComposite, SWT.NONE);
    colorLabelHue.setText(ModelMessages.CustomColorPicker_lblHue);
    colorTextHue = new Text(hsbComposite, SWT.READ_ONLY);
    colorLabelSaturation = new Label(hsbComposite, SWT.NONE);
    colorLabelSaturation.setText(ModelMessages.CustomColorPicker_lblSaturation);
    colorTextSaturation = new Text(hsbComposite, SWT.READ_ONLY);
    colorLabelbrightness = new Label(hsbComposite, SWT.NONE);
    colorLabelbrightness.setText(ModelMessages.CustomColorPicker_lblBrightness);
    colorTextbrightness = new Text(hsbComposite, SWT.READ_ONLY);
  }
}
