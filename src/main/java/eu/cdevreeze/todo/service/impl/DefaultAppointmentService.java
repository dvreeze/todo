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

package eu.cdevreeze.todo.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.entity.AddressEntity;
import eu.cdevreeze.todo.entity.AddressEntity_;
import eu.cdevreeze.todo.entity.AppointmentEntity;
import eu.cdevreeze.todo.entity.AppointmentEntity_;
import eu.cdevreeze.todo.model.Appointment;
import eu.cdevreeze.todo.service.AppointmentService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Default AppointmentService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
public class DefaultAppointmentService implements AppointmentService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private final EntityManager entityManager;

    public DefaultAppointmentService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAllAppointments() {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addSubgraph(AppointmentEntity_.address);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AppointmentEntity> cq = cb.createQuery(AppointmentEntity.class);

        Root<AppointmentEntity> appointmentRoot = cq.from(AppointmentEntity.class);
        cq.select(appointmentRoot);

        return entityManager.createQuery(cq)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAppointmentsBetween(Instant start, Instant end) {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addSubgraph(AppointmentEntity_.address);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AppointmentEntity> cq = cb.createQuery(AppointmentEntity.class);

        Root<AppointmentEntity> appointmentRoot = cq.from(AppointmentEntity.class);
        cq.where(
                cb.and(
                        cb.greaterThanOrEqualTo(appointmentRoot.get(AppointmentEntity_.start), start),
                        cb.lessThan(appointmentRoot.get(AppointmentEntity_.end), end)
                )
        );
        cq.select(appointmentRoot);

        return entityManager.createQuery(cq)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAppointmentsEndingAfter(Instant end) {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addSubgraph(AppointmentEntity_.address);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AppointmentEntity> cq = cb.createQuery(AppointmentEntity.class);

        Root<AppointmentEntity> appointmentRoot = cq.from(AppointmentEntity.class);
        cq.where(cb.greaterThan(appointmentRoot.get(AppointmentEntity_.end), end));
        cq.select(appointmentRoot);

        return entityManager.createQuery(cq)
                .setHint("jakarta.persistence.fetchgraph", eg)
                .getResultStream()
                .map(AppointmentEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Appointment> findAppointmentsEndingBefore(Instant end) {
        EntityGraph<AppointmentEntity> eg = entityManager.createEntityGraph(AppointmentEntity.class);
        eg.addSubgraph(AppointmentEntity_.address);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AppointmentEntity> cq = cb.createQuery(AppointmentEntity.class);

        Root<AppointmentEntity> appointmentRoot = cq.from(AppointmentEntity.class);
        cq.where(cb.lessThan(appointmentRoot.get(AppointmentEntity_.end), end));
        cq.select(appointmentRoot);

        return entityManager.createQuery(cq)
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

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> addressRoot = cq.from(AddressEntity.class);
            cq.where(cb.equal(addressRoot.get(AddressEntity_.addressName), addressName));
            cq.select(addressRoot);

            AddressEntity address = entityManager.createQuery(cq).getSingleResult();

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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<AppointmentEntity> cd = cb.createCriteriaDelete(AppointmentEntity.class);

        entityManager.createQuery(cd).executeUpdate();
    }
}
