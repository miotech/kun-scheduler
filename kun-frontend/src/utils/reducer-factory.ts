import produce from 'immer';
import isArray from 'lodash/isArray';
import isObject from 'lodash/isObject';

export function setterFactory<
  ModelStateType extends Record<string, any>,
  FieldType extends ModelStateType[any],
>(fieldName: string) {
  return produce((draftState: ModelStateType, payload: FieldType): void => {
    if (typeof payload === 'number' || typeof payload === 'string') {
      (draftState[fieldName] as any) = payload;
      return;
    }
    if (isArray(payload)) {
      // @ts-ignore
      (draftState[fieldName] as any) = [...payload];
      return;
    }
    if (isObject(payload)) {
      (draftState[fieldName] as any) = {
        ...payload,
      };
    }
  });
}

export function updaterFactory<
  ModelStateType extends Record<string, any>,
  FieldType extends (keyof ModelStateType & object),
>(fieldName: string) {
  return produce((draftState: ModelStateType, payload: FieldType): void => {
    (draftState[fieldName] as any) = {
      ...draftState[fieldName],
      // @ts-ignore
      ...payload,
    };
  });
}
