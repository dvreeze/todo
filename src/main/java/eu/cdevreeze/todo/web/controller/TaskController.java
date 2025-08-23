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

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.model.Task;
import eu.cdevreeze.todo.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        ImmutableList<Task> tasks;
        if (isClosed == null) {
            tasks = taskService.findAllTasks();
        } else {
            if (isClosed) {
                tasks = taskService.findAllClosedTasks();
            } else {
                tasks = taskService.findAllOpenTasks();
            }
        }

        model.addAttribute("tasks", tasks);
        return "tasks";
    }
}
