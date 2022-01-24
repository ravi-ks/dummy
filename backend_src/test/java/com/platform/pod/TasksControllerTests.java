package com.platform.pod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.pod.controllers.TasksController;
import com.platform.pod.dto.Task;
import com.platform.pod.entities.TaskPriority;
import com.platform.pod.services.TasksService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TasksControllerTests {
    //    To test rest end points
    private MockMvc mockMvc;

    @Mock
    TasksService tasksService;

    @InjectMocks
    TasksController tasksController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tasksController).build();
    }

    @Test
    public void testingInstance() {
        Assertions.assertNotNull(tasksController);
    }

    @Test
    public void testingGetTasks() throws Exception {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 30), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksService.getTasks(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(taskList);

//        Actual Testing
        mockMvc.perform(MockMvcRequestBuilders.get("/task/team/1/2021-08-01T03:41:02+05:30/2021-08-30T03:41:02+05:30"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(taskList)));
    }

    @Test
    public void testingGetDayTasks() throws Exception {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 30), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksService.getDayTasks(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(taskList);

//        Actual Testing
        mockMvc.perform(MockMvcRequestBuilders.get("/task/team/1/2021-08-01T03:41:02+05:30"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(taskList)));
    }

    @Test
    public void testingGetUserTasks() throws Exception {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 30), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksService.getUserTasks(Mockito.anyString(), Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(taskList);

//        Actual Testing
        mockMvc.perform(MockMvcRequestBuilders.get("/task/1/2021-08-01T03:41:02+05:30/2021-08-30T03:41:02+05:30"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(taskList)));
    }

    @Test
    public void testingGetUserDayTasks() throws Exception {
//        Mocking
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task(1l, "Task Title", TaskPriority.HIGH, getDate(2021, 8, 1), getDate(2021, 8, 30), getTime(0, 0, 0), getTime(23, 59, 59), true, true, true, true, true, true, true));
        Mockito.when(tasksService.getUserDayTasks(Mockito.anyString(), Mockito.any(Date.class))).thenReturn(taskList);

//        Actual Testing
        mockMvc.perform(MockMvcRequestBuilders.get("/task/2021-08-01T03:41:02+05:30"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(taskList)));
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
