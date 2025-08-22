/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Service layer, as technology-agnostic Java interfaces, implemented by concrete Java classes
 * in a sub-package.
 * <p>
 * The concrete implementation classes take care of transaction management, database access
 * (either directly or indirectly), etc.
 * <p>
 * The service API in this package is completely technology-agnostic, and can easily be mocked in
 * unit tests. That is, the service interfaces are purely abstract, and the exchanged data objects
 * are model objects as immutable Java records. The latter objects are also technology-agnostic, in
 * contrast to JPA entities.
 *
 * @author Chris de Vreeze
 */
package eu.cdevreeze.todo.service;
