package com.platform.pod.services;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.*;

import com.platform.pod.common.Constants;
import com.platform.pod.dto.Task;
import com.platform.pod.entities.*;
import com.platform.pod.interfaces.TasksInterface;
import com.platform.pod.repositories.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.platform.pod.dto.CreateTaskWrapper;
import com.platform.pod.dto.TaskDescription;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.RepetitionRepo;
import com.platform.pod.repositories.TasksRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TasksService implements TasksInterface {

    @Autowired
    TasksRepository tasksRepository;

    @Autowired
    TeamsService teamsService;

    @Autowired
    RepetitionRepo repetitionRepo;

    @Autowired
    UserService userService;

    //    Function to fetch the tasks(TODOs) of a given date range of a particular team
    public List<Task> getTasks(int teamId, Date startDate, Date endDate) {
        if (startDate.after(endDate))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Dates: Start Date should be smaller than End Date");

        return tasksRepository.getTasks(teamId, getStartOfDay(startDate), getEndOfDay(endDate), TaskType.TODO);
    }

    //    Function to fetch the tasks (TODOs) of a particular team of a particular day
    public List<Task> getDayTasks(int teamId, Date date) {
        return tasksRepository.getTasks(teamId, getStartOfDay(date), getEndOfDay(date), TaskType.TODO);
    }

    //    Function to fetch the tasks of a user for given date range
    public List<Task> getUserTasks(String email, Date startDate, Date endDate) {
        int userId = userService.getUserIdByEmail(email);
        if (startDate.after(endDate))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Dates: Start Date should be smaller than End Date");

        return tasksRepository.getUserTasks(userId, getStartOfDay(startDate), getEndOfDay(endDate));
    }

    Specification<Repetitions> getTasksByDay(int userId, Date date) {
        System.out.println("Started");
        ZonedDateTime now = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            Join<Repetitions, Tasks> tasks = root.join("task");
            Join<Tasks, Teams> tat = tasks.join("team_assigned_with_task", JoinType.LEFT);
            Join<Tasks, Users> organizer = tasks.join("organizer");
            Join<Teams, Users> teamsUsersJoin = tat.join("users_and_teams", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.equal(organizer.get("user_id"), userId),
                            criteriaBuilder.equal(teamsUsersJoin.get("user_id"), userId)
                    )
            );
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(root.get("startsAt"), getEndOfDay(date))
            );
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("endsOn"), getStartOfDay(date))
            );
            predicates.add(
                    criteriaBuilder.equal(root.get(now.getDayOfWeek().toString().substring(0, 3).toLowerCase()), true)
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    //    Function to fetch the tasks of a user for the given day
    public List<Task> getUserDayTasks(String email, Date date) {
        int userId = userService.getUserIdByEmail(email);
        return repetitionRepo
                .findAll(getTasksByDay(userId, date))
                .stream()
                .map(Task::new)
                .collect(Collectors.toList());
    }

    //    method to get the time of the end of the day
    private Date getEndOfDay(Date date) {
        return new Date(getStartOfDay(date).getTime() + Constants.MILLISECONDS_IN_A_DAY);
    }

    //    method to get the time of the start of the day
    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }


    public Repetitions createTask(CreateTaskWrapper createTaskWrapper, String email) {
        // TODO: Apply Start Date, End Date checks
        //task organizer can only assign task to his team - handler
        Users taskOrganizer = userService.getUserByEmail(email);
        Users teamOrganizer;
        if (createTaskWrapper.getAssignedTeamId() <= 0) {
            teamOrganizer = null;
        } else {
            teamOrganizer = teamsService.getTeam(createTaskWrapper.getAssignedTeamId()).organizer;
        }

        if (createTaskWrapper.getType() == TaskType.EVENT && createTaskWrapper.getMeetingLink() == null) {
            throw new ApiException("Meeting Without a Link is Not Possible");
        }

        if (teamOrganizer != null && !taskOrganizer.equals(teamOrganizer)) {
            throw new ApiException("You are Not the Organizer for the Team");
        }
        Repetitions repetitions = new Repetitions();
        if (createTaskWrapper.getRepetitionType().size() == 1) {
            if (createTaskWrapper.getRepetitionType().get(0) == RepetitionType.Daily) {
                repetitions.setAllDays();
            } else if (createTaskWrapper.getRepetitionType().get(0) == RepetitionType.WorkingDays) {
                repetitions.setWorkingDays();
            } else {
                repetitions.setSomeDays(createTaskWrapper.getRepetitionType());
            }
        } else {
            repetitions.setSomeDays(createTaskWrapper.getRepetitionType());
        }
        repetitions.setStartsAt(createTaskWrapper.getStartsAt());
        repetitions.setEndsOn(createTaskWrapper.getEndsOn());

        Tasks task = new Tasks();
        task.setStartTime(createTaskWrapper.getStartTime());
        task.setEndTime(createTaskWrapper.getEndTime());
        task.setTitle(createTaskWrapper.getTaskTitle());
        task.setMeetingLink(createTaskWrapper.getMeetingLink());
        task.setPriority(createTaskWrapper.getPriority());
        task.setType(createTaskWrapper.getType());
        task.setDescription(createTaskWrapper.getTaskDescription());
        //task.setAttachments(new HashSet<>(createTaskWrapper.getAttachmentsURLList()));
        if (createTaskWrapper.getAssignedTeamId() > 0) {
            task.setTeam_assigned_with_task(teamsService.getTeam(createTaskWrapper.getAssignedTeamId()));
        } else {
            task.setTeam_assigned_with_task(null);
        }
        task.setOrganizer(taskOrganizer);

        repetitions.setTask(task);

        try {
            repetitions = repetitionRepo.save(repetitions);
            return repetitions;
        } catch (Exception e) {
//            throw new ApiException(HttpStatus.NOT_FOUND, "Probable invalid team id - doesn't exist." + " Debug info: " + e.getMessage());
            throw new ApiException(HttpStatus.NOT_FOUND, "Failed To Save the task");
        }
    }


    public Repetitions updateTask(CreateTaskWrapper createTaskWrapper, int taskId, String email) {
        int organizerId = userService.getUserIdByEmail(email);
        Tasks task = repetitionRepo.findRepetitionsByTaskId(taskId, organizerId).getTask();
        Repetitions repetitions = repetitionRepo.findRepetitionsByTaskId(taskId, organizerId);

        //task organizer can only assign task to his team - handler
        Users taskOrganizer = userService.getUserById(organizerId);
        Users teamOrganizer;
        if (createTaskWrapper.getAssignedTeamId() <= 0) {
            teamOrganizer = null;
        } else {
            teamOrganizer = teamsService.getTeam(createTaskWrapper.getAssignedTeamId()).organizer;
        }

        if (teamOrganizer != null && !taskOrganizer.equals(teamOrganizer)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You are not the organizer for the team.");
        }
        if (createTaskWrapper.getRepetitionType().size() == 1) {
            if (createTaskWrapper.getRepetitionType().get(0) == RepetitionType.Daily) {
                repetitions.setAllDays();
            } else if (createTaskWrapper.getRepetitionType().get(0) == RepetitionType.WorkingDays) {
                repetitions.setWorkingDays();
            } else {
                repetitions.setSomeDays(createTaskWrapper.getRepetitionType());
            }
        } else {
            repetitions.setSomeDays(createTaskWrapper.getRepetitionType());
        }
        repetitions.setStartsAt(createTaskWrapper.getStartsAt());
        repetitions.setEndsOn(createTaskWrapper.getEndsOn());

        task.setStartTime(createTaskWrapper.getStartTime());
        task.setEndTime(createTaskWrapper.getEndTime());
        task.setTitle(createTaskWrapper.getTaskTitle());
        task.setMeetingLink(createTaskWrapper.getMeetingLink());
        task.setPriority(createTaskWrapper.getPriority());
        task.setType(createTaskWrapper.getType());
        task.setDescription(createTaskWrapper.getTaskDescription());
        //task.setAttachments(new HashSet<>(createTaskWrapper.getAttachmentsURLList()));
        if (createTaskWrapper.getAssignedTeamId() > 0) {
            task.setTeam_assigned_with_task(teamsService.getTeam(createTaskWrapper.getAssignedTeamId()));
        } else {
            task.setTeam_assigned_with_task(null);
        }
        task.setOrganizer(userService.getUserById(organizerId));

        repetitions.setTask(task);

        try {
            return repetitionRepo.save(repetitions);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to Save the Task");
        }
    }


    public String deleteTask(long taskID, String email) {
        int userId = userService.getUserIdByEmail(email);
        Repetitions repetitions = repetitionRepo.findRepetitionsByTaskId(taskID, userId);
        if (repetitions == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task Not Found");
        }
        repetitionRepo.delete(repetitions);
        return "Task Deleted Successfully";
    }

    public Repetitions getCompleteDescription(long taskID, String email) {
        int userId = userService.getUserIdByEmail(email);
        Repetitions repetitions = repetitionRepo.findRepetitionsByTaskId(taskID, userId);
        if (repetitions == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task Not Found");
        }
        return repetitions;
    }


    public String addTaskToCompletedTask(long taskId, String email) {
        int userId = userService.getUserIdByEmail(email);
        Optional<Tasks> optionalTask = tasksRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task Not Found");
        }
        Tasks task = optionalTask.get();

