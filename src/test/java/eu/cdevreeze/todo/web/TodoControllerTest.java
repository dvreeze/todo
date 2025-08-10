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
import eu.cdevreeze.todo.service.TodoService;
import eu.cdevreeze.todo.web.controller.TodoController;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for the TodoController.
 *
 * @author Chris de Vreeze
 */
@WebMvcTest(TodoController.class)
@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    @Nested
    @DisplayName("GET /tasks.json endpoint tests")
    class GetTasksTest {

        private final Instant now = Instant.now();

        @Test
        void shouldGetAllTasks() throws Exception {
            // Given
            when(todoService.findAllTasks()).thenReturn(testTasks());

            // When/then
            mockMvc.perform(get("/tasks.json").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(expectedTasksJson()));
            verify(todoService, times(1)).findAllTasks();
        }

        private ImmutableList<Task> testTasks() {
            return ImmutableList.of(
                    new Task(
                            OptionalLong.of(1),
                            "opruimen kamer",
                            "opruimen kamer",
                            Optional.of(now.plus(1, ChronoUnit.DAYS)),
                            Optional.empty(),
                            false
                    ),
                    new Task(
                            OptionalLong.of(2),
                            "stofzuigen kamer",
                            "stofzuigen kamer",
                            Optional.of(now.plus(2, ChronoUnit.DAYS)),
                            Optional.empty(),
                            false
                    )
            );
        }

        private String expectedTasksJson() {
            return
                    String.format("""
                                    [
                                      {
                                        "idOption": 1,
                                        "name": "opruimen kamer",
                                        "description": "opruimen kamer",
                                        "targetEndOption": "%s",
                                        "extraInformationOption": null,
                                        "closed": false
                                      },
                                      {
                                        "idOption": 2,
                                        "name": "stofzuigen kamer",
                                        "description": "stofzuigen kamer",
                                        "targetEndOption": "%s",
                                        "extraInformationOption": null,
                                        "closed": false
                                      }
                                    ]
                                    """,
                            now.plus(1, ChronoUnit.DAYS),
                            now.plus(2, ChronoUnit.DAYS));
        }
    }
}
