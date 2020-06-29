// Declare more template types here if needed
export type ParameterDisplayType = 'sql' | 'string' | 'text' | 'datasource' | 'keyvalue';

export interface DisplayParameter {
  name: string;
  type: ParameterDisplayType;
  displayName: string;
  required: boolean;
}

export interface TaskTemplate {
  name: string;
  templateType: ParameterDisplayType;
  templateGroup: string;
  displayParameters: DisplayParameter[];
}
