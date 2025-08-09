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

package eu.cdevreeze.todo;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the TodoService. Requires the database server to run.
 * <p>
 * Note that changes to the database are automatically rolled back.
 * See <a href="https://relentlesscoding.com/posts/automatic-rollback-of-transactions-in-spring-tests/">rollback of transactions in Spring tests</a>
 * for more background on that.
 *
 * @author Chris de Vreeze
 */
@SpringBootTest
@Transactional
class TodoServiceTests {

    @Autowired
    private TodoService todoService;

    @Test
    void testFindingAllTasks() {
        ImmutableList<Task> tasks = todoService.findAllTasks();

        assertThat(tasks).isNotNull();
        assertThat(tasks).isNotEmpty();
    }

    @Test
    void testAddTask() {
        Task newTask = new Task(
                OptionalLong.empty(),
                "opruimen",
                "opruimen van oude tijdschriften",
                Optional.of(Instant.now().plus(14, ChronoUnit.DAYS)),
                Optional.empty(),
                false
        );

        Task task = todoService.addTask(newTask);

        assertThat(task.idOption()).isPresent();
        assertThat(task.name()).isEqualTo(newTask.name());
        assertThat(task.description()).isEqualTo(newTask.description());
        assertThat(task.targetEndOption()).isEqualTo(newTask.targetEndOption());
        assertThat(task.extraInformationOption()).isEqualTo(newTask.extraInformationOption());
        assertThat(task.closed()).isEqualTo(newTask.closed());
    }
}
