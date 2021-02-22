export type GlobalVariable = {
  namespace: string;
  key: string;
  value: string;
  encrypted: boolean;
};

export type GlobalVariableUpsertVO = {
  key: string;
  value: string;
  encrypted: boolean;
};
