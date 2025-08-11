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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.entity.AddressEntity;
import eu.cdevreeze.todo.entity.AppointmentEntity;
import eu.cdevreeze.todo.entity.TaskEntity;
import eu.cdevreeze.todo.model.Address;
import eu.cdevreeze.todo.model.Appointment;
import eu.cdevreeze.todo.model.Task;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Default TodoService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
public class DefaultTodoService implements TodoService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private final EntityManager entityManager;

    public DefaultTodoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findAllTasks() {
        String jpaQuery = "select t from Task t";

        return entityManager.createQuery(jpaQuery, TaskEntity.class)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findAllOpenTasks() {
        String jpaQuery = "select t from Task t where closed = :closed";

        return entityManager.createQuery(jpaQuery, TaskEntity.class)
                .setParameter("closed", false)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findAllClosedTasks() {
        String jpaQuery = "select t from Task t where closed = :closed";

        return entityManager.createQuery(jpaQuery, TaskEntity.class)
                .setParameter("closed", true)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findTasksHavingTargetEndAfter(Instant end) {
        String jpaQuery = "select t from Task t where t.targetEnd > :end";

        return entityManager.createQuery(jpaQuery, TaskEntity.class)
                .setParameter("end", end)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findTasksHavingTargetEndBefore(Instant end) {
        String jpaQuery = "select t from Task t where t.targetEnd < :end";

        return entityManager.createQuery(jpaQuery, TaskEntity.class)
                .setParameter("end", end)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional
    public Task addTask(Task task) {
        Preconditions.checkArgument(task.idOption().isEmpty());
        TaskEntity taskEntity = TaskEntity.fromModel(task);

        entityManager.persist(taskEntity);
        entityManager.flush();

        var resultTask = taskEntity.toModel();
        Preconditions.checkArgument(resultTask.idOption().isPresent());
        return resultTask;
    }

    @Override
    @Transactional
    public void deleteAllTasks() {
        entityManager.createQuery("delete from Task t").executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Address> findAllAddresses() {
        String jpaQuery = "select ad from Address ad";

        return entityManager.createQuery(jpaQuery, AddressEntity.class)
                .getResultStream()
                .map(AddressEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional
    public Address addAddress(Address address) {
        Preconditions.checkArgument(address.idOption().isEmpty());
        AddressEntity addressEntity = AddressEntity.fromModel(address);

        entityManager.persist(addressEntity);
        entityManager.flush();

        var resultAddress = addressEntity.toModel();
        Preconditions.checkArgument(resultAddress.idOption().isPresent());
        return resultAddress;
    }

    @Override
    @Transactional
    public void deleteAllAddresses() {
        entityManager.createQuery("delete from Address ad").executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAllAppointments() {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addAttributeNodes("address");

        String jpaQuery = "select ap from Appointment ap";

        return entityManager.createQuery(jpaQuery, AppointmentEntity.class)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAppointmentsBetween(Instant start, Instant end) {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addAttributeNodes("address");

        String jpaQuery = "select ap from Appointment ap where ap.start >= :start and ap.end < :end";

        return entityManager.createQuery(jpaQuery, AppointmentEntity.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAppointmentsEndingAfter(Instant end) {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addAttributeNodes("address");

        String jpaQuery = "select ap from Appointment ap where ap.end > :end";

        return entityManager.createQuery(jpaQuery, AppointmentEntity.class)
                .setParameter("end", end)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAppointmentsEndingBefore(Instant end) {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addAttributeNodes("address");

        String jpaQuery = "select ap from Appointment ap where ap.end < :end";

        return entityManager.createQuery(jpaQuery, AppointmentEntity.class)
                .setParameter("end", end)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional
    public Appointment addAppointment(Appointment.NewAppointment appointment) {
        AppointmentEntity appointmentEntity =
                AppointmentEntity.newAppointmentIgnoringAssociations(appointment);

        if (appointment.addressNameOption().isPresent()) {
            String addressName = appointment.addressNameOption().orElseThrow();

            String addressQuery = "select addr from Address addr where addr.addressName = :addressName";

            AddressEntity address = entityManager.createQuery(addressQuery, AddressEntity.class)
                    .setParameter("addressName", addressName)
                    .getSingleResult();

            appointmentEntity.setAddress(address);
        }

        entityManager.persist(appointmentEntity);
        entityManager.flush();

        var resultAppointment = appointmentEntity.toModel();
        Preconditions.checkArgument(resultAppointment.idOption().isPresent());
        return resultAppointment;
    }

    @Override
    @Transactional
    public void deleteAllAppointments() {
        entityManager.createQuery("delete from Appointment ap").executeUpdate();
    }
}
