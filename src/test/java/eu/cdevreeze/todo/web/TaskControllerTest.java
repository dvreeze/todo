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

package eu.cdevreeze.todo.web;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TaskService;
import eu.cdevreeze.todo.web.controller.TaskController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for the TaskController.
 *
 * @author Chris de Vreeze
 */
@WebMvcTest(TaskController.class)
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Nested
    @DisplayName("GET /tasks endpoint tests")
    class GetTasksTest {

        private final Instant now = Instant.now();

        @Test
        @DisplayName("should get all tasks")
        void shouldGetAllTasks() throws Exception {
            // Given
            ImmutableList<Task> expectedTasks = testTasks();
            when(taskService.findAllTasks()).thenReturn(expectedTasks);

            // When/then
            mockMvc.perform(get("/tasks").accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(view().name("tasks"))
                    .andExpect(model().attribute("tasks", expectedTasks))
                    .andExpect(model().attribute("title", "Tasks"));
            verify(taskService, times(1)).findAllTasks();
        }

        @Test
        @DisplayName("should get all open tasks")
        void shouldGetAllOpenTasks() throws Exception {
            // Given
            ImmutableList<Task> expectedTasks =
                    testTasks().stream().filter(t -> !t.closed()).collect(ImmutableList.toImmutableList());
            when(taskService.findAllOpenTasks()).thenReturn(expectedTasks);

            // When/then
            mockMvc.perform(get("/tasks").param("closed", String.valueOf(false)).accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(view().name("tasks"))
                    .andExpect(model().attribute("tasks", expectedTasks))
                    .andExpect(model().attribute("title", "Open tasks"));
            verify(taskService, times(1)).findAllOpenTasks();
        }

        @Test
        @DisplayName("should get all closed tasks")
        void shouldGetAllClosedTasks() throws Exception {
            // Given
            ImmutableList<Task> expectedTasks =
                    testTasks().stream().filter(Task::closed).collect(ImmutableList.toImmutableList());
            when(taskService.findAllClosedTasks()).thenReturn(expectedTasks);

            // When/then
            mockMvc.perform(get("/tasks").param("closed", String.valueOf(true)).accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(view().name("tasks"))
                    .andExpect(model().attribute("tasks", expectedTasks))
                    .andExpect(model().attribute("title", "Closed tasks"));
            verify(taskService, times(1)).findAllClosedTasks();
        }

        private ImmutableList<Task> testTasks() {
            return ImmutableList.of(
                    new Task(
                            OptionalLong.of(1),
                            "opruimen kamer (1)",
                            "opruimen kamer (1)",
                            Optional.of(now.plus(-100, ChronoUnit.DAYS)),
                            Optional.empty(),
                            true
                    ),
                    new Task(
                            OptionalLong.of(2),
                            "opruimen kamer (2)",
                            "opruimen kamer (2)",
                            Optional.of(now.plus(1, ChronoUnit.DAYS)),
                            Optional.empty(),
                            false
                    ),
                    new Task(
                            OptionalLong.of(3),
                            "stofzuigen kamer (1)",
                            "stofzuigen kamer (1)",
                            Optional.of(now.plus(2, ChronoUnit.DAYS)),
                            Optional.empty(),
                            false
                    )
            );
        }
    }
}
