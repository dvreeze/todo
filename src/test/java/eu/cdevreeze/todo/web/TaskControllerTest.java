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

import com.google.common.base.Preconditions;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for the TaskController.
 *
 * @author Chris de Vreeze
 */
@WebMvcTest(TaskController.class)
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private final static Instant now = Instant.now();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Nested
    @DisplayName("GET /tasks endpoint tests")
    class GetTasksTest {

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
                    .andExpect(model().attribute("title", "Tasks"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
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
                    .andExpect(model().attribute("title", "Open tasks"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
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
                    .andExpect(model().attribute("title", "Closed tasks"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
            verify(taskService, times(1)).findAllClosedTasks();
        }
    }

    @Nested
    @DisplayName("GET /newTask endpoint tests")
    class GetNewTaskFormTest {

        @Test
        @DisplayName("should get an empty form to add a new task")
        void shouldGetNewTaskForm() throws Exception {
            // When/then
            mockMvc.perform(get("/newTask").accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(view().name("newTask"))
                    .andExpect(model().attributeExists("newTask"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
            verifyNoInteractions(taskService);
        }
    }

    @Nested
    @DisplayName("POST /newTask endpoint tests")
    class PostNewTaskTest {

        @Test
        @DisplayName("should add a new task")
        void shouldAddTask() throws Exception {
            // Given
            LocalDateTime localDateTime = LocalDate.of(2025, 9, 30).atStartOfDay();
            Instant expectedInstant = localDateTime.toInstant(ZoneOffset.UTC);

            Task expectedTask = new Task(
                    OptionalLong.of(11),
                    "krant opzeggen",
                    "krant opzeggen",
                    Optional.of(expectedInstant),
                    Optional.empty(),
                    false
            );
            when(taskService.addTask(any(Task.class))).thenReturn(expectedTask);

            // When/then
            mockMvc.perform(
                            post("/newTask")
                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                    .param("name", "krant opzeggen")
                                    .param("description", "krant opzeggen")
                                    .param("targetEnd", localDateTime.toString())
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/tasks"));
            verify(taskService, times(1)).addTask(eq(expectedTask.withoutId()));
        }
    }

    @Nested
    @DisplayName("GET /updateTask endpoint tests")
    class GetUpdateTaskFormTest {

        @Test
        @DisplayName("should get a form to update an existing task")
        void shouldGetUpdateTaskForm() throws Exception {
            // Given
            long taskId = 2;
            Task expectedOldTask = testTasks().get(1);
            Preconditions.checkArgument(expectedOldTask.idOption().equals(OptionalLong.of(taskId)));
            when(taskService.findTask(taskId)).thenReturn(Optional.of(expectedOldTask));

            // When/then
            mockMvc.perform(get("/updateTask").param("id", String.valueOf(taskId)).accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(view().name("updateTask"))
                    .andExpect(model().attributeExists("task"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
            verify(taskService, times(1)).findTask(taskId);
            verify(taskService, never()).updateTask(any(Task.class));
        }
    }

    @Nested
    @DisplayName("POST /updateTask endpoint tests")
    class PostUpdateTaskTest {

        @Test
        @DisplayName("should update an existing task")
        void shouldUpdateTask() throws Exception {
            // Given
            long taskId = 2;
            Task expectedOldTask = testTasks().get(1);
            Preconditions.checkArgument(expectedOldTask.idOption().equals(OptionalLong.of(taskId)));

            LocalDateTime localDateTime = LocalDate.of(2025, 9, 30).atStartOfDay();
            Instant expectedInstant = localDateTime.toInstant(ZoneOffset.UTC);

            Task expectedNewTask = new Task(
                    expectedOldTask.idOption(),
                    expectedOldTask.name(),
                    expectedOldTask.description(),
                    Optional.of(expectedInstant),
                    Optional.of("Ruim op tijd gedaan"),
                    true
            );
            when(taskService.updateTask(expectedNewTask)).thenReturn(expectedNewTask);

            // When/then
            mockMvc.perform(
                            post("/updateTask")
                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                    .param("id", String.valueOf(taskId))
                                    .param("name", "opruimen kamer (2)")
                                    .param("description", "opruimen kamer (2)")
                                    .param("targetEnd", localDateTime.toString())
                                    .param("extraInformation", "Ruim op tijd gedaan")
                                    .param("closed", "true")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/tasks"));
            verify(taskService, times(1)).updateTask(eq(expectedNewTask));
        }
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
