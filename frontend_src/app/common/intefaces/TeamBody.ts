export interface CreateTeamBody {
  userEmails: string[];
  teamName: string;
}

export interface CopyTeamBody {
  teamId: number;
  teamName: string;
}

export interface UpdateMemberBody {
  email: string;
  teamId: number;
}

export interface UpdateTeamNameBody {
  teamId: number,
  teamName: string
}
