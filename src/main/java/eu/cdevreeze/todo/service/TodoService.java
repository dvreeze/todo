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

package eu.cdevreeze.todo.service;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.model.Address;
import eu.cdevreeze.todo.model.Appointment;
import eu.cdevreeze.todo.model.Task;

import java.time.Instant;

/**
 * API contract for a to-do service, managing tasks and appointments of the logged-in user.
 *
 * @author Chris de Vreeze
 */
public interface TodoService {

    ImmutableList<Task> findAllTasks();

    ImmutableList<Task> filterTasks(boolean isClosed);

    ImmutableList<Task> findTasksHavingTargetEndAfter(Instant end);

    ImmutableList<Task> findTasksHavingTargetEndBefore(Instant end);

    Task addTask(Task task);

    void deleteAllTasks();

    ImmutableList<Address> findAllAddresses();

    Address addAddress(Address address);

    void deleteAllAddresses();

    ImmutableList<Appointment> findAllAppointments();

    ImmutableList<Appointment> findAppointmentsBetween(Instant start, Instant end);

    ImmutableList<Appointment> findAppointmentsEndingAfter(Instant end);

    ImmutableList<Appointment> findAppointmentsEndingBefore(Instant end);

    Appointment addAppointment(Appointment.NewAppointment appointment);

    void deleteAllAppointments();
}
