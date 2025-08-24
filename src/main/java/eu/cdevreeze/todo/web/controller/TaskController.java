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

package eu.cdevreeze.todo.web.controller;

import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TaskService;
import eu.cdevreeze.todo.web.formdata.TaskFormData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Web MVC controller for tasks.
 *
 * @author Chris de Vreeze
 */
@Controller
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping(value = "/tasks")
    public String findAllTasks(
            @RequestParam(name = "closed", required = false) Boolean isClosed,
            Model model
    ) {
        if (isClosed == null) {
            model.addAttribute("tasks", taskService.findAllTasks());
            model.addAttribute("title", "Tasks");
        } else {
            if (isClosed) {
                model.addAttribute("tasks", taskService.findAllClosedTasks());
                model.addAttribute("title", "Closed tasks");
            } else {
                model.addAttribute("tasks", taskService.findAllOpenTasks());
                model.addAttribute("title", "Open tasks");
            }
        }

        return "tasks";
    }

    @GetMapping(value = "/newTask")
    public String getFormToAddTask(Model model) {
        model.addAttribute("newTask", new TaskFormData());

        return "newTask";
    }

    @PostMapping(value = "/newTask")
    public String addTask(@ModelAttribute TaskFormData taskFormData) {
        Task task = Task.newTask(
                taskFormData.getName(),
                taskFormData.getDescription(),
                Optional.ofNullable(taskFormData.getTargetEnd())
                        .map(dt -> dt.toInstant(ZoneOffset.UTC)),
                taskFormData.getExtraInformation().isBlank() ? Optional.empty() : Optional.of(taskFormData.getExtraInformation()),
                taskFormData.isClosed()
        );

        taskService.addTask(task);

        return "redirect:/tasks";
    }
}
