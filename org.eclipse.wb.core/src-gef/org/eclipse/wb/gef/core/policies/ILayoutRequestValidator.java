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
package org.eclipse.wb.gef.core.policies;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;

/**
 * Validator for known layout requests {@link Request#REQ_CREATE}, {@link Request#REQ_PASTE},
 * {@link Request#REQ_MOVE} and {@link Request#REQ_ADD}.
 * <p>
 * We use this validator as convenient way to check that layout {@link EditPolicy} understands given
 * {@link Request}, so this {@link Request} should not go to other {@link EditPolicy}'s.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage gef.core
 */
public interface ILayoutRequestValidator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link Request#REQ_CREATE} {@link Request} is valid.
   */
  boolean validateCreateRequest(EditPart host, CreateRequest request);

  /**
   * @return <code>true</code> if {@link Request#REQ_PASTE} {@link Request} is valid.
   */
  boolean validatePasteRequest(EditPart host, PasteRequest request);

  /**
   * @return <code>true</code> if {@link Request#REQ_MOVE} {@link Request} is valid.
   */
  boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request);

  /**
   * @return <code>true</code> if {@link Request#REQ_ADD} {@link Request} is valid.
   */
  boolean validateAddRequest(EditPart host, ChangeBoundsRequest request);

  ////////////////////////////////////////////////////////////////////////////
  //
  // LITERALS
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Instance of validator allowing all requests.
   */
  ILayoutRequestValidator TRUE = new LayoutRequestValidatorStubTrue() {
  };
  /**
   * Instance of validator disallowing all requests.
   */
  ILayoutRequestValidator FALSE = new LayoutRequestValidatorStubFalse() {
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default validator stubs
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ILayoutRequestValidator} that allows all requests.
   */
  public abstract class LayoutRequestValidatorStubTrue implements ILayoutRequestValidator {
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      return true;
    }

    public boolean validatePasteRequest(EditPart host, PasteRequest request) {
      return true;
    }

    public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
      return true;
    }

    public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
      return true;
    }
  }
  /**
   * Implementation of {@link ILayoutRequestValidator} which denies all requests.
   */
  public abstract class LayoutRequestValidatorStubFalse implements ILayoutRequestValidator {
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      return false;
    }

    public boolean validatePasteRequest(EditPart host, PasteRequest request) {
      return false;
    }

    public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
      return false;
    }

    public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
      return false;
    }
  }
}
