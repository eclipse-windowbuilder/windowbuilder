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
package org.eclipse.wb.tests.designer.editor.validator;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.mock.EasyMockTemplate;
import org.eclipse.wb.tests.designer.tests.mock.MockRunnable;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Abstract tests for {@link ILayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class AbstractLayoutRequestValidatorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that all <code>validate</code> methods return <code>true</code>.
   */
  protected static void assertTrue(ILayoutRequestValidator validator) {
    assertTrue(validator.validateCreateRequest(null, null));
    assertTrue(validator.validatePasteRequest(null, null));
    assertTrue(validator.validateMoveRequest(null, null));
    assertTrue(validator.validateAddRequest(null, null));
  }

  /**
   * Asserts that all <code>validate</code> methods return <code>false</code>.
   */
  protected static void assertFalse(ILayoutRequestValidator validator) {
    assertFalse(validator.validateCreateRequest(null, null));
    assertFalse(validator.validatePasteRequest(null, null));
    assertFalse(validator.validateMoveRequest(null, null));
    assertFalse(validator.validateAddRequest(null, null));
  }

  protected static void assert_validateCMA(ILayoutRequestValidator validator,
      boolean expected,
      Object parent,
      Object child) throws Exception {
    EditPart host = createHost(parent);
    // to assert for parent EditPart
    assert_validateCMA(validator, expected, host, child);
  }

  protected static void assert_validateCMA(ILayoutRequestValidator validator,
      boolean expected,
      EditPart host,
      Object child) throws Exception {
    assert_validateCreateRequest(validator, expected, host, child);
    assert_validateMoveRequest(validator, expected, host, child);
    assert_validateAddRequest(validator, expected, host, child);
  }

  protected static void assert_validateCreateRequest(ILayoutRequestValidator validator,
      boolean expected,
      EditPart host,
      final Object child) {
    assertEquals(expected, validator.validateCreateRequest(host, new CreateRequest(null) {
      @Override
      public Object getNewObject() {
        return child;
      }
    }));
  }

  protected static void assert_validatePasteRequest(ILayoutRequestValidator validator,
      boolean expected,
      Object parent,
      JavaInfo child) throws Exception {
    EditPart host = createHost(parent);
    // to assert for parent EditPart
    assert_validatePasteRequest(validator, expected, host, child);
  }

  protected static void assert_validatePasteRequest(ILayoutRequestValidator validator,
      boolean expected,
      EditPart host,
      JavaInfo child) throws Exception {
    JavaInfoMemento memento = JavaInfoMemento.createMemento(child);
    PasteRequest request = new PasteRequest(ImmutableList.of(memento));
    assertEquals(expected, validator.validatePasteRequest(host, request));
  }

  protected static void assert_validateMoveRequest(final ILayoutRequestValidator validator,
      final boolean expected,
      final EditPart host,
      final Object child) throws Exception {
    final IMocksControl mocksControl = EasyMock.createNiceControl();
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      EditPart editPart = mocksControl.createMock(EditPart.class);

      public void expectations() throws Exception {
        expect(editPart.getModel()).andReturn(child).anyTimes();
      }

      public void codeToTest() throws Exception {
        ChangeBoundsRequest request = new ChangeBoundsRequest();
        request.setEditParts(ImmutableList.of(editPart));
        assertEquals(expected, validator.validateMoveRequest(host, request));
      }
    });
  }

  protected static void assert_validateAddRequest(final ILayoutRequestValidator validator,
      final boolean expected,
      final EditPart host,
      final Object child) throws Exception {
    final IMocksControl mocksControl = EasyMock.createNiceControl();
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      EditPart editPart = mocksControl.createMock(EditPart.class);

      public void expectations() throws Exception {
        expect(editPart.getModel()).andReturn(child).anyTimes();
      }

      public void codeToTest() throws Exception {
        ChangeBoundsRequest request = new ChangeBoundsRequest();
        request.setEditParts(ImmutableList.of(editPart));
        assertEquals(expected, validator.validateAddRequest(host, request));
      }
    });
  }

  private static EditPart createHost(Object parent) {
    IMocksControl mocksControl = EasyMock.createNiceControl();
    EditPart host = mocksControl.createMock(EditPart.class);
    expect(host.getModel()).andReturn(parent).anyTimes();
    mocksControl.replay();
    return host;
  }
}
