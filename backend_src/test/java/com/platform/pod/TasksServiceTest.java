package com.platform.pod;

import com.platform.pod.entities.TaskType;
import com.platform.pod.entities.Tasks;
import com.platform.pod.entities.Teams;
import com.platform.pod.entities.Users;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.TasksRepository;
import com.platform.pod.services.TasksService;
import com.platform.pod.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.Task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class TasksServiceTest {

    @InjectMocks
    private TasksService tasksService;

    @Mock
    private TasksRepository tasksRepository;

    @Mock
    private UserService userService;

    @Test
    public void testAddTaskToCompletedTask() {
        Tasks task = getTask();
        Users user1 = new Users(1, "u1@gmail.com", "user1");
        Users user2 = new Users(2, "u2@gmail.com", "user2");
        List<Users> list = Arrays.asList(user1, user2);
        task.setTeam_assigned_with_task(new Teams(1, "superlatives", new HashSet<>(list), user1));
        task.setOrganizer(user1);
        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(task));
        when(userService.getUserById(Mockito.anyInt())).thenReturn(user1);
        tasksService.addTaskToCompletedTask(1, "abc@test.com");

        verify(userService, times(1)).getUserById(Mockito.anyInt());
        verify(tasksRepository, times(1)).findById(Mockito.anyLong());
        verify(tasksRepository, times(1)).save(task);

        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> tasksService.addTaskToCompletedTask(1, "abc@test.com"));

        task.setType(TaskType.EVENT);
        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(task));
        assertThrows(ApiException.class, () -> tasksService.addTaskToCompletedTask(1, "abc@test.com"));

        task.setType(TaskType.TODO);
        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(task));
        assertThrows(ApiException.class, () -> tasksService.addTaskToCompletedTask(1, "abc@test.com"));

        task.setUsersWhoCompletedTask(new HashSet<>(list));
        assertThrows(ApiException.class, () -> tasksService.addTaskToCompletedTask(1, "abc@test.com"));
    }

    @Test
    public void testGetTaskDescription() {
        Tasks task = getTask();
        Users user1 = new Users(1, "u1@gmail.com", "user1");
        Users user2 = new Users(2, "u2@gmail.com", "user2");
        task.setOrganizer(user1);
        List<Users> list = Arrays.asList(user1, user2);
        task.setTeam_assigned_with_task(new Teams(1, "superlatives", new HashSet<>(list), user1));
        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(task));
        when(userService.getUserById(Mockito.anyInt())).thenReturn(user1);
        tasksService.getTaskDescription(1, "abc@test.com");

        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> tasksService.getTaskDescription(1, "abc@test.com"));

        when(tasksRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(task));
        when(userService.getUserById(Mockito.anyInt())).thenReturn(new Users(3, "u3@gmail.com", "user3"));
        assertThrows(ApiException.class, () -> tasksService.getTaskDescription(1, "abc@test.com"));
    }

    public Tasks getTask() {
        Tasks task = new Tasks();
        task.setType(TaskType.TODO);
        return task;
    }
}
