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

import eu.cdevreeze.todo.service.AddressService;
import eu.cdevreeze.todo.service.AppointmentService;
import eu.cdevreeze.todo.service.TaskService;
import eu.cdevreeze.todo.web.controller.TodoController;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sanity check for bootstrapping of the entire ApplicationContext as an integration test.
 *
 * @author Chris de Vreeze
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class TodoApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private TodoController todoController;

    @Test
    @DisplayName("context loads")
    void contextLoads() {
        assertThat(entityManager).isNotNull();
        System.out.printf("EntityManager: %s%n", entityManager.getClass());
        assertThat(entityManager.isOpen()).isTrue();

        assertThat(taskService).isNotNull();
        System.out.printf("TaskService: %s%n", taskService.getClass());

        assertThat(addressService).isNotNull();
        System.out.printf("AddressService: %s%n", addressService.getClass());

        assertThat(appointmentService).isNotNull();
        System.out.printf("AppointmentService: %s%n", appointmentService.getClass());

        assertThat(todoController).isNotNull();
        System.out.printf("TodoController: %s%n", todoController.getClass());
    }

    @Test
    @DisplayName("service layer responds as expected")
    void serviceLayerWorks() {
        assertThat(taskService.findAllTasks())
                .isNotEmpty();

        assertThat(addressService.findAllAddresses())
                .isNotEmpty();

        assertThat(appointmentService.findAllAppointments())
                .isNotEmpty();
    }

    @Test
    @DisplayName("web layer responds as expected")
    void webLayerWorks() throws Exception {
        mockMvc.perform(get("/tasks.json").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
