// Declare more template types here if needed
export type ParameterDisplayType = 'sql' | 'string' | 'text' | 'datasource' | 'keyvalue' | 'list' | 'single-select';

export interface DisplayParameter {
  name: string;
  type: ParameterDisplayType;
  displayName: string;
  // "items" only available on type 'single-select'
  items?: { label?: string; value: string }[];
  required: boolean;
}

export interface DisplayParameterWithSelectType extends DisplayParameter {
  name: 'single-select';
  items: { label?: string; value: string }[];
}

export interface TaskTemplate {
  name: string;
  templateType: ParameterDisplayType;
  templateGroup: string;
  displayParameters: DisplayParameter[];
}
