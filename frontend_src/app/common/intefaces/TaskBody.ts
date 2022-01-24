import { TaskType } from "../types/TaskType";

export interface CreateTask {
  taskTitle: string;
  taskDescription: string;
  startTime: string;
  endTime: string;
  attachmentsURLList: Attachments[];
  type: TaskType;
  priority: string;
  meetingLink: string;
  repetitionType: string[];
  startsAt: string;
  endsOn: string;
  assignedTeamId: number;
}

export interface Attachments {
  attachment_url: string;
  name: string;
  filetype?: string | null;
  imageSource?: String | null;
}
