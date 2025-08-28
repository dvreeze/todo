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
import eu.cdevreeze.todo.entity.TaskEntity;
import eu.cdevreeze.todo.entity.TaskEntity_;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TaskService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Default TaskService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
public class DefaultTaskService implements TaskService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private final EntityManager entityManager;

    public DefaultTaskService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findAllTasks() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);

        Root<TaskEntity> taskRoot = cq.from(TaskEntity.class);
        cq.select(taskRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findAllOpenTasks() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);

        Root<TaskEntity> taskRoot = cq.from(TaskEntity.class);
        cq.where(cb.equal(taskRoot.get(TaskEntity_.closed), false));
        cq.select(taskRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findAllClosedTasks() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);

        Root<TaskEntity> taskRoot = cq.from(TaskEntity.class);
        cq.where(cb.equal(taskRoot.get(TaskEntity_.closed), true));
        cq.select(taskRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findTasksHavingTargetEndAfter(Instant end) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);

        Root<TaskEntity> taskRoot = cq.from(TaskEntity.class);
        cq.where(cb.greaterThan(taskRoot.get(TaskEntity_.targetEnd), end));
        cq.select(taskRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Task> findTasksHavingTargetEndBefore(Instant end) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);

        Root<TaskEntity> taskRoot = cq.from(TaskEntity.class);
        cq.where(cb.lessThan(taskRoot.get(TaskEntity_.targetEnd), end));
        cq.select(taskRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(TaskEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findTask(long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);

        Root<TaskEntity> taskRoot = cq.from(TaskEntity.class);
        cq.where(cb.equal(taskRoot.get(TaskEntity_.id), id));
        cq.select(taskRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(TaskEntity::toModel)
                .findFirst();
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
    public Task updateTask(Task task) {
        Preconditions.checkArgument(task.idOption().isPresent());

        TaskEntity taskEntity = entityManager.find(TaskEntity.class, task.idOption().orElseThrow());
        Preconditions.checkArgument(taskEntity.getName().equals(task.name()));

        taskEntity.setDescription(task.description());
        taskEntity.setTargetEnd(task.targetEndOption().orElse(null));
        taskEntity.setExtraInformation(task.extraInformationOption().orElse(null));
        taskEntity.setClosed(task.closed());

        entityManager.merge(taskEntity);
        entityManager.flush();

        return taskEntity.toModel();
    }

    @Override
    @Transactional
    public void deleteTask(long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<TaskEntity> cd = cb.createCriteriaDelete(TaskEntity.class);

        cd.where(cb.equal(cd.getRoot().get(TaskEntity_.id), id));

        entityManager.createQuery(cd).executeUpdate();
    }

    @Override
    @Transactional
    public void deleteAllTasks() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<TaskEntity> cd = cb.createCriteriaDelete(TaskEntity.class);

        entityManager.createQuery(cd).executeUpdate();
    }
}