//        TaskType taskType=task.getType();
//        if(taskType.equals(TaskType.EVENT)){
//            throw new ApiException(HttpStatus.BAD_REQUEST,"Not a TaskType");
//        }
        Users user = userService.getUserById(userId);
        if (
                task.getOrganizer().getUser_id() != userId &&
                        (task.getTeam_assigned_with_task() != null && !task.getTeam_assigned_with_task().getUsers_and_teams().contains(user))
        ) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User not in Team");
        }

        if (task.getUsersWhoCompletedTask().contains(user)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Task Already Checked");
        }
        task.getUsersWhoCompletedTask().add(user);
        tasksRepository.save(task);
        return "Task Checked";
    }

    public TaskDescription getTaskDescription(long taskId, String email) {
        int userId = userService.getUserIdByEmail(email);
        Optional<Tasks> optionalTask = tasksRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task Not Found");
        }
        Tasks task = optionalTask.get();
        TaskType taskType = task.getType();

        Users user = userService.getUserById(userId);
        if (
                (task.getTeam_assigned_with_task() != null && !task.getTeam_assigned_with_task().getUsers_and_teams().contains(user)) &&
                        task.getOrganizer().getUser_id() != userId
        ) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User not in Team");
        }

        TaskDescription taskDto = new TaskDescription();
        int totalChecks = 0, totalMembersInTeam = 0;
        if (taskType.equals(TaskType.TODO)) {
            totalChecks = task.getUsersWhoCompletedTask().size();
            if (task.getTeam_assigned_with_task() != null) {
                totalMembersInTeam = task.getTeam_assigned_with_task().getUsers_and_teams().size();
            } else {
                totalMembersInTeam = 0;
            }
        }
        taskDto.setTitle(task.getTitle());
        taskDto.setId(task.getTask_id());
        taskDto.setAttachments(task.getAttachments());
        taskDto.setDescription(task.getDescription());
        taskDto.setTotalChecks(totalChecks);
        taskDto.setTotalMemberInTeam(totalMembersInTeam);
        taskDto.setPriority(task.getPriority());
        taskDto.setTaskType(task.getType());
        taskDto.setMeetingLink(task.getMeetingLink());

        taskDto.setOrganizer(task.getOrganizer());
        taskDto.setIsOrganizer(task.getOrganizer().getUser_id() == userId);

        return taskDto;
    }

    public List<Task> getTodos(String email, Date startDate) {
        int userId = userService.getUserIdByEmail(email);
        return tasksRepository.getTaskFromStartDateAndType(userId, startDate, TaskType.TODO);
    }

    public List<Task> getEvents(String email, Date date) {
        int userId = userService.getUserIdByEmail(email);
        ZonedDateTime now = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        Specification<Repetitions> test = (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            Join<Repetitions, Tasks> tasks = root.join("task");
            Join<Tasks, Teams> tat = tasks.join("team_assigned_with_task", JoinType.LEFT);
            Join<Tasks, Users> organizer = tasks.join("organizer");
            Join<Teams, Users> teamsUsersJoin = tat.join("users_and_teams", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.equal(organizer.get("user_id"), userId),
                            criteriaBuilder.equal(teamsUsersJoin.get("user_id"), userId)
                    )
            );
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(root.get("startsAt"), getEndOfDay(date))
            );
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("endsOn"), getStartOfDay(date))
            );
            predicates.add(
                    criteriaBuilder.equal(root.get(now.getDayOfWeek().toString().substring(0, 3).toLowerCase()), true)
            );
            predicates.add(
                    criteriaBuilder.equal(tasks.get("type"), TaskType.EVENT)
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return repetitionRepo
                .findAll(test)
                .stream()
                .map(Task::new)
                .collect(Collectors.toList());
    }

    public List<Task> getUserTasksFromStartDate(int userId, Date startDate) {
        return tasksRepository.getUserTasksFromStartDate(userId, getStartOfDay(startDate));
    }

    public List<Task> getTodosForADay(String email, Date date) {
        int userId = userService.getUserIdByEmail(email);
        System.out.println("\n\nlogsss");
        System.out.println(tasksRepository.getUserTasksByType(
                userId,
                getStartOfDay(date),
                getEndOfDay(date),
                TaskType.TODO
        ));
        return tasksRepository.getUserTasksByType(
                userId,
                getStartOfDay(date),
                getEndOfDay(date),
                TaskType.TODO
        );
    }

    public void deleteTaskByTeamId(int teamId) {
        tasksRepository.deleteRepetitionsByTeamId(teamId);
        tasksRepository.deleteTasksByTeamId(teamId);
    }
}
