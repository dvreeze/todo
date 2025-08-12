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
import eu.cdevreeze.todo.entity.TaskEntity;
import eu.cdevreeze.todo.model.Task;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
}
