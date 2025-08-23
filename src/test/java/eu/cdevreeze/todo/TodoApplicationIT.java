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
import eu.cdevreeze.todo.web.controller.TodoRestController;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity check for bootstrapping of the entire ApplicationContext as an integration test.
 *
 * @author Chris de Vreeze
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoApplicationIT {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private TodoRestController todoRestController;

    @LocalServerPort
    private int localServerPort;

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

        assertThat(todoRestController).isNotNull();
        System.out.printf("TodoRestController: %s%n", todoRestController.getClass());
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
    void webLayerWorks() {
        RestClient restClient = RestClient.create();

        ResponseEntity<String> responseEntity = restClient.get()
                .uri(String.format("http://localhost:%d/tasks.json", localServerPort))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toEntity(String.class);

        assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(responseEntity.getBody())
                .isNotEmpty();
        assertThat(responseEntity.getBody()).contains("hogedrukreiniger").contains("2026-05-01");
    }
}
