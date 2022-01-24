package com.platform.pod.controllers;

import com.platform.pod.common.Constants;
import com.platform.pod.common.ResponseMessage;
import com.platform.pod.dto.CreateTaskWrapper;
import com.platform.pod.dto.TaskDescription;
import com.platform.pod.entities.Repetitions;

import java.util.Date;
import java.util.List;

import com.platform.pod.dto.Task;
import com.platform.pod.services.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TasksController {
    @Autowired
    TasksService tasksService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Repetitions create(
            @RequestBody CreateTaskWrapper createTaskWrapper,
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email
    ) {
        return tasksService.createTask(createTaskWrapper, email);
    }

    @RequestMapping(value = "/update/{taskId}", method = RequestMethod.PUT)
    public Repetitions update(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @RequestBody CreateTaskWrapper createTaskWrapper,
            @PathVariable("taskId") int taskId
    ) {
        System.out.println("\n\n\n\nUpdating: " + createTaskWrapper);
        return tasksService.updateTask(createTaskWrapper, taskId, email);
    }

    @DeleteMapping("/{taskID}")
    public ResponseMessage delete(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("taskID") long taskID
    ) {
        return new ResponseMessage(
                tasksService.deleteTask(taskID, email)
        );
    }

    @GetMapping("/complete/{taskID}")
    public Repetitions getCompleteTaskDetails(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("taskID") long taskID
    ) {
        return tasksService.getCompleteDescription(taskID, email);
    }


    //    To get the tasks of a particular team in a given date range
    @GetMapping(value = "/team/{team_id}/{start_date}/{end_date}")
    public List<Task> getTasks(
            @PathVariable("team_id") int teamId,
            @PathVariable("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @PathVariable("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate
    ) {
        return tasksService.getTasks(teamId, startDate, endDate);
    }

    //    To get the tasks of a particular team on a given day
    @GetMapping(value = "/team/{team_id}/{date}")
    public List<Task> getDayTasks(
            @PathVariable("team_id") int teamId,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date
    ) {
        return tasksService.getDayTasks(teamId, date);
    }

    @GetMapping(value = "/eventsFromToday/{start_date}")
    public List<Task> getEventsFromToday(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate
    ) {
        return tasksService.getEvents(email, startDate);
    }


    //    To get the tasks of a particular user in a given date range
    @GetMapping(value = "/{start_date}/{end_date}")
    public List<Task> getUserTasks(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @PathVariable("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate
    ) {
        return tasksService.getUserTasks(email, startDate, endDate);
    }

    //    To get the tasks of a particular user of a given day
    @GetMapping(value = "/{date}")
    public List<Task> getUserDayTask(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date
    ) {
        return tasksService.getUserDayTasks(email, date);
    }

    @GetMapping(value = "/todos/{date}")
    public List<Task> getUserDayTodo(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date
    ) {
        return tasksService.getTodosForADay(email, date);
    }

    @GetMapping(value = "/todosFromToday/{start_date}")
    public List<Task> getTodosFromToday(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate
    ) {
        return tasksService.getTodos(email, startDate);
    }

    @RequestMapping(value = "/checkTask/{id}", method = RequestMethod.POST)
    public ResponseMessage checkTask(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("id") int taskId
    ) {
        String result = tasksService.addTaskToCompletedTask(taskId, email);
        return new ResponseMessage(result);
    }

    @RequestMapping(value = "/taskDescription/{id}", method = RequestMethod.GET)
    public TaskDescription getTaskDescription(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("id") int taskId
    ) {
        return tasksService.getTaskDescription(taskId, email);
    }


}
