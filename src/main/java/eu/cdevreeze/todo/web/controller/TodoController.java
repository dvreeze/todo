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

package eu.cdevreeze.todo.web.controller;

import eu.cdevreeze.todo.model.Appointment;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TodoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Web MVC controller for tasks and appointments.
 *
 * @author Chris de Vreeze
 */
@RestController
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/tasks.json")
    public List<Task> findAllTasks() {
        return todoService.findAllTasks();
    }

    @GetMapping("/appointments.json")
    public List<Appointment> findAllAppointments() {
        return todoService.findAllAppointments();
    }
}
