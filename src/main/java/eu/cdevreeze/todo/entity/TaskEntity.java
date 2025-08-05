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

package eu.cdevreeze.todo.entity;

import eu.cdevreeze.todo.model.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Task JPA entity.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Task")
public class TaskEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "target_end")
    private Instant targetEnd;

    @Column(name = "extra_information")
    private String extraInformation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTargetEnd() {
        return targetEnd;
    }

    public void setTargetEnd(Instant targetEnd) {
        this.targetEnd = targetEnd;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }

    public Task toModel() {
        return new Task(
                Stream.ofNullable(id).mapToLong(i -> i).findFirst(),
                name,
                description,
                Optional.ofNullable(targetEnd),
                Optional.ofNullable(extraInformation)
        );
    }
}
