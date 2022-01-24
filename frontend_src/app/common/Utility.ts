function getUserInitials(name: string | null): string {
  if (name === null) {
    return "";
  }
  let initials: string = "";
  let parts = name.split(" ");
  for (let i = 0; i < Math.min(2, parts.length); i++) {
    initials += parts[i][0];
  }
  return initials.toUpperCase();
}

export default {
  getUserInitials
};
