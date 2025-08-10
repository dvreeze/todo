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
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the TodoService.
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
class TodoServiceTest {

    // TODO Use SpringExtension (and automatic TX rollback)

    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private TodoService todoService;

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
        this.todoService = new DefaultTodoService(entityManager.getEntityManager());
        this.todoService.deleteAllTasks(); // Much better: Spring-offered automatic rollback
    }

    @AfterEach
    void afterEach() {
        this.todoService = null;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldReturnAllTasks() {
        addSomeTasks();

        ImmutableList<Task> tasks = todoService.findAllTasks();

        assertThat(tasks)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .allMatch(task -> Stream.of("opruimen", "stofzuigen").anyMatch(str -> task.name().contains(str)));
    }

    @Test
    void shouldAddTask() {
        addSomeTasks();
        int initSize = todoService.findAllTasks().size();

        Task newTask = new Task(
                OptionalLong.empty(),
                "opruimen",
                "opruimen van oude tijdschriften",
                Optional.of(Instant.now().plus(14, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        );

        Task task = todoService.addTask(newTask);

        assertThat(task)
                .satisfies(t -> {
                    assertThat(t.idOption()).isPresent();
                    assertThat(t.name()).isEqualTo(newTask.name());
                    assertThat(t.description()).isEqualTo(newTask.description());
                    assertThat(t.targetEndOption()).isEqualTo(newTask.targetEndOption());
                    assertThat(t.extraInformationOption()).isEqualTo(newTask.extraInformationOption());
                    assertThat(t.closed()).isEqualTo(newTask.closed());
                });
        assertThat(todoService.findAllTasks()).hasSize(initSize + 1);
    }

    private void addSomeTasks() {
        Instant now = Instant.now();
        todoService.addTask(new Task(
                OptionalLong.empty(),
                "opruimen kamer",
                "opruimen kamer",
                Optional.of(now.plus(1, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        ));
        todoService.addTask(new Task(
                OptionalLong.empty(),
                "stofzuigen kamer",
                "stofzuigen kamer",
                Optional.of(now.plus(2, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        ));
        todoService.addTask(new Task(
                OptionalLong.empty(),
                "opruimen slaapkamer",
                "opruimen slaapkamer",
                Optional.of(now.plus(7, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        ));
    }
}
