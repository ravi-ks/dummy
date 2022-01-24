import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { Observable, Subject } from 'rxjs';
import { CommonResponse } from 'src/app/common/intefaces/CommonResponse';
import { Repetition, TaskDescription, TaskDetail } from 'src/app/common/intefaces/TaskResponse';
import { environment } from 'src/environments/environment';
import { CreateTask } from '../../common/intefaces/TaskBody';

@Injectable({
  providedIn: 'root'
})
export class TasksService {
  private readonly apiUrl = `${environment.apiURL}task`;
  private taskCreated: Subject<TaskDetail> = new Subject<TaskDetail>();
  private todoTaskCreated: Subject<TaskDetail> = new Subject<TaskDetail>();
  private eventTaskCreated: Subject<TaskDetail> = new Subject<TaskDetail>();

  //trigger task removal from view
  private removeTaskFromViewTriggered: Subject<Number> = new Subject<Number>();
  private reloadTasks: Subject<void> = new Subject<void>();
  private checkTaskSubject: Subject<number> = new Subject<number>();

  constructor(private http: HttpClient) { }


  //get the trigger to remove task from view as observable
  get onTriggerRemoveTaskFromView() {
    return this.removeTaskFromViewTriggered.asObservable();
  }

  //trigger to remove task from view
  triggerToReloadTasks() {
    this.reloadTasks.next();
  }

  //get the trigger to reload tasks after editing them.
  onTriggerToReloadTasks() {
    return this.reloadTasks.asObservable();
  }


  /**
   * Creates a Task.
   * @param task Task
   */
  createTask(task: CreateTask): void {
    this.http.post<Repetition>(`${this.apiUrl}/create`, task).subscribe(repetition => {
      const taskDetail: TaskDetail = {
        taskId: repetition.task.task_id,
        title: repetition.task.title,
        priority: repetition.task.priority,
        startDate: repetition.startsAt,
        endDate: repetition.endsOn,
        checked: false,
        endTime: repetition.task.endTime,
        startTime: repetition.task.startTime,
        repeat: {
          sun: repetition.sun,
          mon: repetition.mon,
          tue: repetition.tue,
          wed: repetition.wed,
          thu: repetition.thu,
          fri: repetition.fri,
          sat: repetition.sat
        },
        team: repetition.task.team_assigned_with_task === null ? undefined : repetition.task.team_assigned_with_task,
        canBeChecked: (repetition.task.team_assigned_with_task === null)
      };
      if (repetition.task.type === "TODO") {
        this.todoTaskCreated.next(taskDetail);
      } else if (repetition.task.type === "EVENT") {
        this.eventTaskCreated.next(taskDetail);
      }
      this.taskCreated.next(taskDetail);
    });
  }

  /**
   * Delete a Task.
   * @param task_id Task ID.
   * @returns Observable
   */
  deleteTask(task_id: number) {
    const deleted: Subject<void> = new Subject<void>();
    this.http.delete<CommonResponse>(`${this.apiUrl}/${task_id}`)
      .subscribe(_ => {
        this.removeTaskFromViewTriggered.next(task_id);
        deleted.next();
      });
    return deleted;
  }

  /**
   * Check a Task.
   * @param task_id Task ID.
   * @returns Observable.
   */
  checkTask(task_id: number) {
    const checked: Subject<Repetition> = new Subject<Repetition>();
    this.http.post<Repetition>(`${this.apiUrl}/checkTask/${task_id}`, {})
      .subscribe(value => {
        checked.next(value);
        this.checkTaskSubject.next(task_id);
      });
    return checked;
  }

  /**
   * Get Complete Information about a Task.
   * @param task_id Task ID.
   * @returns Repetition Observable.
   */
  completeDescription(task_id: number) {
    return this.http.get<Repetition>(`${this.apiUrl}/complete/${task_id}`);
  }

  /**
   * Get List for Task for particular Day.
   * @param day The Day For which we have to fetch task.
   * @returns Task Detial Observable.
   */
  getDailyTaskForUser(day: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/${moment(day).format()}`);
  }

  /**
   * Get List of Task For a particular range.
   * @param start Starting Date.
   * @param end Ending Date.
   * @returns List of Task Details.
   */
  getTaskInRangeForUser(start: Date, end: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/${moment(start).format()}/${moment(end).format()}`);
  }

  /**
   * For a Given day get all the task for team.
   * @param teamId Team ID.
   * @param day The Day for which we need Tasks.
   * @returns Observable of List of Tasks.
   */
  getDailyTaskForTeam(teamId: number, day: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/team/${teamId}/${moment(day).format()}`);
  }

  /**
   * For a Given range get all the task for a team.
   * @param teamId Team ID.
   * @param start Starting Date.
   * @param end Ending Date.
   * @returns Observable of List of Tasks.
   */
  getTaskInRangeForTeam(teamId: number, start: Date, end: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/team/${teamId}/${moment(start).format()}/${moment(end).format()}`);
  }

  /**
   * Update a Given task.
   * @param taskId Task ID.
   * @param task Task Information.
   * @returns Repetition Observable.
   */
  updateTask(taskId: number, task: CreateTask) {
    const subject = new Subject<Repetition>();
    this.http.put<Repetition>(`${this.apiUrl}/update/${taskId}`, task)
      .subscribe(
        value => {
          subject.next(value);
          this.reloadTasks.next();
        }
      );
    return subject;
  }

  /**
   * Get Task Description (Summary).
   * @param taskID Task ID.
   * @returns Task Description Observable.
   */
  getTaskDescription(taskID: number) {
    return this.http.get<TaskDescription>(`${this.apiUrl}/taskDescription/${taskID}`);
  }

  /**
   * Get the Tasks of type Event.
   * @param day The day for which we need to fetch task.
   * @returns List Task Detail Observable.
   */
  getEventsByDay(day: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/eventsFromToday/${moment(day).format()}`);
  }

  /**
   * Get the Tasks of type Todo.
   * @param day The day for which we need to fetch task.
   * @returns List Task Detail Observable.
   */
  getTodosByDay(day: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/todos/${moment(day).format()}`);
  }

  /**
 * Get the Tasks of type Todo From a Given Date.
 * @param day The day for which we need to fetch task.
 * @returns List Task Detail Observable.
 */
  getTodosFromDay(day: Date) {
    return this.http.get<TaskDetail[]>(`${this.apiUrl}/todosFromToday/${moment(day).format()}`);
  }

  /**
   * Get the Observable when task is created.
   */
  get OnTaskCreated() {
    return this.taskCreated.asObservable();
  }

  /**
   * Get the Observable when TODO task is created.
   */
  get OnTodoTaskCreated() {
    return this.todoTaskCreated.asObservable();
  }

  /**
   * Get the Observable when Event task is created.
   */
  get OnEventTaskCreated() {
    return this.eventTaskCreated.asObservable();
  }


  /**
   * Get the Observable for checking Task.
   */
  get OnCheckTask() {
    return this.checkTaskSubject.asObservable();
  }
}
