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

import com.google.common.base.Preconditions;
import eu.cdevreeze.todo.model.Address;
import eu.cdevreeze.todo.model.Appointment;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TodoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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

    @GetMapping(value = "/tasks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Task> findAllTasks(
            @RequestParam(name = "closed", required = false) Boolean isClosed
    ) {
        if (isClosed == null) {
            return todoService.findAllTasks();
        } else {
            if (isClosed) {
                return todoService.findAllClosedTasks();
            } else {
                return todoService.findAllOpenTasks();
            }
        }
    }

    @PostMapping(value = "/tasks.json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Task addTask(@RequestBody Task task) {
        return todoService.addTask(task);
    }

    @GetMapping(value = "/addresses.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Address> findAllAddresses() {
        return todoService.findAllAddresses();
    }

    @PostMapping(value = "/addresses.json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Address addAddress(@RequestBody Address address) {
        return todoService.addAddress(address);
    }

    @GetMapping(value = "/appointments.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Appointment> findAllAppointments(
            @RequestParam(name = "start", required = false) Instant start,
            @RequestParam(name = "end", required = false) Instant end
    ) {
        if (start == null) {
            Preconditions.checkArgument(end == null);
            return todoService.findAllAppointments();
        } else {
            Preconditions.checkArgument(end != null);
            return todoService.findAppointmentsBetween(start, end);
        }
    }

    @PostMapping(value = "/appointments.json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Appointment addAppointment(@RequestBody Appointment.NewAppointment appointment) {
        return todoService.addAppointment(appointment);
    }
}
