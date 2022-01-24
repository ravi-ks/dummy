export interface TeamSummary {
  teamId: number;
  name: string;
  membersCount: number;
}

export interface TeamDetails {
  teamId: number;
  teamName: string;
  members: Member[];
}

export interface Member {
  userId: number;
  email: string;
  name: string | null;
}
