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
package org.eclipse.wb.core.gef.policy.validator;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;

/**
 * Factory for creating {@link ILayoutRequestValidator}'s.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class LayoutRequestValidators {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ILayoutRequestValidator} that checks that child model has expected class.
   */
  public static ILayoutRequestValidator modelType(Class<?> requiredModelClass) {
    ILayoutRequestValidator validator = new ModelClassLayoutRequestValidator(requiredModelClass);
    validator = cache(validator);
    return mixWithMandatory(validator);
  }

  /**
   * @return {@link ILayoutRequestValidator} that checks that "child" has expected component class.
   */
  public static ILayoutRequestValidator componentType(String requiredClassName) {
    ILayoutRequestValidator validator = new ComponentClassLayoutRequestValidator(requiredClassName);
    validator = cache(validator);
    return mixWithMandatory(validator);
  }

  /**
   * @return the {@link ILayoutRequestValidator} which caches results of given one. Note, that not
   *         all validators can be cached, only validators which don't depend on mutable state of
   *         {@link Request}.
   */
  public static ILayoutRequestValidator cache(ILayoutRequestValidator validator) {
    return new CachingLayoutRequestValidator(validator);
  }

  /**
   * @return {@link ILayoutRequestValidator} that contains given one, plus all mandatory validators
   *         that should be mixed into final validator.
   */
  public static ILayoutRequestValidator finalize(ILayoutRequestValidator specificValidator) {
    return mixWithMandatory(specificValidator);
  }

  /**
   * Mixes mandatory validators with given specific validator.
   */
  private static ILayoutRequestValidator mixWithMandatory(ILayoutRequestValidator specificValidator) {
    return and(
        BorderOfChildLayoutRequestValidator.INSTANCE,
        specificValidator,
        CompatibleLayoutRequestValidator.INSTANCE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compound
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ILayoutRequestValidator} that combines {@link ILayoutRequestValidator}'s using
   *         <code>AND</code> operator.
   */
  public static ILayoutRequestValidator and(final ILayoutRequestValidator... validators) {
    return new ILayoutRequestValidator() {
      public boolean validateCreateRequest(EditPart host, CreateRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (!validator.validateCreateRequest(host, request)) {
            return false;
          }
        }
        return true;
      }

      public boolean validatePasteRequest(EditPart host, PasteRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (!validator.validatePasteRequest(host, request)) {
            return false;
          }
        }
        return true;
      }

      public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (!validator.validateMoveRequest(host, request)) {
            return false;
          }
        }
        return true;
      }

      public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (!validator.validateAddRequest(host, request)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  /**
   * @return {@link ILayoutRequestValidator} that combines {@link ILayoutRequestValidator}'s using
   *         <code>OR</code> operator.
   */
  public static ILayoutRequestValidator or(final ILayoutRequestValidator... validators) {
    return new ILayoutRequestValidator() {
      public boolean validateCreateRequest(EditPart host, CreateRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (validator.validateCreateRequest(host, request)) {
            return true;
          }
        }
        return false;
      }

      public boolean validatePasteRequest(EditPart host, PasteRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (validator.validatePasteRequest(host, request)) {
            return true;
          }
        }
        return false;
      }

      public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (validator.validateMoveRequest(host, request)) {
            return true;
          }
        }
        return false;
      }

      public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
        for (ILayoutRequestValidator validator : validators) {
          if (validator.validateAddRequest(host, request)) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
