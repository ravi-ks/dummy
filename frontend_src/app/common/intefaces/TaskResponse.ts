import { TaskPriority } from "../types/Priority";
import { TaskType } from "../types/TaskType";
import { Attachments } from "./TaskBody";

export interface Organizer {
  name: string;
}

export interface TeamAssignedWithTask {
  team_id: number;
  team_name: string;
}

export interface Task {
  task_id: number;
  startTime: string;
  endTime: string;
  title: string;
  meetingLink: string;
  priority: TaskPriority;
  type: TaskType;
  description: string;
  organizer: Organizer;
  team_assigned_with_task: TeamAssignedWithTask | null;
  attachments: Attachments[];
}
export interface Repeat {
  sun: boolean;
  mon: boolean;
  tue: boolean;
  wed: boolean;
  thu: boolean;
  fri: boolean;
  sat: boolean;
}

export interface Repetition extends Repeat {
  endsOn: Date;
  startsAt: Date;
  frequency_gap: number;
  task: Task;
}

export interface TaskDetail {
  taskId: number;
  title: string;
  priority: TaskPriority;
  startDate: Date;
  endDate: Date;
  startTime: string;
  endTime: string;
  repeat: Repeat;
  checked: boolean;
  canBeChecked: boolean;
  team?: TeamAssignedWithTask;
}

export interface TaskDescription {
  title: string;
  id: number;
  attachments: Attachments[];
  description: string;
  totalChecks: number;
  totalMemberInTeam: number;
  priority: string;
  taskType: TaskType;
  meetingLink: string;
  organizer: Organizer;
  isOrganizer: boolean;
}
