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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for <i>non-visual model</i> described over Javadoc-style doc comment.
 *
 * @author lobas_av
 * @coverage core.model.nonvisual
 */
final class JavadocNonVisualBeanInfo extends NonVisualBeanInfo {
  private static final String NON_VISUAL_TAG = "@wbp.nonvisual";
  private static final String LOCATION_PREFIX = "location=";
  //
  private final BodyDeclaration m_bodyDeclaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  JavadocNonVisualBeanInfo(BodyDeclaration bodyDeclaration) {
    m_location = new Point();
    m_bodyDeclaration = bodyDeclaration;
  }

  private JavadocNonVisualBeanInfo(BodyDeclaration bodyDeclaration, Point location) {
    this(bodyDeclaration);
    m_location.setLocation(location);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link NonVisualBeanInfo} if given {@link BodyDeclaration} contains special non-visual
   *         comment or <code>null</code>.
   */
  static NonVisualBeanInfo getNonVisualBeanInfo(BodyDeclaration bodyDeclaration) {
    Javadoc javadoc = bodyDeclaration.getJavadoc();
    if (javadoc != null) {
      // try to find non-visual tag
      for (TagElement tagElement : DomGenerics.tags(javadoc)) {
        if (NON_VISUAL_TAG.equals(tagElement.getTagName())) {
          // prepare fragments
          List<ASTNode> fragments = DomGenerics.fragments(tagElement);
          Assert.isTrueException(
              !fragments.isEmpty(),
              ICoreExceptionConstants.PARSER_WRONG_NON_VISUAL_COMMENT,
              tagElement);
          // extract location element
          ASTNode fragment = fragments.get(0);
          Assert.isTrueException(
              fragment instanceof TextElement,
              ICoreExceptionConstants.PARSER_WRONG_NON_VISUAL_COMMENT,
              tagElement);
          //
          TextElement textElement = (TextElement) fragment;
          String text = textElement.getText().trim();
          Assert.isTrueException(
              text.startsWith(LOCATION_PREFIX),
              ICoreExceptionConstants.PARSER_WRONG_NON_VISUAL_COMMENT,
              tagElement);
          // prepare location
          String[] locationParts = StringUtils.split(text.substring(LOCATION_PREFIX.length()), ',');
          Assert.isTrueException(
              locationParts.length == 2,
              ICoreExceptionConstants.PARSER_WRONG_NON_VISUAL_COMMENT,
              tagElement);
          Point location = new Point();
          try {
            location.x = Integer.parseInt(locationParts[0].trim());
            location.y = Integer.parseInt(locationParts[1].trim());
          } catch (NumberFormatException e) {
            Assert.isTrueException(
                false,
                ICoreExceptionConstants.PARSER_WRONG_NON_VISUAL_COMMENT,
                tagElement);
          }
          // create model
          return new JavadocNonVisualBeanInfo(bodyDeclaration, location);
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void moveLocation(Point moveDelta) throws Exception {
    m_location.translate(moveDelta);
    m_javaInfo.getEditor().setJavadocTagText(
        m_bodyDeclaration,
        NON_VISUAL_TAG,
        " "
            + LOCATION_PREFIX
            + Integer.toString(m_location.x)
            + ","
            + Integer.toString(m_location.y));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Remove
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void remove() throws Exception {
    m_javaInfo.getEditor().setJavadocTagText(m_bodyDeclaration, NON_VISUAL_TAG, null);
    super.remove();
  }
}