/*
 * Copyright 2021-2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.fhir.proxy.interfaces;

// TODO: change this interface to pass the access token and other required objects to `checkAccess`
// and remove `AccessCheckerFactory` (implementations can choose to have a factory internally).
public interface AccessChecker {

  /**
   * Checks whether the current user has access to requested resources.
   *
   * @param requestDetails details about the resource and operation requested
   * @return the outcome of access checking
   */
  AccessDecision checkAccess(RequestDetailsReader requestDetails);
}
