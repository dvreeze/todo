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
import eu.cdevreeze.todo.model.Task;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
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
@DataJpaTest
@TestPropertySource("/application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskServiceTest {

    // TODO Use SpringExtension (and automatic TX rollback)

    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private TaskService taskService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void beforeEach() {
        this.taskService = new DefaultTaskService(entityManager.getEntityManager());
        this.taskService.deleteAllTasks(); // Much better: Spring-offered automatic rollback
    }

    @AfterEach
    void afterEach() {
        this.taskService = null;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("should return all tasks")
    void shouldReturnAllTasks() {
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
    @DisplayName("should add task")
    void shouldAddTask() {
        addSomeTasks();
        int initSize = taskService.findAllTasks().size();

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
        assertThat(taskService.findAllTasks()).hasSize(initSize + 1);
    }

    private final Instant now = Instant.now();

    private void addSomeTasks() {
        taskService.addTask(new Task(
                OptionalLong.empty(),
                "opruimen kamer",
                "opruimen kamer",
                Optional.of(now.plus(1, ChronoUnit.DAYS)),
                Optional.empty(),
                true
        ));
        taskService.addTask(new Task(
                OptionalLong.empty(),
                "stofzuigen kamer",
                "stofzuigen kamer",
                Optional.of(now.plus(2, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        ));
        taskService.addTask(new Task(
                OptionalLong.empty(),
                "opruimen slaapkamer",
                "opruimen slaapkamer",
                Optional.of(now.plus(7, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        ));
    }
}
