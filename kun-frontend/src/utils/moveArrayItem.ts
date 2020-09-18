const moveArrayItem = (arr: any[], fromIndex: number, toIndex: number) => {
  if (fromIndex === toIndex) {
    return arr;
  }
  for (let index = 0; index < arr.length; index += 1) {
    const item = arr[index];
    if (index === fromIndex) {
      arr.splice(index, 1);
      arr.splice(toIndex, 0, item);
      break;
    }
  }
  return arr;
};

export default moveArrayItem;
