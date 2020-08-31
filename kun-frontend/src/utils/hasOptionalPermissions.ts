/**
 * 是否有其中之一的权限
 * @param currentPer
 * @param needPer
 */
const hasOptionalPermissions = (currentPer: string[], needPer?: string[]) => {
  if (!needPer) {
    return true;
  }
  let has = false;
  currentPer.forEach(i => {
    if (needPer.includes(i)) {
      has = true;
    }
  });
  return has;
};

export default hasOptionalPermissions;
