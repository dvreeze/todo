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

package eu.cdevreeze.todo.web.formdata;

import eu.cdevreeze.todo.model.Task;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Optional;

/**
 * Task form data.
 *
 * @author Chris de Vreeze
 */
public class TaskFormData {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Long id;
    private String name;
    private String description;
    private LocalDateTime targetEnd;
    private String extraInformation;
    private boolean closed;

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

    public LocalDateTime getTargetEnd() {
        return targetEnd;
    }

    public void setTargetEnd(LocalDateTime targetEnd) {
        this.targetEnd = targetEnd;
    }

    // Inspired by https://blog.devatlant.com/blog/2018/02/25/how-to-fix-datetime-local-input-in-chrome/
    // Used for HTML input element of type datetime-local

    public String getFormattedTargetEnd() {
        if (targetEnd == null) {
            return null;
        } else {
            return dateTimeFormatter.format(targetEnd);
        }
    }

    public void setFormattedTargetEnd(String formattedTargetEnd) {
        if (formattedTargetEnd == null) {
            setTargetEnd(null);
        } else {
            setTargetEnd(LocalDateTime.parse(formattedTargetEnd, dateTimeFormatter));
        }
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Task toModel() {
        return new Task(
                Optional.ofNullable(this.getId()).stream().mapToLong(id -> id).findFirst(),
                Optional.ofNullable(this.getName()).orElseThrow(),
                Optional.ofNullable(this.getDescription()).orElseThrow(),
                Optional.ofNullable(this.getTargetEnd())
                        .map(dt -> dt.toInstant(ZoneOffset.UTC).with(ChronoField.NANO_OF_SECOND, 0)),
                Optional.ofNullable(this.getExtraInformation()).filter(v -> !v.isBlank()),
                this.isClosed()
        );
    }

    public static TaskFormData fromModel(Task task) {
        TaskFormData formData = new TaskFormData();
        formData.setId(task.idOption().stream().boxed().findFirst().orElse(null));
        formData.setName(task.name());
        formData.setDescription(task.description());
        formData.setTargetEnd(
                task.targetEndOption()
                        .map(instant -> LocalDateTime.ofInstant(instant.with(ChronoField.NANO_OF_SECOND, 0), ZoneOffset.UTC))
                        .orElse(null)
        );
        formData.setExtraInformation(task.extraInformationOption().orElse(null));
        formData.setClosed(task.closed());
        return formData;
    }
}
