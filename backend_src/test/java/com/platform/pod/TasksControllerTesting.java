package com.platform.pod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.pod.common.ResponseMessage;
import com.platform.pod.controllers.TasksController;

import com.platform.pod.dto.TaskDescription;
import com.platform.pod.services.TasksService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@SpringBootTest
public class TasksControllerTesting {

    private MockMvc mockMvc;

    @InjectMocks
    private TasksController tasksController;

    @Mock
    private TasksService tasksService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(tasksController).build();
    }

    @Test
    public void testCheckTask() throws Exception {
        String expected = "Task Id : 1 Checked";
        ResponseMessage responseMessage = new ResponseMessage(expected);
        Mockito.when(tasksService.addTaskToCompletedTask(1, "abc@test.com")).thenReturn(expected);
        mockMvc.perform(MockMvcRequestBuilders.post("/task/checkTask/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(responseMessage)));
    }

    @Test
    public void testTaskDescription() throws Exception {
        TaskDescription taskDescription = new TaskDescription();
        Mockito.when(tasksService.getTaskDescription(1, "abc@test.com")).thenReturn(taskDescription);
        mockMvc.perform(MockMvcRequestBuilders.get("/task/taskDescription/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(taskDescription)));
    }
}
