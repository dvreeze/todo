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
import eu.cdevreeze.todo.entity.AppointmentEntity;
import eu.cdevreeze.todo.entity.TaskEntity;
import eu.cdevreeze.todo.model.Appointment;
import eu.cdevreeze.todo.model.Task;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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

    private final EntityManagerFactory emf;

    public DefaultTodoService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    @Transactional
    public ImmutableList<Task> findAllTasks() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpaQuery = "select t from Task t";

            return em.createQuery(jpaQuery, TaskEntity.class)
                    .getResultStream()
                    .map(TaskEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    public ImmutableList<Task> filterTasks(boolean isClosed) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpaQuery = "select t from Task t where closed = :closed";

            return em.createQuery(jpaQuery, TaskEntity.class)
                    .setParameter("closed", isClosed)
                    .getResultStream()
                    .map(TaskEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    public ImmutableList<Task> findTasksHavingTargetEndAfter(Instant end) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpaQuery = "select t from Task t where t.targetEnd > :end";

            return em.createQuery(jpaQuery, TaskEntity.class)
                    .setParameter("end", end)
                    .getResultStream()
                    .map(TaskEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    public ImmutableList<Task> findTasksHavingTargetEndBefore(Instant end) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpaQuery = "select t from Task t where t.targetEnd < :end";

            return em.createQuery(jpaQuery, TaskEntity.class)
                    .setParameter("end", end)
                    .getResultStream()
                    .map(TaskEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    @Transactional
    public ImmutableList<Appointment> findAllAppointments() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityGraph<AppointmentEntity> eg = em.createEntityGraph(AppointmentEntity.class);
            eg.addAttributeNodes("address");

            String jpaQuery = "select ap from Appointment ap";

            return em.createQuery(jpaQuery, AppointmentEntity.class)
                    .setHint("jakarta.persistence.fetchgraph", eg)
                    .getResultStream()
                    .map(AppointmentEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    public ImmutableList<Appointment> findAppointmentsBetween(Instant start, Instant end) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityGraph<AppointmentEntity> eg = em.createEntityGraph(AppointmentEntity.class);
            eg.addAttributeNodes("address");

            String jpaQuery = "select ap from Appointment ap where ap.start >= :start and ap.end > :end";

            return em.createQuery(jpaQuery, AppointmentEntity.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setHint("jakarta.persistence.fetchgraph", eg)
                    .getResultStream()
                    .map(AppointmentEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    public ImmutableList<Appointment> findAppointmentsEndingAfter(Instant end) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityGraph<AppointmentEntity> eg = em.createEntityGraph(AppointmentEntity.class);
            eg.addAttributeNodes("address");

            String jpaQuery = "select ap from Appointment ap where ap.end > :end";

            return em.createQuery(jpaQuery, AppointmentEntity.class)
                    .setParameter("end", end)
                    .setHint("jakarta.persistence.fetchgraph", eg)
                    .getResultStream()
                    .map(AppointmentEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Override
    public ImmutableList<Appointment> findAppointmentsEndingBefore(Instant end) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityGraph<AppointmentEntity> eg = em.createEntityGraph(AppointmentEntity.class);
            eg.addAttributeNodes("address");

            String jpaQuery = "select ap from Appointment ap where ap.end < :end";

            return em.createQuery(jpaQuery, AppointmentEntity.class)
                    .setParameter("end", end)
                    .setHint("jakarta.persistence.fetchgraph", eg)
                    .getResultStream()
                    .map(AppointmentEntity::toModel)
                    .collect(ImmutableList.toImmutableList());
        }
    }
}
