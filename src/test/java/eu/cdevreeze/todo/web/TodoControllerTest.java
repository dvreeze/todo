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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.AddressService;
import eu.cdevreeze.todo.service.AppointmentService;
import eu.cdevreeze.todo.service.TaskService;
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
    private TaskService taskService;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private AppointmentService appointmentService;

    @Nested
    @DisplayName("GET /tasks.json endpoint tests")
    class GetTasksTest {

        private final Instant now = Instant.now();

        @Test
        @DisplayName("should get all tasks")
        void shouldGetAllTasks() throws Exception {
            // Given
            when(taskService.findAllTasks()).thenReturn(testTasks());

            // When/then
            mockMvc.perform(get("/tasks.json").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(expectedTasksJsonString()));
            verify(taskService, times(1)).findAllTasks();
        }

        @Test
        @DisplayName("should get all open tasks")
        void shouldGetAllOpenTasks() throws Exception {
            // Given
            when(taskService.findAllOpenTasks()).thenReturn(
                    testTasks().stream().filter(t -> !t.closed()).collect(ImmutableList.toImmutableList())
            );

            // When/then
            mockMvc.perform(get("/tasks.json").param("closed", "false").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(expectedOpenTasksJsonString()));
            verify(taskService, times(1)).findAllOpenTasks();
        }

        @Test
        @DisplayName("should get all closed tasks")
        void shouldGetAllClosedTasks() throws Exception {
            // Given
            when(taskService.findAllClosedTasks()).thenReturn(
                    testTasks().stream().filter(Task::closed).collect(ImmutableList.toImmutableList())
            );

            // When/then
            mockMvc.perform(get("/tasks.json").param("closed", "true").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(expectedClosedTasksJsonString()));
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

        private ArrayNode expectedTasksJsonArray() {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode result = objectMapper.createArrayNode();

            ObjectNode task1Json = objectMapper.createObjectNode();
            task1Json.put("idOption", 1);
            task1Json.put("name", "opruimen kamer (1)");
            task1Json.put("description", "opruimen kamer (1)");
            task1Json.put("targetEndOption", now.plus(-100, ChronoUnit.DAYS).toString());
            task1Json.putNull("extraInformationOption");
            task1Json.put("closed", true);
            result.add(task1Json);

            ObjectNode task2Json = objectMapper.createObjectNode();
            task2Json.put("idOption", 2);
            task2Json.put("name", "opruimen kamer (2)");
            task2Json.put("description", "opruimen kamer (2)");
            task2Json.put("targetEndOption", now.plus(1, ChronoUnit.DAYS).toString());
            task2Json.putNull("extraInformationOption");
            task2Json.put("closed", false);
            result.add(task2Json);

            ObjectNode task3Json = objectMapper.createObjectNode();
            task3Json.put("idOption", 3);
            task3Json.put("name", "stofzuigen kamer (1)");
            task3Json.put("description", "stofzuigen kamer (1)");
            task3Json.put("targetEndOption", now.plus(2, ChronoUnit.DAYS).toString());
            task3Json.putNull("extraInformationOption");
            task3Json.put("closed", false);
            result.add(task3Json);

            Preconditions.checkArgument(ImmutableList.copyOf(result.elements()).size() == 3);
            return result;
        }

        private String expectedTasksJsonString() {
            return expectedTasksJsonArray().toPrettyString();
        }

        private String expectedOpenTasksJsonString() {
            ArrayNode result = expectedTasksJsonArray().deepCopy();
            result.removeIf(json -> json.isObject() && json.optional("closed").equals(Optional.of(BooleanNode.TRUE)));
            Preconditions.checkArgument(ImmutableList.copyOf(result.elements()).size() == 2);
            return result.toPrettyString();
        }

        private String expectedClosedTasksJsonString() {
            ArrayNode result = expectedTasksJsonArray().deepCopy();
            result.removeIf(json -> json.isObject() && json.optional("closed").equals(Optional.of(BooleanNode.FALSE)));
            Preconditions.checkArgument(ImmutableList.copyOf(result.elements()).size() == 1);
            return result.toPrettyString();
        }
    }
}
