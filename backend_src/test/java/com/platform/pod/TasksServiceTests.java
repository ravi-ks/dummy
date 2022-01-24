package com.platform.pod;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.platform.pod.dto.Task;
import com.platform.pod.entities.TaskPriority;
import com.platform.pod.entities.TaskType;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.TasksRepository;
import com.platform.pod.services.TasksService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TasksServiceTests {
    @Mock
    TasksRepository tasksRepository;

    @InjectMocks
    TasksService tasksService;

    @Test
    public void testingInstances() {
        Assertions.assertNotNull(tasksService);
    }

    @Test
    public void testingGetTasks() {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 30), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksRepository.getTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), TaskType.TODO)).thenReturn(taskList);

//        Actual Testing
        List<Task> result = tasksService.getTasks(1, getDate(2021, 8, 1), getDate(2021, 8, 30));

//        Assertions
        Assertions.assertEquals(1, result.size());

        Task task = result.get(0);
        Assertions.assertEquals(1l, task.getTaskId());
        Assertions.assertEquals("Task Title", task.getTitle());
        Assertions.assertEquals(TaskPriority.HIGH, task.getPriority());
        Assertions.assertEquals(getDate(2021, 8, 1).toString(), task.getStartDate().toString());
        Assertions.assertEquals(getDate(2021, 8, 30).toString(), task.getEndDate().toString());
        Assertions.assertEquals(getTime(0, 0, 0), task.getStartTime());
        Assertions.assertEquals(getTime(23, 59, 59), task.getEndTime());
    }

    @Test
    public void testingGetTasksException() {
//        Mocking
        Mockito.when(tasksRepository.getTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), TaskType.TODO)).thenThrow(ApiException.class);

//        Actual Testing
        Assertions.assertThrows(ApiException.class, () -> {
            tasksService.getTasks(1, getDate(2021, 8, 1), getDate(2021, 8, 30));
        });
    }

    @Test
    public void testingGetDayTasks() {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 1), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksRepository.getTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), TaskType.TODO)).thenReturn(taskList);

//        Actual Testing
        List<Task> result = tasksService.getDayTasks(1, getDate(2021, 8, 1));

        //        Assertions
        Assertions.assertEquals(1, result.size());

        Task task = result.get(0);
        Assertions.assertEquals(1l, task.getTaskId());
        Assertions.assertEquals("Task Title", task.getTitle());
        Assertions.assertEquals(TaskPriority.HIGH, task.getPriority());
        Assertions.assertEquals(getDate(2021, 8, 1).toString(), task.getStartDate().toString());
        Assertions.assertEquals(getDate(2021, 8, 1).toString(), task.getEndDate().toString());
        Assertions.assertEquals(getTime(0, 0, 0), task.getStartTime());
        Assertions.assertEquals(getTime(23, 59, 59), task.getEndTime());
    }

    @Test
    public void testingGetUserTasks() {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 30), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksRepository.getUserTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(taskList);

//        Actual Testing
        List<Task> result = tasksService.getUserTasks("abc@test.com", getDate(2021, 8, 1), getDate(2021, 8, 30));

//        Assertions
        Assertions.assertEquals(1, result.size());

        Task task = result.get(0);
        Assertions.assertEquals(1l, task.getTaskId());
        Assertions.assertEquals("Task Title", task.getTitle());
        Assertions.assertEquals(TaskPriority.HIGH, task.getPriority());
        Assertions.assertEquals(getDate(2021, 8, 1).toString(), task.getStartDate().toString());
        Assertions.assertEquals(getDate(2021, 8, 30).toString(), task.getEndDate().toString());
        Assertions.assertEquals(getTime(0, 0, 0), task.getStartTime());
        Assertions.assertEquals(getTime(23, 59, 59), task.getEndTime());
    }

    @Test
    public void testingGetUserTasksException() {
//        Mocking
        Mockito.when(tasksRepository.getUserTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class))).thenThrow(ApiException.class);

//        Actual Testing
        Assertions.assertThrows(ApiException.class, () -> {
            tasksService.getUserTasks("abc@test.com", getDate(2021, 8, 1), getDate(2021, 8, 30));
        });
    }

    @Test
    public void testingGetUserDayTasks() {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 1), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksRepository.getUserTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(taskList);

//        Actual Testing
        List<Task> result = tasksService.getUserDayTasks("abc@test.com", getDate(2021, 8, 1));

        //        Assertions
        Assertions.assertEquals(1, result.size());

        Task task = result.get(0);
        Assertions.assertEquals(1l, task.getTaskId());
        Assertions.assertEquals("Task Title", task.getTitle());
        Assertions.assertEquals(TaskPriority.HIGH, task.getPriority());
        Assertions.assertEquals(getDate(2021, 8, 1).toString(), task.getStartDate().toString());
        Assertions.assertEquals(getDate(2021, 8, 1).toString(), task.getEndDate().toString());
        Assertions.assertEquals(getTime(0, 0, 0), task.getStartTime());
        Assertions.assertEquals(getTime(23, 59, 59), task.getEndTime());
    }

    //    Function to get the date.
    private Date getDate(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        return new Date(date.getTimeInMillis());
    }

    //    Function to get time.
    private OffsetTime getTime(int hour, int min, int sec) {
        return OffsetTime.of(hour, min, sec, 0, ZoneOffset.UTC);
    }
}
