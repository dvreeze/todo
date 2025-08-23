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
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the TaskService.
 * <p>
 * See <a href="https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/">Spring Boot and Testcontainers</a>
 * for the use of PostgreSQL test containers in Spring Boot tests.
 * <p>
 * Note that changes to the database should be automatically rolled back, using SpringExtension.
 * See <a href="https://relentlesscoding.com/posts/automatic-rollback-of-transactions-in-spring-tests/">rollback of transactions in Spring tests</a>
 * for more background on that. This has not been realized yet in this test class.
 *
 * @author Chris de Vreeze
 */
class TaskServiceTest extends AbstractServiceTest {

    // TODO Use SpringExtension (and automatic TX rollback)

    private TaskService taskService;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        this.taskService = new DefaultTaskService(entityManager.getEntityManager());
        this.entityManager.clear(); // Much better: Spring-offered automatic rollback
        this.entityManager.flush();
    }

    @AfterEach
    void afterEach() {
        this.taskService = null;
    }

    @Test
    @DisplayName("should return all tasks")
    void shouldReturnAllTasks() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        addSomeTasks();

        ImmutableList<Task> tasks = taskService.findAllTasks();

        assertThat(tasks)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .satisfies(taskList ->
                        assertThat(taskList).extracting(Task::name)
                                .isEqualTo(List.of("opruimen kamer", "stofzuigen kamer", "opruimen slaapkamer"))
                );
    }

    @Test
    @DisplayName("should return all open tasks")
    void shouldReturnAllOpenTasks() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        addSomeTasks();

        ImmutableList<Task> tasks = taskService.findAllOpenTasks();

        assertThat(tasks)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .satisfies(taskList ->
                        assertThat(taskList).extracting(Task::name)
                                .isEqualTo(List.of("stofzuigen kamer", "opruimen slaapkamer"))
                );
    }

    @Test
    @DisplayName("should return all closed tasks")
    void shouldReturnAllClosedTasks() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        addSomeTasks();

        ImmutableList<Task> tasks = taskService.findAllClosedTasks();

        assertThat(tasks)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .satisfies(taskList ->
                        assertThat(taskList).extracting(Task::name)
                                .isEqualTo(List.of("opruimen kamer"))
                );
    }

    @Test
    @DisplayName("should return all tasks having target end after")
    void shouldReturnTasksHavingTargetEndAfter() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        addSomeTasks();

        Instant end = now.plus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS);
        ImmutableList<Task> tasks = taskService.findTasksHavingTargetEndAfter(end);

        assertThat(tasks)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .satisfies(taskList ->
                        assertThat(taskList).extracting(Task::name)
                                .isEqualTo(List.of("stofzuigen kamer", "opruimen slaapkamer"))
                );
    }

    @Test
    @DisplayName("should return all tasks having target end before")
    void shouldReturnTasksHavingTargetEndBefore() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        addSomeTasks();

        Instant end = now.plus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS);
        ImmutableList<Task> tasks = taskService.findTasksHavingTargetEndBefore(end);

        assertThat(tasks)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .satisfies(taskList ->
                        assertThat(taskList).extracting(Task::name)
                                .isEqualTo(List.of("opruimen kamer"))
                );
    }

    @Test
    @DisplayName("should return a task with the given ID")
    void shouldReturnTaskByPrimaryKey() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        List<Task> addedTasks = addSomeTasks();
        Preconditions.checkArgument(addedTasks.size() >= 3);
        Task selectedTask = addedTasks.get(1);
        Preconditions.checkArgument(selectedTask.name().equals("stofzuigen kamer"));

        Optional<Task> taskOption = taskService.findTask(selectedTask.idOption().orElseThrow());

        assertThat(taskOption)
                .isNotNull()
                .isNotEmpty()
                .get()
                .satisfies(task -> {
                    assertThat(task.idOption()).isEqualTo(selectedTask.idOption());
                    assertThat(task.name()).isEqualTo(selectedTask.name());
                    assertThat(task.description()).isEqualTo(selectedTask.description());
                    assertThat(task.targetEndOption()).isEqualTo(selectedTask.targetEndOption());
                    assertThat(task.extraInformationOption()).isEqualTo(selectedTask.extraInformationOption());
                    assertThat(task.closed()).isEqualTo(selectedTask.closed());

                    assertThat(task).isEqualTo(selectedTask);
                });
    }

    @Test
    @DisplayName("should add task")
    void shouldAddTask() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        List<Task> addedTasks = addSomeTasks();
        int initSize = addedTasks.size();

        Task newTask = new Task(
                OptionalLong.empty(),
                "opruimen",
                "opruimen van oude tijdschriften",
                Optional.of(Instant.now().plus(14, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        );

        Task task = taskService.addTask(newTask);

        assertThat(task)
                .satisfies(t -> {
                    assertThat(t.idOption()).isPresent();
                    assertThat(t.name()).isEqualTo(newTask.name());
                    assertThat(t.description()).isEqualTo(newTask.description());
                    assertThat(t.targetEndOption()).isEqualTo(newTask.targetEndOption());
                    assertThat(t.extraInformationOption()).isEqualTo(newTask.extraInformationOption());
                    assertThat(t.closed()).isEqualTo(newTask.closed());
                });
        assertThat(
                entityManager.getEntityManager().createQuery("select t from Task t").getResultList()
        ).hasSize(initSize + 1);
    }

    @Test
    @DisplayName("should update task")
    void shouldUpdateTask() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        List<Task> addedTasks = addSomeTasks();
        int initSize = addedTasks.size();
        Preconditions.checkArgument(initSize >= 3);

        Task taskToUpdate = addedTasks.stream().filter(t -> t.name().equals("stofzuigen kamer")).findFirst().orElseThrow();

        Task taskUpdate = new Task(
                taskToUpdate.idOption(),
                taskToUpdate.name(),
                taskToUpdate.description(),
                taskToUpdate.targetEndOption(),
                Optional.of("de slaapkamers zijn ook meegenomen bij het stofzuigen"),
                true
        );

        Task task = taskService.updateTask(taskUpdate);

        assertThat(task)
                .satisfies(t -> {
                    assertThat(t.idOption()).isEqualTo(taskToUpdate.idOption());
                    assertThat(t.name()).isEqualTo(taskToUpdate.name());
                    assertThat(t.description()).isEqualTo(taskToUpdate.description());
                    assertThat(t.targetEndOption()).isEqualTo(taskToUpdate.targetEndOption());
                    assertThat(t.extraInformationOption()).isEqualTo(Optional.of("de slaapkamers zijn ook meegenomen bij het stofzuigen"));
                    assertThat(t.closed()).isEqualTo(true);
                });
        assertThat(
                entityManager.getEntityManager().createQuery("select t from Task t").getResultList()
        ).hasSize(initSize);
    }

    @Test
    @DisplayName("should delete all tasks")
    void shouldDeleteAllTasks() {
        System.out.printf("PostgreSQL container name: %s%n", postgres.getContainerName());

        List<Task> addedTasks = addSomeTasks();
        int initSize = addedTasks.size();
        Preconditions.checkArgument(initSize >= 3);

        taskService.deleteAllTasks();

        assertThat(
                entityManager.getEntityManager().createQuery("select t from Task t").getResultList()
        ).isEmpty();
    }

    private final Instant now = Instant.now();

    private List<Task> addSomeTasks() {
        return List.of(
                entityManager.persistFlushFind(
                        TaskEntity.fromModel(
                                new Task(
                                        OptionalLong.empty(),
                                        "opruimen kamer",
                                        "opruimen kamer",
                                        Optional.of(now.plus(1, ChronoUnit.DAYS)),
                                        Optional.empty(),
                                        true
                                )
                        )
                ).toModel(),
                entityManager.persistFlushFind(
                        TaskEntity.fromModel(
                                new Task(
                                        OptionalLong.empty(),
                                        "stofzuigen kamer",
                                        "stofzuigen kamer",
                                        Optional.of(now.plus(2, ChronoUnit.DAYS)),
                                        Optional.empty(),
                                        false
                                )
                        )
                ).toModel(),
                entityManager.persistFlushFind(
                        TaskEntity.fromModel(
                                new Task(
                                        OptionalLong.empty(),
                                        "opruimen slaapkamer",
                                        "opruimen slaapkamer",
                                        Optional.of(now.plus(7, ChronoUnit.DAYS)),
                                        Optional.empty(),
                                        false
                                )
                        )
                ).toModel()
        );
    }
}
