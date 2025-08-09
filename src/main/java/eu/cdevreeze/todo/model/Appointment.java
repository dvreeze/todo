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

package eu.cdevreeze.todo.model;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Immutable appointment record. The name plus start date-time are a unique key.
 *
 * @author Chris de Vreeze
 */
public record Appointment(
        OptionalLong idOption,
        String name,
        Instant start,
        Instant end,
        Optional<Address> addressOption,
        Optional<String> extraInformationOption
) {

    public record NewAppointment(
            String name,
            Instant start,
            Instant end,
            Optional<String> addressNameOption,
            Optional<String> extraInformationOption
    ) {
    }
}
